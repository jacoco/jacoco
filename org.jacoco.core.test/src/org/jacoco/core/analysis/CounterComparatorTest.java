/*******************************************************************************
 * Copyright (c) 2009 Mountainminds GmbH & Co. KG and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 * $Id: $
 *******************************************************************************/
package org.jacoco.core.analysis;

import static org.jacoco.core.analysis.ICoverageNode.ElementType.CUSTOM;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.jacoco.core.analysis.ICoverageNode.CounterEntity;
import org.junit.Test;

/**
 * Unit tests for {@link CounterComparator}.
 * 
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public class CounterComparatorTest {

	@Test
	public void testTotalItemsComparator() {
		final Comparator<ICounter> cmp = CounterComparator.TOTALITEMS;
		assertCmpLess(cmp, 24, 5, 25, 6);
		assertCmpEquals(cmp, 25, 5, 25, 6);
		assertCmpGreater(cmp, 26, 5, 25, 6);
	}

	@Test
	public void testCoveredItemsComparator() {
		final Comparator<ICounter> cmp = CounterComparator.COVEREDITEMS;
		assertCmpLess(cmp, 80, 7, 50, 8);
		assertCmpEquals(cmp, 50, 8, 90, 8);
		assertCmpGreater(cmp, 30, 9, 40, 8);
	}

	@Test
	public void testNotCoveredItemsComparator() {
		final Comparator<ICounter> cmp = CounterComparator.NOTCOVEREDITEMS;
		assertCmpLess(cmp, 50, 40, 91, 80);
		assertCmpEquals(cmp, 50, 40, 90, 80);
		assertCmpGreater(cmp, 50, 39, 90, 80);
	}

	@Test
	public void testCoveredRatioComparator() {
		final Comparator<ICounter> cmp = CounterComparator.COVEREDRATIO;
		assertCmpLess(cmp, 50, 25, 90, 46);
		assertCmpEquals(cmp, 50, 10, 80, 16);
		assertCmpGreater(cmp, 50, 25, 90, 44);
	}

	@Test
	public void testNotCoveredRatioComparator() {
		final Comparator<ICounter> cmp = CounterComparator.NOTCOVEREDRATIO;
		assertCmpLess(cmp, 50, 25, 90, 44);
		assertCmpEquals(cmp, 50, 10, 80, 16);
		assertCmpGreater(cmp, 50, 25, 90, 46);
	}

	@Test
	public void testReverseComparator() {
		final Comparator<ICounter> cmp = CounterComparator.TOTALITEMS.reverse();
		assertCmpGreater(cmp, 24, 5, 25, 6);
		assertCmpEquals(cmp, 25, 5, 25, 6);
		assertCmpLess(cmp, 26, 5, 25, 6);
	}

	@Test
	public void testDataComparator1() {
		ICoverageNode d1 = new MockBlockData(18);
		ICoverageNode d2 = new MockBlockData(15);
		final Comparator<ICoverageNode> cmp = CounterComparator.TOTALITEMS
				.getDataComparator(CounterEntity.BLOCK);
		assertTrue(cmp.compare(d1, d2) > 0);
	}

	@Test
	public void testDataComparator2() {
		ICoverageNode d1 = new MockBlockData(18);
		ICoverageNode d2 = new MockBlockData(15);
		final Comparator<ICoverageNode> cmp = CounterComparator.TOTALITEMS
				.getDataComparator(CounterEntity.LINE);
		assertEquals(0, cmp.compare(d1, d2), 0.0);
	}

	@Test
	public void testSort() {
		ICoverageNode d1 = new MockBlockData(18);
		ICoverageNode d2 = new MockBlockData(21);
		ICoverageNode d3 = new MockBlockData(30);
		ICoverageNode d4 = new MockBlockData(60);
		ICoverageNode d5 = new MockBlockData(99);
		final List<ICoverageNode> result = CounterComparator.TOTALITEMS
				.sort(Arrays.asList(d3, d5, d1, d4, d2), CounterEntity.BLOCK);
		assertEquals(Arrays.asList(d1, d2, d3, d4, d5), result);
	}

	private void assertCmpEquals(Comparator<ICounter> cmp, int total1,
			int covered1, int total2, int covered2) {
		assertEquals(0, cmp.compare(ctr(total1, covered1),
				ctr(total2, covered2)), 0.0);
	}

	private void assertCmpLess(Comparator<ICounter> cmp, int total1,
			int covered1, int total2, int covered2) {
		assertTrue(cmp.compare(ctr(total1, covered1), ctr(total2, covered2)) < 0);
	}

	private void assertCmpGreater(Comparator<ICounter> cmp, int total1,
			int covered1, int total2, int covered2) {
		assertTrue(cmp.compare(ctr(total1, covered1), ctr(total2, covered2)) > 0);
	}

	private CounterImpl ctr(int total, int covered) {
		return CounterImpl.getInstance(total, covered);
	}

	private static final class MockBlockData extends CoverageNodeImpl {
		MockBlockData(int total) {
			super(CUSTOM, "mock", false);
			blockCounter = CounterImpl.getInstance(total, false);
		}
	}

}
