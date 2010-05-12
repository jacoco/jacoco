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
 * $Id: $
 *******************************************************************************/
package org.jacoco.report.html;

import java.io.IOException;

import org.jacoco.core.analysis.ICoverageNode;
import org.jacoco.report.IReportVisitor;
import org.jacoco.report.ISourceFileLocator;
import org.jacoco.report.ReportOutputFolder;
import org.jacoco.report.html.resources.Resources;

/**
 * Report page that represents a coverage node.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public abstract class NodePage extends ReportPage implements IReportVisitor,
		ICoverageTableItem {

	private ICoverageNode node;

	/**
	 * Creates a new node page.
	 * 
	 * @param node
	 *            corresponding node
	 * @param parent
	 *            optional hierarchical parent
	 * @param folder
	 *            base folder to create this report in
	 * @param context
	 *            settings context
	 */
	protected NodePage(final ICoverageNode node, final ReportPage parent,
			final ReportOutputFolder folder, final IHTMLReportContext context) {
		super(parent, folder, context);
		this.node = node;
	}

	@Override
	protected String getElementStyle() {
		return Resources.getElementStyle(node.getElementType());
	}

	public void visitEnd(final ISourceFileLocator sourceFileLocator)
			throws IOException {
		renderDocument();
		this.node = node.getPlainCopy();
	}

	// === ICoverageTableItem ===

	@Override
	public String getLabel() {
		return node.getName();
	}

	public ICoverageNode getNode() {
		return node;
	}

}
