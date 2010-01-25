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

import org.jacoco.core.analysis.ClassCoverage;
import org.jacoco.core.analysis.ICoverageNode;
import org.jacoco.core.analysis.ICoverageNode.ElementType;
import org.jacoco.report.IReportVisitor;
import org.jacoco.report.ISourceFileLocator;

/**
 * Wrapper for an {@link XMLElement} that contains package coverage data
 * 
 * @author Brock Janiczak
 * @version $Revision: $
 */
public class PackageNode extends NodeWithCoverage {

	/**
	 * Creates a new Package coverage element under the supplied group element
	 * 
	 * @param parent
	 *            Parent element that will own this class element
	 * @param packageNode
	 *            Package coverage node
	 * @throws IOException
	 *             IO Error creating the element
	 */
	public PackageNode(final GroupNode parent, final ICoverageNode packageNode)
			throws IOException {
		super(parent, "package", packageNode);
	}

	public IReportVisitor visitChild(final ICoverageNode node)
			throws IOException {
		if (node.getElementType() == ElementType.CLASS) {
			return new ClassNode(this, (ClassCoverage) node);
		} else if (node.getElementType() == ElementType.SOURCEFILE) {
			return new IReportVisitor() {

				public void visitEnd(final ISourceFileLocator sourceFileLocator)
						throws IOException {
				}

				public IReportVisitor visitChild(final ICoverageNode node)
						throws IOException {
					return this;
				}
			};
		}
		return null;
	}

}
