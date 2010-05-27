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
 * $Id: $
 *******************************************************************************/
package org.jacoco.report.html;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;

import org.jacoco.core.analysis.ILines;
import org.jacoco.report.html.resources.Styles;

/**
 * Creates a highlighted output of a source file.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public class SourceHighlighter {

	/** Number of characters reserved for the line number column */
	private static final int LINENR_WIDTH = 5;

	private String tabReplacement;

	private String lang = "java";

	/**
	 * Creates a new highlighter with default settings.
	 */
	public SourceHighlighter() {
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
	 * @param lines
	 *            highlighting information
	 * @param contents
	 *            contents of the source file
	 * @throws IOException
	 *             problems while reading the source file or writing the output
	 */
	public void render(final HTMLElement parent, final ILines lines,
			final Reader contents) throws IOException {
		final HTMLElement pre = parent.pre(Styles.SOURCE + " lang-" + lang);
		final BufferedReader lineBuffer = new BufferedReader(contents);
		String line;
		int nr = 0;
		while ((line = lineBuffer.readLine()) != null) {
			nr++;
			renderLineNr(pre, nr);
			renderCodeLine(pre, line, lines.getStatus(nr));
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

	private void renderCodeLine(final HTMLElement pre, final String line,
			final int status) throws IOException {
		highlight(pre, status).text(line.replace("\t", tabReplacement));
		pre.text("\n");
	}

	private HTMLElement highlight(final HTMLElement pre, final int status)
			throws IOException {
		switch (status) {
		case ILines.NOT_COVERED:
			return pre.span(Styles.NOT_COVERED);
		case ILines.FULLY_COVERED:
			return pre.span(Styles.FULLY_COVERED);
		case ILines.PARTLY_COVERED:
			return pre.span(Styles.PARTLY_COVERED);
		default:
			return pre;
		}
	}

}
