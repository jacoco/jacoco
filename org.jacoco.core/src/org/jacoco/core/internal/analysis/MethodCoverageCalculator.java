/*******************************************************************************
 * Copyright (c) 2009, 2018 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.core.internal.analysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jacoco.core.analysis.ISourceFileCoverage;
import org.jacoco.core.analysis.ISourceNode;
import org.jacoco.core.internal.analysis.filter.IFilterOutput;
import org.objectweb.asm.tree.AbstractInsnNode;

/**
 * Calculates the filtered coverage of a single method. A instance of this class
 * can be first used as {@link IFilterOutput} before the coverage result is
 * calculated.
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

	private final Map<AbstractInsnNode, Set<AbstractInsnNode>> replacements;

	MethodCoverageCalculator(
			final Map<AbstractInsnNode, Instruction> instructions) {
		this.instructions = instructions;
		this.ignored = new HashSet<AbstractInsnNode>();
		this.merged = new HashMap<AbstractInsnNode, AbstractInsnNode>();
		this.replacements = new HashMap<AbstractInsnNode, Set<AbstractInsnNode>>();
	}

	/**
	 * Applies all specified filtering commands and calculates the resulting
	 * coverage.
	 * 
	 * @param coverage
	 *            the result is added to this coverage node
	 */
	void calculate(final MethodCoverageImpl coverage) {

		// Merge and calculate line range:
		int firstLine = ISourceFileCoverage.UNKNOWN_LINE;
		int lastLine = ISourceFileCoverage.UNKNOWN_LINE;

		for (final Map.Entry<AbstractInsnNode, Instruction> entry : instructions
				.entrySet()) {

			final Instruction instruction = entry.getValue();
			final AbstractInsnNode instructionNode = entry.getKey();
			final AbstractInsnNode representativeNode = findRepresentative(
					instructionNode);
			if (representativeNode != instructionNode) {
				ignored.add(instructionNode);
				instructions.put(representativeNode, instructions
						.get(representativeNode).merge(instruction));
				continue;
			}

			if (ignored.contains(instructionNode)) {
				continue;
			}

			final int line = instruction.getLine();
			if (line != ISourceNode.UNKNOWN_LINE) {
				if (firstLine > line || lastLine == ISourceNode.UNKNOWN_LINE) {
					firstLine = line;
				}
				if (lastLine < line) {
					lastLine = line;
				}
			}

		}

		// Performance optimization to avoid incremental increase of line array:
		coverage.ensureCapacity(firstLine, lastLine);

		// Apply replacements and report result:
		for (final Map.Entry<AbstractInsnNode, Instruction> entry : instructions
				.entrySet()) {
			final AbstractInsnNode instructionNode = entry.getKey();
			if (ignored.contains(instructionNode)) {
				continue;
			}

			Instruction instruction = entry.getValue();

			final Set<AbstractInsnNode> r = replacements.get(instructionNode);
			if (r != null) {
				final List<Instruction> newBranches = new ArrayList<Instruction>(
						r.size());
				for (final AbstractInsnNode b : r) {
					newBranches.add(instructions.get(findRepresentative(b)));
				}
				instruction = instruction.replaceBranches(newBranches);
			}

			coverage.increment(instruction.getInstructionCounter(),
					instruction.getBranchCounter(), instruction.getLine());
		}
		coverage.incrementMethodCounter();
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

	public void replaceBranches(final AbstractInsnNode source,
			final Set<AbstractInsnNode> newTargets) {
		replacements.put(source, newTargets);
	}

}
