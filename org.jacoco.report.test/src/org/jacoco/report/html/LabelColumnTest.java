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

import org.jacoco.core.analysis.CoverageNodeImpl;
import org.jacoco.core.analysis.ICoverageNode;
import org.jacoco.core.analysis.ICoverageNode.ElementType;
import org.jacoco.report.MemoryMultiReportOutput;
import org.jacoco.report.ReportOutputFolder;
import org.jacoco.report.html.resources.Resources;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

/**
 * Unit tests for {@link LabelColumn}.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public class LabelColumnTest {

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
		column = new LabelColumn();
	}

	@After
	public void teardown() {
		output.assertAllClosed();
	}

	@Test
	public void testHeader() throws Exception {
		column.header(tr, resources, root);
		doc.close();
		final Document doc = support.parse(output.getFile("Test.html"));
		assertEquals("Element", support.findStr(doc,
				"/html/body/table/tr/td/text()"));
	}

	@Test
	public void testFooter() throws Exception {
		column.footer(tr,
				new CoverageNodeImpl(ElementType.GROUP, "Foo", false),
				resources, root);
		doc.close();
		final Document doc = support.parse(output.getFile("Test.html"));
		assertEquals("Total", support.findStr(doc,
				"/html/body/table/tr/td/text()"));
	}

	@Test
	public void testItemWithoutLink() throws Exception {
		column.item(tr, createItem("Abc", null), resources, root);
		doc.close();
		final Document doc = support.parse(output.getFile("Test.html"));
		assertEquals("Abc", support.findStr(doc,
				"/html/body/table/tr/td/span/text()"));
		assertEquals("el_group", support.findStr(doc,
				"/html/body/table/tr/td/span/@class"));
	}

	@Test
	public void testItemWithLink() throws Exception {
		column.item(tr, createItem("Def", "def.html"), resources, root);
		doc.close();
		final Document doc = support.parse(output.getFile("Test.html"));
		assertEquals("Def", support.findStr(doc,
				"/html/body/table/tr/td/a/text()"));
		assertEquals("def.html", support.findStr(doc,
				"/html/body/table/tr/td/a/@href"));
		assertEquals("el_group", support.findStr(doc,
				"/html/body/table/tr/td/a/@class"));
	}

	private ICoverageTableItem createItem(final String name, final String link) {
		final ICoverageNode node = new CoverageNodeImpl(ElementType.GROUP,
				name, false);
		return new ICoverageTableItem() {
			public String getLabel() {
				return name;
			}

			public String getLink(ReportOutputFolder base) {
				return link;
			}

			public ICoverageNode getNode() {
				return node;
			}
		};
	}

}
