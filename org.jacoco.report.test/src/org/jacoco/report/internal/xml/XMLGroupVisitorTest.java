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
package org.jacoco.report.internal.xml;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.StringWriter;

import javax.xml.parsers.ParserConfigurationException;

import org.jacoco.report.ReportStructureTestDriver;
import org.jacoco.report.xml.XMLFormatter;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Unit tests for {@link XMLGroupVisitor}.
 */
public class XMLGroupVisitorTest {

	private XMLElement root;

	private StringWriter buffer;

	private XMLSupport support;

	private XMLGroupVisitor handler;

	private ReportStructureTestDriver driver;

	@Before
	public void setup() throws Exception {
		buffer = new StringWriter();
		support = new XMLSupport(XMLFormatter.class);
		root = new XMLDocument("report", "-//JACOCO//DTD Report 1.0//EN",
				"report.dtd", "UTF-8", true, buffer);
		root.attr("name", "Report");
		handler = new XMLGroupVisitor(root, null);
		driver = new ReportStructureTestDriver();
	}

	@Test
	public void testVisitBundle() throws Exception {
		driver.sendBundle(handler);
		root.close();
		final Document doc = getDocument();
		assertEquals("bundle", support.findStr(doc, "//report/group/@name"));
	}

	@Test
	public void testVisitGroup() throws Exception {
		driver.sendGroup(handler);
		root.close();
		final Document doc = getDocument();
		assertEquals("group", support.findStr(doc, "//report/group/@name"));
	}

	@Test
	public void testVisitEnd() throws Exception {
		driver.sendBundle(handler);
		handler.visitEnd();
		root.close();
		final Document doc = getDocument();
		assertEquals("2", support.findStr(doc,
				"//report/counter[@type='BRANCH']/@covered"));
	}

	private Document getDocument() throws SAXException, IOException,
			ParserConfigurationException {
		return support.parse(buffer.toString());
	}

}
