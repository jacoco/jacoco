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
package org.jacoco.core.analysis;

import java.util.Collection;

import org.jacoco.core.data.ProbeMode;
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

	/** The mode of the probe used to create this node */
	protected ProbeMode probeMode;

	/** Here or any child has EBigO data */
	protected boolean containsEBigO;

	/** The function of the worst case of any child */
	protected EBigOFunction eBigOFunction;

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
		probeMode = null;
		containsEBigO = false;
		eBigOFunction = EBigOFunction.UNDEFINED;
	}

	/**
	 * Increments the counters by the values given by another element.
	 * 
	 * @param child
	 *            counters to add
	 */
	public void increment(final ICoverageNode child) {
		instructionCounter = instructionCounter.increment(child
				.getInstructionCounter());
		branchCounter = branchCounter.increment(child.getBranchCounter());
		lineCounter = lineCounter.increment(child.getLineCounter());
		complexityCounter = complexityCounter.increment(child
				.getComplexityCounter());
		methodCounter = methodCounter.increment(child.getMethodCounter());
		classCounter = classCounter.increment(child.getClassCounter());
		mergeProbeMode(child);

		containsEBigO |= child.containsEBigO();
		if (eBigOFunction.compareTo(child.getEBigOFunction()) < 0) {
			setEBigOFunction(child.getEBigOFunction());
		}
	}

	/**
	 * Set the results of an E-Big-O analysis on this node
	 * 
	 * @param eBigOFunction
	 *            the results of an E-Big-O analysis on this noe
	 */
	public void setEBigOFunction(final EBigOFunction eBigOFunction) {
		this.eBigOFunction = eBigOFunction;
		this.containsEBigO = true;
	}

	/**
	 * Merge the probe mode of the child into this node's probe mode.
	 * 
	 * @param child
	 *            probe mode to consider
	 */
	protected void mergeProbeMode(final ICoverageNode child) {
		if (probeMode == null) {
			probeMode = child.getProbeMode();
		} else {
			switch (probeMode) {
			case exists:
				break;
			case count:
				if (child.getProbeMode() == ProbeMode.exists) {
					probeMode = ProbeMode.exists;
				}
				break;
			case parallelcount:
				probeMode = child.getProbeMode();
				break;
			}
		}
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

	// === ICoverageNode ===

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

	public ProbeMode getProbeMode() {
		return probeMode;
	}

	public double getParallelPercent() {
		final int instructionExecutionCount = instructionCounter
				.getExecutionCount();
		if (instructionExecutionCount == 0) {
			return 0D;
		}
		final int parallelExecutionCount = branchCounter.getExecutionCount();
		return 100.0D * parallelExecutionCount / instructionExecutionCount;
	}

	public boolean containsEBigO() {
		return containsEBigO;
	}

	public EBigOFunction getEBigOFunction() {
		return eBigOFunction;
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
		copy.probeMode = probeMode;
		copy.containsEBigO = containsEBigO;
		return copy;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append(name).append(" [").append(elementType).append("]");
		return sb.toString();
	}

}
