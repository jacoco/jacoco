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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;

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
 * Unit tests for {@link CounterColumn}.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public class CounterColumnTest {

	private MemoryMultiReportOutput output;

	private ReportOutputFolder root;

	private Resources resources;

	private HTMLDocument doc;

	private HTMLElement td;

	private HTMLSupport support;

	@Before
	public void setup() throws Exception {
		output = new MemoryMultiReportOutput();
		root = new ReportOutputFolder(output);
		resources = new Resources(root);
		doc = new HTMLDocument(root.createFile("Test.html"), "UTF-8");
		doc.head().title();
		td = doc.body().table("somestyle").tr().td();
		support = new HTMLSupport();
	}

	@After
	public void teardown() {
		output.assertAllClosed();
	}

	@Test
	public void testInitVisible() throws Exception {
		IColumnRenderer column = CounterColumn
				.newTotal(CounterEntity.LINE);
		final ITableItem item = createItem(1, 3);
		assertTrue(column.init(Arrays.asList(item), item.getNode()));
		doc.close();
	}

	@Test
	public void testInitInvisible() throws Exception {
		IColumnRenderer column = CounterColumn
				.newTotal(CounterEntity.LINE);
		final ITableItem item = createItem(0, 0);
		assertFalse(column.init(Arrays.asList(item), createNode(1, 0)));
		doc.close();
	}

	@Test
	public void testItemTotal() throws Exception {
		IColumnRenderer column = CounterColumn
				.newTotal(CounterEntity.LINE);
		final ITableItem item = createItem(150, 50);
		column.init(Collections.singletonList(item), item.getNode());
		column.item(td, item, resources, root);
		doc.close();
		final Document doc = support.parse(output.getFile("Test.html"));
		assertEquals("150",
				support.findStr(doc, "/html/body/table/tr/td[1]/text()"));
	}

	@Test
	public void testItemMissed() throws Exception {
		IColumnRenderer column = CounterColumn
				.newMissed(CounterEntity.LINE);
		final ITableItem item = createItem(150, 50);
		column.init(Collections.singletonList(item), item.getNode());
		column.item(td, item, resources, root);
		doc.close();
		final Document doc = support.parse(output.getFile("Test.html"));
		assertEquals("100",
				support.findStr(doc, "/html/body/table/tr/td[1]/text()"));
	}

	@Test
	public void testItemCovered() throws Exception {
		IColumnRenderer column = CounterColumn
				.newCovered(CounterEntity.LINE);
		final ITableItem item = createItem(150, 50);
		column.init(Collections.singletonList(item), item.getNode());
		column.item(td, item, resources, root);
		doc.close();
		final Document doc = support.parse(output.getFile("Test.html"));
		assertEquals("50",
				support.findStr(doc, "/html/body/table/tr/td[1]/text()"));
	}

	@Test
	public void testFooter() throws Exception {
		IColumnRenderer column = CounterColumn
				.newTotal(CounterEntity.LINE);
		final ITableItem item = createItem(80, 60);
		column.init(Collections.singletonList(item), item.getNode());
		column.footer(td, item.getNode(), resources, root);
		doc.close();
		final Document doc = support.parse(output.getFile("Test.html"));
		assertEquals("80",
				support.findStr(doc, "/html/body/table/tr/td[1]/text()"));
	}

	private ITableItem createItem(final int total, final int covered) {
		final ICoverageNode node = createNode(total, covered);
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

	private CoverageNodeImpl createNode(final int total, final int covered) {
		return new CoverageNodeImpl(ElementType.GROUP, "Foo", false) {
			{
				this.lineCounter = CounterImpl.getInstance(total, covered);
			}
		};
	}
}
