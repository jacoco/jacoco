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
package org.jacoco.report.internal.html;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jacoco.core.analysis.ICoverageNode;
import org.jacoco.report.IReportVisitor;
import org.jacoco.report.ISourceFileLocator;
import org.jacoco.report.ReportOutputFolder;
import org.jacoco.report.internal.html.resources.Resources;

/**
 * Page showing coverage information for a node that groups other nodes. The
 * page shows a table of linked nodes.
 * 
 * @author Marc R. Hoffmann
 * @version $qualified.bundle.version$
 */
public class GroupPage extends NodePage {

	private final List<NodePage> children = new ArrayList<NodePage>();

	/**
	 * Creates a new visitor in the given context.
	 * 
	 * @param node
	 * @param parent
	 * @param folder
	 * @param context
	 */
	public GroupPage(final ICoverageNode node, final ReportPage parent,
			final ReportOutputFolder folder, final IHTMLReportContext context) {
		super(node, parent, folder, context);
	}

	public IReportVisitor visitChild(final ICoverageNode node) {
		final NodePage child;
		switch (node.getElementType()) {
		case PACKAGE:
			child = new PackagePage(node, this, folder.subFolder(node.getName()
					.replace('/', '.')), context);
			break;
		default:
			child = new GroupPage(node, this, folder.subFolder(node.getName()),
					context);
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
	protected void headExtra(final HTMLElement head) throws IOException {
		super.headExtra(head);
		head.script("text/javascript",
				context.getResources().getLink(folder, Resources.SORT_SCRIPT));
	}

	@Override
	protected String getOnload() {
		return "initialSort(['breadcrumb', 'coveragetable'])";
	}

	@Override
	protected void content(final HTMLElement body) throws IOException {
		context.getTable().render(body, children, getNode(),
				context.getResources(), folder);
	}

	@Override
	protected String getFileName() {
		return "index.html";
	}

}
