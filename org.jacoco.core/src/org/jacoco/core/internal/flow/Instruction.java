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
	 * Marks one branch of this instruction as covered. Also recursively marks
	 * all predecessor instructions as covered if this is the first covered
	 * branch.
	 */
	public void setCovered() {
		Instruction i = this;
		while (i != null && i.coveredBranches++ == 0) {
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

}
