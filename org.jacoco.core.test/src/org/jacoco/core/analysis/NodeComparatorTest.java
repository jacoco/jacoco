/*******************************************************************************
 * Copyright (c) 2009, 2011 Mountainminds GmbH & Co. KG and Contributors
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
 * 
 * @author Marc R. Hoffmann
 * @version $qualified.bundle.version$
 */
public class NodeComparatorTest {

	@Test
	public void testSort() {
		ICoverageNode d1 = new MockBlockData(18);
		ICoverageNode d2 = new MockBlockData(21);
		ICoverageNode d3 = new MockBlockData(30);
		ICoverageNode d4 = new MockBlockData(60);
		ICoverageNode d5 = new MockBlockData(99);
		final List<ICoverageNode> result = CounterComparator.TOTALITEMS.on(
				CounterEntity.INSTRUCTION).sort(
				Arrays.asList(d3, d5, d1, d4, d2));
		assertEquals(Arrays.asList(d1, d2, d3, d4, d5), result);
	}

	@Test
	public void testSecond1() {
		ICoverageNode d1 = new MockBlockLineData(5, 30);
		ICoverageNode d2 = new MockBlockLineData(3, 80);
		final NodeComparator c1 = CounterComparator.TOTALITEMS
				.on(CounterEntity.INSTRUCTION);
		final NodeComparator c2 = CounterComparator.TOTALITEMS
				.on(CounterEntity.LINE);
		assertTrue(c1.second(c2).compare(d1, d2) > 0);
	}

	@Test
	public void testSecond2() {
		ICoverageNode d1 = new MockBlockLineData(5, 30);
		ICoverageNode d2 = new MockBlockLineData(5, 80);
		final NodeComparator c1 = CounterComparator.TOTALITEMS
				.on(CounterEntity.INSTRUCTION);
		final NodeComparator c2 = CounterComparator.TOTALITEMS
				.on(CounterEntity.LINE);
		assertTrue(c1.second(c2).compare(d1, d2) < 0);
	}

	private static final class MockBlockData extends CoverageNodeImpl {
		MockBlockData(int total) {
			super(GROUP, "mock");
			instructionCounter = CounterImpl.getInstance(total, 0);
		}
	}

	private static final class MockBlockLineData extends CoverageNodeImpl {
		MockBlockLineData(int totalInstruction, int totalLine) {
			super(GROUP, "mock");
			instructionCounter = CounterImpl.getInstance(totalInstruction, 0);
			lineCounter = CounterImpl.getInstance(totalLine, 0);
		}
	}
}
