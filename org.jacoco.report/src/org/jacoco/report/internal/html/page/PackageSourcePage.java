/*******************************************************************************
 * Copyright (c) 2009, 2023 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.report.internal.html.page;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import org.jacoco.core.analysis.IPackageCoverage;
import org.jacoco.core.analysis.ISourceFileCoverage;
import org.jacoco.report.ISourceFileLocator;
import org.jacoco.report.internal.ReportOutputFolder;
import org.jacoco.report.internal.html.HTMLElement;
import org.jacoco.report.internal.html.IHTMLReportContext;
import org.jacoco.report.internal.html.ILinkable;
import org.jacoco.report.internal.html.resources.Styles;

/**
 * Page showing coverage information for a Java package. The page contains a
 * table with all classes of the package.
 */
public class PackageSourcePage extends TablePage<IPackageCoverage> {

	private final ISourceFileLocator locator;
	private final Map<String, ILinkable> sourceFilePages;
	private final ILinkable packagePage;

	/**
	 * Creates a new visitor in the given context.
	 *
	 * @param node
	 *            coverage data for this package
	 * @param parent
	 *            optional hierarchical parent
	 * @param locator
	 *            source locator
	 * @param folder
	 *            base folder to create this page in
	 * @param context
	 *            settings context
	 * @param packagePage
	 *            page listing the classes of this package
	 */
	public PackageSourcePage(final IPackageCoverage node,
			final ReportPage parent, final ISourceFileLocator locator,
			final ReportOutputFolder folder, final IHTMLReportContext context,
			final ILinkable packagePage) {
		super(node, parent, folder, context);
		this.locator = locator;
		this.packagePage = packagePage;
		this.sourceFilePages = new HashMap<String, ILinkable>();
	}

	@Override
	public void render() throws IOException {
		renderSourceFilePages();
		super.render();
	}

	/**
	 * Returns the link to the source file page of the source file with the
	 * given name. If no source file was located, <code>null</code> is
	 * returned..
	 */
	ILinkable getSourceFilePage(final String name) {
		return sourceFilePages.get(name);
	}

	private final void renderSourceFilePages() throws IOException {
		final String packagename = getNode().getName();
		for (final ISourceFileCoverage s : getNode().getSourceFiles()) {
			if (!s.containsCode()) {
				continue;
			}
			final String sourcename = s.getName();
			final Reader reader = locator.getSourceFile(packagename,
					sourcename);
			if (reader == null) {
				addItem(new SourceFileItem(s));
			} else {
				final SourceFilePage sourcePage = new SourceFilePage(s, reader,
						locator.getTabWidth(), this, folder, context);
				sourcePage.render();
				sourceFilePages.put(sourcename, sourcePage);
				addItem(sourcePage);
			}

		}
	}

	@Override
	protected String getOnload() {
		return "initialSort(['breadcrumb', 'coveragetable'])";
	}

	@Override
	protected String getFileName() {
		return "index.source.html";
	}

	@Override
	public String getLinkLabel() {
		return context.getLanguageNames().getPackageName(getNode().getName());
	}

	@Override
	protected void infoLinks(final HTMLElement span) throws IOException {
		final String link = packagePage.getLink(folder);
		span.a(link, Styles.EL_CLASS).text("Classes");
		super.infoLinks(span);
	}

}
