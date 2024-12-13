/*******************************************************************************
 * Copyright (c) 2009, 2024 Mountainminds GmbH & Co. KG and Contributors
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
package org.jacoco.core.internal.analysis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.jacoco.core.analysis.ISourceNode;
import org.jacoco.core.internal.analysis.filter.IFilterOutput;
import org.objectweb.asm.tree.AbstractInsnNode;

/**
 * Calculates the filtered coverage of a single method. An instance of this
 * class can be first used as {@link IFilterOutput} before the coverage result
 * is calculated.
 */
class MethodCoverageCalculator implements IFilterOutput {

	private final Map<AbstractInsnNode, Instruction> instructions;

	private final Set<AbstractInsnNode> ignored;

	/**
	 * Instructions that should be merged form disjoint sets. Coverage
	 * information from instructions of one set will be merged into
	 * representative instruction of set.
	 *
	 * Each such set is represented as a singly linked list: each element except
	 * one references another element from the same set, element without
	 * reference - is a representative of this set.
	 *
	 * This map stores reference (value) for elements of sets (key).
	 */
	private final Map<AbstractInsnNode, AbstractInsnNode> merged;

	private final Map<AbstractInsnNode, Iterable<Collection<InstructionBranch>>> replacements;

	MethodCoverageCalculator(
			final Map<AbstractInsnNode, Instruction> instructions) {
		this.instructions = instructions;
		this.ignored = new HashSet<AbstractInsnNode>();
		this.merged = new HashMap<AbstractInsnNode, AbstractInsnNode>();
		this.replacements = new HashMap<AbstractInsnNode, Iterable<Collection<InstructionBranch>>>();
	}

	/**
	 * Applies all specified filtering commands and calculates the resulting
	 * coverage.
	 *
	 * @param coverage
	 *            the result is added to this coverage node
	 */
	void calculate(final MethodCoverageImpl coverage) {
		applyMerges();
		applyReplacements();
		ensureCapacity(coverage);

		for (final Entry<AbstractInsnNode, Instruction> entry : instructions
				.entrySet()) {
			if (!ignored.contains(entry.getKey())) {
				final Instruction instruction = entry.getValue();
				coverage.increment(instruction.getInstructionCounter(),
						instruction.getBranchCounter(), instruction.getLine());
			}
		}

		coverage.incrementMethodCounter();
	}

	private void applyMerges() {
		// Merge to the representative:
		for (final Entry<AbstractInsnNode, AbstractInsnNode> entry : merged
				.entrySet()) {
			final AbstractInsnNode node = entry.getKey();
			final Instruction instruction = instructions.get(node);
			final AbstractInsnNode representativeNode = findRepresentative(
					node);
			ignored.add(node);
			instructions.put(representativeNode,
					instructions.get(representativeNode).merge(instruction));
			entry.setValue(representativeNode);
		}

		// Get merged value back from representative
		for (final Entry<AbstractInsnNode, AbstractInsnNode> entry : merged
				.entrySet()) {
			instructions.put(entry.getKey(),
					instructions.get(entry.getValue()));
		}
	}

	private void applyReplacements() {
		for (final Entry<AbstractInsnNode, Iterable<Collection<InstructionBranch>>> entry : replacements
				.entrySet()) {
			final Iterable<Collection<InstructionBranch>> targets = entry
					.getValue();
			int i = 0;
			for (final Collection<InstructionBranch> list : targets) {
				i += list.size();
			}
			final int[] branches = new int[i];
			final Instruction[] fromInstructions = new Instruction[i];
			final int[] fromBranches = new int[i];

			i = 0;
			int b = 0;
			for (final Collection<InstructionBranch> list : targets) {
				for (final InstructionBranch ib : list) {
					branches[i] = b;
					fromInstructions[i] = instructions.get(ib.instruction);
					fromBranches[i] = ib.branch;
					i++;
				}
				b++;
			}

			final AbstractInsnNode node = entry.getKey();
			instructions.put(node, instructions.get(node)
					.replaceBranches(branches, fromInstructions, fromBranches));
		}
	}

	private void ensureCapacity(final MethodCoverageImpl coverage) {
		// Determine line range:
		int firstLine = ISourceNode.UNKNOWN_LINE;
		int lastLine = ISourceNode.UNKNOWN_LINE;
		for (final Entry<AbstractInsnNode, Instruction> entry : instructions
				.entrySet()) {
			if (!ignored.contains(entry.getKey())) {
				final int line = entry.getValue().getLine();
				if (line != ISourceNode.UNKNOWN_LINE) {
					if (firstLine > line
							|| lastLine == ISourceNode.UNKNOWN_LINE) {
						firstLine = line;
					}
					if (lastLine < line) {
						lastLine = line;
					}
				}
			}
		}

		// Performance optimization to avoid incremental increase of line array:
		coverage.ensureCapacity(firstLine, lastLine);
	}

	private AbstractInsnNode findRepresentative(AbstractInsnNode i) {
		AbstractInsnNode r;
		while ((r = merged.get(i)) != null) {
			i = r;
		}
		return i;
	}

	// === IFilterOutput API ===

	public void ignore(final AbstractInsnNode fromInclusive,
			final AbstractInsnNode toInclusive) {
		for (AbstractInsnNode i = fromInclusive; i != toInclusive; i = i
				.getNext()) {
			ignored.add(i);
		}
		ignored.add(toInclusive);
	}

	public void merge(AbstractInsnNode i1, AbstractInsnNode i2) {
		i1 = findRepresentative(i1);
		i2 = findRepresentative(i2);
		if (i1 != i2) {
			merged.put(i2, i1);
		}
	}

	/**
	 * @deprecated use {@link #replaceBranches(AbstractInsnNode, Iterable)}
	 *             instead
	 */
	@Deprecated
	public void replaceBranches(final AbstractInsnNode source,
			final Set<AbstractInsnNode> newTargets) {
		final HashMap<AbstractInsnNode, Collection<InstructionBranch>> newBranches = new HashMap<AbstractInsnNode, Collection<InstructionBranch>>();
		for (final AbstractInsnNode target : newTargets) {
			final ArrayList<InstructionBranch> list = new ArrayList<InstructionBranch>();
			final int branches = Math.max(
					instructions.get(target).getBranchCounter().getTotalCount(),
					1);
			for (int branch = 0; branch < branches; branch++) {
				list.add(new InstructionBranch(target, branch));
			}
			newBranches.put(target, list);
		}
		replaceBranches(source, newBranches.values());
	}

	public void replaceBranches(final AbstractInsnNode source,
			final Iterable<Collection<InstructionBranch>> newBranches) {
		replacements.put(source, newBranches);
	}

	/**
	 * {@link #instruction} and index of its {@link #branch}.
	 */
	static class InstructionBranch {
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
	}

}
