/*******************************************************************************
 * Copyright (c) 2009, 2013 Mountainminds GmbH & Co. KG and Contributors
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

import org.jacoco.core.analysis.IBundleCoverage;
import org.jacoco.core.analysis.ICoverageNode;
import org.jacoco.report.ISourceFileLocator;
import org.jacoco.report.internal.AbstractGroupVisitor;
import org.jacoco.report.internal.ReportOutputFolder;
import org.jacoco.report.internal.html.page.BundlePage;
import org.jacoco.report.internal.html.page.GroupPage;
import org.jacoco.report.internal.html.page.NodePage;
import org.jacoco.report.internal.html.page.ReportPage;

/**
 * Group visitor for HTML reports.
 */
public class HTMLGroupVisitor extends AbstractGroupVisitor {

	private final ReportOutputFolder folder;

	private final IHTMLReportContext context;

	private final GroupPage page;

	private final boolean linkToSource;

	/**
	 * Create a new group handler.
	 * 
	 * @param parent
	 *            optional hierarchical parent
	 * @param folder
	 *            base folder for this group
	 * @param linkToSource
	 *            link to source file instead of table of methods
	 * @param context
	 *            settings context
	 * @param name
	 *            group name
	 */
	public HTMLGroupVisitor(final ReportPage parent,
			final ReportOutputFolder folder, final boolean linkToSource,
			final IHTMLReportContext context, final String name) {
		super(name);
		this.folder = folder;
		this.context = context;
		this.linkToSource = linkToSource;
		page = new GroupPage(total, parent, folder, context);
	}

	/**
	 * Returns the page rendered for this group.
	 * 
	 * @return page for this group
	 */
	public NodePage<ICoverageNode> getPage() {
		return page;
	}

	@Override
	protected void handleBundle(final IBundleCoverage bundle,
			final ISourceFileLocator locator) throws IOException {
		final BundlePage bundlepage = new BundlePage(bundle, page, locator,
				folder.subFolder(bundle.getName()), linkToSource, context);
		bundlepage.render();
		page.addItem(bundlepage);
	}

	@Override
	protected AbstractGroupVisitor handleGroup(final String name)
			throws IOException {
		final HTMLGroupVisitor handler = new HTMLGroupVisitor(page,
				folder.subFolder(name), linkToSource, context, name);
		page.addItem(handler.getPage());
		return handler;
	}

	@Override
	protected void handleEnd() throws IOException {
		page.render();
	}

}
