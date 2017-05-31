/*******************************************************************************
 * Copyright (c) 2009, 2017 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Maurice Quach - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.report.totalcoverage;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.jacoco.report.IReportVisitor;
import org.jacoco.report.MemoryOutput;
import org.jacoco.report.ReportStructureTestDriver;
import org.junit.Before;
import org.junit.Test;

public class TotalCoverageFormatterTest {
	private static final String expectedPercentage = "60.0";

	private ReportStructureTestDriver driver;

	private TotalCoverageFormatter formatter;

	private IReportVisitor visitor;

	private MemoryOutput output;

	@Before
	public void setup() throws Exception {
		driver = new ReportStructureTestDriver();
		formatter = new TotalCoverageFormatter();
		output = new MemoryOutput();
		visitor = formatter.createVisitor(output);
	}

	@Test
	public void testPercentage() throws IOException {
		driver.sendBundle(visitor);
		final List<String> lines = getLines();
		assertEquals(expectedPercentage, lines.get(0));
	}

	@Test
	public void testSetEncoding() throws Exception {
		formatter.setOutputEncoding("UTF-16");
		visitor = formatter.createVisitor(output);
		driver.sendBundle(visitor);
		final List<String> lines = getLines("UTF-16");
		assertEquals(expectedPercentage, lines.get(0));
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
