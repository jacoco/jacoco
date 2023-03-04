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
package org.jacoco.core.analysis;

import static org.jacoco.core.analysis.ICoverageNode.ElementType.GROUP;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.jacoco.core.analysis.ICoverageNode.CounterEntity;
import org.jacoco.core.internal.analysis.CounterImpl;
import org.junit.Test;

/**
 * Unit test for {@link NodeComparator}.
 */
public class NodeComparatorTest {

	@Test
	public void testSort() {
		ICoverageNode d1 = new MockNode(18);
		ICoverageNode d2 = new MockNode(21);
		ICoverageNode d3 = new MockNode(30);
		ICoverageNode d4 = new MockNode(60);
		ICoverageNode d5 = new MockNode(99);
		final List<ICoverageNode> result = CounterComparator.TOTALITEMS
				.on(CounterEntity.INSTRUCTION)
				.sort(Arrays.asList(d3, d5, d1, d4, d2));
		assertEquals(Arrays.asList(d1, d2, d3, d4, d5), result);
	}

	@Test
	public void testSecond1() {
		ICoverageNode d1 = new MockLineData(5, 30);
		ICoverageNode d2 = new MockLineData(3, 80);
		final NodeComparator c1 = CounterComparator.TOTALITEMS
				.on(CounterEntity.INSTRUCTION);
		final NodeComparator c2 = CounterComparator.TOTALITEMS
				.on(CounterEntity.LINE);
		assertTrue(c1.second(c2).compare(d1, d2) > 0);
	}

	@Test
	public void testSecond2() {
		ICoverageNode d1 = new MockLineData(5, 30);
		ICoverageNode d2 = new MockLineData(5, 80);
		final NodeComparator c1 = CounterComparator.TOTALITEMS
				.on(CounterEntity.INSTRUCTION);
		final NodeComparator c2 = CounterComparator.TOTALITEMS
				.on(CounterEntity.LINE);
		assertTrue(c1.second(c2).compare(d1, d2) < 0);
	}

	private static final class MockNode extends CoverageNodeImpl {
		MockNode(int total) {
			super(GROUP, "mock");
			instructionCounter = CounterImpl.getInstance(total, 0);
		}
	}

	private static final class MockLineData extends CoverageNodeImpl {
		MockLineData(int totalInstruction, int totalLine) {
			super(GROUP, "mock");
			instructionCounter = CounterImpl.getInstance(totalInstruction, 0);
			lineCounter = CounterImpl.getInstance(totalLine, 0);
		}
	}
}
