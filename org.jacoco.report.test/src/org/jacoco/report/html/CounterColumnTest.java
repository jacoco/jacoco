/*******************************************************************************
 * Copyright (c) 2009 Mountainminds GmbH & Co. KG and others
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

import org.jacoco.core.analysis.CounterImpl;
import org.jacoco.core.analysis.CoverageNodeImpl;
import org.jacoco.core.analysis.ICoverageNode;
import org.jacoco.core.analysis.ICoverageNode.CounterEntity;
import org.jacoco.core.analysis.ICoverageNode.ElementType;
import org.jacoco.report.MemoryReportOutput;
import org.jacoco.report.ReportOutputFolder;
import org.jacoco.report.html.resources.Resources;
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

	private MemoryReportOutput output;

	private ReportOutputFolder root;

	private Resources resources;

	private HTMLDocument doc;

	private HTMLElement tr;

	private HTMLSupport support;

	private CounterColumn counterColumn;

	@Before
	public void setup() throws Exception {
		output = new MemoryReportOutput();
		root = new ReportOutputFolder(output);
		resources = new Resources(root);
		doc = new HTMLDocument(root.createFile("Test.html"));
		doc.head().title();
		tr = doc.body().table("somestyle").tr();
		support = new HTMLSupport();
		counterColumn = new CounterColumn("TestHeader", CounterEntity.LINE);
	}

	@Test
	public void testHeader() throws Exception {
		counterColumn.header(tr, resources, root);
		doc.close();
		final Document doc = support.parse(output.getFile("Test.html"));
		assertEquals("TestHeader", support.findStr(doc,
				"/html/body/table/tr/td/text()"));
	}

	@Test
	public void testItem1() throws Exception {
		counterColumn.item(tr, createItem(150, 50), resources, root);
		doc.close();
		final Document doc = support.parse(output.getFile("Test.html"));
		assertEquals("50 / ", support.findStr(doc,
				"/html/body/table/tr/td[1]/text()"));
		assertEquals("150 = ", support.findStr(doc,
				"/html/body/table/tr/td[2]/text()"));
		assertEquals("33%", support.findStr(doc,
				"/html/body/table/tr/td[3]/text()"));
	}

	@Test
	public void testItem2() throws Exception {
		counterColumn.item(tr, createItem(0, 0), resources, root);
		doc.close();
		final Document doc = support.parse(output.getFile("Test.html"));
		assertEquals("0 / ", support.findStr(doc,
				"/html/body/table/tr/td[1]/text()"));
		assertEquals("0 = ", support.findStr(doc,
				"/html/body/table/tr/td[2]/text()"));
		assertEquals("n/a", support.findStr(doc,
				"/html/body/table/tr/td[3]/text()"));
	}

	@Test
	public void testFooter1() throws Exception {
		counterColumn.footer(tr, createNode(80, 60), resources, root);
		doc.close();
		final Document doc = support.parse(output.getFile("Test.html"));
		assertEquals("60 / ", support.findStr(doc,
				"/html/body/table/tr/td[1]/text()"));
		assertEquals("80 = ", support.findStr(doc,
				"/html/body/table/tr/td[2]/text()"));
		assertEquals("75%", support.findStr(doc,
				"/html/body/table/tr/td[3]/text()"));
	}

	@Test
	public void testFooter2() throws Exception {
		counterColumn.footer(tr, createNode(0, 0), resources, root);
		doc.close();
		final Document doc = support.parse(output.getFile("Test.html"));
		assertEquals("0 / ", support.findStr(doc,
				"/html/body/table/tr/td[1]/text()"));
		assertEquals("0 = ", support.findStr(doc,
				"/html/body/table/tr/td[2]/text()"));
		assertEquals("n/a", support.findStr(doc,
				"/html/body/table/tr/td[3]/text()"));
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
