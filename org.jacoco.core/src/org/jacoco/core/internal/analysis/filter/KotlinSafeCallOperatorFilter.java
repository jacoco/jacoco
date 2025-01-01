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
	 */
	private static Collection<ArrayList<JumpInsnNode>> findChains(
			final MethodNode methodNode) {
		final HashMap<AbstractInsnNode, ArrayList<JumpInsnNode>> chains = new HashMap<AbstractInsnNode, ArrayList<JumpInsnNode>>();
		for (final AbstractInsnNode i : methodNode.instructions) {
			if (i.getOpcode() == Opcodes.IFNULL
					&& i.getPrevious().getOpcode() == Opcodes.DUP) {
				final JumpInsnNode jump = (JumpInsnNode) i;
				final LabelNode label = jump.label;
				if (AbstractMatcher.skipNonOpcodes(label.getNext())
						.getOpcode() != Opcodes.POP) {
					continue;
				}
				ArrayList<JumpInsnNode> chain = chains.get(label);
				if (chain == null) {
					chain = new ArrayList<JumpInsnNode>();
					chains.put(label, chain);
				}
				chain.add(jump);
			}
		}
		return chains.values();
	}

}
