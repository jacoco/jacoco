/*******************************************************************************
 * Copyright (c) 2009, 2016 Mountainminds GmbH & Co. KG and Contributors
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

import org.jacoco.core.analysis.ICounter;
import org.jacoco.core.analysis.IMethodCoverage;
import org.jacoco.core.data.ProbeMode;

/**
 * Implementation of {@link IMethodCoverage}.
 */
public class MethodCoverageImpl extends SourceNodeImpl implements
		IMethodCoverage {

	private final String desc;

	private final String signature;

	/**
	 * Creates a method coverage data object with the given parameters. The
	 * probe mode is set to 'exists'.
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
		this(name, desc, signature, ProbeMode.exists);
	}

	/**
	 * Creates a method coverage data object with the given parameters.
	 * 
	 * @param name
	 *            name of the method
	 * @param desc
	 *            method descriptor
	 * @param signature
	 *            generic signature or <code>null</code>
	 * @param probeMode
	 *            the mode of the probe used to generate this data
	 */
	public MethodCoverageImpl(final String name, final String desc,
			final String signature, final ProbeMode probeMode) {
		super(ElementType.METHOD, name);
		this.desc = desc;
		this.signature = signature;
		this.probeMode = probeMode;
	}

	@Override
	public void increment(final ICounter instructions, final ICounter branches,
			final int line) {
		super.increment(instructions, branches, line);
		// Additionally increment complexity counter:
		if (branches.getTotalCount() > 1) {
			final int c = Math.max(0, branches.getCoveredCount() - 1);
			final int m = Math.max(0, branches.getTotalCount() - c - 1);
			this.complexityCounter = this.complexityCounter.increment(m, c, 0);
		}
	}

	/**
	 * This method must be called exactly once after all instructions and
	 * branches have been incremented for this method coverage node.
	 * 
	 * @param methodExecutions
	 *            the number of times method entry was detected
	 */
	public void incrementMethodCounter(final int methodExecutions) {
		if (this.instructionCounter.getCoveredCount() == 0) {
			this.methodCounter = this.methodCounter
					.increment(CounterImpl.COUNTER_1_0);
			this.complexityCounter = this.complexityCounter
					.increment(CounterImpl.COUNTER_1_0);
		} else {
			this.methodCounter = this.methodCounter.increment(CounterImpl
					.getInstance(0, 1, methodExecutions));
			this.complexityCounter = this.complexityCounter
					.increment(CounterImpl.getInstance(0, 1, 0));
		}
	}

	// === IMethodCoverage implementation ===

	public String getDesc() {
		return desc;
	}

	public String getSignature() {
		return signature;
	}

}
