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
 *******************************************************************************/
package org.jacoco.report.html;

import java.io.IOException;
import java.io.Reader;

import org.jacoco.core.analysis.ICoverageNode;
import org.jacoco.core.analysis.ILines;
import org.jacoco.core.analysis.SourceFileCoverage;
import org.jacoco.report.IReportVisitor;
import org.jacoco.report.ISourceFileLocator;
import org.jacoco.report.ReportOutputFolder;
import org.jacoco.report.internal.html.resources.Resources;

/**
 * Page showing the content of a source file with numbered and highlighted
 * source lines.
 * 
 * @author Marc R. Hoffmann
 * @version $qualified.bundle.version$
 */
public class SourceFilePage extends NodePage {

	private Reader sourceReader;

	private final String packageName;

	private final ILines lines;

	/**
	 * Creates a new page with given information.
	 * 
	 * @param sourceFileNode
	 * @param parent
	 * @param folder
	 * @param context
	 */
	public SourceFilePage(final SourceFileCoverage sourceFileNode,
			final ReportPage parent, final ReportOutputFolder folder,
			final IHTMLReportContext context) {
		super(sourceFileNode, parent, folder, context);
		packageName = sourceFileNode.getPackageName();
		lines = sourceFileNode.getLines();
	}

	public IReportVisitor visitChild(final ICoverageNode node) {
		throw new IllegalStateException("Source don't have child nodes.");
	}

	@Override
	public void visitEnd(final ISourceFileLocator sourceFileLocator)
			throws IOException {
		sourceReader = sourceFileLocator.getSourceFile(packageName, getNode()
				.getName());
		if (sourceReader != null) {
			super.visitEnd(sourceFileLocator);
		}
	}

	@Override
	protected void content(final HTMLElement body) throws IOException {
		final SourceHighlighter hl = new SourceHighlighter(context.getLocale());
		hl.render(body, lines, sourceReader);
		sourceReader.close();
	}

	@Override
	protected void headExtra(final HTMLElement head) throws IOException {
		super.headExtra(head);
		head.link(
				"stylesheet",
				context.getResources().getLink(folder,
						Resources.PRETTIFY_STYLESHEET), "text/css");
		head.script(
				"text/javascript",
				context.getResources().getLink(folder,
						Resources.PRETTIFY_SCRIPT));
	}

	@Override
	protected String getOnload() {
		return "prettyPrint()";
	}

	@Override
	protected String getFileName() {
		return getNode().getName() + ".html";
	}

	/**
	 * Checks whether this page has actually been rendered. This might not be
	 * the case if no source file has been found.
	 * 
	 * @return whether the page has been created
	 */
	public boolean exists() {
		return sourceReader != null;
	}

}
