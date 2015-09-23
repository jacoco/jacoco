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
package org.jacoco.core.internal.analysis;

import org.jacoco.core.analysis.CoverageNodeImpl;
import org.jacoco.core.analysis.EBigOFunction;
import org.jacoco.core.analysis.ICounter;
import org.jacoco.core.analysis.ILine;
import org.jacoco.core.analysis.ISourceNode;

/**
 * Implementation of {@link ISourceNode}.
 */
public class SourceNodeImpl extends CoverageNodeImpl implements ISourceNode {

	private LineImpl[] lines;

	/** first line number in {@link #lines} */
	private int offset;

	private boolean hasEBigO;
	private EBigOFunction eBigOFunction;
	private EBigOFunction[] lineEBigOFunctions;

	/** first line number in {@link #lineEBigOFunctions} */
	private int eBigOOffset;

	/**
	 * Create a new source node implementation instance.
	 * 
	 * @param elementType
	 *            element type
	 * @param name
	 *            name of the element
	 */
	public SourceNodeImpl(final ElementType elementType, final String name) {
		super(elementType, name);
		lines = null;
		offset = UNKNOWN_LINE;
		hasEBigO = false;
		eBigOFunction = EBigOFunction.UNDEFINED;
		lineEBigOFunctions = null;
		eBigOOffset = UNKNOWN_LINE;
	}

	/**
	 * Make sure that the internal buffer can keep lines from first to last.
	 * While the buffer is also incremented automatically, this method allows
	 * optimization in case the total range in known in advance.
	 * 
	 * @param first
	 *            first line number or {@link ISourceNode#UNKNOWN_LINE}
	 * @param last
	 *            last line number or {@link ISourceNode#UNKNOWN_LINE}
	 */
	public void ensureCapacity(final int first, final int last) {
		if (first == UNKNOWN_LINE || last == UNKNOWN_LINE) {
			return;
		}
		if (lines == null) {
			offset = first;
			lines = new LineImpl[last - first + 1];
		} else {
			final int newFirst = Math.min(getFirstLine(), first);
			final int newLast = Math.max(getLastLine(), last);
			final int newLength = newLast - newFirst + 1;
			if (newLength > lines.length) {
				final LineImpl[] newLines = new LineImpl[newLength];
				System.arraycopy(lines, 0, newLines, offset - newFirst,
						lines.length);
				offset = newFirst;
				lines = newLines;
			}
		}
		if (lineEBigOFunctions != null) {
			ensureEBigOCapacityInternal();
		}
	}

	/**
	 * Increments all counters by the values of the given child. When
	 * incrementing the line counter it is assumed that the child refers to the
	 * same source file.
	 * 
	 * @param child
	 *            child node to add
	 */
	public void increment(final ISourceNode child) {
		instructionCounter = instructionCounter.increment(child
				.getInstructionCounter());
		branchCounter = branchCounter.increment(child.getBranchCounter());
		complexityCounter = complexityCounter.increment(child
				.getComplexityCounter());
		methodCounter = methodCounter.increment(child.getMethodCounter());
		classCounter = classCounter.increment(child.getClassCounter());
		mergeProbeMode(child);
		final int firstLine = child.getFirstLine();
		if (firstLine != UNKNOWN_LINE) {
			lineCounter = lineCounter.increment(child.getLineCounter());
			final int lastLine = child.getLastLine();
			ensureCapacity(firstLine, lastLine);
			for (int i = firstLine; i <= lastLine; i++) {
				final ILine line = child.getLine(i);
				incrementLine(line.getInstructionCounter(),
						line.getBranchCounter(), i);
			}
		}
	}

