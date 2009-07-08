/*******************************************************************************
 * Copyright (c) 2009 Mountainminds GmbH & Co. KG and others
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

	/**
	 * Creates a new highlighter with default settings.
	 */
	public SourceHighlighter() {
		setTabWidth(4);
	}

	/**
	 * Specifies the number of spaces that are represented by a single tab.
	 * 
	 * @param width
	 *            spaces per tab
	 */
	public void setTabWidth(int width) {
		final char[] blanks = new char[width];
		Arrays.fill(blanks, ' ');
		tabReplacement = new String(blanks);
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
		final HTMLElement pre = parent.pre(Styles.SOURCE);
		BufferedReader lineBuffer = new BufferedReader(contents);
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
		final HTMLElement linespan = pre.span("nr");
		for (int i = linestr.length(); i < LINENR_WIDTH; i++) {
			linespan.text(" ");
		}
		linespan.text(linestr);
	}

	private void renderCodeLine(final HTMLElement pre, final String line,
			final int status) throws IOException {
		final String lineWithoutTabs = line.replace("\t", tabReplacement);
		switch (status) {
		case ILines.NOT_COVERED:
			pre.span(Styles.NOT_COVERED).text(lineWithoutTabs);
			break;
		case ILines.FULLY_COVERED:
			pre.span(Styles.FULLY_COVERED).text(lineWithoutTabs);
			break;
		case ILines.PARTLY_COVERED:
			pre.span(Styles.PARTLY_COVERED).text(lineWithoutTabs);
			break;
		default:
			pre.text(lineWithoutTabs);
		}
		pre.br();
	}

}
