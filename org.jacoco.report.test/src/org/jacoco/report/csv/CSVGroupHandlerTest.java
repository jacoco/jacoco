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

import java.io.BufferedReader;
import java.io.StringReader;
import java.io.StringWriter;

import org.jacoco.report.IReportGroupVisitor;
import org.jacoco.report.JavaNames;
import org.jacoco.report.ReportStructureTestDriver;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link CSVGroupHandler}.
 */
public class CSVGroupHandlerTest {

	private IReportGroupVisitor handler;

	private StringWriter result;

	private ReportStructureTestDriver driver;

	@Before
	public void setup() throws Exception {
		result = new StringWriter();
		final DelimitedWriter dw = new DelimitedWriter(result);
		final ClassRowWriter rw = new ClassRowWriter(dw, new JavaNames());
		handler = new CSVGroupHandler(rw);
		driver = new ReportStructureTestDriver();
	}

	@Test
	public void testVisitBundle() throws Exception {
		driver.sendBundle(handler);
		final BufferedReader reader = getResultReader();
		reader.readLine();
		assertEquals("bundle,org.jacoco.example,FooClass,10,15,1,2,0,3,1,2,0,1",
				reader.readLine());
		assertEquals("no more lines expected", null, reader.readLine());
	}

	@Test
	public void testVisitGroup() throws Exception {
		driver.sendGroup(handler);
		final BufferedReader reader = getResultReader();
		reader.readLine();
		assertEquals(
				"group/bundle,org.jacoco.example,FooClass,10,15,1,2,0,3,1,2,0,1",
				reader.readLine());
		assertEquals("no more lines expected", null, reader.readLine());
	}

	private BufferedReader getResultReader() {
		return new BufferedReader(new StringReader(result.toString()));
	}

}
