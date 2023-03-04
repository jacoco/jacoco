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
import static org.junit.Assert.assertSame;

import java.io.IOException;

import org.jacoco.core.analysis.CoverageNodeImpl;
import org.jacoco.core.analysis.ICoverageNode;
import org.jacoco.core.analysis.ICoverageNode.ElementType;
import org.jacoco.report.internal.html.HTMLElement;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link NodePage}.
 */
public class NodePageTest extends PageTestBase {

	private CoverageNodeImpl node;

	private NodePage<ICoverageNode> page;

	private class TestNodePage extends NodePage<ICoverageNode> {

		protected TestNodePage(ICoverageNode node, ReportPage parent) {
			super(node, parent, rootFolder, NodePageTest.this.context);
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
	@Override
	public void setup() throws Exception {
		super.setup();
		node = new CoverageNodeImpl(ElementType.GROUP, "Test");
		page = new TestNodePage(node, null);
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
	public void testGetLinkStyle1() throws IOException {
		assertEquals("el_report", page.getLinkStyle());
	}

	@Test
	public void testGetLinkStyle2() throws IOException {
		final TestNodePage group = new TestNodePage(node, page);
		assertEquals("el_group", group.getLinkStyle());
	}

}
