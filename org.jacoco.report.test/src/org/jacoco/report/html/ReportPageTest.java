/*******************************************************************************
 * Copyright (c) 2009, 2010 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.report.html;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Locale;

import org.jacoco.report.ILanguageNames;
import org.jacoco.report.MemoryMultiReportOutput;
import org.jacoco.report.ReportOutputFolder;
import org.jacoco.report.internal.html.index.IIndexUpdate;
import org.jacoco.report.internal.html.resources.Resources;
import org.jacoco.report.internal.html.resources.Styles;
import org.jacoco.report.internal.html.table.Table;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

/**
 * Unit tests for {@link ReportPage}.
 * 
 * @author Marc R. Hoffmann
 * @version $qualified.bundle.version$
 */
public class ReportPageTest {

	private MemoryMultiReportOutput output;

	private ReportOutputFolder root;

	private IHTMLReportContext context;

	private ReportPage page;

	private class TestReportPage extends ReportPage {

		private final String label;
		private final String style;

		protected TestReportPage(String label, String style, ReportPage parent) {
			super(parent, root, ReportPageTest.this.context);
			this.label = label;
			this.style = style;
		}

		@Override
		protected void headExtra(HTMLElement head) throws IOException {
			super.headExtra(head);
			head.script("text/javascript", "test.js");
		}

		@Override
		protected String getOnload() {
			return "init()";
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
	public void setup() {
		output = new MemoryMultiReportOutput();
		root = new ReportOutputFolder(output);
		final Resources resources = new Resources(root);
		context = new IHTMLReportContext() {

			public ILanguageNames getLanguageNames() {
				throw new AssertionError("Unexpected method call.");
			}

			public Resources getResources() {
				return resources;
			}

			public Table getTable() {
				throw new AssertionError("Unexpected method call.");
			}

			public String getFooterText() {
				return "CustomFooter";
			}

			public ILinkable getSessionsPage() {
				return new LinkableStub("sessions.html", "Sessions",
						Styles.EL_SESSION);
			}

			public String getOutputEncoding() {
				return "UTF-8";
			}

			public IIndexUpdate getIndexUpdate() {
				throw new AssertionError("Unexpected method call.");
			}

			public Locale getLocale() {
				return Locale.ENGLISH;
			}

		};
		ReportPage parent = new TestReportPage("Report", "el_report", null);
		page = new TestReportPage("Test", "el_group", parent);
	}

	@After
	public void teardown() {
		output.assertAllClosed();
	}

	@Test
	public void testGetLink() throws IOException {
		ReportOutputFolder base = root.subFolder("here");
		assertEquals("../Test.html", page.getLink(base));
	}

	@Test
	public void testPageContent() throws Exception {
		page.renderDocument();
		final HTMLSupport support = new HTMLSupport();
		final Document doc = support.parse(output.getFile("Test.html"));

		// style sheet
		assertEquals(".resources/report.css", support.findStr(doc,
				"/html/head/link[@rel='stylesheet']/@href"));

		// extra head
		assertEquals("test.js", support.findStr(doc, "/html/head/script/@src"));

		// onload handler
		assertEquals("init()", support.findStr(doc, "/html/body/@onload"));

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
