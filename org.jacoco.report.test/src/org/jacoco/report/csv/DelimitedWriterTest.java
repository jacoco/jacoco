/*******************************************************************************
 * Copyright (c) 2009, 2019 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Brock Janiczak - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.report.csv;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.StringWriter;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link DelimitedWriter}
 */
public class DelimitedWriterTest {

	private StringWriter result;
	private DelimitedWriter writer;

	private static final String NEW_LINE = System.getProperty("line.separator");

	@Before
	public void setUp() {
		result = new StringWriter();
		writer = new DelimitedWriter(result);
	}

	@Test
	public void testNoWrites() throws IOException {
		assertResult("");
	}

	@Test
	public void testSingleField() throws IOException {
		writer.write("test");
		assertResult("test");
	}

	@Test
	public void testFieldContainingDelimiter() throws IOException {
		writer.write("value,1");
		assertResult("\"value,1\"");
	}

	@Test
	public void testFieldContainingDelimiterAndQuote() throws IOException {
		writer.write("\",\"");
		assertResult("\"\"\",\"\"\"");
	}

	@Test
	public void testWriteEmptyHeader() throws IOException {
		writer.write(new String[] {});
		assertResult("");
	}

	@Test
	public void testWriteHeader() throws IOException {
		writer.write("header1", "header2", "header3");
		assertResult("header1,header2,header3");
	}

	@Test
	public void testMultipleFieldsOnOneLine() throws IOException {
		writer.write("test1");
		writer.write("test2");
		assertResult("test1,test2");
	}

	@Test
	public void testMultipleFieldsOnMultipleLines() throws IOException {
		writer.write("test1");
		writer.write("test2");
		writer.nextLine();
		writer.write("test3");
		writer.write("test4");
		assertResult("test1,test2" + NEW_LINE + "test3,test4");
	}

	@Test
	public void testAutoEscapedField() throws IOException {
		writer.write("\"\"");
		assertResult("\"\"\"\"\"\"");
	}

	@Test
	public void testWordWithSpace() throws IOException {
		writer.write("space test");
		assertResult("space test");
	}

	@Test
	public void testInt() throws IOException {
		writer.write(-123000);
		assertResult("-123000");
	}

	@Test
	public void testInts() throws IOException {
		writer.write(1, 20, 300);
		assertResult("1,20,300");
	}

	private void assertResult(String expected) throws IOException {
		writer.close();
		assertEquals(expected, result.toString());
	}
}
