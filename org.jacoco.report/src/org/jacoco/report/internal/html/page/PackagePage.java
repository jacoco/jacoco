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

import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.analysis.IPackageCoverage;
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
public class PackagePage extends TablePage<IPackageCoverage> {

	private final PackageSourcePage packageSourcePage;
	private final boolean sourceCoverageExists;

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
	 */
	public PackagePage(final IPackageCoverage node, final ReportPage parent,
			final ISourceFileLocator locator, final ReportOutputFolder folder,
			final IHTMLReportContext context) {
		super(node, parent, folder, context);
		packageSourcePage = new PackageSourcePage(node, parent, locator, folder,
				context, this);
		sourceCoverageExists = !node.getSourceFiles().isEmpty();
	}

	@Override
	public void render() throws IOException {
		if (sourceCoverageExists) {
			packageSourcePage.render();
		}
		renderClasses();
		super.render();
	}

	private void renderClasses() throws IOException {
		for (final IClassCoverage c : getNode().getClasses()) {
			if (!c.containsCode()) {
				continue;
			}
			final ILinkable sourceFilePage = packageSourcePage
					.getSourceFilePage(c.getSourceFileName());
			final ClassPage page = new ClassPage(c, this, sourceFilePage,
					folder, context);
			page.render();
			addItem(page);
		}
	}

	@Override
	protected String getOnload() {
		return "initialSort(['breadcrumb', 'coveragetable'])";
	}

	@Override
	protected String getFileName() {
		return "index.html";
	}

	@Override
	public String getLinkLabel() {
		return context.getLanguageNames().getPackageName(getNode().getName());
	}

	@Override
	protected void infoLinks(final HTMLElement span) throws IOException {
		if (sourceCoverageExists) {
			final String link = packageSourcePage.getLink(folder);
			span.a(link, Styles.EL_SOURCE).text("Source Files");
		}
		super.infoLinks(span);
	}

}
