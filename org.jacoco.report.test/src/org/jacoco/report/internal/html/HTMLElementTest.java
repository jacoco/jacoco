/*******************************************************************************
 * Copyright (c) 2009, 2015 Mountainminds GmbH & Co. KG and Contributors
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

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.StringWriter;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link HTMLElement}.
 */
public class HTMLElementTest {

	private StringWriter buffer;

	private HTMLElement root;

	@Before
	public void setUp() throws IOException {
		buffer = new StringWriter();
		root = new HTMLElement(buffer, "root") {
			{
				beginOpenTag();
			}
		};
	}

	@Test
	public void testMeta() throws IOException {
		root.meta("key", "value");
		root.close();
		assertEquals(
				"<root><meta http-equiv=\"key\" content=\"value\"/></root>",
				buffer.toString());
	}

	@Test
	public void testLink() throws IOException {
		root.link("stylesheet", "style.css", "text/css");
		root.close();
		assertEquals(
				"<root><link rel=\"stylesheet\" href=\"style.css\" type=\"text/css\"/></root>",
				buffer.toString());
	}

	@Test
	public void testTitle() throws IOException {
		root.title();
		root.close();
		assertEquals("<root><title/></root>", buffer.toString());
	}

	@Test
	public void testH1() throws IOException {
		root.h1();
		root.close();
		assertEquals("<root><h1/></root>", buffer.toString());
	}

	@Test
	public void testP() throws IOException {
		root.p();
		root.close();
		assertEquals("<root><p/></root>", buffer.toString());
	}

	@Test
	public void testSpan1() throws IOException {
		root.span();
		root.close();
		assertEquals("<root><span/></root>", buffer.toString());
	}

	@Test
	public void testSpan2() throws IOException {
		root.span("abc");
		root.close();
		assertEquals("<root><span class=\"abc\"/></root>", buffer.toString());
	}

	@Test
	public void testSpan3() throws IOException {
		root.span("abc", "xy");
		root.close();
		assertEquals("<root><span class=\"abc\" id=\"xy\"/></root>",
				buffer.toString());
	}

	@Test
	public void testPre() throws IOException {
		root.pre("mystyle");
		root.close();
		assertEquals("<root><pre class=\"mystyle\"/></root>", buffer.toString());
	}

	@Test
	public void testDiv() throws IOException {
		root.div("mystyle");
		root.close();
		assertEquals("<root><div class=\"mystyle\"/></root>", buffer.toString());
	}

	@Test
	public void testCode() throws IOException {
		root.code().text("0xCAFEBABE");
		root.close();
		assertEquals("<root><code>0xCAFEBABE</code></root>", buffer.toString());
	}

	@Test
	public void testA1() throws IOException {
		root.a("http://www.jacoco.org/");
		root.close();
		assertEquals("<root><a href=\"http://www.jacoco.org/\"/></root>",
				buffer.toString());
	}

	@Test
	public void testA2() throws IOException {
		root.a("http://www.jacoco.org/", "extern");
		root.close();
		assertEquals(
				"<root><a href=\"http://www.jacoco.org/\" class=\"extern\"/></root>",
				buffer.toString());
	}

	@Test
	public void testALinkable1() throws IOException {
		root.a(new LinkableStub(null, "here", null), null);
		root.close();
		assertEquals("<root><span>here</span></root>", buffer.toString());
	}

	@Test
	public void testALinkable2() throws IOException {
		root.a(new LinkableStub(null, "here", "blue"), null);
		root.close();
		assertEquals("<root><span class=\"blue\">here</span></root>",
				buffer.toString());
	}

	@Test
	public void testALinkable3() throws IOException {
		root.a(new LinkableStub("index.html", "here", null), null);
		root.close();
		assertEquals("<root><a href=\"index.html\">here</a></root>",
				buffer.toString());
	}

	@Test
	public void testALinkable4() throws IOException {
		root.a(new LinkableStub("index.html", "here", "red"), null);
		root.close();
		assertEquals(
				"<root><a href=\"index.html\" class=\"red\">here</a></root>",
				buffer.toString());
	}

	@Test
	public void testTable() throws IOException {
		root.table("tablestyle");
		root.close();
		assertEquals(
				"<root><table class=\"tablestyle\" cellspacing=\"0\"/></root>",
				buffer.toString());
	}

	@Test
	public void testThead() throws IOException {
		root.thead();
		root.close();
		assertEquals("<root><thead/></root>", buffer.toString());
	}

	@Test
	public void testTfoot() throws IOException {
		root.tfoot();
		root.close();
		assertEquals("<root><tfoot/></root>", buffer.toString());
	}

	@Test
	public void testTbody() throws IOException {
		root.tbody();
		root.close();
		assertEquals("<root><tbody/></root>", buffer.toString());
	}

	@Test
	public void testTr() throws IOException {
		root.tr();
		root.close();
		assertEquals("<root><tr/></root>", buffer.toString());
	}

	@Test
	public void testTd1() throws IOException {
		root.td();
		root.close();
		assertEquals("<root><td/></root>", buffer.toString());
	}

	@Test
	public void testTd2() throws IOException {
		root.td("mystyle");
		root.close();
		assertEquals("<root><td class=\"mystyle\"/></root>", buffer.toString());
	}

	@Test
	public void testImg() throws IOException {
		root.img("sample.gif", 16, 32, "Hello");
		root.close();
		assertEquals(
				"<root><img src=\"sample.gif\" width=\"16\" height=\"32\" title=\"Hello\" alt=\"Hello\"/></root>",
				buffer.toString());
	}

	@Test
	public void testScript() throws IOException {
		root.script("text/javascript", "file.js");
		root.close();
		assertEquals(
				"<root><script type=\"text/javascript\" src=\"file.js\"></script></root>",
				buffer.toString());
	}

}
