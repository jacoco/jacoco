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

import static org.junit.Assert.assertEquals;

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
import org.jacoco.report.html.resources.Resources;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

/**
 * Unit tests for {@link CoverageTable}.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public class CoverageTableTest {

	private MemoryMultiReportOutput output;

	private ReportOutputFolder root;

	private Resources resources;

	private HTMLDocument doc;

	private HTMLElement body;

	@Before
	public void setup() throws IOException {
		output = new MemoryMultiReportOutput();
		root = new ReportOutputFolder(output);
		resources = new Resources(root);
		doc = new HTMLDocument(root.createFile("Test.html"), "UTF-8");
		doc.head().title();
		body = doc.body();
	}

	@After
	public void teardown() {
		output.assertAllClosed();
	}

	@Test
	public void testCallbackSequence() throws IOException {
		final ICoverageTableColumn recorder = new ICoverageTableColumn() {

			private final StringBuilder store = new StringBuilder();

			public void init(List<ICoverageTableItem> items, ICoverageNode total) {
				store.append("init-");
			}

			public void header(HTMLElement tr, Resources resources,
					ReportOutputFolder base) {
				store.append("header-");
			}

			public void footer(HTMLElement tr, ICoverageNode total,
					Resources resources, ReportOutputFolder base) {
				store.append("footer-");
			}

			public void item(HTMLElement tr, ICoverageTableItem item,
					Resources resources, ReportOutputFolder base) {
				store.append("item").append(item.getLinkLabel()).append("-");
			}

			@Override
			public String toString() {
				return store.toString();
			}
		};
		final List<ICoverageTableItem> items = Arrays.asList(
				createItem("A", 1), createItem("B", 2), createItem("C", 3));
		new CoverageTable(Arrays.asList(recorder),
				CounterComparator.TOTALITEMS.on(CounterEntity.CLASS)).render(
				body, items, createTotal("Sum", 6), resources, root);
		doc.close();
		assertEquals("init-header-footer-itemA-itemB-itemC-",
				recorder.toString());
	}

	@Test
	public void testSorting() throws IOException {
		final ICoverageTableColumn recorder = new ICoverageTableColumn() {

			private final StringBuilder store = new StringBuilder();

			public void init(List<ICoverageTableItem> items, ICoverageNode total) {
			}

			public void header(HTMLElement tr, Resources resources,
					ReportOutputFolder base) {
			}

			public void footer(HTMLElement tr, ICoverageNode total,
					Resources resources, ReportOutputFolder base) {
			}

			public void item(HTMLElement tr, ICoverageTableItem item,
					Resources resources, ReportOutputFolder base) {
				store.append(item.getLinkLabel());
			}

			@Override
			public String toString() {
				return store.toString();
			}
		};
		final List<ICoverageTableItem> items = Arrays.asList(
				createItem("C", 3), createItem("E", 5), createItem("A", 1),
				createItem("D", 4), createItem("B", 2));
		new CoverageTable(Arrays.asList(recorder),
				CounterComparator.TOTALITEMS.on(CounterEntity.CLASS)).render(
				body, items, createTotal("Sum", 6), resources, root);
		doc.close();
		assertEquals("ABCDE", recorder.toString());
	}

	@Test
	public void testValidHTML() throws Exception {
		final ICoverageTableColumn recorder = new ICoverageTableColumn() {

			public void init(List<ICoverageTableItem> items, ICoverageNode total) {
			}

			public void header(HTMLElement tr, Resources resources,
					ReportOutputFolder base) throws IOException {
				tr.td().text("Header");
			}

			public void footer(HTMLElement tr, ICoverageNode total,
					Resources resources, ReportOutputFolder base)
					throws IOException {
				tr.td().text("Footer");
			}

			public void item(HTMLElement tr, ICoverageTableItem item,
					Resources resources, ReportOutputFolder base)
					throws IOException {
				tr.td().text(item.getLinkLabel());
			}
		};
		final List<ICoverageTableItem> items = Arrays.asList(
				createItem("A", 1), createItem("B", 2), createItem("C", 3));
		new CoverageTable(Arrays.asList(recorder),
				CounterComparator.TOTALITEMS.on(CounterEntity.CLASS)).render(
				body, items, createTotal("Sum", 6), resources, root);
		doc.close();

		final HTMLSupport support = new HTMLSupport();
		final Document doc = support.parse(output.getFile("Test.html"));
		assertEquals("Header",
				support.findStr(doc, "/html/body/table/thead/tr/td/text()"));
		assertEquals("Footer",
				support.findStr(doc, "/html/body/table/tfoot/tr/td/text()"));
		assertEquals("A",
				support.findStr(doc, "/html/body/table/tbody/tr[1]/td/text()"));
		assertEquals("B",
				support.findStr(doc, "/html/body/table/tbody/tr[2]/td/text()"));
		assertEquals("C",
				support.findStr(doc, "/html/body/table/tbody/tr[3]/td/text()"));
	}

	private ICoverageTableItem createItem(final String name, final int count) {
		final ICoverageNode node = new CoverageNodeImpl(ElementType.GROUP,
				name, false) {
			{
				this.classCounter = CounterImpl.getInstance(count, false);
			}
		};
		return new ICoverageTableItem() {
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
