/*******************************************************************************
 * Copyright (c) 2009 Mountainminds GmbH & Co. KG and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 * $Id: $
 *******************************************************************************/
package org.jacoco.core.analysis;

import java.util.Collection;

/**
 * Base implementation for coverage data summary.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public class CoverageDataSummaryImpl implements ICoverageDataSummary {

	/** Counter for blocks. */
	protected CounterImpl blockCounter;

	/** Counter for instructions. */
	protected CounterImpl instructionCounter;

	/** Counter for lines, if this element does not have lines. */
	protected CounterImpl lineCounter;

	/** Counter for methods. */
	protected CounterImpl methodCounter;

	/** Counter for classes. */
	protected CounterImpl classCounter;

	/**
	 * Creates a new coverage data instance of the given element type.
	 * 
	 */
	public CoverageDataSummaryImpl() {
		blockCounter = CounterImpl.COUNTER_0_0;
		instructionCounter = CounterImpl.COUNTER_0_0;
		lineCounter = CounterImpl.COUNTER_0_0;
		methodCounter = CounterImpl.COUNTER_0_0;
		classCounter = CounterImpl.COUNTER_0_0;
	}

	/**
	 * Adds the given coverage data summary as a child element. All counters are
	 * incremented by the values of the given summary.
	 * 
	 * @param child
	 *            child element to add
	 */
	public void add(final ICoverageDataSummary child) {
		blockCounter = blockCounter.increment(child.getBlockCounter());
		instructionCounter = instructionCounter.increment(child
				.getInstructionCounter());
		lineCounter = lineCounter.increment(child.getLineCounter());
		methodCounter = methodCounter.increment(child.getMethodCounter());
		classCounter = classCounter.increment(child.getClassCounter());
	}

	/**
	 * Adds the given collection of coverage data summaries as child elements.
	 * All counters are incremented by the values of the given children.
	 * 
	 * @param children
	 *            child elements to add
	 */
	public void addSummaries(
			final Collection<? extends ICoverageDataSummary> children) {
		for (final ICoverageDataSummary child : children) {
			add(child);
		}
	}

	// === ICoverageDataSummary ===

	public ICounter getBlockCounter() {
		return blockCounter;
	}

	public ICounter getInstructionCounter() {
		return instructionCounter;
	}

	public ICounter getLineCounter() {
		return lineCounter;
	}

	public ICounter getMethodCounter() {
		return methodCounter;
	}

	public ICounter getClassCounter() {
		return classCounter;
	}

}
