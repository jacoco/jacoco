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
package org.jacoco.report.internal.html.page;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.io.IOException;
import java.util.Locale;

import org.jacoco.core.analysis.CoverageNodeImpl;
import org.jacoco.core.analysis.ICoverageNode;
import org.jacoco.core.analysis.ICoverageNode.ElementType;
import org.jacoco.report.ILanguageNames;
import org.jacoco.report.MemoryMultiReportOutput;
import org.jacoco.report.internal.ReportOutputFolder;
import org.jacoco.report.internal.html.HTMLElement;
import org.jacoco.report.internal.html.IHTMLReportContext;
import org.jacoco.report.internal.html.ILinkable;
import org.jacoco.report.internal.html.LinkableStub;
import org.jacoco.report.internal.html.index.IIndexUpdate;
import org.jacoco.report.internal.html.resources.Resources;
import org.jacoco.report.internal.html.resources.Styles;
import org.jacoco.report.internal.html.table.Table;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link ReportPage}.
 */
public class NodePageTest {

	private MemoryMultiReportOutput output;

	private ReportOutputFolder root;

	private IHTMLReportContext context;

	private CoverageNodeImpl node;

	private NodePage<ICoverageNode> page;

	private class TestNodePage extends NodePage<ICoverageNode> {

		protected TestNodePage(ICoverageNode node) {
			super(node, null, root, NodePageTest.this.context);
		}

		@Override
		protected void content(HTMLElement body) throws IOException {
		}

		@Override
		protected String getFileName() {
			return "index.html";
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
		node = new CoverageNodeImpl(ElementType.GROUP, "Test");
		page = new TestNodePage(node);
	}

	@After
	public void teardown() throws IOException {
		output.close();
		output.assertAllClosed();
	}

	@Test
	public void testGetNode() throws IOException {
		assertSame(node, page.getNode());
	}

	@Test
	public void testGetLinkLabel() throws IOException {
		assertEquals("Test", page.getLinkLabel());
	}

	@Test
	public void testGetLinkStyle() throws IOException {
		assertEquals("el_group", page.getLinkStyle());
	}

}
