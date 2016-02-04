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
package org.jacoco.core.analysis;

import static org.jacoco.core.analysis.ICoverageNode.CounterEntity.BRANCH;
import static org.jacoco.core.analysis.ICoverageNode.CounterEntity.CLASS;
import static org.jacoco.core.analysis.ICoverageNode.CounterEntity.COMPLEXITY;
import static org.jacoco.core.analysis.ICoverageNode.CounterEntity.INSTRUCTION;
import static org.jacoco.core.analysis.ICoverageNode.CounterEntity.LINE;
import static org.jacoco.core.analysis.ICoverageNode.CounterEntity.METHOD;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Arrays;

import org.jacoco.core.analysis.ICoverageNode.ElementType;
import org.jacoco.core.data.ProbeMode;
import org.jacoco.core.internal.analysis.CounterImpl;
import org.junit.Test;

/**
 * Unit tests for {@link CoverageNodeImpl}.
 */
public class CoverageNodeImplTest {
	private static final double DELTA = 0.00000001D;

	@Test
	public void testProperties() {
		ICoverageNode node = new CoverageNodeImpl(ElementType.GROUP, "sample");
		assertEquals(ElementType.GROUP, node.getElementType());
		assertEquals("sample", node.getName());
	}

	@Test
	public void testInit() {
		ICoverageNode node = new CoverageNodeImpl(ElementType.GROUP, "sample");
		assertEquals(CounterImpl.COUNTER_0_0, node.getBranchCounter());
		assertEquals(CounterImpl.COUNTER_0_0, node.getInstructionCounter());
		assertEquals(CounterImpl.COUNTER_0_0, node.getLineCounter());
		assertEquals(CounterImpl.COUNTER_0_0, node.getComplexityCounter());
		assertEquals(CounterImpl.COUNTER_0_0, node.getMethodCounter());
		assertEquals(CounterImpl.COUNTER_0_0, node.getClassCounter());
	}

	@Test
	public void testIncrement() {
		CoverageNodeImpl parent = new CoverageNodeImpl(ElementType.GROUP,
				"sample");
		ICoverageNode child = new CoverageNodeImpl(ElementType.GROUP, "sample") {
			{
				instructionCounter = CounterImpl.getInstance(1, 41, 41);
				branchCounter = CounterImpl.getInstance(10, 15, 15);
				lineCounter = CounterImpl.getInstance(5, 3, 3);
				complexityCounter = CounterImpl.getInstance(4, 2, 2);
				methodCounter = CounterImpl.getInstance(1, 21, 21);
				classCounter = CounterImpl.getInstance(1, 11, 11);
			}
		};
		parent.increment(child);
		assertEquals(CounterImpl.getInstance(1, 41, 41),
				parent.getCounter(INSTRUCTION));
		assertEquals(CounterImpl.getInstance(1, 41, 41),
				parent.getInstructionCounter());
		assertEquals(CounterImpl.getInstance(10, 15, 15),
				parent.getCounter(BRANCH));
		assertEquals(CounterImpl.getInstance(10, 15, 15),
				parent.getBranchCounter());
		assertEquals(CounterImpl.getInstance(5, 3, 3), parent.getCounter(LINE));
		assertEquals(CounterImpl.getInstance(5, 3, 3), parent.getLineCounter());
		assertEquals(CounterImpl.getInstance(4, 2, 2),
				parent.getCounter(COMPLEXITY));
		assertEquals(CounterImpl.getInstance(4, 2, 2),
				parent.getComplexityCounter());
		assertEquals(CounterImpl.getInstance(1, 21, 21),
				parent.getCounter(METHOD));
		assertEquals(CounterImpl.getInstance(1, 21, 21),
				parent.getMethodCounter());
		assertEquals(CounterImpl.getInstance(1, 11, 11),
				parent.getCounter(CLASS));
		assertEquals(CounterImpl.getInstance(1, 11, 11),
				parent.getClassCounter());
	}

