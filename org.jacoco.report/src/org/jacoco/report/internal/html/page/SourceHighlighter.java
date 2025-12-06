/*******************************************************************************
 * Copyright (c) 2009, 2025 Mountainminds GmbH & Co. KG and Contributors
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.*;

import org.jacoco.core.analysis.ICounter;
import org.jacoco.core.analysis.ILine;
import org.jacoco.core.analysis.ISourceNode;
import org.jacoco.report.internal.html.HTMLElement;
import org.jacoco.report.internal.html.resources.Styles;

import org.jacoco.core.analysis.ICounter;
import org.jacoco.core.analysis.ILine;
import org.jacoco.core.analysis.ISourceNode;
import org.jacoco.core.internal.analysis.SourceFileCoverageImpl;
import org.jacoco.core.internal.diff.ChangeLine;
import org.jacoco.core.internal.diff.ClassInfoDto;
import org.jacoco.core.internal.diff.MethodInfoDto;
import org.jacoco.core.tools.ExecFileLoader;

/**
 * Creates a highlighted output of a source file.
 */
final class SourceHighlighter {

	private final Locale locale;

	private String lang;

	/**
	 * Creates a new highlighter with default settings.
	 *
	 * @param locale
	 *            locale for tooltip rendering
	 */
	public SourceHighlighter(final Locale locale) {
		this.locale = locale;
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
		final HTMLElement pre = parent.pre(Styles.SOURCE + " lang-" + lang + " linenums");
		final BufferedReader lineBuffer = new BufferedReader(contents);
		String line;
		List<ChangeLine> changeLineList = null;
		String key = null;
		if (source instanceof SourceFileCoverageImpl && ExecFileLoader.classInfoDto.get() != null) {
			final String packageName=((SourceFileCoverageImpl) source).getPackageName();
			final String className =source.getName();
			final String classNameInner=packageName+ "/"+className.replace(".java","");
			List<ClassInfoDto> dtoList = ExecFileLoader.classInfoDto.get();
			Optional<ClassInfoDto> classInfoDto = dtoList.stream().filter(i -> i.getClassFile().equals(classNameInner)).findAny();
			if (classInfoDto.isPresent()) {
				changeLineList = classInfoDto.get().getLines();
			}
			key = classNameInner;
			Integer[] integers = new Integer[2];
			integers[0] = 0;
			integers[1] = 0;
			PackageSourcePage.incrementalRowLevelCoverageMap.computeIfAbsent(key, k -> integers);
		}
		int nr = 0;
		while ((line = lineBuffer.readLine()) != null) {
			nr++;
			renderCodeLine(pre, line, source.getLine(nr), nr, changeLineList, key);
		}
	}


	private void renderCodeLine(final HTMLElement pre, final String linesrc, final ILine line, final int lineNr, List<ChangeLine> changeLineList, String key) throws IOException {
		highlight(pre, line, lineNr, changeLineList, key).text(linesrc);
		pre.text("\n");
	}

	// 最终代码染色的逻辑，源代码行通过指令行来染色，行在计算覆盖率时候已经得出
	HTMLElement highlight(final HTMLElement pre, final ILine line,
						  final int lineNr, List<ChangeLine> changeLineList, String key) throws IOException {
		String style;
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
				return pre; // 一些特殊的代码，比如空行、报名、import 等，就不进行染色
		}
		// 如果当前处理的行，在changeLineList中存在
		final String lineId = "L" + Integer.toString(lineNr);
		final ICounter branches = line.getBranchCounter();
		if (changeLineList != null && !changeLineList.isEmpty()) {
			Optional<ChangeLine> chageLine = changeLineList.stream().filter(i -> i.getStartLineNum() <= lineNr && i.getEndLineNum() >= lineNr).findAny();
			if (chageLine.isPresent()) {
				// 如果是增量版本变动行，就进行添加处理
				if (key != null && !key.isEmpty()) {
					Integer[] integers = PackageSourcePage.incrementalRowLevelCoverageMap.get(key);
					if (Styles.FULLY_COVERED.equals(style)) {
						// 只有全量覆盖了的才计算覆盖，未覆盖和部分覆盖的，不进行计算
						integers[0] += 1;
					}
					integers[1]++;
				}
				style += " " + chageLine.get().getType();
			}
		}
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

	private void renderCodeLine(final HTMLElement pre, final String linesrc,
			final ILine line, final int lineNr) throws IOException {
		highlight(pre, line, lineNr).text(linesrc);
		pre.text("\n");
	}

	// 最终代码染色的逻辑，源代码行通过指令行来染色，行在计算覆盖率时候已经得出
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
