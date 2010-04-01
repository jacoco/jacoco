/*******************************************************************************
 * Copyright (c) 2009, 2010 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Brock Janiczak - initial API and implementation
 *    Marc R. Hoffmann - generalized structure 
 *    
 * $Id: $
 *******************************************************************************/
package org.jacoco.report.xml;

import java.io.IOException;

import org.jacoco.core.analysis.ClassCoverage;
import org.jacoco.core.analysis.ICounter;
import org.jacoco.core.analysis.ICoverageNode;
import org.jacoco.core.analysis.MethodCoverage;
import org.jacoco.core.analysis.ICoverageNode.CounterEntity;
import org.jacoco.core.analysis.ICoverageNode.ElementType;
import org.jacoco.report.IReportVisitor;
import org.jacoco.report.ISourceFileLocator;

/**
 * Report visitor that transforms the report structure into XML elements.
 * 
 * @author Brock Janiczak
 * @version $Revision: $
 */
class XMLReportNodeHandler implements IReportVisitor {

	protected final XMLElement element;

	protected final ICoverageNode node;

	/**
	 * New handler for the given coverage node.
	 * 
	 * @param element
	 *            XML-Element representing this coverage node. The start tag
	 *            must not be closed yet to allow adding additional attributes.
	 * @param node
	 *            corresponding coverage node
	 * @throws IOException
	 *             in case of problems with the underlying writer
	 */
	public XMLReportNodeHandler(final XMLElement element,
			final ICoverageNode node) throws IOException {
		this.element = element;
		this.node = node;
		element.attr("name", node.getName());
	}

	public IReportVisitor visitChild(final ICoverageNode node)
			throws IOException {
		final ElementType type = node.getElementType();
		switch (type) {
		case GROUP:
		case BUNDLE:
			return new XMLReportNodeHandler(element.element("group"), node);
		case PACKAGE:
			return new XMLReportNodeHandler(element.element("package"), node);
		case CLASS:
			final XMLElement classChild = element.element("class");
			addClassAttributes(classChild, (ClassCoverage) node);
			return new XMLReportNodeHandler(classChild, node);
		case METHOD:
			final XMLElement methodChild = element.element("method");
			addMethodAttributes(methodChild, (MethodCoverage) node);
			return new XMLReportNodeHandler(methodChild, node);
		}
		return IReportVisitor.NOP;
	}

	public void visitEnd(final ISourceFileLocator sourceFileLocator)
			throws IOException {
		for (final CounterEntity counterEntity : CounterEntity.values()) {
			createCounterElement(counterEntity);
		}
		this.element.close();
	}

	private void createCounterElement(final CounterEntity counterEntity)
			throws IOException {
		final ICounter counter = node.getCounter(counterEntity);
		if (counter.getTotalCount() > 0) {
			final XMLElement counterNode = this.element.element("counter");
			counterNode.attr("type", counterEntity.name());
			counterNode.attr("covered", counter.getCoveredCount());
			counterNode.attr("missed", counter.getMissedCount());
			counterNode.close();
		}
	}

	private static void addClassAttributes(final XMLElement element,
			final ClassCoverage node) throws IOException {
		if (node.getSignature() != null) {
			element.attr("signature", node.getSignature());
		}
		if (node.getSuperName() != null) {
			element.attr("superclass", node.getSuperName());
		}
		if (node.getInterfaceNames() != null) {
			boolean first = true;
			final StringBuilder builder = new StringBuilder();
			for (final String iface : node.getInterfaceNames()) {
				if (first) {
					first = false;
				} else {
					builder.append(' ');
				}
				builder.append(iface);
			}
			element.attr("interfaces", builder.toString());
		}
	}

	private static void addMethodAttributes(final XMLElement element,
			final MethodCoverage node) throws IOException {
		element.attr("desc", node.getDesc());
		final String signature = node.getSignature();
		if (signature != null) {
			element.attr("signature", signature);
		}
	}

}
