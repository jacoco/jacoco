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

import static java.lang.String.format;

import java.io.IOException;
import java.io.Reader;

import org.jacoco.core.analysis.ISourceNode;
import org.jacoco.report.internal.ReportOutputFolder;
import org.jacoco.report.internal.html.HTMLElement;
import org.jacoco.report.internal.html.IHTMLReportContext;
import org.jacoco.report.internal.html.resources.Resources;

/**
 * Page showing the content of a source file with numbered and highlighted
 * source lines.
 */
public class SourceFilePage extends NodePage<ISourceNode> {

	private final Reader sourceReader;

	private final int tabWidth;

	/**
	 * Creates a new page with given information.
	 *
	 * @param sourceFileNode
	 *            coverage data for this source file
	 * @param sourceReader
	 *            reader for the source code
	 * @param tabWidth
	 *            number of character per tab
	 * @param parent
	 *            optional hierarchical parent
	 * @param folder
	 *            base folder for this page
	 * @param context
	 *            settings context
	 */
	public SourceFilePage(final ISourceNode sourceFileNode,
			final Reader sourceReader, final int tabWidth,
			final ReportPage parent, final ReportOutputFolder folder,
			final IHTMLReportContext context) {
		super(sourceFileNode, parent, folder, context);
		this.sourceReader = sourceReader;
		this.tabWidth = tabWidth;
	}

	@Override
	protected void content(final HTMLElement body) throws IOException {
		final SourceHighlighter hl = new SourceHighlighter(context.getLocale());
		hl.render(body, getNode(), sourceReader);
		sourceReader.close();
	}

	@Override
	protected void head(final HTMLElement head) throws IOException {
		super.head(head);
		head.link("stylesheet", context.getResources().getLink(folder,
				Resources.PRETTIFY_STYLESHEET), "text/css");
		head.script(context.getResources().getLink(folder,
				Resources.PRETTIFY_SCRIPT));
	}

	@Override
	protected String getOnload() {
		return format("window['PR_TAB_WIDTH']=%d;prettyPrint()",
				Integer.valueOf(tabWidth));
	}

	@Override
	protected String getFileName() {
		return getNode().getName() + ".html";
	}

}
