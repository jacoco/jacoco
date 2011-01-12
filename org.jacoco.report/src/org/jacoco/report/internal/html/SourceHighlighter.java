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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.Locale;

import org.jacoco.core.analysis.ILine;
import org.jacoco.core.analysis.ISourceNode;
import org.jacoco.report.internal.html.resources.Styles;

/**
 * Creates a highlighted output of a source file.
 * 
 * @author Marc R. Hoffmann
 * @version $qualified.bundle.version$
 */
class SourceHighlighter {

	/** Number of characters reserved for the line number column */
	private static final int LINENR_WIDTH = 5;

	private final Locale locale;

	private String tabReplacement;

	private String lang = "java";

	/**
	 * Creates a new highlighter with default settings.
	 * 
	 * @param locale
	 *            locale for tooltip rendering
	 */
	public SourceHighlighter(final Locale locale) {
		this.locale = locale;
		setTabWidth(4);
	}

	/**
	 * Specifies the number of spaces that are represented by a single tab.
	 * Default is 4.
	 * 
	 * @param width
	 *            spaces per tab
	 */
	public void setTabWidth(final int width) {
		final char[] blanks = new char[width];
		Arrays.fill(blanks, ' ');
		tabReplacement = new String(blanks);
	}

	/**
	 * Specifies the source language. This value might be used for syntax
	 * highlighting. Default is "java".
	 * 
	 * @param lang
	 *            source language identifier
	 */
	public void setLanguage(final String lang) {
		this.lang = lang;
	}

	/**
	 * Highlights the given source file.
	 * 
	 * @param parent
	 *            parent HTML element
	 * @param source
	 *            highlighting information
	 * @param contents
	 *            contents of the source file
	 * @throws IOException
	 *             problems while reading the source file or writing the output
	 */
	public void render(final HTMLElement parent, final ISourceNode source,
			final Reader contents) throws IOException {
		final HTMLElement pre = parent.pre(Styles.SOURCE + " lang-" + lang);
		final BufferedReader lineBuffer = new BufferedReader(contents);
		String line;
		int nr = 0;
		while ((line = lineBuffer.readLine()) != null) {
			nr++;
			renderLineNr(pre, nr);
			renderCodeLine(pre, line, source.getLine(nr));
		}
	}

	private void renderLineNr(final HTMLElement pre, final int nr)
			throws IOException {
		final String linestr = String.valueOf(nr);
		final HTMLElement linespan = pre.span(Styles.NR, "L" + linestr);
		for (int i = linestr.length(); i < LINENR_WIDTH; i++) {
			linespan.text("\u00A0"); // non-breaking space
		}
		linespan.text(linestr);
	}

	private void renderCodeLine(final HTMLElement pre, final String linesrc,
			final ILine line) throws IOException {
		highlight(pre, line).text(linesrc.replace("\t", tabReplacement));
		pre.text("\n");
	}

	HTMLElement highlight(final HTMLElement pre, final ILine line)
			throws IOException {
		final String style;
		switch (line.getStatus()) {
		case ILine.NOT_COVERED:
			style = Styles.NOT_COVERED;
			break;
		case ILine.FULLY_COVERED:
			style = Styles.FULLY_COVERED;
			break;
		case ILine.PARTLY_COVERED:
			style = Styles.PARTLY_COVERED;
			break;
		default:
			return pre;
		}
		final int totalBranches = line.getBranchCounter().getTotalCount();
		if (totalBranches == 0) {
			return pre.span(style);
		}
		final int missedBranches = line.getBranchCounter().getMissedCount();
		final Integer t = Integer.valueOf(totalBranches);
		if (missedBranches == 0) {
			return span(pre, style, Styles.BRANCH_FULLY_COVERED,
					"All %d branches covered.", t);
		}
		if (missedBranches == totalBranches) {
			return span(pre, style, Styles.BRANCH_NOT_COVERED,
					"All %d branches missed.", t);
		}
		final Integer m = Integer.valueOf(missedBranches);
		return span(pre, style, Styles.BRANCH_PARTLY_COVERED,
				"%d of %d branches missed.", m, t);
	}

	private HTMLElement span(final HTMLElement parent, final String style1,
			final String style2, final String title, final Object... params)
			throws IOException {
		final HTMLElement span = parent.span(style1 + " " + style2);
		span.attr("title", String.format(locale, title, params));
		return span;
	}

}
