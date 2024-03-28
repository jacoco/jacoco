/*******************************************************************************
 * Copyright (c) 2009, 2019 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.report.internal.xml;

import static junit.framework.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.jacoco.core.analysis.ICoverageNode;
import org.jacoco.core.internal.analysis.CounterImpl;
import org.jacoco.core.internal.analysis.LineImpl;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit test for {@link ReportElement}.
 */
public class ReportElementTest {

	private ByteArrayOutputStream buffer;

	private ReportElement root;

	@Before
	public void setup() throws Exception {
		buffer = new ByteArrayOutputStream();
		root = new ReportElement("sourcefile", buffer, "UTF-8");
	}

	@Test
	public void should_not_write_zeros_for_line_attributes() throws Exception {
		root.line(1, LineImpl.EMPTY.increment(CounterImpl.COUNTER_1_0,
				CounterImpl.COUNTER_0_0));
		root.line(2, LineImpl.EMPTY.increment(CounterImpl.COUNTER_0_1,
				CounterImpl.COUNTER_0_0));
		root.line(3, LineImpl.EMPTY.increment(CounterImpl.COUNTER_0_0,
				CounterImpl.COUNTER_1_0));
		root.line(4, LineImpl.EMPTY.increment(CounterImpl.COUNTER_0_0,
				CounterImpl.COUNTER_0_1));
		root.line(5, LineImpl.EMPTY);

		assertTrue(actual().contains("<line nr=\"1\" mi=\"1\"/>"));
		assertTrue(actual().contains("<line nr=\"2\" ci=\"1\"/>"));
		assertTrue(actual().contains("<line nr=\"3\" mb=\"1\"/>"));
		assertTrue(actual().contains("<line nr=\"4\" cb=\"1\"/>"));
		assertTrue(actual().contains("<line nr=\"5\"/>"));
	}

	@Test
	public void should_not_write_zeros_for_counter_attributes()
			throws Exception {
		root.counter(ICoverageNode.CounterEntity.CLASS,
				CounterImpl.COUNTER_0_0);

		assertTrue(actual().contains("<counter type=\"CLASS\"/>"));
	}

	private String actual() throws IOException {
		root.close();
		return buffer.toString("UTF-8");
	}

}