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
import java.util.HashSet;

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
			if (chain.size() == 1) {
				continue;
			}
			final JumpInsnNode lastJump = chain.get(chain.size() - 1);
			final HashSet<AbstractInsnNode> newTargets = new HashSet<AbstractInsnNode>();
			newTargets.add(AbstractMatcher.skipNonOpcodes(lastJump.getNext()));
			newTargets.add(AbstractMatcher.skipNonOpcodes(lastJump.label));
			for (final AbstractInsnNode i : chain) {
				output.replaceBranches(i, newTargets);
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
				if (i.getPrevious().getOpcode() != Opcodes.ALOAD) {
					continue;
				}
				if (chain != null) {
					final AbstractInsnNode p1 = i.getPrevious();
					final AbstractInsnNode p2 = p1.getPrevious();
					if (p2 == null || p2.getOpcode() != Opcodes.ASTORE
							|| ((VarInsnNode) p1).var != ((VarInsnNode) p2).var) {
						continue;
					}
				}
			} else {
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

}
