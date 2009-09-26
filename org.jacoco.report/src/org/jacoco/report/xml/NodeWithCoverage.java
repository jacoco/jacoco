/*******************************************************************************
 * Copyright (c) 2009 Mountainminds GmbH & Co. KG and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Brock Janiczak -initial API and implementation
 *    
 * $Id: $
 *******************************************************************************/
package org.jacoco.report.xml;

import java.io.IOException;

import org.jacoco.core.analysis.ICounter;
import org.jacoco.core.analysis.ICoverageNode;
import org.jacoco.core.analysis.ICoverageNode.CounterEntity;
import org.jacoco.report.IReportVisitor;
import org.jacoco.report.ISourceFileLocator;

/**
 * Base class for implementing XML Elements that contain coverage elements
 * 
 * @author Brock Janiczak
 * @version $Revision: $
 */
public abstract class NodeWithCoverage extends XMLElement implements
		IReportVisitor {
	private static final CounterEntity[] DEFAULT_COUNTERS = {
			CounterEntity.CLASS, CounterEntity.METHOD, CounterEntity.BLOCK,
			CounterEntity.LINE, CounterEntity.INSTRUCTION, };

	private final ICoverageNode node;

	/**
	 * Creates a new Coverage node under the supplied parent
	 * 
	 * @param parent
	 *            Parent element
	 * @param elementName
	 *            Name of this element
	 * @param node
	 *            Coverage node
	 * @throws IOException
	 *             IO Error creating this element
	 */
	public NodeWithCoverage(final XMLElement parent, final String elementName,
			final ICoverageNode node) throws IOException {
		super(parent.writer, elementName);
		parent.addChildElement(this);
		this.node = node;
		this.attr("name", node.getName());
	}

	public final void visitEnd(final ISourceFileLocator sourceFileLocator)
			throws IOException {

		for (final CounterEntity counterEntity : getCounterEntities()) {
			createCounterElement(counterEntity);
		}

		this.close();
	}

	/**
	 * Retrieves the list of counters supported by this element
	 * 
	 * @return Counters supported by this element
	 */
	protected CounterEntity[] getCounterEntities() {
		return DEFAULT_COUNTERS;
	}

	private void createCounterElement(final CounterEntity counterEntity)
			throws IOException {
		final ICounter counter = node.getCounter(counterEntity);

		final XMLElement counterNode = this.element("counter");
		counterNode.attr("type", counterEntity.name());
		counterNode
				.attr("covered", Integer.toString(counter.getCoveredCount()));
		counterNode.attr("notcovered", Integer.toString(counter
				.getNotCoveredCount()));

		counterNode.close();
	}

}
