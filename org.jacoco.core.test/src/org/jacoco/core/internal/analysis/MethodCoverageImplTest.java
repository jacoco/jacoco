/*******************************************************************************
 * Copyright (c) 2009, 2025 Mountainminds GmbH & Co. KG and Contributors
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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.jacoco.core.analysis.ICoverageNode;
import org.junit.Test;

/**
 * Unit test for {@link MethodCoverageImpl}.
 */
public class MethodCoverageImplTest {

	@Test
	public void testProperties() {
		// Example: java.util.Collections.emptySet()
		MethodCoverageImpl node = new MethodCoverageImpl("emptySet",
				"()Ljava/util/Set;",
				"<T:Ljava/lang/Object;>()Ljava/util/Set<TT;>;");
		assertEquals(ICoverageNode.ElementType.METHOD, node.getElementType());
		assertEquals("emptySet", node.getName());
		assertEquals("()Ljava/util/Set;", node.getDesc());
		assertEquals("<T:Ljava/lang/Object;>()Ljava/util/Set<TT;>;",
				node.getSignature());
	}

	@Test
	public void testEmptyMethod() {
		ICoverageNode node = new MethodCoverageImpl("sample", "()V", null);

		assertEquals(CounterImpl.COUNTER_0_0, node.getInstructionCounter());
		assertEquals(CounterImpl.COUNTER_0_0, node.getBranchCounter());
		assertEquals(CounterImpl.COUNTER_0_0, node.getLineCounter());
		assertEquals(CounterImpl.COUNTER_0_0, node.getComplexityCounter());
		assertEquals(CounterImpl.COUNTER_0_0, node.getMethodCounter());
		assertEquals(CounterImpl.COUNTER_0_0, node.getClassCounter());
	}

	@Test
	public void testIncrementMissedInstructions() {
		MethodCoverageImpl node = new MethodCoverageImpl("sample", "()V", null);
		node.increment(CounterImpl.getInstance(25, 0), CounterImpl.COUNTER_0_0,
				3);
		node.incrementMethodCounter();
		assertEquals(CounterImpl.COUNTER_1_0, node.getMethodCounter());
		assertEquals(CounterImpl.COUNTER_1_0, node.getComplexityCounter());
	}

	@Test
	public void testIncrementCoveredInstructions() {
		MethodCoverageImpl node = new MethodCoverageImpl("sample", "()V", null);
		node.increment(CounterImpl.getInstance(12, 13), CounterImpl.COUNTER_0_0,
				3);
		node.incrementMethodCounter();
		assertEquals(CounterImpl.COUNTER_0_1, node.getMethodCounter());
		assertEquals(CounterImpl.COUNTER_0_1, node.getComplexityCounter());
	}

	@Test
	public void testIncrementComplexity1() {
		MethodCoverageImpl node = new MethodCoverageImpl("sample", "()V", null);
		node.increment(CounterImpl.COUNTER_0_0, CounterImpl.COUNTER_0_0, 3);
		assertEquals(CounterImpl.COUNTER_0_0, node.getComplexityCounter());
	}

	@Test
	public void testIncrementComplexity2() {
		MethodCoverageImpl node = new MethodCoverageImpl("sample", "()V", null);
		node.increment(CounterImpl.COUNTER_0_0, CounterImpl.getInstance(2, 0),
				3);
		assertEquals(CounterImpl.getInstance(1, 0),
				node.getComplexityCounter());
	}

	@Test
	public void testIncrementComplexity3() {
		MethodCoverageImpl node = new MethodCoverageImpl("sample", "()V", null);
		node.increment(CounterImpl.COUNTER_0_0, CounterImpl.getInstance(1, 1),
				3);
		assertEquals(CounterImpl.getInstance(1, 0),
				node.getComplexityCounter());
	}

	@Test
	public void testIncrementComplexity4() {
		MethodCoverageImpl node = new MethodCoverageImpl("sample", "()V", null);
		node.increment(CounterImpl.COUNTER_0_0, CounterImpl.getInstance(0, 2),
				3);
		assertEquals(CounterImpl.getInstance(0, 1),
				node.getComplexityCounter());
	}

