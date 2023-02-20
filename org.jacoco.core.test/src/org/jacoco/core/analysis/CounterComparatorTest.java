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

import java.util.Comparator;

import org.jacoco.core.analysis.ICoverageNode.CounterEntity;
import org.jacoco.core.internal.analysis.CounterImpl;
import org.junit.Test;

/**
 * Unit tests for {@link CounterComparator}.
 */
public class CounterComparatorTest {

	@Test
	public void testTotalItemsComparator() {
		final Comparator<ICounter> cmp = CounterComparator.TOTALITEMS;
		assertCmpLess(cmp, 19, 5, 19, 6);
		assertCmpEquals(cmp, 20, 5, 19, 6);
		assertCmpGreater(cmp, 21, 5, 19, 6);
	}

	@Test
	public void testCoveredItemsComparator() {
		final Comparator<ICounter> cmp = CounterComparator.COVEREDITEMS;
		assertCmpLess(cmp, 73, 7, 42, 8);
		assertCmpEquals(cmp, 42, 8, 82, 8);
		assertCmpGreater(cmp, 21, 9, 32, 8);
	}

	@Test
	public void testMissedItemsComparator() {
		final Comparator<ICounter> cmp = CounterComparator.MISSEDITEMS;
		assertCmpLess(cmp, 10, 40, 11, 80);
		assertCmpEquals(cmp, 10, 40, 10, 80);
		assertCmpGreater(cmp, 11, 39, 10, 80);
	}

	@Test
	public void testCoveredRatioComparator() {
		final Comparator<ICounter> cmp = CounterComparator.COVEREDRATIO;
		assertCmpLess(cmp, 25, 25, 44, 46);
		assertCmpEquals(cmp, 40, 10, 64, 16);
		assertCmpGreater(cmp, 25, 25, 46, 44);
	}

	@Test
	public void testMissedRatioComparator() {
		final Comparator<ICounter> cmp = CounterComparator.MISSEDRATIO;
		assertCmpLess(cmp, 25, 25, 46, 44);
		assertCmpEquals(cmp, 40, 10, 64, 16);
		assertCmpGreater(cmp, 25, 25, 44, 46);
	}

	@Test
	public void testReverseComparator() {
		final Comparator<ICounter> cmp = CounterComparator.TOTALITEMS.reverse();
		assertCmpGreater(cmp, 19, 5, 19, 6);
		assertCmpEquals(cmp, 20, 5, 19, 6);
		assertCmpLess(cmp, 21, 5, 19, 6);
	}

	@Test
	public void testReverseReverseComparator() {
		final Comparator<ICounter> cmp = CounterComparator.TOTALITEMS.reverse()
				.reverse();
		assertCmpGreater(cmp, 21, 5, 19, 6);
		assertCmpEquals(cmp, 20, 5, 19, 6);
		assertCmpLess(cmp, 19, 5, 19, 6);
	}

	@Test
	public void testNodeComparator1() {
		ICoverageNode d1 = new MockNode(18);
		ICoverageNode d2 = new MockNode(15);
		final Comparator<ICoverageNode> cmp = CounterComparator.TOTALITEMS
				.on(CounterEntity.INSTRUCTION);
		assertTrue(cmp.compare(d1, d2) > 0);
	}

	@Test
	public void testNodeComparator2() {
		ICoverageNode d1 = new MockNode(18);
		ICoverageNode d2 = new MockNode(15);
		final Comparator<ICoverageNode> cmp = CounterComparator.TOTALITEMS
				.on(CounterEntity.LINE);
		assertEquals(0, cmp.compare(d1, d2), 0.0);
	}

	private void assertCmpEquals(Comparator<ICounter> cmp, int missed1,
			int covered1, int missed2, int covered2) {
		assertEquals(0,
				cmp.compare(ctr(missed1, covered1), ctr(missed2, covered2)),
				0.0);
	}

	private void assertCmpLess(Comparator<ICounter> cmp, int missed1,
			int covered1, int missed2, int covered2) {
		assertTrue(cmp.compare(ctr(missed1, covered1),
				ctr(missed2, covered2)) < 0);
	}

	private void assertCmpGreater(Comparator<ICounter> cmp, int missed1,
			int covered1, int missed2, int covered2) {
		assertTrue(cmp.compare(ctr(missed1, covered1),
				ctr(missed2, covered2)) > 0);
	}

	private CounterImpl ctr(int missed, int covered) {
		return CounterImpl.getInstance(missed, covered);
	}

	private static final class MockNode extends CoverageNodeImpl {
		MockNode(int total) {
			super(GROUP, "mock");
			instructionCounter = CounterImpl.getInstance(total, 0);
		}
	}

}
