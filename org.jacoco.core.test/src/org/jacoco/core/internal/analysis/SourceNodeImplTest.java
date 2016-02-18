/*******************************************************************************
 * Copyright (c) 2009, 2016 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.core.internal.analysis;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.jacoco.core.analysis.EBigOFunction;
import org.jacoco.core.analysis.EBigOFunction.Type;
import org.jacoco.core.analysis.ICoverageNode;
import org.jacoco.core.analysis.ICoverageNode.ElementType;
import org.jacoco.core.analysis.ISourceNode;
import org.jacoco.core.data.ProbeMode;
import org.junit.Test;

/**
 * Unit tests for {@link SourceNodeImpl}.
 */
public class SourceNodeImplTest {

	@Test
	public void testInit() {
		final SourceNodeImpl node = new SourceNodeImpl(ElementType.CLASS, "Foo");
		assertEquals(ElementType.CLASS, node.getElementType());
		assertEquals("Foo", node.getName());
		assertEquals(ISourceNode.UNKNOWN_LINE, node.getFirstLine());
		assertEquals(ISourceNode.UNKNOWN_LINE, node.getLastLine());
		assertEquals(LineImpl.EMPTY, node.getLine(123));
		assertEquals(EBigOFunction.UNDEFINED, node.getLineEBigOFunction(123));
	}

	@Test
	public void testGetLine() {
		final SourceNodeImpl node = new SourceNodeImpl(ElementType.CLASS, "Foo");
		node.ensureCapacity(10, 20);
		assertEquals(LineImpl.EMPTY, node.getLine(5));
		assertEquals(LineImpl.EMPTY, node.getLine(15));
		assertEquals(LineImpl.EMPTY, node.getLine(25));
		assertEquals(EBigOFunction.UNDEFINED, node.getLineEBigOFunction(5));
		assertEquals(EBigOFunction.UNDEFINED, node.getLineEBigOFunction(15));
		assertEquals(EBigOFunction.UNDEFINED, node.getLineEBigOFunction(25));
	}

	@Test
	public void testEnsureCapacityUnknown1() {
		final SourceNodeImpl node = new SourceNodeImpl(ElementType.CLASS, "Foo");
		node.ensureCapacity(10, ISourceNode.UNKNOWN_LINE);
		assertEquals(LineImpl.EMPTY, node.getLine(10));
		assertEquals(EBigOFunction.UNDEFINED, node.getLineEBigOFunction(10));
	}

	@Test
	public void testEnsureCapacityUnknown2() {
		final SourceNodeImpl node = new SourceNodeImpl(ElementType.CLASS, "Foo");
		node.ensureCapacity(ISourceNode.UNKNOWN_LINE, 10);
		assertEquals(LineImpl.EMPTY, node.getLine(10));
		assertEquals(EBigOFunction.UNDEFINED, node.getLineEBigOFunction(10));
	}

	@Test
	public void testIncrementLineUnknown() {
		final SourceNodeImpl node = new SourceNodeImpl(ElementType.CLASS, "Foo");
		node.increment(CounterImpl.getInstance(1, 2, 2),
				CounterImpl.getInstance(3, 4, 4), ISourceNode.UNKNOWN_LINE);
		assertEquals(CounterImpl.getInstance(1, 2, 2),
				node.getInstructionCounter());
		assertEquals(CounterImpl.getInstance(3, 4, 4), node.getBranchCounter());
		assertEquals(CounterImpl.COUNTER_0_0, node.getLineCounter());
	}

	@Test
	public void testIncrementLines() {
		final SourceNodeImpl node = new SourceNodeImpl(ElementType.CLASS, "Foo");
		node.increment(CounterImpl.getInstance(1, 1, 0), CounterImpl.COUNTER_0_0,
				10);
		node.increment(CounterImpl.getInstance(2, 2, 0), CounterImpl.COUNTER_0_0,
				12);

		assertEquals(CounterImpl.getInstance(1, 1, 0), node.getLine(10)
				.getInstructionCounter());
		assertEquals(CounterImpl.COUNTER_0_0, node.getLine(11)
				.getInstructionCounter());
		assertEquals(CounterImpl.getInstance(2, 2, 0), node.getLine(12)
				.getInstructionCounter());
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
		final SourceNodeImpl node = new SourceNodeImpl(ElementType.CLASS, "Foo");
		node.increment(CounterImpl.getInstance(mi1, ci1, 0),
				CounterImpl.COUNTER_0_0, 33);
		node.increment(CounterImpl.getInstance(mi2, ci2, 0),
				CounterImpl.COUNTER_0_0, 33);
		assertEquals(CounterImpl.getInstance(expectedMissedLines,
				expectedCoveredLines, 0), node.getLineCounter());
		assertEquals(CounterImpl.getInstance(mi1 + mi2, ci1 + ci2, 0), node
				.getLine(33).getInstructionCounter());
	}

