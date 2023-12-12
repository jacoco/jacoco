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
package org.jacoco.core.internal.analysis;

import static org.junit.Assert.assertEquals;

import org.jacoco.core.analysis.ICoverageNode.ElementType;
import org.jacoco.core.analysis.ISourceNode;
import org.junit.Test;

/**
 * Unit tests for {@link SourceNodeImpl}.
 */
public class SourceNodeImplTest {

	@Test
	public void testInit() {
		final SourceNodeImpl node = new SourceNodeImpl(ElementType.CLASS,
				"Foo");
		assertEquals(ElementType.CLASS, node.getElementType());
		assertEquals("Foo", node.getName());
		assertEquals(ISourceNode.UNKNOWN_LINE, node.getFirstLine());
		assertEquals(ISourceNode.UNKNOWN_LINE, node.getLastLine());
		assertEquals(LineImpl.EMPTY, node.getLine(123));
	}

	@Test
	public void testGetLine() {
		final SourceNodeImpl node = new SourceNodeImpl(ElementType.CLASS,
				"Foo");
		node.ensureCapacity(10, 20);
		assertEquals(LineImpl.EMPTY, node.getLine(5));
		assertEquals(LineImpl.EMPTY, node.getLine(15));
		assertEquals(LineImpl.EMPTY, node.getLine(25));
	}

	@Test
	public void testEnsureCapacityUnknown1() {
		final SourceNodeImpl node = new SourceNodeImpl(ElementType.CLASS,
				"Foo");
		node.ensureCapacity(10, ISourceNode.UNKNOWN_LINE);
		assertEquals(LineImpl.EMPTY, node.getLine(10));
	}

	@Test
	public void testEnsureCapacityUnknown2() {
		final SourceNodeImpl node = new SourceNodeImpl(ElementType.CLASS,
				"Foo");
		node.ensureCapacity(ISourceNode.UNKNOWN_LINE, 10);
		assertEquals(LineImpl.EMPTY, node.getLine(10));
	}

	@Test
	public void testIncrementLineUnknown() {
		final SourceNodeImpl node = new SourceNodeImpl(ElementType.CLASS,
				"Foo");
		node.increment(CounterImpl.getInstance(1, 2),
				CounterImpl.getInstance(3, 4), ISourceNode.UNKNOWN_LINE);
		assertEquals(CounterImpl.getInstance(1, 2),
				node.getInstructionCounter());
		assertEquals(CounterImpl.getInstance(3, 4), node.getBranchCounter());
		assertEquals(CounterImpl.COUNTER_0_0, node.getLineCounter());
	}

	@Test
	public void testIncrementLines() {
		final SourceNodeImpl node = new SourceNodeImpl(ElementType.CLASS,
				"Foo");
		node.increment(CounterImpl.getInstance(1, 1), CounterImpl.COUNTER_0_0,
				10);
		node.increment(CounterImpl.getInstance(2, 2), CounterImpl.COUNTER_0_0,
				12);

		assertEquals(CounterImpl.getInstance(1, 1),
				node.getLine(10).getInstructionCounter());
		assertEquals(CounterImpl.COUNTER_0_0,
				node.getLine(11).getInstructionCounter());
		assertEquals(CounterImpl.getInstance(2, 2),
				node.getLine(12).getInstructionCounter());
	}

	@Test
	public void testIncrementLine1_1() {
		testIncrementLine(0, 0, 0, 0, 0, 0);
	}

	@Test
	public void testIncrementLine1_2() {
		testIncrementLine(0, 0, 5, 0, 1, 0);
	}

	@Test
	public void testIncrementLine1_3() {
		testIncrementLine(0, 0, 0, 5, 0, 1);
	}

	@Test
	public void testIncrementLine1_4() {
		testIncrementLine(0, 0, 5, 5, 0, 1);
	}

	@Test
	public void testIncrementLine2_1() {
		testIncrementLine(3, 0, 0, 0, 1, 0);
	}

