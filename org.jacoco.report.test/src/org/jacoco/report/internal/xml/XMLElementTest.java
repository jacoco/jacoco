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
package org.jacoco.report.internal.xml;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link XMLElement}.
 */
public class XMLElementTest {

	private static final String DECL = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";

	private ByteArrayOutputStream buffer;

	private XMLElement root;

	@Before
	public void setup() throws IOException {
		buffer = new ByteArrayOutputStream();
		root = new XMLElement("root", null, null, false, "UTF-8", buffer);
	}

	@Test
	public void init_should_write_doctype_when_given() throws IOException {
		root = new XMLElement("root", "-//JACOCO//TEST", "test.dtd", false,
				"UTF-8", buffer);
		assertEquals(DECL
				+ "<!DOCTYPE root PUBLIC \"-//JACOCO//TEST\" \"test.dtd\"><root/>",
				actual());
	}

	@Test
	public void init_should_write_standalone_when_given() throws IOException {
		root = new XMLElement("root", null, null, true, "UTF-8", buffer);
		assertEquals(
				"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><root/>",
				actual());
	}

	@Test
	public void close_should_emit_empty_element_when_no_children_exist()
			throws IOException {
		assertContent("<root/>");
	}

	@Test
	public void close_should_be_allowed_multiple_times() throws IOException {
		root.close();
		root.close();
		assertContent("<root/>");
	}

	@Test(expected = IOException.class)
	public void attr_should_throw_exception_when_closed() throws IOException {
		root.close();
		root.attr("attr", "value");
	}

	@Test(expected = IOException.class)
	public void element_should_throw_exception_when_closed()
			throws IOException {
		root.close();
		root.element("child");
	}

	@Test(expected = IOException.class)
	public void text_should_throw_exception_when_closed() throws IOException {
		root.close();
		root.text("text");
	}

	@Test
	public void element_should_emit_nested_element() throws IOException {
		root.element("world");
		assertContent("<root><world/></root>");
	}

	@Test
	public void element_should_allow_multiple_nested_elements()
			throws IOException {
		root.element("world");
		root.element("universe");
		assertContent("<root><world/><universe/></root>");
	}

	@Test
	public void text_should_emit_text() throws IOException {
		root.text("world");
		assertContent("<root>world</root>");
	}

	@Test
	public void text_should_allow_mixing_with_elements() throws IOException {
		root.element("tag1");
		root.text("world");
		root.element("tag2");
		assertContent("<root><tag1/>world<tag2/></root>");
	}

	@Test
	public void test_should_be_quoted() throws IOException {
		root.text("<black&white\">");
		assertContent("<root>&lt;black&amp;white&quot;&gt;</root>");
	}

	@Test
	public void attr_should_ignore_call_when_value_is_null()
			throws IOException {
		root.attr("id", null);
		assertContent("<root/>");
	}

	@Test
	public void attr_should_emit_string_value() throws IOException {
		root.attr("id", "12345");
		assertContent("<root id=\"12345\"/>");
	}

	@Test
	public void attr_should_quote_string_value() throws IOException {
		root.attr("quote", "<\">");
		assertContent("<root quote=\"&lt;&quot;&gt;\"/>");
	}

	@Test
	public void attr_should_emit_int_value() throws IOException {
		root.attr("missed", 0);
		root.attr("total", 123);
		assertContent("<root missed=\"0\" total=\"123\"/>");
	}

	@Test
	public void attr_should_emit_long_value() throws IOException {
		root.attr("min", Long.MIN_VALUE);
		root.attr("max", Long.MAX_VALUE);
		assertContent(
				"<root min=\"-9223372036854775808\" max=\"9223372036854775807\"/>");
	}

	@Test(expected = IOException.class)
	public void attr_should_throw_exception_when_text_was_added()
			throws IOException {
		root.text("text");
		root.attr("id", "12345");
	}

	@Test(expected = IOException.class)
	public void attr_should_throw_exception_when_child_was_added()
			throws IOException {
		root.element("child");
		root.attr("id", "12345");
	}

	private void assertContent(String expected) throws IOException {
		assertEquals(DECL + expected, actual());
	}

	private String actual() throws IOException {
		root.close();
		return buffer.toString("UTF-8");
	}

}
