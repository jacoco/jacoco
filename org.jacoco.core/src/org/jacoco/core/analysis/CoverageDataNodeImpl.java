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

import java.util.ArrayList;
import java.util.Collection;

/**
 * Base implementation for coverage data nodes.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public class CoverageDataNodeImpl implements ICoverageDataNode {

	private final ElementType elementType;

	private final Collection<ICoverageDataNode> children;

	/** Counter for blocks. */
	protected CounterImpl blockCounter;

	/** Counter for instructions. */
	protected CounterImpl instructionCounter;

	/** Counter for lines, if this element does not have lines. */
	protected CounterImpl lineCounter;

	/** Line information if this element has lines. */
	protected final LinesImpl lines;

	/** Counter for methods. */
	protected CounterImpl methodCounter;

	/** Counter for classes. */
	protected CounterImpl classCounter;

	/**
	 * Creates a new coverage data instance of the given element type.
	 * 
	 * @param elementType
	 *            type of the element represented by this instance
	 * @param hasLines
	 *            <code>true</code> id this element has source lines
	 */
	public CoverageDataNodeImpl(final ElementType elementType,
			final boolean hasLines) {
		this.elementType = elementType;
		children = new ArrayList<ICoverageDataNode>();
		blockCounter = CounterImpl.COUNTER_0_0;
		instructionCounter = CounterImpl.COUNTER_0_0;
		if (hasLines) {
			lineCounter = null;
			lines = new LinesImpl();
		} else {
			lineCounter = CounterImpl.COUNTER_0_0;
			lines = null;
		}
		methodCounter = CounterImpl.COUNTER_0_0;
		classCounter = CounterImpl.COUNTER_0_0;

	}

	/**
	 * Adds the given coverage data instance as a child element. All counters
	 * are incremented by the values of the given child.
	 * 
	 * @param child
	 *            child element to add
	 */
	public void add(final ICoverageDataNode child) {
		children.add(child);
		blockCounter = blockCounter.increment(child.getBlockCounter());
		instructionCounter = instructionCounter.increment(child
				.getInstructionCounter());
		if (lineCounter != null) {
			lineCounter = lineCounter.increment(child.getLineCounter());
		}
		if (lines != null) {
			lines.increment(child.getLines());
		}
		methodCounter = methodCounter.increment(child.getMethodCounter());
		classCounter = classCounter.increment(child.getClassCounter());
	}

	/**
	 * Adds the given collection of coverage data instances as child elements.
	 * All counters are incremented by the values of the given children.
	 * 
	 * @param children
	 *            child elements to add
	 */
	public void addAll(final Collection<ICoverageDataNode> children) {
		for (final ICoverageDataNode child : children) {
			add(child);
		}
	}

	// === ICoverageDataNode ===

	public ElementType getElementType() {
		return elementType;
	}

	public Collection<ICoverageDataNode> getChilden() {
		return children;
	}

	public ICounter getBlockCounter() {
		return blockCounter;
	}

	public ICounter getInstructionCounter() {
		return instructionCounter;
	}

	public ICounter getLineCounter() {
		return lines == null ? lineCounter : lines;
	}

	public ILines getLines() {
		return lines;
	}

	public ICounter getMethodCounter() {
		return methodCounter;
	}

	public ICounter getClassCounter() {
		return classCounter;
	}

}
