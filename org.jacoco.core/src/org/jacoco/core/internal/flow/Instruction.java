/*******************************************************************************
 * Copyright (c) 2009, 2018 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.core.internal.flow;

import org.objectweb.asm.tree.AbstractInsnNode;

import java.util.BitSet;

/**
 * Representation of a byte code instruction for analysis. Internally used for
 * analysis.
 */
public class Instruction {

	private final AbstractInsnNode node;

	private final int line;

	private int branches;

	private final BitSet coveredBranches;

	private Instruction predecessor;

	private int predecessorBranch;

	/**
	 * New instruction at the given line.
	 * 
	 * @param node
	 *            corresponding node
	 * @param line
	 *            source line this instruction belongs to
	 */
	public Instruction(final AbstractInsnNode node, final int line) {
		this.node = node;
		this.line = line;
		this.branches = 0;
		this.coveredBranches = new BitSet();
	}

	/**
	 * @return corresponding node
	 */
	public AbstractInsnNode getNode() {
		return node;
	}

	/**
	 * Adds an branch to this instruction.
	 */
	public void addBranch() {
		branches++;
	}

	/**
	 * Sets the given instruction as a predecessor of this instruction and adds
	 * branch to the predecessor. Probes are inserted in a way that every
	 * instruction has at most one direct predecessor.
	 * 
	 * @see #addBranch()
	 * @param predecessor
	 *            predecessor instruction
	 * @param branch
	 *            branch number in predecessor that should be marked as covered
	 *            when this instruction marked as covered
	 */
	public void setPredecessor(final Instruction predecessor,
			final int branch) {
		this.predecessor = predecessor;
		predecessor.addBranch();
		this.predecessorBranch = branch;
	}

	/**
	 * Marks one branch of this instruction as covered. Also recursively marks
	 * all predecessor instructions as covered if this is the first covered
	 * branch.
	 *
	 * @param branch
	 *            branch number to mark as covered
	 */
	public void setCovered(final int branch) {
		Instruction i = this;
		int b = branch;
		while (i != null) {
			if (!i.coveredBranches.isEmpty()) {
				i.coveredBranches.set(b);
				break;
			}
			i.coveredBranches.set(b);
			b = i.predecessorBranch;
			i = i.predecessor;
		}
	}

	/**
	 * Returns the source line this instruction belongs to.
	 * 
	 * @return corresponding source line
	 */
	public int getLine() {
		return line;
	}

	/**
	 * Returns the total number of branches starting from this instruction.
	 * 
	 * @return total number of branches
	 */
	public int getBranches() {
		return branches;
	}

	/**
	 * Returns the number of covered branches starting from this instruction.
	 * 
	 * @return number of covered branches
	 */
	public int getCoveredBranches() {
		return coveredBranches.cardinality();
	}

	/**
	 * Merges information about covered branches of given instruction into this
	 * instruction.
	 * 
	 * @param instruction
	 *            instruction from which to merge
	 */
	public void merge(Instruction instruction) {
		this.coveredBranches.or(instruction.coveredBranches);
	}

	@Override
	public String toString() {
		return coveredBranches.toString();
	}

}
