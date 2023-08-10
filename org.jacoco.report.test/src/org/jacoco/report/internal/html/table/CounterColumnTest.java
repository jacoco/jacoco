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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;

import org.jacoco.core.analysis.CoverageNodeImpl;
import org.jacoco.core.analysis.ICoverageNode;
import org.jacoco.core.analysis.ICoverageNode.CounterEntity;
import org.jacoco.core.analysis.ICoverageNode.ElementType;
import org.jacoco.core.internal.analysis.CounterImpl;
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
 * Unit tests for {@link CounterColumn}.
 */
public class CounterColumnTest {

	private MemoryMultiReportOutput output;

	private ReportOutputFolder root;

	private Resources resources;

	private HTMLElement html;

	private HTMLElement td;

	private HTMLSupport support;

	private Locale locale;

	@Before
	public void setup() throws Exception {
		output = new MemoryMultiReportOutput();
		root = new ReportOutputFolder(output);
		resources = new Resources(root);
		html = new HTMLElement(root.createFile("Test.html"), "UTF-8");
		html.head().title();
		td = html.body().table("somestyle").tr().td();
		support = new HTMLSupport();
		locale = Locale.ENGLISH;
	}

	@After
	public void teardown() throws IOException {
		output.close();
		output.assertAllClosed();
	}

	@Test
	public void testInitVisible() throws Exception {
		IColumnRenderer column = CounterColumn.newTotal(CounterEntity.LINE,
				locale);
		final ITableItem item = createItem(0, 3);
		assertTrue(column.init(Arrays.asList(item), item.getNode()));
		html.close();
	}

	@Test
	public void testInitInvisible() throws Exception {
		IColumnRenderer column = CounterColumn.newTotal(CounterEntity.LINE,
				locale);
		final ITableItem item = createItem(0, 0);
		assertFalse(column.init(Arrays.asList(item), createNode(1, 0)));
		html.close();
	}

	@Test
	public void testItemTotal() throws Exception {
		IColumnRenderer column = CounterColumn.newTotal(CounterEntity.LINE,
				locale);
		final ITableItem item = createItem(100, 50);
		column.init(Collections.singletonList(item), item.getNode());
		column.item(td, item, resources, root);
		final Document doc = parseDoc();
		assertEquals("150",
				support.findStr(doc, "/html/body/table/tr/td[1]/text()"));
	}

	@Test
	public void testItemMissed() throws Exception {
		IColumnRenderer column = CounterColumn.newMissed(CounterEntity.LINE,
				locale);
		final ITableItem item = createItem(100, 50);
		column.init(Collections.singletonList(item), item.getNode());
		column.item(td, item, resources, root);
		final Document doc = parseDoc();
		assertEquals("100",
				support.findStr(doc, "/html/body/table/tr/td[1]/text()"));
	}

	@Test
	public void testItemCovered() throws Exception {
		IColumnRenderer column = CounterColumn.newCovered(CounterEntity.LINE,
				locale);
		final ITableItem item = createItem(100, 50);
		column.init(Collections.singletonList(item), item.getNode());
		column.item(td, item, resources, root);
		final Document doc = parseDoc();
		assertEquals("50",
				support.findStr(doc, "/html/body/table/tr/td[1]/text()"));
	}

	@Test
	public void testLocale() throws Exception {
		IColumnRenderer column = CounterColumn.newTotal(CounterEntity.LINE,
				Locale.ITALIAN);
		final ITableItem item = createItem(1000, 0);
		column.init(Collections.singletonList(item), item.getNode());
		column.item(td, item, resources, root);
		final Document doc = parseDoc();
		assertEquals("1.000",
				support.findStr(doc, "/html/body/table/tr/td[1]/text()"));
	}

	@Test
	public void testFooter() throws Exception {
		IColumnRenderer column = CounterColumn.newTotal(CounterEntity.LINE,
				locale);
		final ITableItem item = createItem(20, 60);
		column.init(Collections.singletonList(item), item.getNode());
		column.footer(td, item.getNode(), resources, root);
		final Document doc = parseDoc();
		assertEquals("80",
				support.findStr(doc, "/html/body/table/tr/td[1]/text()"));
	}

	@Test
	public void testComparatorTotal() throws Exception {
		IColumnRenderer column = CounterColumn.newTotal(CounterEntity.LINE,
				locale);
		final Comparator<ITableItem> c = column.getComparator();
		final ITableItem i1 = createItem(30, 0);
		final ITableItem i2 = createItem(40, 0);
		assertEquals(0, c.compare(i1, i1));
		assertTrue(c.compare(i1, i2) > 0);
		assertTrue(c.compare(i2, i1) < 0);
		html.close();
	}

	@Test
	public void testComparatorCovered() throws Exception {
		IColumnRenderer column = CounterColumn.newCovered(CounterEntity.LINE,
				locale);
		final Comparator<ITableItem> c = column.getComparator();
		final ITableItem i1 = createItem(70, 30);
		final ITableItem i2 = createItem(50, 50);
		assertEquals(0, c.compare(i1, i1));
		assertTrue(c.compare(i1, i2) > 0);
		assertTrue(c.compare(i2, i1) < 0);
		html.close();
	}

	@Test
	public void testComparatorMissed() throws Exception {
		IColumnRenderer column = CounterColumn.newMissed(CounterEntity.LINE,
				locale);
		final Comparator<ITableItem> c = column.getComparator();
		final ITableItem i1 = createItem(20, 80);
		final ITableItem i2 = createItem(50, 50);
		assertEquals(0, c.compare(i1, i1));
		assertTrue(c.compare(i1, i2) > 0);
		assertTrue(c.compare(i2, i1) < 0);
		html.close();
	}

	private ITableItem createItem(final int missed, final int covered) {
		final ICoverageNode node = createNode(missed, covered);
		return new ITableItem() {
			public String getLinkLabel() {
				return "Foo";
			}

			public String getLink(ReportOutputFolder base) {
				return null;
			}

			public String getLinkStyle() {
				return Resources.getElementStyle(node.getElementType());
			}

			public ICoverageNode getNode() {
				return node;
			}
		};
	}

	private CoverageNodeImpl createNode(final int missed, final int covered) {
		return new CoverageNodeImpl(ElementType.GROUP, "Foo") {
			{
				this.lineCounter = CounterImpl.getInstance(missed, covered);
			}
		};
	}

	private Document parseDoc() throws Exception {
		html.close();
		return support.parse(output.getFile("Test.html"));
	}

}
