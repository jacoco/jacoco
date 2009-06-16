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
public class CoverageDataNodeImpl extends CoverageDataSummaryImpl implements
		ICoverageDataNode {

	private final ElementType elementType;

	private final String name;

	private final Collection<ICoverageDataNode> children;

	/** Line information if this element has lines. */
	protected final LinesImpl lines;

	/**
	 * Creates a new coverage data instance of the given element type.
	 * 
	 * @param elementType
	 *            type of the element represented by this instance
	 * @param name
	 *            name of this node
	 * @param hasLines
	 *            <code>true</code> id this element has source lines
	 */
	public CoverageDataNodeImpl(final ElementType elementType,
			final String name, final boolean hasLines) {
		super();
		this.elementType = elementType;
		this.name = name;
		children = new ArrayList<ICoverageDataNode>();
		lines = hasLines ? new LinesImpl() : null;
	}

	/**
	 * Adds the given coverage data instance as a child element. All counters
	 * are incremented by the values of the given child.
	 * 
	 * @param child
	 *            child element to add
	 */
	public void add(final ICoverageDataNode child) {
		super.add(child);
		children.add(child);
		if (lines != null) {
			lines.increment(child.getLines());
		}
	}

	/**
	 * Adds the given collection of coverage data summaries as child elements.
	 * All counters are incremented by the values of the given children.
	 * 
	 * @param children
	 *            child elements to add
	 */
	public void addNodes(final Collection<? extends ICoverageDataNode> children) {
		for (final ICoverageDataNode child : children) {
			add(child);
		}
	}

	// === ICoverageDataNode ===

	public ElementType getElementType() {
		return elementType;
	}

	public String getName() {
		return name;
	}

	public Collection<ICoverageDataNode> getChilden() {
		return children;
	}

	@Override
	public ICounter getLineCounter() {
		return lines == null ? super.getLineCounter() : lines;
	}

	public ILines getLines() {
		return lines;
	}

}
