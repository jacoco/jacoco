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
import static org.junit.Assert.assertSame;

import java.io.IOException;

import org.jacoco.core.analysis.CoverageNodeImpl;
import org.jacoco.core.analysis.ICoverageNode.ElementType;
import org.jacoco.report.IReportVisitor;
import org.jacoco.report.ISourceFileLocator;
import org.jacoco.report.MemoryReportOutput;
import org.jacoco.report.ReportOutputFolder;
import org.jacoco.report.html.resources.Resources;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

/**
 * Unit tests for {@link ReportPage}.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public class ReportPageTest {

	private MemoryReportOutput output;

	private ReportOutputFolder root;

	private Resources resources;

	private CoverageNodeImpl node;

	private ReportPage page;

	private class TestReportPage extends ReportPage {

		protected TestReportPage(String name, ReportPage parent) {
			super(ElementType.GROUP, name, parent, root,
					ReportPageTest.this.resources);
		}

		@Override
		protected void content(HTMLElement body,
				ISourceFileLocator sourceFileLocator) throws IOException {
			body.div("testcontent").text("Hello Test");
		}

		@Override
		protected String getFileName() {
			return name + ".html";
		}

		public IReportVisitor visitChild(ElementType type, String name)
				throws IOException {
			throw new UnsupportedOperationException();
		}

	}

	@Before
	public void setup() {
		output = new MemoryReportOutput();
		root = new ReportOutputFolder(output);
		resources = new Resources(root);
		ReportPage parent = new TestReportPage("Parent", null);
		node = new CoverageNodeImpl(ElementType.GROUP, "Test", false);
		page = new TestReportPage(node.getName(), parent);
	}

	@Test
	public void testGetNode() throws IOException {
		page.visitEnd(node, null);
		assertSame(node, page.getNode());
	}

	@Test
	public void testLink() throws IOException {
		ReportOutputFolder base = root.subFolder("here");
		assertEquals("../Test.html", page.getLink(base));
	}

	@Test
	public void testPageContent() throws Exception {
		page.visitEnd(node, null);
		final HTMLSupport support = new HTMLSupport();
		final Document doc = support.parse(output.getFile("Test.html"));

		// style sheet
		assertEquals(".resources/report.css", support.findStr(doc,
				"/html/head/link[@rel='stylesheet']/@href"));

		// bread crumb
		assertEquals("Parent", support.findStr(doc,
				"/html/body/div[@class='breadcrumb']/a[1]/text()"));
		assertEquals("Parent.html", support.findStr(doc,
				"/html/body/div[@class='breadcrumb']/a[1]/@href"));
		assertEquals("el_group", support.findStr(doc,
				"/html/body/div[@class='breadcrumb']/a[1]/@class"));
		assertEquals("Test", support.findStr(doc,
				"/html/body/div[@class='breadcrumb']/span[1]/text()"));
		assertEquals("el_group", support.findStr(doc,
				"/html/body/div[@class='breadcrumb']/span[1]/@class"));

		// Header
		assertEquals("Test", support.findStr(doc, "/html/body/h1/text()"));

		// Content
		assertEquals("Hello Test", support.findStr(doc,
				"/html/body/div[@class='testcontent']/text()"));
	}

	@Test
	public void testCreatedPage() {

	}

}
