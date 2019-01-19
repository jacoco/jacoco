/*******************************************************************************
 * Copyright (c) 2009, 2019 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.report.check;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.jacoco.core.analysis.CoverageNodeImpl;
import org.jacoco.core.analysis.ICounter.CounterValue;
import org.jacoco.core.analysis.ICoverageNode.CounterEntity;
import org.jacoco.core.internal.analysis.CounterImpl;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link Limit}.
 */
public class LimitTest {

	private Limit limit;

	@Before
	public void setup() {
		limit = new Limit();
	}

	@Test
	public void testDefaults() {
		assertNull(limit.getMinimum());
		assertNull(limit.getMaximum());
		assertEquals(CounterEntity.INSTRUCTION, limit.getEntity());
		assertEquals(CounterValue.COVEREDRATIO, limit.getValue());
	}

	@Test
	public void testTotalCount() {
		limit.setValue(CounterValue.TOTALCOUNT.name());
		limit.setMaximum("-1");
		assertEquals(CounterValue.TOTALCOUNT, limit.getValue());
		assertEquals(
				"instructions total count is 0, but expected maximum is -1",
				limit.check(new TestNode()));
	}

	@Test
	public void testMissedCount() {
		limit.setValue(CounterValue.MISSEDCOUNT.name());
		limit.setMaximum("-1");
		assertEquals(CounterValue.MISSEDCOUNT, limit.getValue());
		assertEquals(
				"instructions missed count is 0, but expected maximum is -1",
				limit.check(new TestNode()));
	}

	@Test
	public void testCoveredCount() {
		limit.setValue(CounterValue.COVEREDCOUNT.name());
		limit.setMaximum("-1");
		assertEquals(CounterValue.COVEREDCOUNT, limit.getValue());
		assertEquals(
				"instructions covered count is 0, but expected maximum is -1",
				limit.check(new TestNode()));
	}

	@Test
	public void testMissedRatio() {
		limit.setValue(CounterValue.MISSEDRATIO.name());
		limit.setMaximum("-1");
		assertEquals(CounterValue.MISSEDRATIO, limit.getValue());
		assertEquals(
				"instructions missed ratio is 0, but expected maximum is -1",
				limit.check(new TestNode() {
					{
						instructionCounter = CounterImpl.COUNTER_0_1;
					}
				}));
	}

	@Test
	public void testCoveredRatio() {
		limit.setValue(CounterValue.COVEREDRATIO.name());
		limit.setMaximum("-1");
		assertEquals(CounterValue.COVEREDRATIO, limit.getValue());
		assertEquals(
				"instructions covered ratio is 0, but expected maximum is -1",
				limit.check(new TestNode() {
					{
						instructionCounter = CounterImpl.COUNTER_1_0;
					}
				}));
	}

	@Test
	public void testInstruction() {
		limit.setValue(CounterValue.TOTALCOUNT.name());
		limit.setCounter(CounterEntity.INSTRUCTION.name());
		limit.setMaximum("-1");
		assertEquals(CounterEntity.INSTRUCTION, limit.getEntity());
		assertEquals(
				"instructions total count is 0, but expected maximum is -1",
				limit.check(new TestNode()));
	}

	@Test
	public void testBranch() {
		limit.setValue(CounterValue.TOTALCOUNT.name());
		limit.setCounter(CounterEntity.BRANCH.name());
		limit.setMaximum("-1");
		assertEquals(CounterEntity.BRANCH, limit.getEntity());
		assertEquals("branches total count is 0, but expected maximum is -1",
				limit.check(new TestNode()));
	}

	@Test
	public void testLine() {
		limit.setValue(CounterValue.TOTALCOUNT.name());
		limit.setCounter(CounterEntity.LINE.name());
		limit.setMaximum("-1");
		assertEquals(CounterEntity.LINE, limit.getEntity());
		assertEquals("lines total count is 0, but expected maximum is -1",
				limit.check(new TestNode()));
	}

	@Test
	public void testComlexity() {
		limit.setValue(CounterValue.TOTALCOUNT.name());
		limit.setCounter(CounterEntity.COMPLEXITY.name());
		limit.setMaximum("-1");
		assertEquals(CounterEntity.COMPLEXITY, limit.getEntity());
		assertEquals("complexity total count is 0, but expected maximum is -1",
				limit.check(new TestNode()));
	}

	@Test
	public void testClass() {
		limit.setValue(CounterValue.TOTALCOUNT.name());
		limit.setCounter(CounterEntity.CLASS.name());
		limit.setMaximum("-1");
		assertEquals(CounterEntity.CLASS, limit.getEntity());
		assertEquals("classes total count is 0, but expected maximum is -1",
				limit.check(new TestNode()));
	}

	@Test
	public void testMethod() {
		limit.setValue(CounterValue.TOTALCOUNT.name());
		limit.setCounter(CounterEntity.METHOD.name());
		limit.setMaximum("-1");
		assertEquals(CounterEntity.METHOD, limit.getEntity());
		assertEquals("methods total count is 0, but expected maximum is -1",
				limit.check(new TestNode()));
	}

