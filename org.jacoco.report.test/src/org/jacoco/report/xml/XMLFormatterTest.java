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
package org.jacoco.report.xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jacoco.core.data.ExecutionData;
import org.jacoco.core.data.SessionInfo;
import org.jacoco.report.IReportVisitor;
import org.jacoco.report.MemoryOutput;
import org.jacoco.report.ReportStructureTestDriver;
import org.jacoco.report.internal.xml.XMLSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

/**
 * Unit tests for {@link XMLFormatter}.
 */
public class XMLFormatterTest {

	private ReportStructureTestDriver driver;

	private XMLFormatter formatter;

	private MemoryOutput output;

	private List<SessionInfo> infos;

	private Collection<ExecutionData> data;

	@Before
	public void setup() {
		driver = new ReportStructureTestDriver();
		formatter = new XMLFormatter();
		output = new MemoryOutput();
		infos = new ArrayList<SessionInfo>();
		data = new ArrayList<ExecutionData>();
	}

	@After
	public void teardown() {
		output.assertClosed();
	}

	@Test
	public void testSessionInfo() throws Exception {
		infos.add(new SessionInfo("session-1", 12345, 67890));
		infos.add(new SessionInfo("session-2", 1, 2));
		infos.add(new SessionInfo("session-3", 1, 2));
		final IReportVisitor visitor = formatter.createVisitor(output);
		visitor.visitInfo(infos, data);
		visitor.visitGroup("foo");
		visitor.visitEnd();
		assertPathMatches("session-1", "/report/sessioninfo[1]/@id");
		assertPathMatches("12345", "/report/sessioninfo[1]/@start");
		assertPathMatches("67890", "/report/sessioninfo[1]/@dump");
		assertPathMatches("session-2", "/report/sessioninfo[2]/@id");
		assertPathMatches("session-3", "/report/sessioninfo[3]/@id");
	}

	@Test
	public void testStructureWithNestedGroups() throws Exception {
		final IReportVisitor visitor = formatter.createVisitor(output);
		visitor.visitInfo(infos, data);
		driver.sendNestedGroups(visitor);
		assertPathMatches("report", "/report/@name");
		assertPathMatches("group1", "/report/group[1]/@name");
		assertPathMatches("group", "/report/group[1]/group[1]/@name");
		assertPathMatches("bundle", "/report/group[1]/group[1]/group[1]/@name");
		assertPathMatches("bundle", "/report/group[2]/@name");
	}

	@Test
	public void testStructureWithGroup() throws Exception {
		final IReportVisitor visitor = formatter.createVisitor(output);
		visitor.visitInfo(infos, data);
		driver.sendGroup(visitor);
		assertPathMatches("group", "/report/@name");
		assertPathMatches("bundle", "/report/group/@name");
		assertPathMatches("org/jacoco/example", "/report/group/package/@name");
		assertPathMatches("org/jacoco/example/FooClass",
				"/report/group/package/class/@name");
		assertPathMatches("FooClass.java",
				"/report/group/package/class/@sourcefilename");
		assertPathMatches("fooMethod",
				"/report/group/package/class/method/@name");

		assertPathMatches("1", "count(/report/counter[@type='INSTRUCTION'])");
		assertPathMatches("10", "report/counter[@type='INSTRUCTION']/@missed");
		assertPathMatches("15", "report/counter[@type='INSTRUCTION']/@covered");

		assertPathMatches("1", "count(/report/counter[@type='BRANCH'])");
		assertPathMatches("1", "report/counter[@type='BRANCH']/@missed");
		assertPathMatches("2", "report/counter[@type='BRANCH']/@covered");

		assertPathMatches("1", "count(/report/counter[@type='COMPLEXITY'])");
		assertPathMatches("1", "report/counter[@type='COMPLEXITY']/@missed");
		assertPathMatches("2", "report/counter[@type='COMPLEXITY']/@covered");

		assertPathMatches("1", "count(/report/counter[@type='LINE'])");
		assertPathMatches("0", "report/counter[@type='LINE']/@missed");
		assertPathMatches("3", "report/counter[@type='LINE']/@covered");

		assertPathMatches("1", "count(/report/counter[@type='METHOD'])");
		assertPathMatches("0", "report/counter[@type='METHOD']/@missed");
		assertPathMatches("1", "report/counter[@type='METHOD']/@covered");

		assertPathMatches("1", "count(/report/counter[@type='CLASS'])");
		assertPathMatches("0", "report/counter[@type='CLASS']/@missed");
		assertPathMatches("1", "report/counter[@type='CLASS']/@covered");
	}

