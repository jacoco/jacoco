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
package org.jacoco.report.xml;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.StringWriter;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link XMLElement}. *
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public class XMLElementTest {

	private StringWriter buffer;

	@Before
	public void setUp() {
		buffer = new StringWriter();
	}

	@Test
	public void testEmptyNode() throws IOException {
		final XMLElement n = new XMLElement(buffer, "hello_world1");
		n.close();
		// Second close has no effect:
		n.close();
		assertEquals("<hello_world1/>", buffer.toString());
	}

	@Test(expected = IOException.class)
	public void testAddAttributeToClosedNode() throws IOException {
		final XMLElement n = new XMLElement(buffer, "hello");
		n.close();
		n.attr("attr", "value");
	}

	@Test(expected = IOException.class)
	public void testAddChildToClosedNode() throws IOException {
		final XMLElement n = new XMLElement(buffer, "hello");
		n.close();
		n.element("child");
	}

	@Test(expected = IOException.class)
	public void testAddTextToClosedNode() throws IOException {
		final XMLElement n = new XMLElement(buffer, "hello");
		n.close();
		n.text("text");
	}

	@Test
	public void testNestedElement() throws IOException {
		final XMLElement n = new XMLElement(buffer, "hello");
		n.element("world");
		n.close();
		assertEquals("<hello><world/></hello>", buffer.toString());
	}

	@Test
	public void test2NestedElements() throws IOException {
		final XMLElement n = new XMLElement(buffer, "hello");
		n.element("world");
		n.element("universe");
		n.close();
		assertEquals("<hello><world/><universe/></hello>", buffer.toString());
	}

	@Test
	public void testText() throws IOException {
		final XMLElement n = new XMLElement(buffer, "hello");
		n.text("world");
		n.close();
		assertEquals("<hello>world</hello>", buffer.toString());
	}

	@Test
	public void testMixedContent() throws IOException {
		final XMLElement n = new XMLElement(buffer, "hello");
		n.element("tag");
		n.text("world");
		n.close();
		assertEquals("<hello><tag/>world</hello>", buffer.toString());
	}

	@Test
	public void testQuotedText() throws IOException {
		final XMLElement n = new XMLElement(buffer, "hello");
		n.text("<black&white\">");
		n.close();
		assertEquals("<hello>&lt;black&amp;white&quot;&gt;</hello>", buffer
				.toString());
	}

	@Test
	public void testAttributes() throws IOException {
		final XMLElement n = new XMLElement(buffer, "hello");
		n.attr("id", "12345").attr("quote", "<>");
		n.close();
		assertEquals("<hello id=\"12345\" quote=\"&lt;&gt;\"/>", buffer
				.toString());
	}

	@Test(expected = IOException.class)
	public void testInvalidAttributeOutput1() throws IOException {
		final XMLElement n = new XMLElement(buffer, "hello");
		n.text("text");
		n.attr("id", "12345");
	}

	@Test(expected = IOException.class)
	public void testInvalidAttributeOutput2() throws IOException {
		final XMLElement n = new XMLElement(buffer, "hello");
		n.element("child");
		n.attr("id", "12345");
	}

}
