/*******************************************************************************
 * Copyright (c) 2009, 2025 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.internal.analysis.filter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

/**
 * Filters bytecode that Kotlin compiler generates for chains of safe call
 * operators ({@code ?.}).
 */
final class KotlinSafeCallOperatorFilter implements IFilter {

	public void filter(final MethodNode methodNode,
			final IFilterContext context, final IFilterOutput output) {
		for (final ArrayList<JumpInsnNode> chain : findChains(methodNode)) {
			final AbstractInsnNode ifNonNullInstruction = chain.get(0).label
					.getPrevious();
			if (chain.size() == 1
					&& ifNonNullInstruction.getOpcode() != Opcodes.IFNONNULL) {
				continue;
			}
			final AbstractInsnNode nullTarget = AbstractMatcher
					.skipNonOpcodes(chain.get(0).label);
			for (final AbstractInsnNode ifNullInstruction : chain) {
				final Replacements replacements = new Replacements();
				replacements.add(ifNullInstruction, ifNullInstruction, 0);
				replacements.add(nullTarget, nullTarget, 0);
				output.replaceBranches(ifNullInstruction, replacements);
			}
			if (ifNonNullInstruction.getOpcode() == Opcodes.IFNONNULL) {
				final Replacements replacements = new Replacements();
				replacements.add(nullTarget, nullTarget, 0);
				replacements.add(ifNonNullInstruction, ifNonNullInstruction, 1);
				output.replaceBranches(ifNonNullInstruction, replacements);
			}
		}
	}

	/**
	 * "optimized" chain:
	 *
	 * <pre>
	 * DUP
	 * IFNULL label
	 * ... // call 0
	 *
	 * ...
	 *
	 * DUP
	 * IFNULL label
	 * ... // call N
	 *
	 * label:
	 * POP
	 * </pre>
	 *
	 * "unoptimized" chain:
	 *
	 * <pre>
	 * ALOAD v0
	 * IFNULL label
	 * ... // call 0
	 *
	 * ...
	 *
	 * ASTORE v1
	 * ALOAD v1
	 * IFNULL label
	 * ... // call N
	 *
	 * label:
	 * ACONST_NULL
	 * </pre>
	 *
	 * "unoptimized" safe call operator(s) followed by elvis operator:
	 *
	 * <pre>
	 * ALOAD v0
	 * IFNULL nullCase
	 * ... // call 0
	 *
	 * ASTORE v1
	 * ALOAD v1
	 * IFNULL nullCase
	 * ... // call 1
	 *
	 * ...
	 *
	 * ASTORE vN
	 * ALOAD vN
	 * IFNULL nullCase
	 * ALOAD vN
	 * GOTO nonNullCase
	 * nullCase:
	 * ... // right hand side of elvis operator
	 * nonNullCase:
	 * ...
	 * </pre>
	 */
	private static Collection<ArrayList<JumpInsnNode>> findChains(
			final MethodNode methodNode) {
		final HashMap<AbstractInsnNode, ArrayList<JumpInsnNode>> chains = new HashMap<AbstractInsnNode, ArrayList<JumpInsnNode>>();
		for (final AbstractInsnNode i : methodNode.instructions) {
			if (i.getOpcode() != Opcodes.IFNULL) {
				continue;
			}
			final JumpInsnNode jump = (JumpInsnNode) i;
			final LabelNode label = jump.label;
			final AbstractInsnNode target = AbstractMatcher
					.skipNonOpcodes(label);
			ArrayList<JumpInsnNode> chain = chains.get(label);
			if (target.getOpcode() == Opcodes.POP) {
				if (i.getPrevious().getOpcode() != Opcodes.DUP) {
					continue;
				}
			} else if (target.getOpcode() == Opcodes.ACONST_NULL) {
				final AbstractInsnNode p1 = preceding(i, Opcodes.ALOAD);
				if (p1 == null) {
					continue;
				}
				if (chain != null) {
					final AbstractInsnNode p2 = preceding(p1, Opcodes.ASTORE);
					if (p2 == null
							|| ((VarInsnNode) p1).var != ((VarInsnNode) p2).var) {
						continue;
					}
				}
			} else if (!isUnoptimizedSafeCallFollowedByElvis(jump, target,
					chain)) {
				continue;
			}
			if (chain == null) {
				chain = new ArrayList<JumpInsnNode>();
				chains.put(label, chain);
			}
			chain.add(jump);
		}
		return chains.values();
	}

	private static boolean isUnoptimizedSafeCallFollowedByElvis(
			final JumpInsnNode jump, final AbstractInsnNode target,
			final ArrayList<JumpInsnNode> chain) {
		if (target.getType() == AbstractInsnNode.JUMP_INSN
				|| target.getType() == AbstractInsnNode.TABLESWITCH_INSN
				|| target.getType() == AbstractInsnNode.LOOKUPSWITCH_INSN) {
			return false;
		}
		final AbstractInsnNode p1 = preceding(jump, Opcodes.ALOAD);
		if (p1 == null) {
			return false;
		} else if (chain == null) {
			final AbstractInsnNode gotoInstruction = preceding(jump.label,
					Opcodes.GOTO);
			final AbstractInsnNode loadInstruction1 = preceding(gotoInstruction,
					Opcodes.ALOAD);
			final AbstractInsnNode ifNullInstruction = preceding(
					loadInstruction1, Opcodes.IFNULL);
			final AbstractInsnNode loadInstruction2 = preceding(
					ifNullInstruction, Opcodes.ALOAD);
			final AbstractInsnNode storeInstruction = preceding(
					loadInstruction2, Opcodes.ASTORE);
			return storeInstruction != null
					&& ((JumpInsnNode) ifNullInstruction).label == jump.label
					&& ((VarInsnNode) loadInstruction1).var == ((VarInsnNode) loadInstruction2).var
					&& ((VarInsnNode) loadInstruction1).var == ((VarInsnNode) storeInstruction).var;
		} else {
			final AbstractInsnNode p2 = preceding(p1, Opcodes.ASTORE);
			return p2 != null
					&& ((VarInsnNode) p1).var == ((VarInsnNode) p2).var;
		}
	}

	/**
	 * @return non pseudo-instruction preceding given if it has given opcode,
	 *         {@code null} otherwise
	 */
	private static AbstractInsnNode preceding(AbstractInsnNode i,
			final int opcode) {
		if (i == null) {
			return null;
		}
		do {
			i = i.getPrevious();
			if (i == null) {
				return null;
			}
		} while (i.getType() == AbstractInsnNode.LABEL
				|| i.getType() == AbstractInsnNode.LINE
				|| i.getType() == AbstractInsnNode.FRAME);
		return i.getOpcode() == opcode ? i : null;
	}

}
