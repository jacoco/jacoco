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
package org.jacoco.report.csv;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.jacoco.report.MemorySingleReportOutput;
import org.jacoco.report.ReportStructureTestDriver;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Unit tests for {@link CsvFormatter}.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public class CsvFormatterTest {

	private static final String HEADER = "GROUP,BUNDLE,PACKAGE,CLASS,METHOD_COVERED,METHOD_MISSED,BLOCK_COVERED,BLOCK_MISSED,LINE_COVERED,LINE_MISSED,INSTRUCTION_COVERED,INSTRUCTION_MISSED";

	private ReportStructureTestDriver driver;

	private CsvFormatter formatter;

	private MemorySingleReportOutput output;

	@Before
	public void setup() {
		driver = new ReportStructureTestDriver();
		formatter = new CsvFormatter();
		output = new MemorySingleReportOutput();
		formatter.setReportOutput(output);
	}

	@Test
	public void testStructureWithGroup() throws IOException {
		driver.sendGroup(formatter);
		final List<String> lines = getLines();
		assertEquals(HEADER, lines.get(0));
		assertEquals(",bundle,org.jacoco.example,FooClass,0,1,0,0,0,0,0,0",
				lines.get(1));
	}

	// TODO: Trac #67
	@Ignore
	@Test
	public void testStructureWithBundleOnly() throws IOException {
		driver.sendBundle(formatter);
		final List<String> lines = getLines();
		assertEquals(HEADER, lines.get(0));
		assertEquals(",,org.jacoco.example,FooClass,0,1,0,0,0,0,0,0", lines
				.get(1));
	}

	private List<String> getLines() throws IOException {
		final BufferedReader reader = new BufferedReader(new InputStreamReader(
				output.getFileAsStream(), "UTF-8"));
		final List<String> lines = new ArrayList<String>();
		String line;
		while ((line = reader.readLine()) != null) {
			lines.add(line);
		}
		return lines;
	}
}
