/*******************************************************************************
 * Copyright (c) 2009, 2011 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.report;

import java.io.IOException;

import org.jacoco.core.analysis.ICoverageNode;

/**
 * Output-Interface for hierarchical coverage data information. To allow data
 * streaming and to save memory {@link ICoverageNode}s are traversed in a
 * deep-first fashion. The interface is implemented by the different report
 * writers.
 */
public interface IReportVisitor {

	/**
	 * Visitor without any operation.
	 */
	public static final IReportVisitor NOP = new IReportVisitor() {

		public IReportVisitor visitChild(final ICoverageNode node) {
			return NOP;
		}

		public void visitEnd(final ISourceFileLocator sourceFileLocator) {
		}

	};

	/**
	 * Called for every direct child.
	 * 
	 * @param node
	 *            Node for the child in the implementation class specific to
	 *            this type. The counters are may yet be populated.
	 * 
	 * @return visitor instance for processing the child node
	 * 
	 * @throws IOException
	 *             in case of IO problems with the report writer
	 */
	IReportVisitor visitChild(ICoverageNode node) throws IOException;

	/**
	 * Called at the very end, when all child node have been processed and the
	 * counters for this node are properly populated.
	 * 
	 * @param sourceFileLocator
	 *            source file locator valid for this node
	 * @throws IOException
	 *             in case of IO problems with the report writer
	 */
	void visitEnd(ISourceFileLocator sourceFileLocator) throws IOException;

}
