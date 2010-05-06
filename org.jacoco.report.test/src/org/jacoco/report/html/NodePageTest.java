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
 * $Id: $
 *******************************************************************************/
package org.jacoco.report.html;

import static org.junit.Assert.assertSame;

import java.io.IOException;

import org.jacoco.core.analysis.CoverageNodeImpl;
import org.jacoco.core.analysis.ICoverageNode;
import org.jacoco.core.analysis.ICoverageNode.ElementType;
import org.jacoco.report.ILanguageNames;
import org.jacoco.report.IReportVisitor;
import org.jacoco.report.MemoryMultiReportOutput;
import org.jacoco.report.ReportOutputFolder;
import org.jacoco.report.html.resources.Resources;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link ReportPage}.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public class NodePageTest {

	private MemoryMultiReportOutput output;

	private ReportOutputFolder root;

	private IHTMLReportContext context;

	private CoverageNodeImpl node;

	private NodePage page;

	private class TestNodePage extends NodePage {

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

		public IReportVisitor visitChild(ICoverageNode node) {
			throw new UnsupportedOperationException();
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

			public CoverageTable getTable(ElementType type) {
				throw new AssertionError("Unexpected method call.");
			}

			public String getFooterText() {
				return "CustomFooter";
			}

			public String getInfoPageLink(ReportOutputFolder base) {
				return "info.html";
			}

			public String getOutputEncoding() {
				return "UTF-8";
			}
		};
		node = new CoverageNodeImpl(ElementType.GROUP, "Test", false);
		page = new TestNodePage(node);
	}

	@After
	public void teardown() {
		output.assertAllClosed();
	}

	@Test
	public void testGetNode() throws IOException {
		assertSame(node, page.getNode());
	}

	@Test
	public void testGetLabel() throws IOException {
		assertSame("Test", page.getLabel());
	}

	@Test
	public void testGetElementStyle() throws IOException {
		assertSame("el_group", page.getElementStyle());
	}

	@Test
	public void testVisitEnd() throws IOException {
		page.visitEnd(null);
		output.assertSingleFile("index.html");
	}

}
