/*******************************************************************************
 * Copyright (c) 2009, 2019 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.internal.analysis.filter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LookupSwitchInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TableSwitchInsnNode;

/**
 * Filters bytecode that Kotlin compiler generates for <code>when</code>
 * expressions which list all cases of <code>enum</code> or
 * <code>sealed class</code>, i.e. which don't require explicit
 * <code>else</code>.
 */
public final class KotlinWhenFilter implements IFilter {

	private static final String EXCEPTION = "kotlin/NoWhenBranchMatchedException";

	public void filter(final MethodNode methodNode,
			final IFilterContext context, final IFilterOutput output) {
		final Matcher matcher = new Matcher();
		for (AbstractInsnNode i = methodNode.instructions
				.getFirst(); i != null; i = i.getNext()) {
			matcher.match(i, output);
		}
	}

	private static class Matcher extends AbstractMatcher {
		void match(final AbstractInsnNode start, final IFilterOutput output) {
			if (start.getType() != InsnNode.LABEL) {
				return;
			}
			cursor = start;

			nextIsType(Opcodes.NEW, EXCEPTION);
			nextIs(Opcodes.DUP);
			nextIsInvoke(Opcodes.INVOKESPECIAL, EXCEPTION, "<init>", "()V");
			nextIs(Opcodes.ATHROW);

			for (AbstractInsnNode i = cursor; i != null; i = i.getPrevious()) {
				if (i.getOpcode() == Opcodes.IFEQ
						&& ((JumpInsnNode) i).label == start) {
					output.ignore(i, i);
					output.ignore(start, cursor);
					return;

				} else if (getDefaultLabel(i) == start) {
					ignoreDefaultBranch(i, output);
					output.ignore(start, cursor);
					return;

				}
			}
		}
	}

	private static LabelNode getDefaultLabel(final AbstractInsnNode i) {
		switch (i.getOpcode()) {
		case Opcodes.LOOKUPSWITCH:
			return ((LookupSwitchInsnNode) i).dflt;
		case Opcodes.TABLESWITCH:
			return ((TableSwitchInsnNode) i).dflt;
		default:
			return null;
		}
	}

	private static void ignoreDefaultBranch(final AbstractInsnNode switchNode,
			final IFilterOutput output) {
		final List<LabelNode> labels;
		if (switchNode.getOpcode() == Opcodes.LOOKUPSWITCH) {
			labels = ((LookupSwitchInsnNode) switchNode).labels;
		} else {
			labels = ((TableSwitchInsnNode) switchNode).labels;
		}
		final Set<AbstractInsnNode> newTargets = new HashSet<AbstractInsnNode>();
		for (LabelNode label : labels) {
			newTargets.add(AbstractMatcher.skipNonOpcodes(label));
		}
		output.replaceBranches(switchNode, newTargets);
	}

}