	@Test
	public void testStructureWithBundleOnly() throws Exception {
		final IReportVisitor visitor = formatter.createVisitor(output);
		visitor.visitInfo(infos, data);
		driver.sendBundle(visitor);
		assertPathMatches("bundle", "/report/@name");

		assertPathMatches("2", "count(/report/package)");
		assertPathMatches("org/jacoco/example", "/report/package/@name");

		assertPathMatches("3", "count(/report/package/class)");
		assertPathMatches("org/jacoco/example/FooClass",
				"/report/package/class/@name");

		assertPathMatches("1", "count(/report/package/class/method)");
		assertPathMatches("fooMethod", "/report/package/class/method/@name");

		assertPathMatches("1", "count(/report/counter[@type='INSTRUCTION'])");
		assertPathMatches("10", "report/counter[@type='INSTRUCTION']/@missed");
		assertPathMatches("15", "report/counter[@type='INSTRUCTION']/@covered");

		assertPathMatches("1", "count(/report/counter[@type='BRANCH'])");
		assertPathMatches("1", "report/counter[@type='BRANCH']/@missed");
		assertPathMatches("2", "report/counter[@type='BRANCH']/@covered");

		assertPathMatches("1", "count(/report/counter[@type='COMPLEXITY'])");
		assertPathMatches("1", "report/counter[@type='COMPLEXITY']/@missed");
		assertPathMatches("2", "report/counter[@type='COMPLEXITY']/@covered");

		assertPathMatches("1", "count(/report/counter[@type='LINE'])");
		assertPathMatches("0", "report/counter[@type='LINE']/@missed");
		assertPathMatches("3", "report/counter[@type='LINE']/@covered");

		assertPathMatches("1", "count(/report/counter[@type='METHOD'])");
		assertPathMatches("0", "report/counter[@type='METHOD']/@missed");
		assertPathMatches("1", "report/counter[@type='METHOD']/@covered");

		assertPathMatches("1", "count(/report/counter[@type='CLASS'])");
		assertPathMatches("0", "report/counter[@type='CLASS']/@missed");
		assertPathMatches("1", "report/counter[@type='CLASS']/@covered");

		assertPathMatches("2",
				"count(report/package[@name='org/jacoco/example']/sourcefile)");
		assertPathMatches("3", "count(report/package/sourcefile/line)");
		assertPathMatches("1",
				"report/package/sourcefile[@name='FooClass.java']/line[1]/@nr");
		assertPathMatches("3",
				"report/package/sourcefile[@name='FooClass.java']/line[1]/@mi");
		assertPathMatches("2",
				"report/package/sourcefile[@name='FooClass.java']/line[2]/@nr");
		assertPathMatches("2",
				"report/package/sourcefile[@name='FooClass.java']/line[2]/@cb");
		// empty line is skipped
		assertPathMatches("4",
				"report/package/sourcefile[@name='FooClass.java']/line[3]/@nr");
		assertPathMatches("4",
				"report/package/sourcefile[@name='FooClass.java']/line[3]/@mi");

		assertPathMatches("0", "count(/report/package[@name='empty']/counter)");

		assertPathMatches("1", "count(/report/package[@name='empty']/class)");
		assertPathMatches("empty/Empty",
				"/report/package[@name='empty']/class/@name");
		assertPathMatches("0", "count(report/package[@name='empty']/class/*)");

		assertPathMatches("1",
				"count(/report/package[@name='empty']/sourcefile)");
		assertPathMatches("Empty.java",
				"report/package[@name='empty']/sourcefile/@name");
		assertPathMatches("0",
				"count(report/package[@name='empty']/sourcefile/*)");
	}

	@Test
	public void testDefaultEncoding() throws Exception {
		final IReportVisitor visitor = formatter.createVisitor(output);
		visitor.visitInfo(infos, data);
		driver.sendBundle(visitor);
		final BufferedReader reader = new BufferedReader(
				new InputStreamReader(output.getContentsAsStream(), "UTF-8"));
		final String line = reader.readLine();
		assertTrue(line,
				line.startsWith("<?xml version=\"1.0\" encoding=\"UTF-8\""));
	}

	@Test
	public void testSetEncoding() throws Exception {
		formatter.setOutputEncoding("UTF-16");
		final IReportVisitor visitor = formatter.createVisitor(output);
		visitor.visitInfo(infos, data);
		driver.sendBundle(visitor);
		final BufferedReader reader = new BufferedReader(
				new InputStreamReader(output.getContentsAsStream(), "UTF-16"));
		final String line = reader.readLine();
		assertTrue(line,
				line.startsWith("<?xml version=\"1.0\" encoding=\"UTF-16\""));
	}

	private void assertPathMatches(String expected, String path)
			throws Exception {
		XMLSupport support = new XMLSupport(XMLFormatter.class);
		Document document = support.parse(output);
		assertEquals(expected, support.findStr(document, path));
	}

}
