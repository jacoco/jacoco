/*******************************************************************************
 * Copyright (c) 2009, 2018 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.examples;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;

import org.junit.Rule;
import org.junit.Test;

/**
 * Unit test for {@link XMLDiff}.
 */
public class XMLDiffTest {

	@Rule
	public ConsoleOutput console = new ConsoleOutput();

	@Test
	public void should_compare() {
		final Map<String, Map<Integer, XMLDiff.LineInfo>> left = new HashMap<String, Map<Integer, XMLDiff.LineInfo>>();
		left.put("OnlyInLeft.java", new HashMap<Integer, XMLDiff.LineInfo>());
		final Map<Integer, XMLDiff.LineInfo> leftLines = new HashMap<Integer, XMLDiff.LineInfo>();
		leftLines.put(1, new XMLDiff.LineInfo(1, 2, 3, 4));
		leftLines.put(2, new XMLDiff.LineInfo(1, 2, 3, 4));
		// no line 3
		leftLines.put(4, new XMLDiff.LineInfo(1, 2, 3, 4));
		left.put("InBoth.java", leftLines);

		final Map<String, Map<Integer, XMLDiff.LineInfo>> right = new HashMap<String, Map<Integer, XMLDiff.LineInfo>>();
		right.put("OnlyInRight.java", new HashMap<Integer, XMLDiff.LineInfo>());
		final Map<Integer, XMLDiff.LineInfo> rightLines = new HashMap<Integer, XMLDiff.LineInfo>();
		rightLines.put(1, new XMLDiff.LineInfo(1, 2, 3, 4));
		// no line 2
		rightLines.put(3, new XMLDiff.LineInfo(1, 2, 3, 4));
		rightLines.put(4, new XMLDiff.LineInfo(4, 3, 2, 1));
		right.put("InBoth.java", rightLines);

		XMLDiff.compare(console.stream, left, right);

		console.expect(ConsoleOutput.containsLine("-OnlyInLeft.java"));
		console.expect(ConsoleOutput.containsLine("+OnlyInRight.java"));
		console.expect(ConsoleOutput
				.containsLine(" InBoth.java:2 mi: -1 ci: -2 mb: -3 cb: -4"));
		console.expect(ConsoleOutput
				.containsLine(" InBoth.java:3 mi: +1 ci: +2 mb: +3 cb: +4"));
		console.expect(ConsoleOutput.containsLine(
				" InBoth.java:4 mi: -1 +4 ci: -2 +3 mb: -3 +2 cb: -4 +1"));
	}

	@Test
	public void should_read() throws Exception {
		final String xml = "" //
				+ "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" //
				+ "<!DOCTYPE report PUBLIC \"-//JACOCO//DTD Report 1.1//EN\" \"report.dtd\">" //
				+ "<report>" //
				+ "  <group>" //
				+ "    <package name='org/jacoco/examples'>" //
				+ "      <sourcefile name='Example.java'>" //
				+ "        <line nr='1' mi='2' ci='3' mb='4' cb='5' />" //
				+ "        <line nr='2' />" //
				+ "      </sourcefile>" //
				+ "    </package>" //
				+ "  </group>" //
				+ "</report>"; //

		final Map<String, Map<Integer, XMLDiff.LineInfo>> r = XMLDiff
				.read(new ByteArrayInputStream(xml.getBytes()));
		final Map<Integer, XMLDiff.LineInfo> c = r
				.get("org/jacoco/examples/Example.java");

		final XMLDiff.LineInfo line1 = c.get(1);
		assertEquals(2, line1.mi);
		assertEquals(3, line1.ci);
		assertEquals(4, line1.mb);
		assertEquals(5, line1.cb);

		final XMLDiff.LineInfo line2 = c.get(2);
		assertEquals(0, line2.mi);
		assertEquals(0, line2.ci);
		assertEquals(0, line2.mb);
		assertEquals(0, line2.cb);

		assertEquals(2, c.size());
		assertEquals(1, r.size());
	}

	@Test
	public void should_not_read_report_with_duplicate_source_files()
			throws Exception {
		final String xml = "" //
				+ "<report>" //
				+ "  <group>" //
				+ "    <package name='org/jacoco/examples'>" //
				+ "      <sourcefile name='Example.java' />" //
				+ "    </package>" //
				+ "  </group>" //
				+ "  <group>" //
				+ "    <package name='org/jacoco/examples'>" //
				+ "      <sourcefile name='Example.java' />" //
				+ "    </package>" //
				+ "  </group>" //
				+ "</report>"; //

		try {
			XMLDiff.read(new ByteArrayInputStream(xml.getBytes()));
			fail("exception expected");
		} catch (IllegalStateException e) {
			assertEquals("duplicate org/jacoco/examples/Example.java",
					e.getMessage());
		}
	}

}