	@Test
	public void testIncrementChildNoLines() {
		final SourceNodeImpl node = new SourceNodeImpl(ElementType.CLASS, "Foo");
		final SourceNodeImpl child = new SourceNodeImpl(ElementType.CLASS,
				"Foo") {
			{
				this.instructionCounter = CounterImpl.getInstance(1, 11, 11);
				this.branchCounter = CounterImpl.getInstance(2, 22, 22);
				this.methodCounter = CounterImpl.getInstance(3, 33, 33);
				this.classCounter = CounterImpl.getInstance(4, 44, 44);
			}
		};
		node.increment(child);
		assertEquals(CounterImpl.getInstance(1, 11, 11),
				node.getInstructionCounter());
		assertEquals(CounterImpl.getInstance(2, 22, 22),
				node.getBranchCounter());
		assertEquals(CounterImpl.getInstance(3, 33, 33),
				node.getMethodCounter());
		assertEquals(CounterImpl.getInstance(4, 44, 44), node.getClassCounter());
	}

	@Test
	public void testIncrementChildWithLines() {
		final SourceNodeImpl node = new SourceNodeImpl(ElementType.CLASS, "Foo");

		final SourceNodeImpl child = new SourceNodeImpl(ElementType.CLASS,
				"Foo");
		child.increment(CounterImpl.getInstance(1, 11, 11),
				CounterImpl.getInstance(3, 33, 0), 5);

		node.increment(child);
		node.increment(child);

		assertEquals(CounterImpl.getInstance(2, 22, 22), node.getInstructionCounter());
		assertEquals(CounterImpl.getInstance(6, 66, 0), node.getBranchCounter());
		assertEquals(CounterImpl.getInstance(0, 2, 22), node.getLineCounter());
		assertEquals(CounterImpl.getInstance(0, 0, 0), node.getClassCounter());
	}

	@Test
	public void testProbeMode_initial() {
		SourceNodeImpl node = new SourceNodeImpl(ElementType.GROUP, "sample");
		assertNull(node.getProbeMode());
	}

	@Test
	public void testProbeMode_incrementFromNull() {
		SourceNodeImpl node = new SourceNodeImpl(ElementType.GROUP, "sample");
		ICoverageNode child = new SourceNodeImpl(ElementType.CLASS, "sample") {
			{
				probeMode = ProbeMode.parallelcount;
			}
		};
		node.increment(child);
		assertEquals(ProbeMode.parallelcount, node.getProbeMode());
	}

	@Test
	public void testProbeMode_increment1() {
		probeModeTest(ProbeMode.exists, ProbeMode.exists, ProbeMode.exists);
	}

	@Test
	public void testProbeMode_increment2() {
		probeModeTest(ProbeMode.exists, ProbeMode.count, ProbeMode.exists);
	}

	@Test
	public void testProbeMode_increment3() {
		probeModeTest(ProbeMode.exists, ProbeMode.parallelcount,
				ProbeMode.exists);
	}

	@Test
	public void testProbeMode_increment4() {
		probeModeTest(ProbeMode.count, ProbeMode.exists, ProbeMode.exists);
	}

	@Test
	public void testProbeMode_increment5() {
		probeModeTest(ProbeMode.count, ProbeMode.count, ProbeMode.count);
	}

	@Test
	public void testProbeMode_increment6() {
		probeModeTest(ProbeMode.count, ProbeMode.parallelcount, ProbeMode.count);
	}

	@Test
	public void testProbeMode_increment7() {
		probeModeTest(ProbeMode.parallelcount, ProbeMode.exists,
				ProbeMode.exists);
	}

	@Test
	public void testProbeMode_increment8() {
		probeModeTest(ProbeMode.parallelcount, ProbeMode.count, ProbeMode.count);
	}

	@Test
	public void testProbeMode_increment9() {
		probeModeTest(ProbeMode.parallelcount, ProbeMode.parallelcount,
				ProbeMode.parallelcount);
	}

	private void probeModeTest(final ProbeMode parentMode,
			final ProbeMode childMode, final ProbeMode expectedMode) {
		SourceNodeImpl node = new SourceNodeImpl(ElementType.GROUP, "sample") {
			{
				probeMode = parentMode;
			}
		};
		ICoverageNode child = new SourceNodeImpl(ElementType.CLASS, "sample") {
			{
				probeMode = childMode;
			}
		};
		node.increment(child);
		assertEquals(expectedMode, node.getProbeMode());
	}

