/*******************************************************************************
 * Copyright (c) 2009 Mountainminds GmbH & Co. KG and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Brock Janiczak - initial API and implementation
 *    
 * $Id: $
 *******************************************************************************/
package org.jacoco.report.csv;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.StringWriter;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link DelimitedWriter}
 * 
 * @author Brock Janiczak
 * @version $Revision: $
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
		writer.writeField("test");
		assertResult("test");
	}

	@Test
	public void testFieldContainingDelimiter() throws IOException {
		writer.writeField("value,1");
		assertResult("\"value,1\"");
	}

	@Test
	public void testFieldContainingDelimiterAndQuote() throws IOException {
		writer.writeField("\",\"");
		assertResult("\"\"\",\"\"\"");
	}

	@Test
	public void testWriteEmptyHeader() throws IOException {
		writer.writeFields();
		assertResult("");
	}

	@Test
	public void testWriteHeader() throws IOException {
		writer.writeFields("header1", "header2", "header3");
		assertResult("header1,header2,header3");
	}

	@Test
	public void testMultipleFieldsOnOneLine() throws IOException {
		writer.writeField("test1");
		writer.writeField("test2");
		assertResult("test1,test2");
	}

	@Test
	public void testMultipleFieldsOnMultipleLines() throws IOException {
		writer.writeField("test1");
		writer.writeField("test2");
		writer.nextLine();
		writer.writeField("test3");
		writer.writeField("test4");
		assertResult("test1,test2" + NEW_LINE + "test3,test4");
	}

	@Test
	public void testAutoEscapedField() throws IOException {
		writer.writeField("\"\"");
		assertResult("\"\"\"\"\"\"");
	}

	@Test
	public void testWordWithSpace() throws IOException {
		writer.writeField("space test");
		assertResult("space test");
	}

	private void assertResult(String expected) throws IOException {
		writer.close();
		assertEquals(expected, result.toString());
	}
}
