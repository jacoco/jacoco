/*******************************************************************************
 * Copyright (c) 2009, 2010 Mountainminds GmbH & Co. KG and Contributors
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

/**
 * Base implementation for coverage data nodes.
 * 
 * @author Marc R. Hoffmann
 * @version $qualified.bundle.version$
 */
public class CoverageNodeImpl implements ICoverageNode {

	private final ElementType elementType;

	private final String name;

	/** Counter for branches. */
	protected CounterImpl branchCounter;

	/** Counter for instructions. */
	protected CounterImpl instructionCounter;

	/** Counter for lines, if this element does not have lines. */
	protected CounterImpl lineCounter;

	/** Counter for methods. */
	protected CounterImpl methodCounter;

	/** Counter for classes. */
	protected CounterImpl classCounter;

	/** Line information if this element has lines. */
	protected final LinesImpl lines;

	/**
	 * Creates a new coverage data node.
	 * 
	 * @param elementType
	 *            type of the element represented by this instance
	 * @param name
	 *            name of this node
	 * @param hasLines
	 *            <code>true</code> id this element has source lines
	 */
	public CoverageNodeImpl(final ElementType elementType, final String name,
			final boolean hasLines) {
		this.elementType = elementType;
		this.name = name;
		this.branchCounter = CounterImpl.COUNTER_0_0;
		this.instructionCounter = CounterImpl.COUNTER_0_0;
		this.methodCounter = CounterImpl.COUNTER_0_0;
		this.classCounter = CounterImpl.COUNTER_0_0;
		this.lineCounter = hasLines ? null : CounterImpl.COUNTER_0_0;
		this.lines = hasLines ? new LinesImpl() : null;
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
		methodCounter = methodCounter.increment(child.getMethodCounter());
		classCounter = classCounter.increment(child.getClassCounter());
		if (lines == null) {
			lineCounter = lineCounter.increment(child.getLineCounter());
		} else {
			lines.increment(child.getLines());
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
		return lines != null ? lines : lineCounter;
	}

	public ICounter getMethodCounter() {
		return methodCounter;
	}

	public ICounter getClassCounter() {
		return classCounter;
	}

	public ICounter getCounter(final CounterEntity entity) {
		switch (entity) {
		case INSTRUCTION:
			return getInstructionCounter();
		case BRANCH:
			return getBranchCounter();
		case LINE:
			return getLineCounter();
		case METHOD:
			return getMethodCounter();
		case CLASS:
			return getClassCounter();
		}
		throw new AssertionError(entity);
	}

	public ILines getLines() {
		return lines;
	}

	public ICoverageNode getPlainCopy() {
		final boolean hasLines = lines != null;
		final CoverageNodeImpl copy = new CoverageNodeImpl(elementType, name,
				hasLines);
		copy.instructionCounter = CounterImpl.getInstance(instructionCounter);
		copy.branchCounter = CounterImpl.getInstance(branchCounter);
		copy.methodCounter = CounterImpl.getInstance(methodCounter);
		copy.classCounter = CounterImpl.getInstance(classCounter);
		if (hasLines) {
			copy.lines.increment(lines);
		} else {
			copy.lineCounter = CounterImpl.getInstance(lineCounter);
		}
		return copy;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append(name).append(" [").append(elementType).append("]");
		return sb.toString();
	}

}
