package org.jacoco.core.analysis;

import static org.jacoco.core.analysis.ICoverageNode.ElementType.GROUP;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.jacoco.core.analysis.ICoverageNode.CounterEntity;
import org.junit.Test;

public class NodeComparatorTest {

	@Test
	public void testSort() {
		ICoverageNode d1 = new MockBlockData(18);
		ICoverageNode d2 = new MockBlockData(21);
		ICoverageNode d3 = new MockBlockData(30);
		ICoverageNode d4 = new MockBlockData(60);
		ICoverageNode d5 = new MockBlockData(99);
		final List<ICoverageNode> result = CounterComparator.TOTALITEMS
				.on(CounterEntity.BLOCK).sort(
						Arrays.asList(d3, d5, d1, d4, d2));
		assertEquals(Arrays.asList(d1, d2, d3, d4, d5), result);
	}

	@Test
	public void testSecond1() {
		ICoverageNode d1 = new MockBlockLineData(5, 30);
		ICoverageNode d2 = new MockBlockLineData(3, 80);
		final NodeComparator c1 = CounterComparator.TOTALITEMS
				.on(CounterEntity.BLOCK);
		final NodeComparator c2 = CounterComparator.TOTALITEMS
				.on(CounterEntity.LINE);
		assertTrue(c1.second(c2).compare(d1, d2) > 0);
	}

	@Test
	public void testSecond2() {
		ICoverageNode d1 = new MockBlockLineData(5, 30);
		ICoverageNode d2 = new MockBlockLineData(5, 80);
		final NodeComparator c1 = CounterComparator.TOTALITEMS
				.on(CounterEntity.BLOCK);
		final NodeComparator c2 = CounterComparator.TOTALITEMS
				.on(CounterEntity.LINE);
		assertTrue(c1.second(c2).compare(d1, d2) < 0);
	}

	private static final class MockBlockData extends CoverageNodeImpl {
		MockBlockData(int total) {
			super(GROUP, "mock", false);
			blockCounter = CounterImpl.getInstance(total, false);
		}
	}

	private static final class MockBlockLineData extends CoverageNodeImpl {
		MockBlockLineData(int totalBlock, int totalLine) {
			super(GROUP, "mock", false);
			blockCounter = CounterImpl.getInstance(totalBlock, false);
			lineCounter = CounterImpl.getInstance(totalLine, false);
		}
	}
}
