/*******************************************************************************
 * Copyright (c) 2009, 2015 Mountainminds GmbH & Co. KG and Contributors
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

/**
 * Representation of a byte code instruction for analysis. Internally used for
 * analysis.
 */
public class Instruction {

	private final int line;

	private int executions;

	private int parallelExecutions;

	private int branches;

	private int coveredBranches;

	private Instruction predecessor;

	/**
	 * New instruction at the given line.
	 * 
	 * @param line
	 *            source line this instruction belongs to
	 */
	public Instruction(final int line) {
		this.line = line;
		this.executions = 0;
		this.parallelExecutions = 0;
		this.branches = 0;
		this.coveredBranches = 0;
	}

	/**
	 * Adds an branch to this instruction.
	 */
	public void addBranch() {
		branches++;
	}

	/**
	 * Sets the given instruction as a predecessor of this instruction. This
	 * will add an branch to the predecessor.
	 * 
	 * @see #addBranch()
	 * @param predecessor
	 *            predecessor instruction
	 */
	public void setPredecessor(final Instruction predecessor) {
		this.predecessor = predecessor;
		predecessor.addBranch();
	}

	/**
	 * Add to the total number of executions at this instruction
	 * 
	 * @param executions
	 *            the number of executions at this instruction
	 */
	public void addExecutions(final int executions) {
		this.executions += executions;
	}

	/**
	 * Add to the total number of parallel executions at this instruction
	 * 
	 * @param parallelExecutions
	 *            the number of parallel executions at this instruction
	 */
	public void addParallelExecutions(final int parallelExecutions) {
		this.parallelExecutions += parallelExecutions;
	}

	/**
	 * Marks one branch of this instruction as covered. Also recursively marks
	 * all predecessor instructions as covered if this is the first covered
	 * branch.
	 */
	public void setCovered() {
		Instruction i = this;
		while (i != null && i.coveredBranches++ == 0) {
			if (i != this) {
				i.executions += this.executions;
				i.parallelExecutions += this.parallelExecutions;
			}
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
		return coveredBranches;
	}

	/**
	 * Returns the total number of executions on this instruction.
	 * 
	 * @return total number of executions on this instruction.
	 */
	public int getExecutions() {
		return executions;
	}

	/**
	 * Returns the total number of parallel executions on this instruction.
	 * 
	 * @return total number of parallel executions on this instruction.
	 */
	public int getParallelExecutions() {
		return parallelExecutions;
	}

}
