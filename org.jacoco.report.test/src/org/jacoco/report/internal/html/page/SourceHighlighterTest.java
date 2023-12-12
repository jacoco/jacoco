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

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.util.Locale;

import org.jacoco.core.analysis.ICoverageNode.ElementType;
import org.jacoco.core.internal.analysis.CounterImpl;
import org.jacoco.core.internal.analysis.SourceNodeImpl;
import org.jacoco.report.internal.html.HTMLElement;
import org.jacoco.report.internal.html.HTMLSupport;
import org.jacoco.report.internal.html.resources.Styles;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

/**
 * Unit tests for {@link SourceHighlighter}.
 */
public class SourceHighlighterTest {

	private HTMLSupport htmlSupport;

	private ByteArrayOutputStream buffer;

	private HTMLElement html;

	private HTMLElement parent;

	private SourceHighlighter sourceHighlighter;

	private SourceNodeImpl source;

	@Before
	public void setup() throws Exception {
		htmlSupport = new HTMLSupport();
		source = new SourceNodeImpl(ElementType.SOURCEFILE, "Foo.java");
		buffer = new ByteArrayOutputStream();
		html = new HTMLElement(buffer, "UTF-8");
		html.head().title();
		parent = html.body();
		sourceHighlighter = new SourceHighlighter(Locale.US);
	}

	@Test
	public void testDefaultTabWidth() throws Exception {
		final String src = "\tA";
		sourceHighlighter.render(parent, source, new StringReader(src));
		final Document doc = parseDoc();

		// Assert that we no longer replace tabs with spaces
		assertEquals("\tA\n", htmlSupport.findStr(doc, "//pre/text()"));
	}

	@Test
	public void testDefaultLanguage() throws Exception {
		sourceHighlighter.render(parent, source, new StringReader(""));
		final Document doc = parseDoc();
		assertEquals("source lang-java linenums",
				htmlSupport.findStr(doc, "//pre/@class"));
	}

	@Test
	public void testSetLanguage() throws Exception {
		sourceHighlighter.setLanguage("scala");
		sourceHighlighter.render(parent, source, new StringReader(""));
		final Document doc = parseDoc();
		assertEquals("source lang-scala linenums",
				htmlSupport.findStr(doc, "//pre/@class"));
	}

	@Test
	public void testHighlighting() throws Exception {
		final String src = "A\nB\nC\nD";
		source.increment(CounterImpl.COUNTER_1_0, CounterImpl.COUNTER_0_0, 1);
		source.increment(CounterImpl.COUNTER_1_0, CounterImpl.COUNTER_0_0, 2);
		source.increment(CounterImpl.COUNTER_0_1, CounterImpl.COUNTER_0_0, 2);
		source.increment(CounterImpl.COUNTER_0_1, CounterImpl.COUNTER_0_0, 3);
		sourceHighlighter.render(parent, source, new StringReader(src));
		final Document doc = parseDoc();
		assertEquals(Styles.NOT_COVERED,
				htmlSupport.findStr(doc, "//pre/span[text() = 'A']/@class"));
		assertEquals(Styles.PARTLY_COVERED,
				htmlSupport.findStr(doc, "//pre/span[text() = 'B']/@class"));
		assertEquals(Styles.FULLY_COVERED,
				htmlSupport.findStr(doc, "//pre/span[text() = 'C']/@class"));
		assertEquals("",
				htmlSupport.findStr(doc, "//pre/span[text() = 'D']/@class"));
	}

	@Test
	public void testHighlightNone() throws Exception {
		sourceHighlighter.highlight(parent, source.getLine(1), 1);
		final Document doc = parseDoc();
		assertEquals("", htmlSupport.findStr(doc, "//pre"));
	}

	@Test
	public void testHighlightBranchesFC() throws Exception {
		source.increment(CounterImpl.COUNTER_0_1, CounterImpl.getInstance(0, 5),
				1);
		sourceHighlighter.highlight(parent.pre(null), source.getLine(1), 1);
		final Document doc = parseDoc();
		assertEquals("fc bfc", htmlSupport.findStr(doc, "//pre/span/@class"));
		assertEquals("All 5 branches covered.",
				htmlSupport.findStr(doc, "//pre/span/@title"));
	}

	@Test
	public void testHighlightBranchesPC() throws Exception {
		source.increment(CounterImpl.COUNTER_0_1, CounterImpl.getInstance(2, 3),
				1);
		sourceHighlighter.highlight(parent.pre(null), source.getLine(1), 1);
		final Document doc = parseDoc();
		assertEquals("pc bpc", htmlSupport.findStr(doc, "//pre/span/@class"));
		assertEquals("2 of 5 branches missed.",
				htmlSupport.findStr(doc, "//pre/span/@title"));
	}

	@Test
	public void testHighlightBranchesNC() throws Exception {
		source.increment(CounterImpl.COUNTER_0_1, CounterImpl.getInstance(5, 0),
				1);
		sourceHighlighter.highlight(parent.pre(null), source.getLine(1), 1);
		final Document doc = parseDoc();
		assertEquals("pc bnc", htmlSupport.findStr(doc, "//pre/span/@class"));
		assertEquals("All 5 branches missed.",
				htmlSupport.findStr(doc, "//pre/span/@title"));
	}

	private Document parseDoc() throws Exception {
		html.close();
		return htmlSupport.parse(buffer);
	}

}
