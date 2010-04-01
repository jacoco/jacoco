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
package org.jacoco.report.xml;

import static org.junit.Assert.assertEquals;

import org.jacoco.report.MemorySingleReportOutput;
import org.jacoco.report.ReportStructureTestDriver;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

/**
 * Unit tests for {@link XMLFormatter}.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public class XMLFormatterTest {

	private ReportStructureTestDriver driver;

	private XMLFormatter formatter;

	private MemorySingleReportOutput output;

	@Before
	public void setup() {
		driver = new ReportStructureTestDriver();
		formatter = new XMLFormatter();
		output = new MemorySingleReportOutput();
		formatter.setReportOutput(output);
	}

	@After
	public void teardown() {
		output.assertClosed();
	}

	@Test
	public void testStructureWithGroup() throws Exception {
		driver.sendGroup(formatter);
		assertPathMatches("group", "/report/@name");
		assertPathMatches("bundle", "/report/group/@name");
		assertPathMatches("org/jacoco/example", "/report/group/package/@name");
		assertPathMatches("org/jacoco/example/FooClass",
				"/report/group/package/class/@name");
		assertPathMatches("fooMethod",
				"/report/group/package/class/method/@name");
	}

	@Test
	public void testStructureWithBundleOnly() throws Exception {
		driver.sendBundle(formatter);
		assertPathMatches("bundle", "/report/@name");
		assertPathMatches("org/jacoco/example", "/report/package/@name");
		assertPathMatches("org/jacoco/example/FooClass",
				"/report/package/class/@name");
		assertPathMatches("fooMethod", "/report/package/class/method/@name");
	}

	private void assertPathMatches(String expected, String path)
			throws Exception {
		XMLSupport support = new XMLSupport(XMLFormatter.class);
		Document document = support.parse(output.getFile());
		assertEquals(expected, support.findStr(document, path));
	}

}
