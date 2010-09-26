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
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Comparator;

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
 * Unit tests for {@link BarColumn}.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public class BarColumnTest {

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
	public void testInit() throws Exception {
		final BarColumn column = new BarColumn(CounterEntity.LINE);
		final ITableItem i = createItem(30, 24);
		assertTrue(column.init(Arrays.asList(i), i.getNode()));
		doc.close();
	}

	@Test
	public void testFooter() throws Exception {
		new BarColumn(CounterEntity.LINE).footer(td, createNode(20, 5),
				resources, root);
		doc.close();
		final Document doc = support.parse(output.getFile("Test.html"));
		assertEquals("", support.findStr(doc, "/html/body/table/tr/td/text()"));
	}

	@Test
	public void testBarWidths() throws Exception {
		final BarColumn col = new BarColumn(CounterEntity.LINE);
		final ITableItem i1 = createItem(20, 5);
		final ITableItem i2 = createItem(30, 24);
		col.init(Arrays.asList(i1, i2), createNode(50, 29));
		col.item(td, i1, resources, root);
		doc.close();
		final Document doc = support.parse(output.getFile("Test.html"));

		assertEquals("2",
				support.findStr(doc, "count(/html/body/table/tr[1]/td/img)"));

		// red bar
		assertEquals(".resources/redbar.gif",
				support.findStr(doc, "/html/body/table/tr[1]/td/img[1]/@src"));
		assertEquals("15",
				support.findStr(doc, "/html/body/table/tr[1]/td/img[1]/@alt"));
		assertEquals("60",
				support.findStr(doc, "/html/body/table/tr[1]/td/img[1]/@width"));

		// green bar
		assertEquals(".resources/greenbar.gif",
				support.findStr(doc, "/html/body/table/tr[1]/td/img[2]/@src"));
		assertEquals("5",
				support.findStr(doc, "/html/body/table/tr[1]/td/img[2]/@alt"));
		assertEquals("20",
				support.findStr(doc, "/html/body/table/tr[1]/td/img[2]/@width"));
	}

	@Test
	public void testRedBarOnly() throws Exception {
		final BarColumn col = new BarColumn(CounterEntity.LINE);
		final ITableItem i1 = createItem(20, 0);
		col.init(Arrays.asList(i1), createNode(20, 0));
		col.item(td, i1, resources, root);
		doc.close();
		final Document doc = support.parse(output.getFile("Test.html"));

		assertEquals("1",
				support.findStr(doc, "count(/html/body/table/tr[1]/td/img)"));

		// red bar
		assertEquals(".resources/redbar.gif",
				support.findStr(doc, "/html/body/table/tr[1]/td/img[1]/@src"));
		assertEquals("20",
				support.findStr(doc, "/html/body/table/tr[1]/td/img[1]/@alt"));
		assertEquals("120",
				support.findStr(doc, "/html/body/table/tr[1]/td/img[1]/@width"));
	}

	@Test
	public void testGreenBarOnly() throws Exception {
		final BarColumn col = new BarColumn(CounterEntity.LINE);
		final ITableItem i1 = createItem(20, 20);
		col.init(Arrays.asList(i1), createNode(20, 20));
		col.item(td, i1, resources, root);
		doc.close();
		final Document doc = support.parse(output.getFile("Test.html"));

		assertEquals("1",
				support.findStr(doc, "count(/html/body/table/tr[1]/td/img)"));

		// red bar
		assertEquals(".resources/greenbar.gif",
				support.findStr(doc, "/html/body/table/tr[1]/td/img[1]/@src"));
		assertEquals("20",
				support.findStr(doc, "/html/body/table/tr[1]/td/img[1]/@alt"));
		assertEquals("120",
				support.findStr(doc, "/html/body/table/tr[1]/td/img[1]/@width"));
	}

	@Test
	public void testComparator1() throws Exception {
		final BarColumn col = new BarColumn(CounterEntity.LINE);
		final Comparator<ITableItem> c = col.getComparator();
		final ITableItem i1 = createItem(100, 50);
		final ITableItem i2 = createItem(100, 80);
		assertTrue(c.compare(i1, i2) < 0);
		assertTrue(c.compare(i2, i1) > 0);
		assertEquals(0, c.compare(i1, i1));
		doc.close();
	}

	@Test
	public void testComparator2() throws Exception {
		final BarColumn col = new BarColumn(CounterEntity.LINE);
		final Comparator<ITableItem> c = col.getComparator();
		final ITableItem i1 = createItem(110, 60);
		final ITableItem i2 = createItem(100, 50);
		assertTrue(c.compare(i1, i2) < 0);
		assertTrue(c.compare(i2, i1) > 0);
		assertEquals(0, c.compare(i1, i1));
		doc.close();
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