	/**
	 * Increments instructions and branches by the given counter values. If a
	 * optional line number is specified the instructions and branches are added
	 * to the given line. The line counter is incremented accordingly.
	 * 
	 * @param instructions
	 *            instructions to add
	 * @param branches
	 *            branches to add
	 * @param line
	 *            optional line number or {@link ISourceNode#UNKNOWN_LINE}
	 */
	public void increment(final ICounter instructions, final ICounter branches,
			final int line) {
		if (line != UNKNOWN_LINE) {

			// Increment line counter:
			final LineImpl l = getLine(line);
			final int oldTotal = l.getInstructionCounter().getTotalCount();
			final int oldCovered = l.getInstructionCounter().getCoveredCount();
			final int oldExec = l.getInstructionCounter().getExecutionCount();

			incrementLine(instructions, branches, line);

			int missedAdj = 0;
			int coveredAdj = 0;
			if (instructions.getTotalCount() > 0) {
				if (instructions.getCoveredCount() == 0) {
					if (oldTotal == 0) {
						missedAdj = 1;
					}
				} else {
					if (oldTotal == 0) {
						coveredAdj = 1;
					} else {
						if (oldCovered == 0) {
							missedAdj = -1;
							coveredAdj = +1;
						}
					}
				}
			}
			final int execAdj = Math.max(oldExec,
					instructions.getExecutionCount())
					- oldExec;
			lineCounter = lineCounter.increment(missedAdj, coveredAdj, execAdj);
		}
		instructionCounter = instructionCounter.increment(instructions);
		branchCounter = branchCounter.increment(branches);

	}

	private void incrementLine(final ICounter instructions,
			final ICounter branches, final int line) {
		ensureCapacity(line, line);
		lines[line - offset] = getLine(line).increment(instructions, branches);
	}

	/**
	 * Set the results of an E-Big-O analysis on this node
	 * 
	 * @param eBigOFunction
	 *            the results of an E-Big-O analysis on this noe
	 */
	public void setEBigOFunction(final EBigOFunction eBigOFunction) {
		this.eBigOFunction = eBigOFunction;
		this.hasEBigO = true;
		this.containsEBigO = true;
	}

	/**
	 * Make sure that the internal buffer can keep E-BigO function lines from
	 * first to last. While the buffer is also incremented automatically, this
	 * method allows optimization in case the total range in known in advance.
	 * 
	 * @param first
	 *            first line number or {@link ISourceNode#UNKNOWN_LINE}
	 * @param last
	 *            last line number or {@link ISourceNode#UNKNOWN_LINE}
	 */
	public void ensureEBigOCapacity(final int first, final int last) {
		if (first == UNKNOWN_LINE || last == UNKNOWN_LINE) {
			return;
		}
		ensureCapacity(first, last);
		ensureEBigOCapacityInternal();
	}

	private void ensureEBigOCapacityInternal() {
		if (lineEBigOFunctions != null
				&& lineEBigOFunctions.length == lines.length) {
			return;
		}

		final EBigOFunction[] newLineEBigOFunctions = new EBigOFunction[lines.length];
		if (lineEBigOFunctions != null) {
			System.arraycopy(lineEBigOFunctions, 0, newLineEBigOFunctions,
					eBigOOffset - offset, lineEBigOFunctions.length);
		}
		eBigOOffset = offset;
		lineEBigOFunctions = newLineEBigOFunctions;
	}

	/**
	 * Set the results of an E-Big-O analysis on a line of this node
	 * 
	 * @param lineEBigOFunction
	 *            the results of an E-Big-O analysis
	 * @param line
	 *            the line to set
	 */
	public void setLineEBigOFunction(final EBigOFunction lineEBigOFunction,
			final int line) {
		ensureEBigOCapacity(line, line);
		lineEBigOFunctions[line - offset] = lineEBigOFunction;
		this.hasEBigO = true;
		this.containsEBigO = true;
	}

	// === ISourceNode implementation ===

	public int getFirstLine() {
		return offset;
	}

	public int getLastLine() {
		return lines == null ? UNKNOWN_LINE : (offset + lines.length - 1);
	}

	public LineImpl getLine(final int nr) {
		if (lines == null || nr < getFirstLine() || nr > getLastLine()) {
			return LineImpl.EMPTY;
		}
		final LineImpl line = lines[nr - offset];
		return line == null ? LineImpl.EMPTY : line;
	}

	public boolean hasEBigO() {
		return hasEBigO;
	}

	public EBigOFunction getEBigOFunction() {
		return eBigOFunction;
	}

	public EBigOFunction getLineEBigOFunction(final int nr) {
		if (lineEBigOFunctions == null || nr < getFirstLine()
				|| (nr - offset) >= lineEBigOFunctions.length) {
			return EBigOFunction.UNDEFINED;
		}
		final EBigOFunction eBigOFunction = lineEBigOFunctions[nr - offset];
		return eBigOFunction == null ? EBigOFunction.UNDEFINED : eBigOFunction;
	}

}
