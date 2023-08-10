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

import org.jacoco.report.ReportStructureTestDriver;
import org.jacoco.report.xml.XMLFormatter;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

/**
 * Unit tests for {@link XMLGroupVisitor}.
 */
public class XMLGroupVisitorTest {

	private ReportElement root;

	private ByteArrayOutputStream buffer;

	private XMLSupport support;

	private XMLGroupVisitor handler;

	private ReportStructureTestDriver driver;

	@Before
	public void setup() throws Exception {
		buffer = new ByteArrayOutputStream();
		support = new XMLSupport(XMLFormatter.class);
		root = new ReportElement("Report", buffer, "UTF-8");
		handler = new XMLGroupVisitor(root, null);
		driver = new ReportStructureTestDriver();
	}

	@Test
	public void testVisitBundle() throws Exception {
		driver.sendBundle(handler);
		final Document doc = parseDoc();
		assertEquals("bundle", support.findStr(doc, "//report/group/@name"));
	}

	@Test
	public void testVisitGroup() throws Exception {
		driver.sendGroup(handler);
		final Document doc = parseDoc();
		assertEquals("group", support.findStr(doc, "//report/group/@name"));
	}

	@Test
	public void testVisitEnd() throws Exception {
		driver.sendBundle(handler);
		handler.visitEnd();
		final Document doc = parseDoc();
		assertEquals("2", support.findStr(doc,
				"//report/counter[@type='BRANCH']/@covered"));
	}

	private Document parseDoc() throws Exception {
		root.close();
		return support.parse(buffer);
	}

}