	@Test
	public void testIncrementComplexity5() {
		MethodCoverageImpl node = new MethodCoverageImpl("sample", "()V", null);
		node.increment(CounterImpl.COUNTER_0_0, CounterImpl.getInstance(3, 0),
				3);
		assertEquals(CounterImpl.getInstance(2, 0),
				node.getComplexityCounter());
	}

	@Test
	public void testIncrementComplexity6() {
		MethodCoverageImpl node = new MethodCoverageImpl("sample", "()V", null);
		node.increment(CounterImpl.COUNTER_0_0, CounterImpl.getInstance(2, 1),
				3);
		assertEquals(CounterImpl.getInstance(2, 0),
				node.getComplexityCounter());
	}

	@Test
	public void testIncrementComplexity7() {
		MethodCoverageImpl node = new MethodCoverageImpl("sample", "()V", null);
		node.increment(CounterImpl.COUNTER_0_0, CounterImpl.getInstance(1, 2),
				3);
		assertEquals(CounterImpl.getInstance(1, 1),
				node.getComplexityCounter());
	}

	@Test
	public void testIncrementComplexity8() {
		MethodCoverageImpl node = new MethodCoverageImpl("sample", "()V", null);
		node.increment(CounterImpl.COUNTER_0_0, CounterImpl.getInstance(0, 3),
				3);
		assertEquals(CounterImpl.getInstance(0, 2),
				node.getComplexityCounter());
	}

	/**
	 * {@link MethodCoverageImpl#applyFragment(SourceNodeImpl)}
	 */
	@Test
	public void testApplyFragment() {
		// uncovered
		MethodCoverageImpl node = new MethodCoverageImpl("sample", "()V", null);
		node.increment(CounterImpl.COUNTER_1_0, CounterImpl.COUNTER_0_0, 42);
		node.incrementMethodCounter();
		// covered
		final SourceNodeImpl fragment = new SourceNodeImpl(null, "fragment");
		fragment.increment(CounterImpl.COUNTER_0_1, CounterImpl.COUNTER_0_0,
				42);
		assertTrue(node.applyFragment(fragment));

		assertEquals(CounterImpl.COUNTER_0_1, node.getInstructionCounter());
		assertEquals(CounterImpl.COUNTER_0_1, node.getLineCounter());
		assertEquals(CounterImpl.COUNTER_0_0, node.getBranchCounter());
		assertEquals(CounterImpl.COUNTER_0_1, node.getComplexityCounter());
		assertEquals(CounterImpl.COUNTER_0_1, node.getMethodCounter());
		final LineImpl line = node.getLine(42);
		assertEquals(CounterImpl.COUNTER_0_1, line.getInstructionCounter());
		assertEquals(CounterImpl.COUNTER_0_0, line.getBranchCounter());
	}

	/**
	 * {@link MethodCoverageImpl#applyFragment(SourceNodeImpl)}
	 */
	@Test
	public void testApplyFragment2() {
		// uncovered
		MethodCoverageImpl node = new MethodCoverageImpl("sample", "()V", null);
		node.increment(CounterImpl.COUNTER_1_0, CounterImpl.getInstance(2, 0),
				42);
		node.incrementMethodCounter();
		// covered unrelated
		final SourceNodeImpl fragment = new SourceNodeImpl(null, "fragment");
		fragment.increment(CounterImpl.COUNTER_0_1, CounterImpl.COUNTER_0_0,
				13);
		assertFalse(node.applyFragment(fragment));
		// should not change
		assertEquals(CounterImpl.COUNTER_1_0, node.getInstructionCounter());
		assertEquals(CounterImpl.COUNTER_1_0, node.getLineCounter());
		assertEquals(CounterImpl.getInstance(2, 0), node.getBranchCounter());
		assertEquals(CounterImpl.getInstance(2, 0),
				node.getComplexityCounter());
		assertEquals(CounterImpl.COUNTER_1_0, node.getMethodCounter());
		final LineImpl line = node.getLine(42);
		assertEquals(CounterImpl.COUNTER_1_0, line.getInstructionCounter());
		assertEquals(CounterImpl.getInstance(2, 0), line.getBranchCounter());
	}

}
