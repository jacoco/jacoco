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
 * Unit tests for {@link PercentageColumn}.
 */
public class PercentageColumnTest {

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
		column = new PercentageColumn(CounterEntity.LINE, Locale.ENGLISH);
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
	public void testItem1() throws Exception {
		final ITableItem item = createItem(100, 50);
		column.item(td, item, resources, root);
		final Document doc = parseDoc();
		assertEquals("33%",
				support.findStr(doc, "/html/body/table/tr/td[1]/text()"));
	}

	@Test
	public void testItem2() throws Exception {
		final ITableItem item = createItem(0, 0);
		column.item(td, item, resources, root);
		final Document doc = parseDoc();
		assertEquals("n/a",
				support.findStr(doc, "/html/body/table/tr/td[1]/text()"));
	}

	@Test
	public void testRounding() throws Exception {
		final ITableItem item = createItem(1, 199);
		column.item(td, item, resources, root);
		final Document doc = parseDoc();
		assertEquals("99%",
				support.findStr(doc, "/html/body/table/tr/td[1]/text()"));
	}

	@Test
	public void testLocale() throws Exception {
		IColumnRenderer column = new PercentageColumn(CounterEntity.LINE,
				Locale.FRENCH);
		final ITableItem item = createItem(0, 1000);
		column.item(td, item, resources, root);
		final Document doc = parseDoc();
		// After integration of JEP 252 into JDK9, CLDR locale data is used by
		// default, which results in usage of non-breaking space below, while
		// the legacy locale data uses regular space:
		assertTrue(support.findStr(doc, "/html/body/table/tr/td[1]/text()")
				.matches("100[ \u00A0]%"));
	}

	@Test
	public void testFooter1() throws Exception {
		final ITableItem item = createItem(20, 60);
		column.footer(td, item.getNode(), resources, root);
		final Document doc = parseDoc();
		assertEquals("75%", support.findStr(doc, "/html/body/table/tr"));
	}

	@Test
	public void testFooter2() throws Exception {
		final ITableItem item = createItem(0, 0);
		column.footer(td, item.getNode(), resources, root);
		final Document doc = parseDoc();
		assertEquals("n/a", support.findStr(doc, "/html/body/table/tr"));
	}

	@Test
	public void testComparator() throws Exception {
		final Comparator<ITableItem> c = column.getComparator();
		final ITableItem i1 = createItem(50, 50);
		final ITableItem i2 = createItem(800, 200);
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
