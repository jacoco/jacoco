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
package org.jacoco.report.internal.html.page;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.analysis.IPackageCoverage;
import org.jacoco.core.analysis.ISourceFileCoverage;
import org.jacoco.report.ISourceFileLocator;
import org.jacoco.report.internal.ReportOutputFolder;
import org.jacoco.report.internal.html.IHTMLReportContext;

/**
 * Page showing coverage information for a Java package. The page contains a
 * table with all classes of the package.
 */
public class PackagePage extends TablePage<IPackageCoverage> {

	private final ISourceFileLocator locator;

	private final boolean linkToSource;

	private final Set<String> addedSourceFiles;

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
	 * @param linkToSource
	 *            link to source file instead of table of methods
	 * @param context
	 *            settings context
	 */
	public PackagePage(final IPackageCoverage node, final ReportPage parent,
			final ISourceFileLocator locator, final ReportOutputFolder folder,
			final boolean linkToSource, final IHTMLReportContext context) {
		super(node, parent, folder, context);
		this.locator = locator;
		this.linkToSource = linkToSource;
		this.addedSourceFiles = new HashSet<String>();
	}

	@Override
	public void render() throws IOException {
		final Map<String, SourceFilePage> sourceFiles = renderSourceFiles();
		renderClasses(sourceFiles);
		super.render();
	}

	private final Map<String, SourceFilePage> renderSourceFiles()
			throws IOException {
		final Map<String, SourceFilePage> sourceFiles = new HashMap<String, SourceFilePage>();
		final String packagename = getNode().getName();
		for (final ISourceFileCoverage s : getNode().getSourceFiles()) {
			final String sourcename = s.getName();
			final Reader reader = locator
					.getSourceFile(packagename, sourcename);
			if (reader != null) {
				final SourceFilePage sourcePage = new SourceFilePage(s, reader,
						locator.getTabWidth(), this, folder, context);
				sourcePage.render();
				sourceFiles.put(sourcename, sourcePage);
			}

		}
		return sourceFiles;
	}

	private void renderClasses(final Map<String, SourceFilePage> sourceFiles)
			throws IOException {
		for (final IClassCoverage c : getNode().getClasses()) {
			final ClassPage page = new ClassPage(c, this, sourceFiles.get(c
					.getSourceFileName()), folder, context);
			page.render();

			if (linkToSource && sourceFiles.containsKey(c.getSourceFileName())) {
				if (!addedSourceFiles.contains(c.getSourceFileName())) {
					addItem(sourceFiles.get(c.getSourceFileName()));
					addedSourceFiles.add(c.getSourceFileName());
				}
			} else {
				addItem(page);
			}
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

}
