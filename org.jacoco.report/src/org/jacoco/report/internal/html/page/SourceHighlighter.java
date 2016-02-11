/*******************************************************************************
 * Copyright (c) 2009, 2016 Mountainminds GmbH & Co. KG and Contributors
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Locale;

import org.jacoco.core.analysis.EBigOFunction;
import org.jacoco.core.analysis.ICounter;
import org.jacoco.core.analysis.ILine;
import org.jacoco.core.analysis.ISourceNode;
import org.jacoco.core.data.ProbeMode;
import org.jacoco.report.internal.html.HTMLElement;
import org.jacoco.report.internal.html.resources.Styles;

/**
 * Creates a highlighted output of a source file.
 */
final class SourceHighlighter {

	private final Locale locale;

	private final ProbeMode probeMode;

	private String lang;

	/**
	 * Creates a new highlighter with default settings.
	 * 
	 * @param locale
	 *            locale for tooltip rendering
	 * @param probeMode
	 *            the probeMode of the source coverage being rendered
	 */
	public SourceHighlighter(final Locale locale, final ProbeMode probeMode) {
		this.locale = locale;
		this.probeMode = probeMode;
		lang = "java";
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
		final HTMLElement pre = parent.pre(Styles.SOURCE + " lang-" + lang
				+ " linenums");
		final boolean hasEBigO = source.hasEBigO();
		final BufferedReader lineBuffer = new BufferedReader(contents);
		String line;
		int nr = 0;
		while ((line = lineBuffer.readLine()) != null) {
			nr++;
			renderCodeLine(pre, line, source.getLine(nr),
					(hasEBigO ? source.getLineEBigOFunction(nr) : null), nr);
		}
	}

	private void renderCodeLine(final HTMLElement pre, final String linesrc,
			final ILine line, final EBigOFunction ebigo, final int lineNr)
			throws IOException {
		addExtras(pre, line, ebigo);
		highlight(pre, line, lineNr).text(linesrc);
		pre.text("\n");
	}

	private void addExtras(final HTMLElement pre, final ILine line,
			final EBigOFunction ebigo) throws IOException {

		addEBigO(pre, ebigo);
		addParallelPercentPrefix(pre, line);
	}

	void addEBigO(final HTMLElement pre, final EBigOFunction ebigo)
			throws IOException {

		if (ebigo == null) {
			return;
		}

		final String style;
		switch (ebigo.getType()) {
		default:
			style = "e";
			break;
		case Logarithmic:
			style = "e efc";
			break;
		case Linear:
			style = "e efc";
			break;
		case PowerLaw:
			style = "e epc";
			break;
		case Exponential:
			style = "e enc";
			break;
		}
		final HTMLElement span = pre.span(style);
		addEBigOText(span, ebigo);
	}

	private void addEBigOText(final HTMLElement span, final EBigOFunction ebigo)
			throws IOException {
		final StringBuffer buffer = new StringBuffer();

		buffer.append(ebigo.getOrderOfMagnitude());
		while (buffer.length() < 7) {
			buffer.append(' ');
		}
		span.text(buffer.toString());
	}

	void addParallelPercentPrefix(final HTMLElement pre, final ILine line)
			throws IOException {

		if (ProbeMode.parallelcount != probeMode) {
			return;
		}

		final double parallelPct;
		{
			double pp = line.getParallelPercent() - ROUND_DOWN;
			if (pp < 0) {
				pp = 0;
			}
			parallelPct = pp;
		}

		final int status = line.getStatus();
		final boolean showPP = status != ICounter.EMPTY
				&& status != ICounter.NOT_COVERED;
		final String style;
		if (!showPP) {
			style = "e";
		} else if (parallelPct > 95.0D) {
			style = "e efc";
		} else if (parallelPct > 50.0D) {
			style = "e epc";
		} else {
			style = "e enc";
		}
		final HTMLElement span = pre.span(style);
		if (showPP) {
			span.text(String.format("%7.3f%%", new Double(parallelPct)));
		} else {
			span.text("        ");
		}
	}

	HTMLElement highlight(final HTMLElement pre, final ILine line,
			final int lineNr) throws IOException {
		final String style;
		switch (line.getStatus()) {
		case ICounter.NOT_COVERED:
			style = Styles.NOT_COVERED;
			break;
		case ICounter.FULLY_COVERED:
			style = Styles.FULLY_COVERED;
			break;
		case ICounter.PARTLY_COVERED:
			style = Styles.PARTLY_COVERED;
			break;
		default:
			return pre;
		}

		final String lineId = "L" + Integer.toString(lineNr);
		final ICounter branches = line.getBranchCounter();
		switch (branches.getStatus()) {
		case ICounter.NOT_COVERED:
			return span(pre, lineId, style, Styles.BRANCH_NOT_COVERED,
					"All %2$d branches missed.", branches);
		case ICounter.FULLY_COVERED:
			return span(pre, lineId, style, Styles.BRANCH_FULLY_COVERED,
					"All %2$d branches covered.", branches);
		case ICounter.PARTLY_COVERED:
			return span(pre, lineId, style, Styles.BRANCH_PARTLY_COVERED,
					"%1$d of %2$d branches missed.", branches);
		default:
			return pre.span(style, lineId);
		}
	}

	private HTMLElement span(final HTMLElement parent, final String id,
			final String style1, final String style2, final String title,
			final ICounter branches) throws IOException {
		final HTMLElement span = parent.span(style1 + " " + style2, id);
		final Integer missed = Integer.valueOf(branches.getMissedCount());
		final Integer total = Integer.valueOf(branches.getTotalCount());
		span.attr("title", String.format(locale, title, missed, total));
		return span;
	}

}
