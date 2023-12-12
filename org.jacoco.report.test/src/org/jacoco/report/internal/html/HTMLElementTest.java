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
package org.jacoco.report.internal.html;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link HTMLElement}.
 */
public class HTMLElementTest {

	private static final String PREFIX = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\"><html xmlns=\"http://www.w3.org/1999/xhtml\">";

	private static final String SUFFIX = "</html>";

	private ByteArrayOutputStream buffer;

	private HTMLElement root;

	@Before
	public void setup() throws IOException {
		buffer = new ByteArrayOutputStream();
		root = new HTMLElement(buffer, "UTF-8");
	}

	@Test
	public void should_create_minimal_valid_html_document() throws Exception {
		root.head().title();
		root.body();
		root.close();
		new HTMLSupport().parse(buffer);
	}

	@Test
	public void head_should_create_head_tag() throws IOException {
		root.head();
		assertContent("<head/>");
	}

	@Test
	public void meta_should_create_meta_tag_with_attributes()
			throws IOException {
		root.meta("key", "value");
		assertContent("<meta http-equiv=\"key\" content=\"value\"/>");
	}

	@Test
	public void body_should_create_body_tag() throws IOException {
		root.body();
		assertContent("<body/>");
	}

	@Test
	public void link_should_create_link_tag_with_attributes()
			throws IOException {
		root.link("stylesheet", "style.css", "text/css");
		assertContent(
				"<link rel=\"stylesheet\" href=\"style.css\" type=\"text/css\"/>");
	}

	@Test
	public void title_should_create_title_tag() throws IOException {
		root.title();
		assertContent("<title/>");
	}

	@Test
	public void h1_should_create_h1_tag() throws IOException {
		root.h1();
		assertContent("<h1/>");
	}

	@Test
	public void p_should_create_p_tag() throws IOException {
		root.p();
		assertContent("<p/>");
	}

	@Test
	public void span_should_create_span_tag() throws IOException {
		root.span();
		assertContent("<span/>");
	}

	@Test
	public void span_should_create_span_tag_with_class_attribute()
			throws IOException {
		root.span("abc");
		assertContent("<span class=\"abc\"/>");
	}

	@Test
	public void span_should_create_span_tag_with_class_and_id_attribute()
			throws IOException {
		root.span("abc", "xy");
		assertContent("<span class=\"abc\" id=\"xy\"/>");
	}

	@Test
	public void pre_should_create_pre_tag_with_class_attribute()
			throws IOException {
		root.pre("mystyle");
		assertContent("<pre class=\"mystyle\"/>");
	}

	@Test
	public void div_should_create_div_tag_with_class_attribute()
			throws IOException {
		root.div("mystyle");
		assertContent("<div class=\"mystyle\"/>");
	}

	@Test
	public void code_should_create_code_tag() throws IOException {
		root.code().text("0xCAFEBABE");
		assertContent("<code>0xCAFEBABE</code>");
	}

	@Test
	public void a_should_create_a_tag_with_href_attribute() throws IOException {
		root.a("http://www.jacoco.org/");
		assertContent("<a href=\"http://www.jacoco.org/\"/>");
	}

	@Test
	public void a_should_create_a_tag_with_href_and_class_attribute()
			throws IOException {
		root.a("http://www.jacoco.org/", "extern");
		assertContent("<a href=\"http://www.jacoco.org/\" class=\"extern\"/>");
	}

	@Test
	public void a_should_create_span_tag_when_no_link_is_given()
			throws IOException {
		root.a(new LinkableStub(null, "here", null), null);
		assertContent("<span>here</span>");
	}

	@Test
	public void a_should_create_span_tag_with_class_attribute_when_no_link_is_given()
			throws IOException {
		root.a(new LinkableStub(null, "here", "blue"), null);
		assertContent("<span class=\"blue\">here</span>");
	}

	@Test
	public void a_should_create_a_tag_when_link_is_given() throws IOException {
		root.a(new LinkableStub("index.html", "here", null), null);
		assertContent("<a href=\"index.html\">here</a>");
	}

	@Test
	public void a_should_create_a_tag_with_class_attribute_when_link_is_given()
			throws IOException {
		root.a(new LinkableStub("index.html", "here", "red"), null);
		assertContent("<a href=\"index.html\" class=\"red\">here</a>");
	}

	@Test
	public void table_should_create_table_tag_with_attributes()
			throws IOException {
		root.table("tablestyle");
		assertContent("<table class=\"tablestyle\" cellspacing=\"0\"/>");
	}

	@Test
	public void thead_should_create_thead_tag() throws IOException {
		root.thead();
		assertContent("<thead/>");
	}

	@Test
	public void tfoot_should_create_tfoot_tag() throws IOException {
		root.tfoot();
		assertContent("<tfoot/>");
	}

	@Test
	public void tbody_should_create_tbody_tag() throws IOException {
		root.tbody();
		assertContent("<tbody/>");
	}

	@Test
	public void tr_should_create_tr_tag() throws IOException {
		root.tr();
		assertContent("<tr/>");
	}

	@Test
	public void td_should_create_td_tag() throws IOException {
		root.td();
		assertContent("<td/>");
	}

	@Test
	public void td_should_create_td_tag_with_class_attribute()
			throws IOException {
		root.td("mystyle");
		assertContent("<td class=\"mystyle\"/>");
	}

	@Test
	public void img_should_create_img_tag_with_attributes() throws IOException {
		root.img("sample.gif", 16, 32, "Hello");
		assertContent(
				"<img src=\"sample.gif\" width=\"16\" height=\"32\" title=\"Hello\" alt=\"Hello\"/>");
	}

	@Test
	public void script_should_create_script_tag_with_attributes()
			throws IOException {
		root.script("file.js");
		assertContent(
				"<script type=\"text/javascript\" src=\"file.js\"></script>");
	}

	private void assertContent(String expected) throws IOException {
		root.close();
		assertEquals(PREFIX + expected + SUFFIX, buffer.toString("UTF-8"));
	}

}
