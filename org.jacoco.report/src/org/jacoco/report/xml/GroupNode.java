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

import org.jacoco.core.analysis.ICoverageNode;
import org.jacoco.core.analysis.ICoverageNode.ElementType;
import org.jacoco.report.IReportVisitor;

/**
 * Wrapper for an {@link XMLElement} that contains 'groups' of coverage data.
 * Group Nodes can represent either be Bundle or Group coverage data
 * 
 * @author Brock Janiczak
 * @version $Revision: $
 */
public class GroupNode extends NodeWithCoverage {

	/**
	 * Creates a new top level Group coverage element for the supplied session
	 * coverage node
	 * 
	 * @param file
	 *            Root element to attach to
	 * @param coverageNode
	 *            Coverage node
	 * @throws IOException
	 *             IO Error creating the element
	 */
	public GroupNode(final XMLReportFile file, final ICoverageNode coverageNode)
			throws IOException {
		this((XMLElement) file, coverageNode);
	}

	/**
	 * Creates a new Group coverage element under an existing group element for
	 * the supplied coverage node
	 * 
	 * @param parent
	 *            Element to attach to
	 * @param node
	 *            Coverage node
	 * @throws IOException
	 *             IO Error creating the element
	 */
	public GroupNode(final GroupNode parent, final ICoverageNode node)
			throws IOException {
		this((XMLElement) parent, node);
	}

	private GroupNode(final XMLElement parent, final ICoverageNode node)
			throws IOException {
		super(parent, "group", node);
	}

	public IReportVisitor visitChild(final ICoverageNode node)
			throws IOException {

		if (node.getElementType() == ElementType.GROUP
				|| node.getElementType() == ElementType.BUNDLE) {
			return new GroupNode(this, node);
		}

		return new PackageNode(this, node);
	}

}