	@Test
	public void testIncrementLine2_2() {
		testIncrementLine(3, 0, 5, 0, 1, 0);
	}

	@Test
	public void testIncrementLine2_3() {
		testIncrementLine(3, 0, 0, 5, 0, 1);
	}

	@Test
	public void testIncrementLine2_4() {
		testIncrementLine(3, 0, 5, 5, 0, 1);
	}

	@Test
	public void testIncrementLine3_1() {
		testIncrementLine(0, 3, 0, 0, 0, 1);
	}

	@Test
	public void testIncrementLine3_2() {
		testIncrementLine(0, 3, 5, 0, 0, 1);
	}

	@Test
	public void testIncrementLine3_3() {
		testIncrementLine(0, 3, 0, 5, 0, 1);
	}

	@Test
	public void testIncrementLine3_4() {
		testIncrementLine(0, 3, 5, 5, 0, 1);
	}

	@Test
	public void testIncrementLine4_1() {
		testIncrementLine(3, 3, 0, 0, 0, 1);
	}

	@Test
	public void testIncrementLine4_2() {
		testIncrementLine(3, 3, 5, 0, 0, 1);
	}

	@Test
	public void testIncrementLine4_3() {
		testIncrementLine(3, 3, 0, 5, 0, 1);
	}

	@Test
	public void testIncrementLine4_4() {
		testIncrementLine(3, 3, 5, 5, 0, 1);
	}

	private void testIncrementLine(int mi1, int ci1, int mi2, int ci2,
			int expectedMissedLines, int expectedCoveredLines) {
		final SourceNodeImpl node = new SourceNodeImpl(ElementType.CLASS,
				"Foo");
		node.increment(CounterImpl.getInstance(mi1, ci1),
				CounterImpl.COUNTER_0_0, 33);
		node.increment(CounterImpl.getInstance(mi2, ci2),
				CounterImpl.COUNTER_0_0, 33);
		assertEquals(CounterImpl.getInstance(expectedMissedLines,
				expectedCoveredLines), node.getLineCounter());
		assertEquals(CounterImpl.getInstance(mi1 + mi2, ci1 + ci2),
				node.getLine(33).getInstructionCounter());
	}

	@Test
	public void testIncrementChildNoLines() {
		final SourceNodeImpl node = new SourceNodeImpl(ElementType.CLASS,
				"Foo");
		final SourceNodeImpl child = new SourceNodeImpl(ElementType.CLASS,
				"Foo") {
			{
				this.instructionCounter = CounterImpl.getInstance(1, 11);
				this.branchCounter = CounterImpl.getInstance(2, 22);
				this.methodCounter = CounterImpl.getInstance(3, 33);
				this.classCounter = CounterImpl.getInstance(4, 44);
			}
		};
		node.increment(child);
		assertEquals(CounterImpl.getInstance(1, 11),
				node.getInstructionCounter());
		assertEquals(CounterImpl.getInstance(2, 22), node.getBranchCounter());
		assertEquals(CounterImpl.getInstance(3, 33), node.getMethodCounter());
		assertEquals(CounterImpl.getInstance(4, 44), node.getClassCounter());
	}

	@Test
	public void testIncrementChildWithLines() {
		final SourceNodeImpl node = new SourceNodeImpl(ElementType.CLASS,
				"Foo");

		final SourceNodeImpl child = new SourceNodeImpl(ElementType.CLASS,
				"Foo");
		child.increment(CounterImpl.getInstance(1, 11),
				CounterImpl.getInstance(3, 33), 5);

		node.increment(child);
		node.increment(child);

		assertEquals(CounterImpl.getInstance(2, 22),
				node.getInstructionCounter());
		assertEquals(CounterImpl.getInstance(6, 66), node.getBranchCounter());
		assertEquals(CounterImpl.getInstance(0, 1), node.getLineCounter());
	}

}
