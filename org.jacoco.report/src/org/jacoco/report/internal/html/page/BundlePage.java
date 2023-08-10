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

import org.jacoco.core.analysis.IBundleCoverage;
import org.jacoco.core.analysis.ICoverageNode;
import org.jacoco.core.analysis.IPackageCoverage;
import org.jacoco.report.ISourceFileLocator;
import org.jacoco.report.internal.ReportOutputFolder;
import org.jacoco.report.internal.html.HTMLElement;
import org.jacoco.report.internal.html.IHTMLReportContext;

/**
 * Page showing coverage information for a bundle. The page contains a table
 * with all packages of the bundle.
 */
public class BundlePage extends TablePage<ICoverageNode> {

	private final ISourceFileLocator locator;

	private IBundleCoverage bundle;

	/**
	 * Creates a new visitor in the given context.
	 *
	 * @param bundle
	 *            coverage date for the bundle
	 * @param parent
	 *            optional hierarchical parent
	 * @param locator
	 *            source locator
	 * @param folder
	 *            base folder for this bundle
	 * @param context
	 *            settings context
	 */
	public BundlePage(final IBundleCoverage bundle, final ReportPage parent,
			final ISourceFileLocator locator, final ReportOutputFolder folder,
			final IHTMLReportContext context) {
		super(bundle.getPlainCopy(), parent, folder, context);
		this.bundle = bundle;
		this.locator = locator;
	}

	@Override
	public void render() throws IOException {
		renderPackages();
		super.render();
		// Don't keep the bundle structure in memory
		bundle = null;
	}

	private void renderPackages() throws IOException {
		for (final IPackageCoverage p : bundle.getPackages()) {
			if (!p.containsCode()) {
				continue;
			}
			final String packagename = p.getName();
			final String foldername = packagename.length() == 0 ? "default"
					: packagename.replace('/', '.');
			final PackagePage page = new PackagePage(p, this, locator,
					folder.subFolder(foldername), context);
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
	protected void content(HTMLElement body) throws IOException {
		if (bundle.getPackages().isEmpty()) {
			body.p().text("No class files specified.");
		} else if (!bundle.containsCode()) {
			body.p().text(
					"None of the analyzed classes contain code relevant for code coverage.");
		} else {
			super.content(body);
		}
	}

}
