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
package org.jacoco.report.html.table;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.jacoco.core.analysis.CounterComparator;
import org.jacoco.core.analysis.CounterImpl;
import org.jacoco.core.analysis.CoverageNodeImpl;
import org.jacoco.core.analysis.ICoverageNode;
import org.jacoco.core.analysis.ICoverageNode.CounterEntity;
import org.jacoco.core.analysis.ICoverageNode.ElementType;
import org.jacoco.report.MemoryMultiReportOutput;
import org.jacoco.report.ReportOutputFolder;
import org.jacoco.report.html.HTMLDocument;
import org.jacoco.report.html.HTMLElement;
import org.jacoco.report.html.HTMLSupport;
import org.jacoco.report.html.resources.Resources;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

/**
 * Unit tests for {@link Table}.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public class TableTest {

	private MemoryMultiReportOutput output;

	private ReportOutputFolder root;

	private Resources resources;

	private HTMLDocument doc;

	private HTMLElement body;

	private Table table;

	@Before
	public void setup() throws IOException {
		output = new MemoryMultiReportOutput();
		root = new ReportOutputFolder(output);
		resources = new Resources(root);
		doc = new HTMLDocument(root.createFile("Test.html"), "UTF-8");
		doc.head().title();
		body = doc.body();
		table = new Table();
	}

	@After
	public void teardown() {
		output.assertAllClosed();
	}

	@Test
	public void testCallbackSequence() throws IOException {
		final IColumnRenderer recorder = new IColumnRenderer() {

			private final StringBuilder store = new StringBuilder();

			public boolean init(List<? extends ITableItem> items,
					ICoverageNode total) {
				store.append("init-");
				return true;
			}

			public void footer(HTMLElement td, ICoverageNode total,
					Resources resources, ReportOutputFolder base) {
				store.append("footer-");
			}

			public void item(HTMLElement td, ITableItem item,
					Resources resources, ReportOutputFolder base) {
				store.append("item").append(item.getLinkLabel()).append("-");
			}

			@Override
			public String toString() {
				return store.toString();
			}
		};
		final List<ITableItem> items = Arrays.asList(createItem("A", 1),
				createItem("B", 2), createItem("C", 3));
		table.add("Header", null, recorder, null, false);
		table.render(body, items, createTotal("Sum", 6), resources, root);
		doc.close();
		assertEquals("init-footer-itemA-itemB-itemC-", recorder.toString());
	}

	@Test
	public void testInvisible() throws IOException {
		final IColumnRenderer column = new IColumnRenderer() {

			public boolean init(List<? extends ITableItem> items,
					ICoverageNode total) {
				return false;
			}

			public void footer(HTMLElement td, ICoverageNode total,
					Resources resources, ReportOutputFolder base) {
				fail();
			}

			public void item(HTMLElement td, ITableItem item,
					Resources resources, ReportOutputFolder base) {
				fail();
			}
		};
		final List<ITableItem> items = Arrays.asList(createItem("A", 1));
		table.add("Header", null, column, null, false);
		table.render(body, items, createTotal("Sum", 1), resources, root);
		doc.close();
	}

	@Test(expected = IllegalStateException.class)
	public void testTwoDefaultSorts() throws IOException {
		doc.close();
		table.add("Header1", null, null,
				CounterComparator.TOTALITEMS.on(CounterEntity.CLASS), true);
		table.add("Header2", null, null,
				CounterComparator.TOTALITEMS.on(CounterEntity.CLASS), true);
	}

	@Test
	public void testSorting() throws IOException {
		final IColumnRenderer column = new IColumnRenderer() {

			private final StringBuilder store = new StringBuilder();

			public boolean init(List<? extends ITableItem> items,
					ICoverageNode total) {
				return true;
			}

			public void footer(HTMLElement td, ICoverageNode total,
					Resources resources, ReportOutputFolder base) {
			}

			public void item(HTMLElement td, ITableItem item,
					Resources resources, ReportOutputFolder base) {
				store.append(item.getLinkLabel());
			}

			@Override
			public String toString() {
				return store.toString();
			}
		};
		final List<ITableItem> items = Arrays.asList(createItem("C", 3),
				createItem("E", 5), createItem("A", 1), createItem("D", 4),
				createItem("B", 2));
		table.add("Header", null, column,
				CounterComparator.TOTALITEMS.on(CounterEntity.CLASS), true);
		table.render(body, items, createTotal("Sum", 6), resources, root);
		doc.close();
		assertEquals("ABCDE", column.toString());
	}

	@Test
	public void testSortIds() throws Exception {
		final IColumnRenderer column = new IColumnRenderer() {

			public boolean init(List<? extends ITableItem> items,
					ICoverageNode total) {
				return true;
			}

			public void footer(HTMLElement td, ICoverageNode total,
					Resources resources, ReportOutputFolder base) {
			}

			public void item(HTMLElement td, ITableItem item,
					Resources resources, ReportOutputFolder base) {
			}
		};
		final List<ITableItem> items = Arrays.asList(createItem("C", 3),
				createItem("E", 4), createItem("A", 1), createItem("D", 2));
		table.add("Forward", null, column,
				CounterComparator.TOTALITEMS.on(CounterEntity.CLASS), false);
		table.add("Reverse", null, column, CounterComparator.TOTALITEMS
				.reverse().on(CounterEntity.CLASS), false);
		table.render(body, items, createTotal("Sum", 6), resources, root);
		doc.close();

		final HTMLSupport support = new HTMLSupport();
		final Document doc = support.parse(output.getFile("Test.html"));

		// The elements in Column 1 are sorted in forward order:
		assertEquals("sortable",
				support.findStr(doc, "/html/body/table/thead/tr/td[1]/@class"));
		assertEquals("a2",
				support.findStr(doc, "/html/body/table/tbody/tr[1]/td[1]/@id"));
		assertEquals("a3",
				support.findStr(doc, "/html/body/table/tbody/tr[2]/td[1]/@id"));
		assertEquals("a0",
				support.findStr(doc, "/html/body/table/tbody/tr[3]/td[1]/@id"));
		assertEquals("a1",
				support.findStr(doc, "/html/body/table/tbody/tr[4]/td[1]/@id"));

		// The elements in Column 2 are sorted in reverse order:
		assertEquals("sortable",
				support.findStr(doc, "/html/body/table/thead/tr/td[2]/@class"));
		assertEquals("b1",
				support.findStr(doc, "/html/body/table/tbody/tr[1]/td[2]/@id"));
		assertEquals("b0",
				support.findStr(doc, "/html/body/table/tbody/tr[2]/td[2]/@id"));
		assertEquals("b3",
				support.findStr(doc, "/html/body/table/tbody/tr[3]/td[2]/@id"));
		assertEquals("b2",
				support.findStr(doc, "/html/body/table/tbody/tr[4]/td[2]/@id"));
	}

	@Test
	public void testDefaultSorting() throws Exception {
		final IColumnRenderer column = new IColumnRenderer() {

			public boolean init(List<? extends ITableItem> items,
					ICoverageNode total) {
				return true;
			}

			public void footer(HTMLElement td, ICoverageNode total,
					Resources resources, ReportOutputFolder base) {
			}

			public void item(HTMLElement td, ITableItem item,
					Resources resources, ReportOutputFolder base) {
			}
		};
		final List<ITableItem> items = Arrays.asList(createItem("A", 1));
		table.add("Forward", null, column,
				CounterComparator.TOTALITEMS.on(CounterEntity.CLASS), true);
		table.render(body, items, createTotal("Sum", 1), resources, root);
		doc.close();

		final HTMLSupport support = new HTMLSupport();
		final Document doc = support.parse(output.getFile("Test.html"));

		assertEquals("down sortable",
				support.findStr(doc, "/html/body/table/thead/tr/td[1]/@class"));
	}

	private ITableItem createItem(final String name, final int count) {
		final ICoverageNode node = new CoverageNodeImpl(ElementType.GROUP,
				name, false) {
			{
				this.classCounter = CounterImpl.getInstance(count, false);
			}
		};
		return new ITableItem() {
			public String getLinkLabel() {
				return name;
			}

			public String getLink(ReportOutputFolder base) {
				return name + ".html";
			}

			public String getLinkStyle() {
				return Resources.getElementStyle(node.getElementType());
			}

			public ICoverageNode getNode() {
				return node;
			}
		};
	}

	private ICoverageNode createTotal(final String name, final int count) {
		return new CoverageNodeImpl(ElementType.GROUP, name, false) {
			{
				this.classCounter = CounterImpl.getInstance(count, false);
			}
		};
	}

}