	@Test
	public void testEBigO_initial() {
		SourceNodeImpl node = new SourceNodeImpl(ElementType.GROUP, "sample");
		assertEquals(EBigOFunction.Type.Undefined, node.getEBigOFunction()
				.getType());
		assertFalse(node.hasEBigO());
	}

	@Test
	public void testEBigO_setget() {
		SourceNodeImpl node = new SourceNodeImpl(ElementType.GROUP, "sample");
		EBigOFunction ebigo = new EBigOFunction(Type.Linear, 2, 3);
		node.setEBigOFunction(ebigo);
		assertSame(ebigo, node.getEBigOFunction());
		assertTrue(node.hasEBigO());
	}

	@Test
	public void testEBigOLine_initial() {
		SourceNodeImpl node = new SourceNodeImpl(ElementType.GROUP, "sample");
		assertEquals(ISourceNode.UNKNOWN_LINE, node.getFirstLine());
		assertEquals(EBigOFunction.UNDEFINED, node.getLineEBigOFunction(0));
		assertFalse(node.hasEBigO());
	}

	@Test
	public void testEnsureEBigOCapacityUnknown1() {
		final SourceNodeImpl node = new SourceNodeImpl(ElementType.CLASS, "Foo");
		node.ensureEBigOCapacity(10, ISourceNode.UNKNOWN_LINE);
		assertEquals(LineImpl.EMPTY, node.getLine(10));
		assertEquals(EBigOFunction.UNDEFINED, node.getLineEBigOFunction(10));
		assertFalse(node.hasEBigO());
	}

	@Test
	public void testEnsureEBigOCapacityUnknown2() {
		final SourceNodeImpl node = new SourceNodeImpl(ElementType.CLASS, "Foo");
		node.ensureEBigOCapacity(ISourceNode.UNKNOWN_LINE, 10);
		assertEquals(LineImpl.EMPTY, node.getLine(10));
		assertEquals(EBigOFunction.UNDEFINED, node.getLineEBigOFunction(10));
		assertFalse(node.hasEBigO());
	}

	@Test
	public void testLineEBigOFunction1() {
		final SourceNodeImpl node = new SourceNodeImpl(ElementType.CLASS, "Foo");
		EBigOFunction ebigo = new EBigOFunction(Type.Linear, 2, 3);
		node.setLineEBigOFunction(ebigo, 10);
		assertEquals(ebigo, node.getLineEBigOFunction(10));
		assertTrue(node.hasEBigO());
	}

	@Test
	public void testLineEBigOFunction2() {
		final SourceNodeImpl node = new SourceNodeImpl(ElementType.CLASS, "Foo");
		EBigOFunction ebigo = new EBigOFunction(Type.Linear, 2, 3);
		node.setLineEBigOFunction(ebigo, 10);
		node.increment(CounterImpl.getInstance(1, 1, 0),
				CounterImpl.COUNTER_0_0, 10);
		assertEquals(ebigo, node.getLineEBigOFunction(10));
		assertTrue(node.hasEBigO());
	}

	@Test
	public void testLineEBigOFunction3() {
		final SourceNodeImpl node = new SourceNodeImpl(ElementType.CLASS, "Foo");
		EBigOFunction ebigo = new EBigOFunction(Type.Linear, 2, 3);
		node.setLineEBigOFunction(ebigo, 10);
		node.increment(CounterImpl.getInstance(1, 1, 0),
				CounterImpl.COUNTER_0_0, 10);
		node.setLineEBigOFunction(ebigo, 11);
		node.increment(CounterImpl.getInstance(1, 1, 0),
				CounterImpl.COUNTER_0_0, 11);
		assertEquals(EBigOFunction.UNDEFINED, node.getLineEBigOFunction(9));
		assertEquals(ebigo, node.getLineEBigOFunction(10));
		assertEquals(ebigo, node.getLineEBigOFunction(11));
		assertEquals(EBigOFunction.UNDEFINED, node.getLineEBigOFunction(12));
		assertTrue(node.hasEBigO());
	}

	@Test
	public void testLineEBigOFunction4() {
		final SourceNodeImpl node = new SourceNodeImpl(ElementType.CLASS, "Foo");
		EBigOFunction ebigo = new EBigOFunction(Type.Linear, 2, 3);
		node.setLineEBigOFunction(ebigo, 10);
		node.increment(CounterImpl.getInstance(1, 1, 0),
				CounterImpl.COUNTER_0_0, 10);
		node.increment(CounterImpl.getInstance(1, 1, 0),
				CounterImpl.COUNTER_0_0, 11);
		assertEquals(EBigOFunction.UNDEFINED, node.getLineEBigOFunction(11));
		assertTrue(node.hasEBigO());
	}
}
