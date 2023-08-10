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
package org.jacoco.core.test.validation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import org.jacoco.core.internal.analysis.CounterImpl;
import org.jacoco.core.internal.analysis.SourceFileCoverageImpl;
import org.jacoco.core.test.validation.Source.Line;
import org.junit.Test;

/**
 * Unit tests for {@link Source}.
 */
public class SourceTest {

	@Test
	public void should_parse_lines() throws IOException {
		String src = "aaa\nbbb\n;";

		final Source s = new Source(new StringReader(src),
				new SourceFileCoverageImpl("Foo", "foo"));

		List<Line> lines = s.getLines();
		assertEquals(3, lines.size());
		assertEquals("aaa", lines.get(0).getText());
		assertEquals("bbb", lines.get(1).getText());
		assertEquals(";", lines.get(2).getText());
	}

	@Test
	public void should_parse_empty_lines() throws IOException {
		String src = "\naaa\n\nbbb\n";

		final Source s = new Source(new StringReader(src),
				new SourceFileCoverageImpl("Foo", "foo"));

		List<Line> lines = s.getLines();
		assertEquals(4, lines.size());
		assertEquals("", lines.get(0).getText());
		assertEquals("aaa", lines.get(1).getText());
		assertEquals("", lines.get(2).getText());
		assertEquals("bbb", lines.get(3).getText());
	}

	@Test
	public void should_parse_crnl_separator() throws IOException {
		String src = "aaa\r\nbbb";

		final Source s = new Source(new StringReader(src),
				new SourceFileCoverageImpl("Foo", "foo"));

		List<Line> lines = s.getLines();
		assertEquals(2, lines.size());
		assertEquals("aaa", lines.get(0).getText());
		assertEquals("bbb", lines.get(1).getText());
	}

	@Test
	public void should_calculate_line_numbers() throws IOException {
		String src = "a\nb\nc";

		final Source s = new Source(new StringReader(src),
				new SourceFileCoverageImpl("Foo", "foo"));

		List<Line> lines = s.getLines();
		assertEquals(3, lines.size());
		assertEquals(1, lines.get(0).getNr());
		assertEquals(2, lines.get(1).getNr());
		assertEquals(3, lines.get(2).getNr());
	}

	@Test
	public void line_should_implement_toString() throws IOException {
		String src = "a\nb";

		final Source s = new Source(new StringReader(src),
				new SourceFileCoverageImpl("Foo", "foo"));

		List<Line> lines = s.getLines();
		assertEquals(2, lines.size());
		assertEquals("Foo:1", lines.get(0).toString());
		assertEquals("Foo:2", lines.get(1).toString());
	}

	@Test
	public void line_should_provide_corresponding_coverage()
			throws IOException {
		String src = "a\nb\nc";
		SourceFileCoverageImpl sc = new SourceFileCoverageImpl("Foo", "foo");
		sc.increment(CounterImpl.getInstance(1, 0), CounterImpl.COUNTER_0_0, 1);
		sc.increment(CounterImpl.getInstance(2, 0), CounterImpl.COUNTER_0_0, 2);
		sc.increment(CounterImpl.getInstance(3, 0), CounterImpl.COUNTER_0_0, 3);

		final Source s = new Source(new StringReader(src), sc);

		List<Line> lines = s.getLines();
		assertEquals(3, lines.size());
		assertEquals(1, lines.get(0).getCoverage().getInstructionCounter()
				.getMissedCount());
		assertEquals(2, lines.get(1).getCoverage().getInstructionCounter()
				.getMissedCount());
		assertEquals(3, lines.get(2).getCoverage().getInstructionCounter()
				.getMissedCount());
	}

	@Test
	public void line_should_return_comment() throws IOException {
		String src = "aaa\nbbb // test()\n}//nospaces()\n/* http://jacoco.org/ */";

		final Source s = new Source(new StringReader(src),
				new SourceFileCoverageImpl("Foo", "foo"));

		List<Line> lines = s.getLines();
		assertEquals(4, lines.size());
		assertNull(lines.get(0).getComment());
		assertEquals(" test()", lines.get(1).getComment());
		assertEquals("nospaces()", lines.get(2).getComment());
		assertNull(lines.get(3).getComment());
	}

}
