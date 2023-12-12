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
package org.jacoco.report.internal.html.table;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.jacoco.core.analysis.CoverageNodeImpl;
import org.jacoco.core.analysis.ICoverageNode;
import org.jacoco.core.analysis.ICoverageNode.ElementType;
import org.jacoco.report.MemoryMultiReportOutput;
import org.jacoco.report.internal.ReportOutputFolder;
import org.jacoco.report.internal.html.HTMLElement;
import org.jacoco.report.internal.html.HTMLSupport;
import org.jacoco.report.internal.html.resources.Resources;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

/**
 * Unit tests for {@link LabelColumn}.
 */
public class LabelColumnTest {

	private MemoryMultiReportOutput output;

	private ReportOutputFolder root;

	private Resources resources;

	private HTMLElement html;

	private HTMLElement td;

	private HTMLSupport support;

	private IColumnRenderer column;

	@Before
	public void setup() throws Exception {
		output = new MemoryMultiReportOutput();
		root = new ReportOutputFolder(output);
		resources = new Resources(root);
		html = new HTMLElement(root.createFile("Test.html"), "UTF-8");
		html.head().title();
		td = html.body().table("somestyle").tr().td();
		support = new HTMLSupport();
		column = new LabelColumn();
	}

	@After
	public void teardown() throws IOException {
		output.close();
		output.assertAllClosed();
	}

	@Test
	public void testInit() throws Exception {
		assertTrue(column.init(null, null));
		html.close();
	}

	@Test
	public void testFooter() throws Exception {
		column.footer(td, new CoverageNodeImpl(ElementType.GROUP, "Foo"),
				resources, root);
		final Document doc = parseDoc();
		assertEquals("Total",
				support.findStr(doc, "/html/body/table/tr/td/text()"));
	}

	@Test
	public void testItemWithoutLink() throws Exception {
		column.item(td, createItem("Abc", null), resources, root);
		final Document doc = parseDoc();
		assertEquals("Abc",
				support.findStr(doc, "/html/body/table/tr/td/span/text()"));
		assertEquals("el_group",
				support.findStr(doc, "/html/body/table/tr/td/span/@class"));
	}

	@Test
	public void testItemWithLink() throws Exception {
		column.item(td, createItem("Def", "def.html"), resources, root);
		final Document doc = parseDoc();
		assertEquals("Def",
				support.findStr(doc, "/html/body/table/tr/td/a/text()"));
		assertEquals("def.html",
				support.findStr(doc, "/html/body/table/tr/td/a/@href"));
		assertEquals("el_group",
				support.findStr(doc, "/html/body/table/tr/td/a/@class"));
	}

	@Test
	public void testComparator1() throws Exception {
		final ITableItem i1 = createItem("abcdef", null);
		final ITableItem i2 = createItem("aBcDeF", null);
		assertEquals(0, column.getComparator().compare(i1, i2));
		html.close();
	}

	@Test
	public void testComparator2() throws Exception {
		final ITableItem i1 = createItem("hello", null);
		final ITableItem i2 = createItem("world", null);
		assertTrue(column.getComparator().compare(i1, i2) < 0);
		assertTrue(column.getComparator().compare(i2, i1) > 0);
		html.close();
	}

	private ITableItem createItem(final String name, final String link) {
		final ICoverageNode node = new CoverageNodeImpl(ElementType.GROUP,
				name);
		return new ITableItem() {
			public String getLinkLabel() {
				return name;
			}

			public String getLink(ReportOutputFolder base) {
				return link;
			}

			public String getLinkStyle() {
				return Resources.getElementStyle(node.getElementType());
			}

			public ICoverageNode getNode() {
				return node;
			}
		};
	}

	private Document parseDoc() throws Exception {
		html.close();
		return support.parse(output.getFile("Test.html"));
	}
}
