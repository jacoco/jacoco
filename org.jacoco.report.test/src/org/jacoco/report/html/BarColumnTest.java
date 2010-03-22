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

import java.util.Arrays;

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

	private HTMLElement tr;

	private HTMLSupport support;

	@Before
	public void setup() throws Exception {
		output = new MemoryMultiReportOutput();
		root = new ReportOutputFolder(output);
		resources = new Resources(root);
		doc = new HTMLDocument(root.createFile("Test.html"), "UTF-8");
		doc.head().title();
		tr = doc.body().table("somestyle").tr();
		support = new HTMLSupport();
	}

	@After
	public void teardown() {
		output.assertAllClosed();
	}

	@Test
	public void testHeader() throws Exception {
		new BarColumn("TestHeader", CounterEntity.LINE).header(tr, resources,
				root);
		doc.close();
		final Document doc = support.parse(output.getFile("Test.html"));
		assertEquals("TestHeader", support.findStr(doc,
				"/html/body/table/tr/td/text()"));
	}

	@Test
	public void testFooter() throws Exception {
		new BarColumn("TestHeader", CounterEntity.LINE).footer(tr, createNode(
				20, 5), resources, root);
		doc.close();
		final Document doc = support.parse(output.getFile("Test.html"));
		assertEquals("", support.findStr(doc, "/html/body/table/tr/td/text()"));
	}

	@Test
	public void testBarWidths() throws Exception {
		final BarColumn col = new BarColumn("", CounterEntity.LINE);
		final ICoverageTableItem i1 = createItem(20, 5);
		final ICoverageTableItem i2 = createItem(30, 24);
		col.init(Arrays.asList(i1, i2), createNode(50, 29));
		col.item(tr, i1, resources, root);
		doc.close();
		final Document doc = support.parse(output.getFile("Test.html"));

		assertEquals("2", support.findStr(doc,
				"count(/html/body/table/tr[1]/td/img)"));

		// red bar
		assertEquals(".resources/redbar.gif", support.findStr(doc,
				"/html/body/table/tr[1]/td/img[1]/@src"));
		assertEquals("15", support.findStr(doc,
				"/html/body/table/tr[1]/td/img[1]/@alt"));
		assertEquals("60", support.findStr(doc,
				"/html/body/table/tr[1]/td/img[1]/@width"));

		// green bar
		assertEquals(".resources/greenbar.gif", support.findStr(doc,
				"/html/body/table/tr[1]/td/img[2]/@src"));
		assertEquals("5", support.findStr(doc,
				"/html/body/table/tr[1]/td/img[2]/@alt"));
		assertEquals("20", support.findStr(doc,
				"/html/body/table/tr[1]/td/img[2]/@width"));
	}

	@Test
	public void testRedBarOnly() throws Exception {
		final BarColumn col = new BarColumn("", CounterEntity.LINE);
		final ICoverageTableItem i1 = createItem(20, 0);
		col.init(Arrays.asList(i1), createNode(20, 0));
		col.item(tr, i1, resources, root);
		doc.close();
		final Document doc = support.parse(output.getFile("Test.html"));

		assertEquals("1", support.findStr(doc,
				"count(/html/body/table/tr[1]/td/img)"));

		// red bar
		assertEquals(".resources/redbar.gif", support.findStr(doc,
				"/html/body/table/tr[1]/td/img[1]/@src"));
		assertEquals("20", support.findStr(doc,
				"/html/body/table/tr[1]/td/img[1]/@alt"));
		assertEquals("120", support.findStr(doc,
				"/html/body/table/tr[1]/td/img[1]/@width"));
	}

	@Test
	public void testGreenBarOnly() throws Exception {
		final BarColumn col = new BarColumn("", CounterEntity.LINE);
		final ICoverageTableItem i1 = createItem(20, 20);
		col.init(Arrays.asList(i1), createNode(20, 20));
		col.item(tr, i1, resources, root);
		doc.close();
		final Document doc = support.parse(output.getFile("Test.html"));

		assertEquals("1", support.findStr(doc,
				"count(/html/body/table/tr[1]/td/img)"));

		// red bar
		assertEquals(".resources/greenbar.gif", support.findStr(doc,
				"/html/body/table/tr[1]/td/img[1]/@src"));
		assertEquals("20", support.findStr(doc,
				"/html/body/table/tr[1]/td/img[1]/@alt"));
		assertEquals("120", support.findStr(doc,
				"/html/body/table/tr[1]/td/img[1]/@width"));
	}

	private ICoverageTableItem createItem(final int total, final int covered) {
		final ICoverageNode node = createNode(total, covered);
		return new ICoverageTableItem() {
			public String getLabel() {
				return "Foo";
			}

			public String getLink(ReportOutputFolder base) {
				return null;
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
