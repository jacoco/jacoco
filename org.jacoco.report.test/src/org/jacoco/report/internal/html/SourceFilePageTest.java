/*******************************************************************************
 * Copyright (c) 2009, 2011 Mountainminds GmbH & Co. KG and Contributors
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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

import org.jacoco.core.analysis.CoverageNodeImpl;
import org.jacoco.core.analysis.ICoverageNode.ElementType;
import org.jacoco.core.internal.analysis.SourceFileCoverageImpl;
import org.jacoco.report.DirectorySourceFileLocator;
import org.jacoco.report.ILanguageNames;
import org.jacoco.report.ISourceFileLocator;
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
 * Unit tests for {@link SourceFilePage}.
 * 
 * @author Marc R. Hoffmann
 * @version $qualified.bundle.version$
 */
public class SourceFilePageTest {

	private MemoryMultiReportOutput output;

	private ReportOutputFolder root;

	private IHTMLReportContext context;

	private ISourceFileLocator locator;

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
		locator = new DirectorySourceFileLocator(new File("./src"), "UTF-8");
	}

	@After
	public void teardown() {
		output.assertAllClosed();
	}

	@Test(expected = IllegalStateException.class)
	public void testVisitChild() {
		final SourceFileCoverageImpl node = new SourceFileCoverageImpl(
				"SourceFilePageTest.java", "org/jacoco/report/html");
		final SourceFilePage page = new SourceFilePage(node, null, root,
				context);
		page.visitChild(new CoverageNodeImpl(ElementType.CLASS, "Foo"));
	}

	@Test
	public void testContents() throws Exception {
		final SourceFileCoverageImpl node = new SourceFileCoverageImpl(
				"SourceFilePageTest.java", "org/jacoco/report/internal/html");
		final SourceFilePage page = new SourceFilePage(node, null, root,
				context);
		page.visitEnd(locator);

		assertTrue(page.exists());

		final HTMLSupport support = new HTMLSupport();
		final Document result = support.parse(output
				.getFile("SourceFilePageTest.java.html"));

		// additional style sheet
		assertEquals(".resources/report.css", support.findStr(result,
				"/html/head/link[@rel='stylesheet'][1]/@href"));
		assertEquals(".resources/prettify.css", support.findStr(result,
				"/html/head/link[@rel='stylesheet'][2]/@href"));

		// highlighting script
		assertEquals("text/javascript",
				support.findStr(result, "/html/head/script/@type"));
		assertEquals(".resources/prettify.js",
				support.findStr(result, "/html/head/script/@src"));
		assertEquals("prettyPrint()",
				support.findStr(result, "/html/body/@onload"));

		// source code
		assertEquals("L1",
				support.findStr(result, "/html/body/pre/span[1]/@id"));
	}

	@Test
	public void testNoSource() throws IOException {
		final SourceFileCoverageImpl node = new SourceFileCoverageImpl(
				"DoesNotExist.java", "org/jacoco/report/html");
		final SourceFilePage page = new SourceFilePage(node, null, root,
				context);
		page.visitEnd(locator);

		assertFalse(page.exists());
		output.assertEmpty();
	}

}
