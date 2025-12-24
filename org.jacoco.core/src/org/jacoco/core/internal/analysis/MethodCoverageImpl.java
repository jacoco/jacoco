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

import org.jacoco.core.analysis.ICounter;
import org.jacoco.core.analysis.ILine;
import org.jacoco.core.analysis.IMethodCoverage;
import org.jacoco.core.internal.diff.ChangeLine;

import java.util.List;
import java.util.Set;

import static org.jacoco.core.analysis.ICounter.FULLY_COVERED;

/**
 * Implementation of {@link IMethodCoverage}.
 */
public class MethodCoverageImpl extends SourceNodeImpl
		implements IMethodCoverage {

	private final String desc;

	private final String signature;

	/**
	 * Creates a method coverage data object with the given parameters.
	 *
	 * @param name
	 *            name of the method
	 * @param desc
	 *            method descriptor
	 * @param signature
	 *            generic signature or <code>null</code>
	 */
	public MethodCoverageImpl(final String name, final String desc,
			final String signature) {
		super(ElementType.METHOD, name);
		this.desc = desc;
		this.signature = signature;
	}

	@Override
	public void increment(final ICounter instructions, final ICounter branches,
			final int line) {
		super.increment(instructions, branches, line);
		// Additionally increment complexity counter:
		if (branches.getTotalCount() > 1) {
			final int c = Math.max(0, branches.getCoveredCount() - 1);
			final int m = Math.max(0, branches.getTotalCount() - c - 1);
			this.complexityCounter = this.complexityCounter.increment(m, c);
		}
	}
	public void increment(final ICounter instructions, final ICounter branches,
						  final int line, List<ChangeLine> changeLineList) {
		super.increment(instructions, branches, line,changeLineList);
		// Additionally increment complexity counter:
		if (branches.getTotalCount() > 1) {
			final int c = Math.max(0, branches.getCoveredCount() - 1);
			final int m = Math.max(0, branches.getTotalCount() - c - 1);
			this.complexityCounter = this.complexityCounter.increment(m, c);
		}
	}

	/**
	 *
	 */
	public void incrementChangeLineCounter(Set<String> lines, List<ChangeLine> changeLineList) {
		for (String line:lines){
			int l = Integer.parseInt(line);
			int flag = 0;
			ILine iLine = getLine(l);
			// 判断此行是否为变动行
			if (changeLineList != null){
				if (changeLineList.stream().anyMatch(i -> i.getStartLineNum() < l && i.getEndLineNum() >= l)) {
					flag = 1;
				}
			}
			if (iLine != null && flag == 1){
				if (iLine.getStatus() == FULLY_COVERED){
					this.changeLineCounter = this.changeLineCounter.increment(CounterImpl.COUNTER_0_1);
				}
				else {
					this.changeLineCounter = this.changeLineCounter.increment(CounterImpl.COUNTER_1_0);
				}
			}
		}
	}


	/**
	 * This method must be called exactly once after all instructions and
	 * branches have been incremented for this method coverage node.
	 */
	public void incrementMethodCounter() {
		final ICounter base = this.instructionCounter.getCoveredCount() == 0
				? CounterImpl.COUNTER_1_0
				: CounterImpl.COUNTER_0_1;
		this.methodCounter = this.methodCounter.increment(base);
		this.complexityCounter = this.complexityCounter.increment(base);
	}

	@Override
	public boolean applyFragment(final SourceNodeImpl fragment) {
		final boolean applied = super.applyFragment(fragment);
		if (applied) {
			methodCounter = instructionCounter.getCoveredCount() == 0
					? CounterImpl.COUNTER_1_0
					: CounterImpl.COUNTER_0_1;
			complexityCounter = methodCounter;
		}
		return applied;
	}

	// === IMethodCoverage implementation ===

	public String getDesc() {
		return desc;
	}

	public String getSignature() {
		return signature;
	}

}
