/*******************************************************************************
 * Copyright (c) 2009, 2017 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.core.analysis;

import java.util.Collection;

import org.jacoco.core.internal.analysis.CounterImpl;

/**
 * Base implementation for coverage data nodes.
 */
public class CoverageNodeImpl implements ICoverageNode {

	private final ElementType elementType;

	private final String name;

	/** Counter for branches. */
	protected CounterImpl branchCounter;

	/** Counter for instructions. */
	protected CounterImpl instructionCounter;

	/** Counter for lines */
	protected CounterImpl lineCounter;

	/** Counter for complexity. */
	protected CounterImpl complexityCounter;

	/** Counter for methods. */
	protected CounterImpl methodCounter;

	/** Counter for classes. */
	protected CounterImpl classCounter;

	/** Treat all branches as being executed, even if they are not. */
	private boolean treatAsFullyCovered;

	/**
	 * Creates a new coverage data node.
	 * 
	 * @param elementType
	 *            type of the element represented by this instance
	 * @param name
	 *            name of this node
	 */
	public CoverageNodeImpl(final ElementType elementType, final String name) {
		this.elementType = elementType;
		this.name = name;
		this.branchCounter = CounterImpl.COUNTER_0_0;
		this.instructionCounter = CounterImpl.COUNTER_0_0;
		this.complexityCounter = CounterImpl.COUNTER_0_0;
		this.methodCounter = CounterImpl.COUNTER_0_0;
		this.classCounter = CounterImpl.COUNTER_0_0;
		this.lineCounter = CounterImpl.COUNTER_0_0;
	}

	/**
	 * Increments the counters by the values given by another element.
	 * 
	 * @param child
	 *            counters to add
	 */
	public void increment(final ICoverageNode child) {
		instructionCounter = instructionCounter
				.increment(child.getInstructionCounter());
		branchCounter = branchCounter.increment(child.getBranchCounter());
		lineCounter = lineCounter.increment(child.getLineCounter());
		complexityCounter = complexityCounter
				.increment(child.getComplexityCounter());
		methodCounter = methodCounter.increment(child.getMethodCounter());
		classCounter = classCounter.increment(child.getClassCounter());
	}

	/**
	 * Increments the counters by the values given by the collection of
	 * elements.
	 * 
	 * @param children
	 *            list of nodes, which counters will be added to this node
	 */
	public void increment(final Collection<? extends ICoverageNode> children) {
		for (final ICoverageNode child : children) {
			increment(child);
		}
	}

	// === ICoverageDataNode ===

	public ElementType getElementType() {
		return elementType;
	}

	public String getName() {
		return name;
	}

	/**
	 * Get the counter after treatments have been applied. Treatments include
	 * being fully countered.
	 * 
	 * @param counter
	 *            Counter to treat.
	 * @return New counter that has had the treaments applied to counter.
	 */
	private ICounter getTreatedCounter(final ICounter counter) {
		if (this.treatAsFullyCovered) {
			return counter.treatAsFullyCovered();
		} else {
			return counter;
		}
	}

	public ICounter getInstructionCounter() {
		return this.getTreatedCounter(instructionCounter);
	}

	public ICounter getBranchCounter() {
		return this.getTreatedCounter(this.branchCounter);
	}

	public ICounter getLineCounter() {
		return this.getTreatedCounter(lineCounter);
	}

	public ICounter getComplexityCounter() {
		return this.getTreatedCounter(complexityCounter);
	}

	public ICounter getMethodCounter() {
		return this.getTreatedCounter(methodCounter);
	}

	public ICounter getClassCounter() {
		return this.getTreatedCounter(classCounter);
	}

	public ICounter getCounter(final CounterEntity entity) {
		switch (entity) {
		case INSTRUCTION:
			return getInstructionCounter();
		case BRANCH:
			return getBranchCounter();
		case LINE:
			return getLineCounter();
		case COMPLEXITY:
			return getComplexityCounter();
		case METHOD:
			return getMethodCounter();
		case CLASS:
			return getClassCounter();
		}
		throw new AssertionError(entity);
	}

	public ICoverageNode getPlainCopy() {
		final CoverageNodeImpl copy = new CoverageNodeImpl(elementType, name);
		copy.instructionCounter = CounterImpl.getInstance(instructionCounter);
		copy.branchCounter = CounterImpl.getInstance(branchCounter);
		copy.lineCounter = CounterImpl.getInstance(lineCounter);
		copy.complexityCounter = CounterImpl.getInstance(complexityCounter);
		copy.methodCounter = CounterImpl.getInstance(methodCounter);
		copy.classCounter = CounterImpl.getInstance(classCounter);
		return copy;
	}

	public void setTreatAsFullyCovered(final boolean treatAsFullyCovered) {
		this.treatAsFullyCovered = treatAsFullyCovered;
	}

	public boolean isTreatedAsFullyCovered() {
		return this.treatAsFullyCovered;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append(name).append(" [").append(elementType);
		if (this.isTreatedAsFullyCovered()) {
			sb.append(" TREAT_AS_FULLY_COVERED");
		}
		sb.append("]");
		return sb.toString();
	}

}