	@Test
	public void testNoRatio() {
		assertNull(limit.check(new TestNode() {
			{
				instructionCounter = CounterImpl.COUNTER_0_0;
			}
		}));
	}

	@Test
	public void testNoLimits() {
		assertNull(limit.check(new TestNode() {
			{
				instructionCounter = CounterImpl.getInstance(1000, 0);
			}
		}));
	}

	@Test
	public void testMin0() {
		limit.setMinimum("0");
		limit.setMinimum((String) null);
		assertNull(limit.getMinimum());
	}

	@Test
	public void testMin1() {
		limit.setMinimum("0.35");
		assertEquals("0.35", limit.getMinimum());
		assertNull(limit.check(new TestNode() {
			{
				instructionCounter = CounterImpl.getInstance(65, 35);
			}
		}));
	}

	@Test
	public void testMin2() {
		limit.setMinimum("0.35");
		assertEquals("0.35", limit.getMinimum());
		assertNull(limit.check(new TestNode() {
			{
				instructionCounter = CounterImpl.getInstance(64, 36);
			}
		}));
	}

	@Test
	public void testMin3() {
		limit.setMinimum("0.3500");
		assertEquals("0.3500", limit.getMinimum());
		assertEquals(
				"instructions covered ratio is 0.3400, but expected minimum is 0.3500",
				limit.check(new TestNode() {
					{
						instructionCounter = CounterImpl.getInstance(66, 34);
					}
				}));
	}

	@Test
	public void testMin4() {
		limit.setMinimum("0.35");
		assertEquals("0.35", limit.getMinimum());
		assertEquals(
				"instructions covered ratio is 0.34, but expected minimum is 0.35",
				limit.check(new TestNode() {
					{
						instructionCounter = CounterImpl.getInstance(65001,
								34999);
					}
				}));
	}

	@Test
	public void testMin5() {
		limit.setMinimum("10000");
		limit.setValue(CounterValue.MISSEDCOUNT.name());
		assertEquals("10000", limit.getMinimum());
		assertEquals(
				"instructions missed count is 9990, but expected minimum is 10000",
				limit.check(new TestNode() {
					{
						instructionCounter = CounterImpl.getInstance(9990, 0);
					}
				}));
	}

	@Test
	public void testMin6() {
		limit.setMinimum("12345");
		assertEquals("12345", limit.getMinimum());
		assertEquals(
				"instructions covered ratio is 0, but expected minimum is 12345",
				limit.check(new TestNode() {
					{
						instructionCounter = CounterImpl.getInstance(1, 999);
					}
				}));
	}

	@Test
	public void testMinPercent() {
		limit.setMinimum("1.55%");
		assertEquals("0.0155", limit.getMinimum());

		limit.setMinimum("1.5%");
		assertEquals("0.015", limit.getMinimum());

		limit.setMinimum("1.00%");
		assertEquals("0.0100", limit.getMinimum());

		limit.setMinimum("1%");
		assertEquals("0.01", limit.getMinimum());
	}

	@Test
	public void testMax0() {
		limit.setMaximum("0");
		limit.setMaximum((String) null);
		assertNull(limit.getMaximum());
	}

	@Test
	public void testMax1() {
		limit.setMaximum("12345678");
		limit.setValue(CounterValue.MISSEDCOUNT.name());
		assertEquals("12345678", limit.getMaximum());
		assertNull(limit.check(new TestNode() {
			{
				instructionCounter = CounterImpl.getInstance(12345678, 0);
			}
		}));
	}

	@Test
	public void testMax2() {
		limit.setMaximum("0.999");
		assertEquals("0.999", limit.getMaximum());
		assertNull(limit.check(new TestNode() {
			{
				instructionCounter = CounterImpl.getInstance(1, 99);
			}
		}));
	}

	@Test
	public void testMax3() {
		limit.setMaximum("0.999");
		assertEquals("0.999", limit.getMaximum());
		assertEquals(
				"instructions covered ratio is 1.000, but expected maximum is 0.999",
				limit.check(new TestNode() {
					{
						instructionCounter = CounterImpl.getInstance(0, 1);
					}
				}));
	}

	@Test
	public void testMax4() {
		limit.setMaximum("0.999");
		assertEquals("0.999", limit.getMaximum());
		assertEquals(
				"instructions covered ratio is 1.000, but expected maximum is 0.999",
				limit.check(new TestNode() {
					{
						instructionCounter = CounterImpl.getInstance(999,
								999001);
					}
				}));
	}

	@Test
	public void testMaxPercent() {
		limit.setMaximum("1.55%");
		assertEquals("0.0155", limit.getMaximum());

		limit.setMaximum("1.5%");
		assertEquals("0.015", limit.getMaximum());

		limit.setMaximum("1.00%");
		assertEquals("0.0100", limit.getMaximum());

		limit.setMaximum("1%");
		assertEquals("0.01", limit.getMaximum());
	}

	private static class TestNode extends CoverageNodeImpl {

		public TestNode() {
			super(ElementType.CLASS, "Foo");
		}

	}

}
