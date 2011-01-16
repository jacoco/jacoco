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
import java.io.Reader;

import org.jacoco.core.analysis.ICoverageNode;
import org.jacoco.core.analysis.ISourceFileCoverage;
import org.jacoco.core.analysis.ISourceNode;
import org.jacoco.report.IReportVisitor;
import org.jacoco.report.ISourceFileLocator;
import org.jacoco.report.ReportOutputFolder;
import org.jacoco.report.internal.html.resources.Resources;

/**
 * Page showing the content of a source file with numbered and highlighted
 * source lines.
 */
public class SourceFilePage extends NodePage {

	private Reader sourceReader;

	private final String packageName;

	private final ISourceNode source;

	/**
	 * Creates a new page with given information.
	 * 
	 * @param sourceFileNode
	 * @param parent
	 * @param folder
	 * @param context
	 */
	public SourceFilePage(final ISourceFileCoverage sourceFileNode,
			final ReportPage parent, final ReportOutputFolder folder,
			final IHTMLReportContext context) {
		super(sourceFileNode, parent, folder, context);
		packageName = sourceFileNode.getPackageName();
		source = sourceFileNode;
	}

	public IReportVisitor visitChild(final ICoverageNode node) {
		throw new AssertionError("Source don't have child nodes.");
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
		hl.render(body, source, sourceReader);
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