	@Test
	public void testIncrementCollection() {
		CoverageNodeImpl parent = new CoverageNodeImpl(ElementType.GROUP,
				"sample");
		ICoverageNode child1 = new CoverageNodeImpl(ElementType.GROUP, "sample") {
			{
				branchCounter = CounterImpl.getInstance(5, 2, 2);
			}
		};
		ICoverageNode child2 = new CoverageNodeImpl(ElementType.GROUP, "sample") {
			{
				branchCounter = CounterImpl.getInstance(3, 3, 3);
			}
		};
		parent.increment(Arrays.asList(child1, child2));
		assertEquals(CounterImpl.getInstance(8, 5, 5),
				parent.getBranchCounter());
	}

	@Test
	public void testProbeMode_initial() {
		CoverageNodeImpl node = new CoverageNodeImpl(ElementType.GROUP,
				"sample");
		assertNull(node.getProbeMode());
	}

	@Test
	public void testProbeMode_incrementFromNull() {
		CoverageNodeImpl node = new CoverageNodeImpl(ElementType.GROUP,
				"sample");
		ICoverageNode child = new CoverageNodeImpl(ElementType.CLASS, "sample") {
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
		CoverageNodeImpl node = new CoverageNodeImpl(ElementType.GROUP,
				"sample") {
			{
				probeMode = parentMode;
			}
		};
		ICoverageNode child = new CoverageNodeImpl(ElementType.CLASS, "sample") {
			{
				probeMode = childMode;
			}
		};
		node.increment(child);
		assertEquals(expectedMode, node.getProbeMode());
	}

	@Test
	public void testParallelPercent1() {
		ICoverageNode node = new CoverageNodeImpl(ElementType.GROUP, "sample") {
			{
				instructionCounter = CounterImpl.getInstance(1, 41, 0);
				branchCounter = CounterImpl.getInstance(10, 15, 15);
			}
		};
		assertEquals(0, node.getParallelPercent(), DELTA);
	}

	@Test
	public void testParallelPercent2() {
		ICoverageNode node = new CoverageNodeImpl(ElementType.GROUP, "sample") {
			{
				instructionCounter = CounterImpl.getInstance(1, 41, 41);
				branchCounter = CounterImpl.getInstance(10, 15, 0);
			}
		};
		assertEquals(0, node.getParallelPercent(), DELTA);
	}

	@Test
	public void testParallelPercent3() {
		ICoverageNode node = new CoverageNodeImpl(ElementType.GROUP, "sample") {
			{
				instructionCounter = CounterImpl.getInstance(1, 41, 41);
				branchCounter = CounterImpl.getInstance(10, 15, 15);
			}
		};
		assertEquals((double) 1500 / (double) 41, node.getParallelPercent(),
				DELTA);
	}

	@Test
	public void testGetPlainCopy() {
		ICoverageNode node = new CoverageNodeImpl(ElementType.CLASS, "Sample") {
			{
				classCounter = CounterImpl.getInstance(1, 1, 1);
				methodCounter = CounterImpl.getInstance(2, 2, 2);
				branchCounter = CounterImpl.getInstance(3, 3, 3);
				instructionCounter = CounterImpl.getInstance(4, 4, 4);
				lineCounter = CounterImpl.getInstance(5, 5, 5);
				complexityCounter = CounterImpl.getInstance(6, 6, 6);
				probeMode = ProbeMode.parallelcount;
			}
		};
		ICoverageNode copy = node.getPlainCopy();
		assertEquals(ElementType.CLASS, copy.getElementType());
		assertEquals("Sample", copy.getName());
		assertEquals(CounterImpl.getInstance(1, 1, 1), copy.getClassCounter());
		assertEquals(CounterImpl.getInstance(2, 2, 2), copy.getMethodCounter());
		assertEquals(CounterImpl.getInstance(3, 3, 3), copy.getBranchCounter());
		assertEquals(CounterImpl.getInstance(4, 4, 4),
				copy.getInstructionCounter());
		assertEquals(CounterImpl.getInstance(5, 5, 5), copy.getLineCounter());
		assertEquals(CounterImpl.getInstance(6, 6, 6),
				copy.getComplexityCounter());
		assertEquals(ProbeMode.parallelcount, copy.getProbeMode());
	}

	@Test
	public void testToString() {
		CoverageNodeImpl node = new CoverageNodeImpl(ElementType.CLASS, "Test");
		assertEquals("Test [CLASS]", node.toString());
	}

}
