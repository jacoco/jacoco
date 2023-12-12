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
package org.jacoco.report.csv;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.jacoco.report.ILanguageNames;
import org.jacoco.report.IReportVisitor;
import org.jacoco.report.MemoryOutput;
import org.jacoco.report.ReportStructureTestDriver;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link CSVFormatter}.
 */
public class CSVFormatterTest {

	private static final String HEADER = "GROUP,PACKAGE,CLASS,INSTRUCTION_MISSED,INSTRUCTION_COVERED,BRANCH_MISSED,BRANCH_COVERED,LINE_MISSED,LINE_COVERED,COMPLEXITY_MISSED,COMPLEXITY_COVERED,METHOD_MISSED,METHOD_COVERED";

	private ReportStructureTestDriver driver;

	private CSVFormatter formatter;

	private IReportVisitor visitor;

	private MemoryOutput output;

	@Before
	public void setup() throws Exception {
		driver = new ReportStructureTestDriver();
		formatter = new CSVFormatter();
		output = new MemoryOutput();
		visitor = formatter.createVisitor(output);
	}

	@After
	public void teardown() {
		output.assertClosed();
	}

	@Test
	public void testStructureWithGroup() throws IOException {
		driver.sendGroup(visitor);
		final List<String> lines = getLines();
		assertEquals(HEADER, lines.get(0));
		assertEquals(
				"group/bundle,org.jacoco.example,FooClass,10,15,1,2,0,3,1,2,0,1",
				lines.get(1));
		assertEquals(2, lines.size());
	}

	@Test
	public void testStructureWithNestedGroups() throws IOException {
		driver.sendNestedGroups(visitor);
		final List<String> lines = getLines();
		assertEquals(HEADER, lines.get(0));
		assertEquals(
				"report/group1/group/bundle,org.jacoco.example,FooClass,10,15,1,2,0,3,1,2,0,1",
				lines.get(1));
		assertEquals(
				"report/bundle,org.jacoco.example,FooClass,10,15,1,2,0,3,1,2,0,1",
				lines.get(2));
		assertEquals(3, lines.size());
	}

	@Test
	public void testStructureWithBundleOnly() throws IOException {
		driver.sendBundle(visitor);
		final List<String> lines = getLines();
		assertEquals(HEADER, lines.get(0));
		assertEquals("bundle,org.jacoco.example,FooClass,10,15,1,2,0,3,1,2,0,1",
				lines.get(1));
		assertEquals(2, lines.size());
	}

	@Test
	public void testSetEncoding() throws Exception {
		formatter.setOutputEncoding("UTF-16");
		visitor = formatter.createVisitor(output);
		driver.sendBundle(visitor);
		final List<String> lines = getLines("UTF-16");
		assertEquals(HEADER, lines.get(0));
	}

	@Test
	public void testGetLanguageNames() throws Exception {
		ILanguageNames names = new ILanguageNames() {
			public String getPackageName(String vmname) {
				return null;
			}

			public String getQualifiedClassName(String vmname) {
				return null;
			}

			public String getClassName(String vmname, String vmsignature,
					String vmsuperclass, String[] vminterfaces) {
				return null;
			}

			public String getMethodName(String vmclassname, String vmmethodname,
					String vmdesc, String vmsignature) {
				return null;
			}

			public String getQualifiedMethodName(String vmclassname,
					String vmmethodname, String vmdesc, String vmsignature) {
				return null;
			}
		};
		formatter.setLanguageNames(names);
		assertSame(names, formatter.getLanguageNames());
		output.close();
	}

	private List<String> getLines() throws IOException {
		return getLines("UTF-8");
	}

	private List<String> getLines(String encoding) throws IOException {
		final BufferedReader reader = new BufferedReader(
				new InputStreamReader(output.getContentsAsStream(), encoding));
		final List<String> lines = new ArrayList<String>();
		String line;
		while ((line = reader.readLine()) != null) {
			lines.add(line);
		}
		return lines;
	}

}
