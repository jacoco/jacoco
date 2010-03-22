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

import java.util.Collections;

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
 * Unit tests for {@link PercentageColumn}.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public class PercentageColumnTest {

	private MemoryMultiReportOutput output;

	private ReportOutputFolder root;

	private Resources resources;

	private HTMLDocument doc;

	private HTMLElement tr;

	private HTMLSupport support;

	private ICoverageTableColumn column;

	@Before
	public void setup() throws Exception {
		output = new MemoryMultiReportOutput();
		root = new ReportOutputFolder(output);
		resources = new Resources(root);
		doc = new HTMLDocument(root.createFile("Test.html"), "UTF-8");
		doc.head().title();
		tr = doc.body().table("somestyle").tr();
		support = new HTMLSupport();
		column = new PercentageColumn("TestHeader", CounterEntity.LINE);
	}

	@After
	public void teardown() {
		output.assertAllClosed();
	}

	@Test
	public void testHeader() throws Exception {
		final ICoverageTableItem item = createItem(1, 3);
		column.init(Collections.singletonList(item), item.getNode());
		column.header(tr, resources, root);
		doc.close();
		final Document doc = support.parse(output.getFile("Test.html"));
		assertEquals("TestHeader", support.findStr(doc,
				"/html/body/table/tr/td/text()"));
	}

	@Test
	public void testItem1() throws Exception {
		final ICoverageTableItem item = createItem(150, 50);
		column.init(Collections.singletonList(item), item.getNode());
		column.item(tr, item, resources, root);
		doc.close();
		final Document doc = support.parse(output.getFile("Test.html"));
		assertEquals("33%", support.findStr(doc,
				"/html/body/table/tr/td[1]/text()"));
	}

	@Test
	public void testItem2() throws Exception {
		final ICoverageTableItem item = createItem(0, 50);
		column.init(Collections.singletonList(item), item.getNode());
		column.item(tr, item, resources, root);
		doc.close();
		final Document doc = support.parse(output.getFile("Test.html"));
		assertEquals("n/a", support.findStr(doc,
				"/html/body/table/tr/td[1]/text()"));
	}

	@Test
	public void testFooter1() throws Exception {
		final ICoverageTableItem item = createItem(80, 60);
		column.init(Collections.singletonList(item), item.getNode());
		column.footer(tr, item.getNode(), resources, root);
		doc.close();
		final Document doc = support.parse(output.getFile("Test.html"));
		assertEquals("75%", support.findStr(doc, "/html/body/table/tr"));
	}

	@Test
	public void testFooter2() throws Exception {
		final ICoverageTableItem item = createItem(0, 60);
		column.init(Collections.singletonList(item), item.getNode());
		column.footer(tr, item.getNode(), resources, root);
		doc.close();
		final Document doc = support.parse(output.getFile("Test.html"));
		assertEquals("n/a", support.findStr(doc, "/html/body/table/tr"));
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
