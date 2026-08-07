/*******************************************************************************
 * Copyright (c) 2009, 2026 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
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

	/** Counter for boundary values of ordered numeric comparisons. */
	protected CounterImpl boundaryCounter;

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
		this.boundaryCounter = CounterImpl.COUNTER_0_0;
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
		boundaryCounter = boundaryCounter
				.increment(child.getCounter(CounterEntity.BOUNDARY));
	}

	/**
	 * Increments the boundary counter. Unlike the other counters this one is
	 * not part of {@link ICoverageNode}, so that adding it does not break
	 * implementations of that interface outside of JaCoCo.
	 *
	 * @param counter
	 *            counter to add
	 */
	public void incrementBoundaryCounter(final ICounter counter) {
		boundaryCounter = boundaryCounter.increment(counter);
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

	public ICounter getInstructionCounter() {
		return instructionCounter;
	}

	public ICounter getBranchCounter() {
		return branchCounter;
	}

	public ICounter getLineCounter() {
		return lineCounter;
	}

	public ICounter getComplexityCounter() {
		return complexityCounter;
	}

	public ICounter getMethodCounter() {
		return methodCounter;
	}

	public ICounter getClassCounter() {
		return classCounter;
	}

	/**
	 * Returns the boundary coverage counter.
	 *
	 * @return boundary coverage counter
	 */
	public ICounter getBoundaryCounter() {
		return boundaryCounter;
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
		case BOUNDARY:
			return getBoundaryCounter();
		}
		throw new AssertionError(entity);
	}

	public boolean containsCode() {
		return getInstructionCounter().getTotalCount() != 0;
	}

	public ICoverageNode getPlainCopy() {
		final CoverageNodeImpl copy = new CoverageNodeImpl(elementType, name);
		copy.instructionCounter = CounterImpl.getInstance(instructionCounter);
		copy.branchCounter = CounterImpl.getInstance(branchCounter);
		copy.lineCounter = CounterImpl.getInstance(lineCounter);
		copy.complexityCounter = CounterImpl.getInstance(complexityCounter);
		copy.methodCounter = CounterImpl.getInstance(methodCounter);
		copy.classCounter = CounterImpl.getInstance(classCounter);
		copy.boundaryCounter = CounterImpl.getInstance(boundaryCounter);
		return copy;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append(name).append(" [").append(elementType).append("]");
		return sb.toString();
	}

}
