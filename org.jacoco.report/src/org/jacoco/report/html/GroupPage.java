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
import java.util.ArrayList;
import java.util.List;

import org.jacoco.core.analysis.ICoverageNode;
import org.jacoco.report.IReportVisitor;
import org.jacoco.report.ISourceFileLocator;
import org.jacoco.report.ReportOutputFolder;

/**
 * Page showing coverage information for a node that groups other nodes. The
 * page shows a table of linked nodes.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public class GroupPage extends ReportPage {

	private final List<ReportPage> children = new ArrayList<ReportPage>();

	/**
	 * Creates a new visitor in the given context.
	 * 
	 * @param node
	 * @param parent
	 * @param outputFolder
	 * @param context
	 */
	public GroupPage(final ICoverageNode node, final ReportPage parent,
			final ReportOutputFolder outputFolder,
			final IHTMLReportContext context) {
		super(node, parent, outputFolder, context);
	}

	public IReportVisitor visitChild(final ICoverageNode node) {
		ReportPage child;
		switch (node.getElementType()) {
		case PACKAGE:
			child = new PackagePage(node, this, outputFolder, context);
			break;
		default:
			child = new GroupPage(node, this, outputFolder, context);
			break;
		}
		children.add(child);
		return child;
	}

	@Override
	public void visitEnd(final ISourceFileLocator sourceFileLocator)
			throws IOException {
		super.visitEnd(sourceFileLocator);
		// free memory, otherwise we will keep the complete tree:
		children.clear();
	}

	@Override
	protected void content(final HTMLElement body,
			final ISourceFileLocator sourceFileLocator) throws IOException {
		context.getTable(getNode().getElementType()).render(body, children,
				getNode(), context.getResources(), outputFolder);
	}

	@Override
	protected String getFileName() {
		return "index.html";
	}

	@Override
	protected ReportOutputFolder getFolder(final ReportOutputFolder base) {
		return base.subFolder(getLabel());
	}

}
