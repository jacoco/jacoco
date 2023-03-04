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
import java.util.Arrays;
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
 * Unit tests for {@link BarColumn}.
 */
public class BarColumnTest {

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
		column = new BarColumn(CounterEntity.LINE, Locale.ENGLISH);
	}

	@After
	public void teardown() throws IOException {
		output.close();
		output.assertAllClosed();
	}

	@Test
	public void testInit() throws Exception {
		final ITableItem i = createItem(6, 24);
		assertTrue(column.init(Arrays.asList(i), i.getNode()));
		html.close();
	}

	@Test
	public void testFooter() throws Exception {
		column.footer(td, createNode(15, 5), resources, root);
		final Document doc = parseDoc();
		assertEquals("15 of 20",
				support.findStr(doc, "/html/body/table/tr/td/text()"));
	}

	@Test
	public void testBarWidths() throws Exception {
		final ITableItem i1 = createItem(15, 5);
		final ITableItem i2 = createItem(6, 24);
		column.init(Arrays.asList(i1, i2), createNode(21, 29));
		column.item(td, i1, resources, root);
		final Document doc = parseDoc();

		assertEquals("2",
				support.findStr(doc, "count(/html/body/table/tr[1]/td/img)"));

		// red bar
		assertEquals("jacoco-resources/redbar.gif",
				support.findStr(doc, "/html/body/table/tr[1]/td/img[1]/@src"));
		assertEquals("15",
				support.findStr(doc, "/html/body/table/tr[1]/td/img[1]/@alt"));
		assertEquals("60", support.findStr(doc,
				"/html/body/table/tr[1]/td/img[1]/@width"));

		// green bar
		assertEquals("jacoco-resources/greenbar.gif",
				support.findStr(doc, "/html/body/table/tr[1]/td/img[2]/@src"));
		assertEquals("5",
				support.findStr(doc, "/html/body/table/tr[1]/td/img[2]/@alt"));
		assertEquals("20", support.findStr(doc,
				"/html/body/table/tr[1]/td/img[2]/@width"));
	}

	@Test
	public void testRedBarOnly() throws Exception {
		final ITableItem i1 = createItem(20, 0);
		column.init(Arrays.asList(i1), createNode(20, 0));
		column.item(td, i1, resources, root);
		final Document doc = parseDoc();

		assertEquals("1",
				support.findStr(doc, "count(/html/body/table/tr[1]/td/img)"));

		// red bar
		assertEquals("jacoco-resources/redbar.gif",
				support.findStr(doc, "/html/body/table/tr[1]/td/img[1]/@src"));
		assertEquals("20",
				support.findStr(doc, "/html/body/table/tr[1]/td/img[1]/@alt"));
		assertEquals("120", support.findStr(doc,
				"/html/body/table/tr[1]/td/img[1]/@width"));
	}

	@Test
	public void testGreenBarOnly() throws Exception {
		final ITableItem i1 = createItem(00, 20);
		column.init(Arrays.asList(i1), createNode(00, 20));
		column.item(td, i1, resources, root);
		final Document doc = parseDoc();

		assertEquals("1",
				support.findStr(doc, "count(/html/body/table/tr[1]/td/img)"));

		// red bar
		assertEquals("jacoco-resources/greenbar.gif",
				support.findStr(doc, "/html/body/table/tr[1]/td/img[1]/@src"));
		assertEquals("20",
				support.findStr(doc, "/html/body/table/tr[1]/td/img[1]/@alt"));
		assertEquals("120", support.findStr(doc,
				"/html/body/table/tr[1]/td/img[1]/@width"));
	}

	@Test
	public void testNoBars() throws Exception {
		final ITableItem i1 = createItem(00, 00);
		column.init(Arrays.asList(i1), createNode(00, 00));
		column.item(td, i1, resources, root);
		final Document doc = parseDoc();

		assertEquals("0",
				support.findStr(doc, "count(/html/body/table/tr[1]/td/img)"));
	}

	@Test
	public void testLocale() throws Exception {
		final BarColumn col = new BarColumn(CounterEntity.LINE, Locale.FRENCH);
		final ITableItem i1 = createItem(0, 123456);
		col.init(Arrays.asList(i1), createNode(00, 20));
		col.item(td, i1, resources, root);
		final Document doc = parseDoc();

		// Prior to integration of JDK-8221432 in JDK 13
		// formatting of a below number was using non-breaking space,
		// after - narrow non-breaking space:
		assertTrue(support.findStr(doc, "/html/body/table/tr[1]/td/img[1]/@alt")
				.matches("123[\u00A0\u202F]456"));
	}

	@Test
	public void testComparator1() throws Exception {
		final Comparator<ITableItem> c = column.getComparator();
		final ITableItem i1 = createItem(50, 50);
		final ITableItem i2 = createItem(20, 80);
		assertTrue(c.compare(i1, i2) < 0);
		assertTrue(c.compare(i2, i1) > 0);
		assertEquals(0, c.compare(i1, i1));
		html.close();
	}

	@Test
	public void testComparator2() throws Exception {
		final Comparator<ITableItem> c = column.getComparator();
		final ITableItem i1 = createItem(50, 60);
		final ITableItem i2 = createItem(50, 50);
		assertTrue(c.compare(i1, i2) < 0);
		assertTrue(c.compare(i2, i1) > 0);
		assertEquals(0, c.compare(i1, i1));
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
