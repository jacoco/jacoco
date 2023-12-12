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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.jacoco.report.internal.ReportOutputFolder;
import org.jacoco.report.internal.html.HTMLElement;
import org.jacoco.report.internal.html.HTMLSupport;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

/**
 * Unit tests for {@link ReportPage}.
 */
public class ReportPageTest extends PageTestBase {

	private ReportPage rootpage;

	private ReportPage page;

	private class TestReportPage extends ReportPage {

		private final String label;
		private final String style;

		protected TestReportPage(String label, String style,
				ReportPage parent) {
			super(parent, rootFolder, ReportPageTest.this.context);
			this.label = label;
			this.style = style;
		}

		@Override
		protected void content(HTMLElement body) throws IOException {
			body.div("testcontent").text("Hello Test");
		}

		@Override
		protected String getFileName() {
			return label + ".html";
		}

		public String getLinkLabel() {
			return label;
		}

		public String getLinkStyle() {
			return style;
		}

	}

	@Before
	@Override
	public void setup() throws Exception {
		super.setup();
		rootpage = new TestReportPage("Report", "el_report", null);
		page = new TestReportPage("Test", "el_group", rootpage);
	}

	@Test
	public void testIsRootPage1() {
		assertFalse(page.isRootPage());
	}

	@Test
	public void testIsRootPage2() {
		assertTrue(rootpage.isRootPage());
	}

	@Test
	public void testGetLink() throws IOException {
		ReportOutputFolder base = rootFolder.subFolder("here");
		assertEquals("../Test.html", page.getLink(base));
	}

	@Test
	public void testPageContent() throws Exception {
		page.render();
		final HTMLSupport support = new HTMLSupport();
		final Document doc = support.parse(output.getFile("Test.html"));

		// language
		assertEquals("en", support.findStr(doc, "/html/@lang"));

		// style sheet
		assertEquals("jacoco-resources/report.css", support.findStr(doc,
				"/html/head/link[@rel='stylesheet']/@href"));

		// bread crumb
		assertEquals("Report", support.findStr(doc,
				"/html/body/div[@class='breadcrumb']/a[1]/text()"));
		assertEquals("Report.html", support.findStr(doc,
				"/html/body/div[@class='breadcrumb']/a[1]/@href"));
		assertEquals("el_report", support.findStr(doc,
				"/html/body/div[@class='breadcrumb']/a[1]/@class"));
		assertEquals("Test", support.findStr(doc,
				"/html/body/div[@class='breadcrumb']/span[2]/text()"));
		assertEquals("el_group", support.findStr(doc,
				"/html/body/div[@class='breadcrumb']/span[2]/@class"));

		// Header
		assertEquals("Test", support.findStr(doc, "/html/body/h1/text()"));

		// Content
		assertEquals("Hello Test", support.findStr(doc,
				"/html/body/div[@class='testcontent']/text()"));

		// Footer
		assertEquals("CustomFooter",
				support.findStr(doc, "/html/body/div[@class='footer']/text()"));
	}

}
