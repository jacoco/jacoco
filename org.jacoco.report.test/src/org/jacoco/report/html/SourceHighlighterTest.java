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

import static org.junit.Assert.assertEquals;

import java.io.StringReader;
import java.io.StringWriter;

import org.jacoco.core.analysis.LinesImpl;
import org.jacoco.report.html.resources.Styles;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

/**
 * Unit tests for {@link SourceHighlighter}.
 * 
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public class SourceHighlighterTest {

	private HTMLSupport htmlSupport;

	private StringWriter buffer;

	private HTMLDocument html;

	private HTMLElement parent;

	private SourceHighlighter sourceHighlighter;

	private LinesImpl lines;

	@Before
	public void setup() throws Exception {
		htmlSupport = new HTMLSupport();
		lines = new LinesImpl();
		buffer = new StringWriter();
		html = new HTMLDocument(buffer, "UTF-8");
		html.head().title();
		parent = html.body();
		sourceHighlighter = new SourceHighlighter();
	}

	@Test
	public void testLineNumbering() throws Exception {
		final String src = "A\nB\nC\nD";
		sourceHighlighter.render(parent, lines, new StringReader(src));
		html.close();
		final Document doc = htmlSupport.parse(buffer.toString());
		assertEquals("    1", htmlSupport.findStr(doc, "//pre/span[1]"));
		assertEquals("    2", htmlSupport.findStr(doc, "//pre/span[2]"));
		assertEquals("    3", htmlSupport.findStr(doc, "//pre/span[3]"));
		assertEquals("    4", htmlSupport.findStr(doc, "//pre/span[4]"));
	}

	@Test
	public void testLineIds() throws Exception {
		final String src = "A\nB\nC\nD";
		sourceHighlighter.render(parent, lines, new StringReader(src));
		html.close();
		final Document doc = htmlSupport.parse(buffer.toString());
		assertEquals("L1", htmlSupport.findStr(doc, "//pre/span[1]/@id"));
		assertEquals("L2", htmlSupport.findStr(doc, "//pre/span[2]/@id"));
		assertEquals("L3", htmlSupport.findStr(doc, "//pre/span[3]/@id"));
		assertEquals("L4", htmlSupport.findStr(doc, "//pre/span[4]/@id"));
	}

	@Test
	public void testTabReplacement() throws Exception {
		final String src = "\tA";
		sourceHighlighter.setTabWidth(3);
		sourceHighlighter.render(parent, lines, new StringReader(src));
		html.close();
		final Document doc = htmlSupport.parse(buffer.toString());
		assertEquals("   A", htmlSupport.findStr(doc, "//pre/text()"));
	}

	@Test
	public void testHighlighting() throws Exception {
		final String src = "A\nB\nC\nD";
		lines.increment(new int[] { 1, 2 }, false);
		lines.increment(new int[] { 2, 3 }, true);
		sourceHighlighter.render(parent, lines, new StringReader(src));
		html.close();
		final Document doc = htmlSupport.parse(buffer.toString());
		assertEquals(Styles.NOT_COVERED, htmlSupport.findStr(doc,
				"//pre/span[text() = 'A']/@class"));
		assertEquals(Styles.PARTLY_COVERED, htmlSupport.findStr(doc,
				"//pre/span[text() = 'B']/@class"));
		assertEquals(Styles.FULLY_COVERED, htmlSupport.findStr(doc,
				"//pre/span[text() = 'C']/@class"));
		assertEquals("", htmlSupport.findStr(doc,
				"//pre/span[text() = 'D']/@class"));
	}

}
