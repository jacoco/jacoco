/*******************************************************************************
 * Copyright (c) 2009, 2021 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Łukasz Suski - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.internal.analysis.filter;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

/**
 * {@link IFilter} for
 * <a href="https://developer.android.com/jetpack/compose">Jetpack Compose</a>
 * compiler injected code.
 */
class KotlinComposeFilter implements IFilter {

	static final String COMPOSABLE_ANNOTATION_DESCRIPTOR = "Landroidx/compose/runtime/Composable;";

	public void filter(MethodNode methodNode, IFilterContext context,
			IFilterOutput output) {
		if (!KotlinGeneratedFilter.isKotlinClass(context)) {
			return;
		}
		List<AnnotationNode> invisibleAnnotations = methodNode.invisibleAnnotations;
		if (invisibleAnnotations != null) {
			for (AnnotationNode annotation : invisibleAnnotations) {
				if (isComposableFunction(annotation)) {
					new Matcher(methodNode, output).filter();
					return;
				}
			}
		}
	}

	private static boolean isComposableFunction(AnnotationNode annotation) {
		return COMPOSABLE_ANNOTATION_DESCRIPTOR.equals(annotation.desc);
	}

	private static class Matcher {

		private final MethodNode methodNode;
		private final IFilterOutput output;
		private final Set<LabelNode> ignoredLabeledGroups = new HashSet<LabelNode>();

		public Matcher(MethodNode methodNode, IFilterOutput output) {

			this.methodNode = methodNode;
			this.output = output;
		}

		void filter() {

			for (AbstractInsnNode instruction : methodNode.instructions) {

				if (instruction.getOpcode() == Opcodes.INVOKEINTERFACE) {
					MethodInsnNode node = (MethodInsnNode) instruction;

					if (isComposeRuntime(node)) {
						ignoreComposeRuntimeInvocation(node);
					}
				}
			}
		}

		private void ignoreComposeRuntimeInvocation(
				MethodInsnNode methodInsnNode) {
			List<JumpInsnNode> jumpNodes = new ArrayList<JumpInsnNode>();

			ListIterator<JumpInsnNode> iterator = jumpNodes.listIterator();
			ignoreLabeledGroupAndJumpsToIt(methodInsnNode, jumpNodes, iterator);

			iterator = jumpNodes.listIterator();
			while (iterator.hasNext()) {
				JumpInsnNode jumpNode = iterator.next();
				iterator.remove();
				LabelNode label = jumpNode.label;
				if (ignoredLabeledGroups.contains(label)) {
					continue;
				}
				ignoreLabeledGroupAndJumpsToIt(label.getNext(), jumpNodes,
						iterator);
			}
		}

		private void ignoreLabeledGroupAndJumpsToIt(
				AbstractInsnNode startSearch, List<JumpInsnNode> jumpNodes,
				ListIterator<JumpInsnNode> iterator) {
			List<AbstractInsnNode> labeledGroupNodes = new ArrayList<AbstractInsnNode>();
			LabelNode previousLabel = findPreviousLabel(startSearch, jumpNodes,
					iterator, labeledGroupNodes);
			if (previousLabel != null) {

				if (ignoredLabeledGroups.contains(previousLabel)) {
					return;
				}
			}
			AbstractInsnNode lastNode = findLabeledGroupEnd(startSearch,

					iterator, jumpNodes, labeledGroupNodes);
			labeledGroupNodes.add(startSearch);
			if (hasNonComposeRuntimeInvocations(labeledGroupNodes)) {
				return;
			}
			if (previousLabel != null) {

				output.ignore(previousLabel, lastNode);
				ignoredLabeledGroups.add(previousLabel);
				AbstractInsnNode previousJumpTo = findPreviousJumpTo(
						previousLabel);
				if (previousJumpTo != null) {
					ignoreLabeledGroupAndJumpsToIt(previousJumpTo, jumpNodes,
							iterator);
				}
			} else {
				output.ignore(methodNode.instructions.getFirst(), lastNode);
			}
		}

		private boolean hasNonComposeRuntimeInvocations(
				List<AbstractInsnNode> labeledGroupNodes) {
			boolean hasNonComposeInvocations = false;
			for (AbstractInsnNode labeledGroupNode : labeledGroupNodes) {
				if (isMethodInsn(labeledGroupNode)) {
					MethodInsnNode methodInsnNode = (MethodInsnNode) labeledGroupNode;
					hasNonComposeInvocations |= !isComposeRuntime(
							methodInsnNode)
							&& !isKotlinIntrinsics(methodInsnNode);
					if (methodInsnNode.owner.equals(
							"androidx/compose/runtime/ScopeUpdateScope")) {
						return false;
					}
				}
			}
			return hasNonComposeInvocations;
		}

		private boolean isKotlinIntrinsics(MethodInsnNode labeledGroupNode) {
			return KotlinFilterCommons.KOTLIN_JVM_INTERNAL_INTRINSICS
					.equals(labeledGroupNode.owner);
		}

		private static boolean isComposeRuntime(
				MethodInsnNode labeledGroupNode) {
			return labeledGroupNode.owner
					.startsWith("androidx/compose/runtime/");
		}

		private boolean isMethodInsn(AbstractInsnNode labeledGroupNode) {
			return labeledGroupNode.getType() == AbstractInsnNode.METHOD_INSN;
		}

		private AbstractInsnNode findPreviousJumpTo(LabelNode labelNode) {
			AbstractInsnNode prev = labelNode.getPrevious();
			while (prev != null && (prev.getType() != AbstractInsnNode.JUMP_INSN
					|| ((JumpInsnNode) prev).label != labelNode)) {
				prev = prev.getPrevious();
			}
			return prev;
		}

		private LabelNode findPreviousLabel(AbstractInsnNode startSearch,
				List<JumpInsnNode> jumpNodes,
				ListIterator<JumpInsnNode> iterator,
				List<AbstractInsnNode> labeledGroupNodes) {
			AbstractInsnNode previous = startSearch.getPrevious();
			while (previous != null
					&& previous.getType() != AbstractInsnNode.LABEL) {
				labeledGroupNodes.add(previous);
				collectJumpNode(previous, iterator, jumpNodes);
				previous = previous.getPrevious();
			}
			if (previous instanceof LabelNode) {
				return (LabelNode) previous;
			}
			return null;
		}

		private void collectJumpNode(AbstractInsnNode previous,
				ListIterator<JumpInsnNode> jumpNodeIterator,
				List<JumpInsnNode> jumpNodes) {
			if (previous.getType() == AbstractInsnNode.JUMP_INSN
					&& !jumpNodes.contains((JumpInsnNode) previous)) {
				jumpNodeIterator.add((JumpInsnNode) previous);
			}
		}

		private AbstractInsnNode findLabeledGroupEnd(
				AbstractInsnNode startSearch,
				ListIterator<JumpInsnNode> jumpNodesIterator,
				List<JumpInsnNode> jumpNodes,
				List<AbstractInsnNode> labeledGroupNodes) {
			AbstractInsnNode next = startSearch.getNext();
			while (next.getType() != AbstractInsnNode.LABEL) {
				labeledGroupNodes.add(next);
				collectJumpNode(next, jumpNodesIterator, jumpNodes);
				if (next.getNext() != null) {
					next = next.getNext();
				} else {
					break;
				}
			}
			return next.getPrevious();
		}
	}
}
