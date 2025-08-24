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
import java.util.LinkedHashMap;
import java.util.List;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LookupSwitchInsnNode;
import org.objectweb.asm.tree.TableSwitchInsnNode;

/**
 * Utility for creating an argument for
 * {@link IFilterOutput#replaceBranches(AbstractInsnNode, Replacements)} with
 * information about how to compute the coverage status of branches of
 * instruction from the coverage status of branches of other instructions.
 */
public final class Replacements {

	private final LinkedHashMap<AbstractInsnNode, Collection<InstructionBranch>> newBranches = new LinkedHashMap<AbstractInsnNode, Collection<InstructionBranch>>();

	/**
	 * Adds branch which has a given target and which should be considered as
	 * covered when a branch with a given index of a given instruction is
	 * covered.
	 * <p>
	 * The branch index should be specified in accordance with the ones assigned
	 * by {@link org.jacoco.core.internal.analysis.MethodAnalyzer} to a given
	 * instruction:
	 *
	 * <ul>
	 * <li>for {@link org.objectweb.asm.tree.TableSwitchInsnNode} (and similarly
	 * for {@link org.objectweb.asm.tree.LookupSwitchInsnNode})
	 * <ul>
	 * <li>the branch index corresponds to the indexes in the list of unique
	 * labels among {@link org.objectweb.asm.tree.TableSwitchInsnNode#dflt} and
	 * {@link org.objectweb.asm.tree.TableSwitchInsnNode#labels}</li>
	 * <li>there are as many branches as unique labels</li>
	 * <li>branch 0 corresponds to continuation of execution at
	 * {@link org.objectweb.asm.tree.TableSwitchInsnNode#dflt}</li>
	 * </ul>
	 * </li>
	 *
	 * <li>for {@link org.objectweb.asm.tree.JumpInsnNode} with
	 * {@link org.objectweb.asm.Opcodes#GOTO} there is only branch 1 that
	 * corresponds to continuation of execution at
	 * {@link org.objectweb.asm.tree.JumpInsnNode#label}</li>
	 *
	 * <li>for other {@link org.objectweb.asm.tree.JumpInsnNode} there are two
	 * branches
	 * <ul>
	 * <li>branch 1 corresponds to continuation of execution at
	 * {@link org.objectweb.asm.tree.JumpInsnNode#label}</li>
	 * <li>branch 0 corresponds to continuation of execution at
	 * {@link AbstractInsnNode#getNext()}</li>
	 * </ul>
	 * </li>
	 *
	 * <li>for instructions with {@link org.objectweb.asm.Opcodes#RETURN} and
	 * {@link org.objectweb.asm.Opcodes#ATHROW} there is only branch 0 that
	 * corresponds to exit from the method</li>
	 *
	 * <li>there are no branches for instructions whose
	 * {@link AbstractInsnNode#getOpcode()} is -1</li>
	 *
	 * <li>for other instructions there is only branch 0 that corresponds to
	 * continuation of execution at {@link AbstractInsnNode#getNext()}</li>
	 * </ul>
	 *
	 * @param target
	 *            instruction uniquely identifying new branch, e.g. its target
	 * @param instruction
	 *            instruction whose branch execution status should be used
	 * @param branchIndex
	 *            index of branch whose execution status should be used
	 */
	public void add(final AbstractInsnNode target,
			final AbstractInsnNode instruction, final int branchIndex) {
		Collection<InstructionBranch> from = newBranches.get(target);
		if (from == null) {
			from = new ArrayList<InstructionBranch>();
			newBranches.put(target, from);
		}
		from.add(new InstructionBranch(instruction, branchIndex));
	}

	/**
	 * @return the accumulated information in the order of
	 *         {@link #add(AbstractInsnNode, AbstractInsnNode, int) additions}
	 */
	public Iterable<Collection<InstructionBranch>> values() {
		return newBranches.values();
	}

	/**
	 * @return information about how to compute coverage status of branches of a
	 *         given {@link TableSwitchInsnNode} or {@link LookupSwitchInsnNode}
	 *         in order to ignore its {@link TableSwitchInsnNode#dflt} or
	 *         {@link LookupSwitchInsnNode#dflt}
	 */
	static Replacements ignoreDefaultBranch(final AbstractInsnNode switchNode) {
		final List<LabelNode> labels;
		final LabelNode defaultLabel;
		if (switchNode.getOpcode() == Opcodes.LOOKUPSWITCH) {
			final LookupSwitchInsnNode s = (LookupSwitchInsnNode) switchNode;
			labels = s.labels;
			defaultLabel = s.dflt;
		} else {
			final TableSwitchInsnNode s = (TableSwitchInsnNode) switchNode;
			labels = s.labels;
			defaultLabel = s.dflt;
		}
		final Replacements replacements = new Replacements();
		int branchIndex = 0;
		for (final LabelNode label : labels) {
			if (label != defaultLabel
					&& replacements.newBranches.get(label) == null) {
				branchIndex++;
				replacements.add(label, switchNode, branchIndex);
			}
		}
		return replacements;
	}

	/**
	 * {@link #instruction} and index of its {@link #branch}.
	 */
	public static final class InstructionBranch {
		/** Instruction. */
		public final AbstractInsnNode instruction;
		/** Branch index. */
		public final int branch;

		/**
		 * Creates a new {@link InstructionBranch}.
		 *
		 * @param instruction
		 *            instruction
		 * @param branch
		 *            branch index
		 */
		public InstructionBranch(final AbstractInsnNode instruction,
				final int branch) {
			this.instruction = instruction;
			this.branch = branch;
		}

		@Override
		public boolean equals(final Object o) {
			if (o == null || getClass() != o.getClass()) {
				return false;
			}
			final InstructionBranch other = (InstructionBranch) o;
			return this.instruction.equals(other.instruction)
					&& this.branch == other.branch;
		}

		@Override
		public int hashCode() {
			return instruction.hashCode() * 31 + branch;
		}
	}

}
