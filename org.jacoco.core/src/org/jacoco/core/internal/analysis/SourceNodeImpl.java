/*******************************************************************************
 * Copyright (c) 2009, 2025 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.internal.analysis;

import org.jacoco.core.analysis.CoverageNodeImpl;
import org.jacoco.core.analysis.ICounter;
import org.jacoco.core.analysis.ILine;
import org.jacoco.core.analysis.ISourceNode;
import org.jacoco.core.internal.diff.ChangeLine;

import java.util.List;

/**
 * Implementation of {@link ISourceNode}.
 */
public class SourceNodeImpl extends CoverageNodeImpl implements ISourceNode {

	private LineImpl[] lines;

	/** first line number in {@link #lines} */
	private int offset;

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
	}

	/**
	 * @param fragment
	 *            fragment to apply
	 * @return <code>true</code> if fragment contains lines of this node
	 */
	public boolean applyFragment(final SourceNodeImpl fragment) {
		boolean applied = false;
		for (int line = getFirstLine(); line <= getLastLine(); line++) {
			final ILine fragmentLine = fragment.getLine(line);
			if (fragmentLine.equals(LineImpl.EMPTY)) {
				continue;
			}
			final LineImpl l = getLine(line);
			final CounterImpl counter;
			if (l.getInstructionCounter().getCoveredCount() > 0 || fragmentLine
					.getInstructionCounter().getCoveredCount() > 0) {
				counter = CounterImpl.COUNTER_0_1;
			} else {
				counter = CounterImpl.COUNTER_1_0;
			}
			lines[line - offset] = LineImpl.EMPTY;
			if (l.instructions.covered > 0) {
				lineCounter = lineCounter.increment(0, -1);
			} else if (l.instructions.missed > 0) {
				lineCounter = lineCounter.increment(-1, 0);
			}
			incrementLine(counter, CounterImpl.COUNTER_0_0, line);
			instructionCounter = instructionCounter.increment(
					counter.missed - l.instructions.missed,
					counter.covered - l.instructions.covered);
			branchCounter = branchCounter.increment(-l.branches.missed,
					-l.branches.covered);
			applied = true;
		}
		return applied;
	}

	/**
	 * Make sure that the internal buffer can keep lines from first to last.
	 * While the buffer is also incremented automatically, this method allows
	 * optimization in case the total range is known in advance.
	 *由于通过ASM的访问者模式访问的方法，所以在进行方法级别覆盖率计算时候需要每次都更新 MethodCoverageImpl coverage offset和last line，
	 * 对应源码的行数，offset是第一行代码行数，因为是根据方法级别的计算，所以可以推断出offset是类的第一个方法第一行代码的行数，这里的test方法第一行是14，
	 * 所以offset=14。coverage lines属性是所有行覆盖率的数组，coverage lines属性是源码染色的依据。
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
		instructionCounter = instructionCounter
				.increment(child.getInstructionCounter());
		branchCounter = branchCounter.increment(child.getBranchCounter());
		complexityCounter = complexityCounter
				.increment(child.getComplexityCounter());
		methodCounter = methodCounter.increment(child.getMethodCounter());
		classCounter = classCounter.increment(child.getClassCounter());
		changeLineCounter = changeLineCounter.increment(child.getChangeLineCounter());
		final int firstLine = child.getFirstLine();
		if (firstLine != UNKNOWN_LINE) {
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
	 * Increments instructions and branches by the given counter values. If an
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
			incrementLine(instructions, branches, line);
		}
		instructionCounter = instructionCounter.increment(instructions);
		branchCounter = branchCounter.increment(branches);
	}
	public void increment(final ICounter instructions, final ICounter branches,
						  final int line, List<ChangeLine> changeLineList) {
		if (line != UNKNOWN_LINE) {
			incrementLine(instructions, branches, line,changeLineList);
		}
		instructionCounter = instructionCounter.increment(instructions);
		branchCounter = branchCounter.increment(branches);
	}

	// 计算行覆盖率的核心方法
	private void incrementLine(final ICounter instructions,
			final ICounter branches, final int line) {

		ensureCapacity(line, line);
		final LineImpl l = getLine(line);
		final int oldTotal = l.getInstructionCounter().getTotalCount();
		final int oldCovered = l.getInstructionCounter().getCoveredCount();
		lines[line - offset] = l.increment(instructions, branches);

		// Increment line counter:
		if (instructions.getTotalCount() > 0) {
			if (instructions.getCoveredCount() == 0) {
				if (oldTotal == 0) {
					lineCounter = lineCounter
							.increment(CounterImpl.COUNTER_1_0);
				}
			} else {
				if (oldTotal == 0) {
					lineCounter = lineCounter
							.increment(CounterImpl.COUNTER_0_1);
				} else {
					if (oldCovered == 0) {
						lineCounter = lineCounter.increment(-1, +1);
					}
				}
			}
		}
	}

	private void incrementLine(final ICounter instructions,
							   final ICounter branches, final int line, List<ChangeLine> changeLineList) {

		ensureCapacity(line, line);
		final LineImpl l = getLine(line);
		final int oldTotal = l.getInstructionCounter().getTotalCount();
		final int oldCovered = l.getInstructionCounter().getCoveredCount();
		lines[line - offset] = l.increment(instructions, branches);
		int flag = 0;
		int style = l.getStatus();
		if (changeLineList != null){
			if (line >= changeLineList.get(0).getStartLineNum() && line <= changeLineList.get(0).getEndLineNum()){
				flag = 1;
			}
		}
		// Increment line counter:
		if (instructions.getTotalCount() > 0) {
			if (instructions.getCoveredCount() == 0) {
				if (oldTotal == 0) {
					lineCounter = lineCounter
							.increment(CounterImpl.COUNTER_1_0);
					if (flag == 1){
						changeLineCounter = changeLineCounter
								.increment(CounterImpl.COUNTER_1_0);
					}
				}
				// changeLine不同，当前未覆盖过去统计为已覆盖需要cover-1,miss+1
				else{
					if (oldCovered != 0 && flag == 1 ) {
						changeLineCounter = changeLineCounter.increment(+1, -1);
					}
				}
			} else {
				if (oldTotal == 0) {
					lineCounter = lineCounter
							.increment(CounterImpl.COUNTER_0_1);
					if (flag == 1){
						changeLineCounter = changeLineCounter
								.increment(CounterImpl.COUNTER_0_1);
					}
				} else {
					// 部分指令覆盖，不能算覆盖了
					if (oldCovered == 0) {
						lineCounter = lineCounter.increment(-1, +1);
					}
				}
			}
		}
		// changeLine还需要进行一次分支校验
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

}
