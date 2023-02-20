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
	public void default_should_define_no_limits() {
		assertNull(limit.getMinimum());
		assertNull(limit.getMaximum());
	}

	@Test
	public void default_should_check_coverageratio_on_instructions() {
		assertEquals(CounterEntity.INSTRUCTION, limit.getEntity());
		assertEquals(CounterValue.COVEREDRATIO, limit.getValue());
	}

	@Test
	public void check_should_fail_on_value_totalcount() {
		limit.setValue(CounterValue.TOTALCOUNT.name());
		limit.setMaximum("-1");
		assertEquals(CounterValue.TOTALCOUNT, limit.getValue());
		assertEquals(
				"instructions total count is 0, but expected maximum is -1",
				limit.check(new TestNode()));
	}

	@Test
	public void check_should_fail_on_value_missedcount() {
		limit.setValue(CounterValue.MISSEDCOUNT.name());
		limit.setMaximum("-1");
		assertEquals(CounterValue.MISSEDCOUNT, limit.getValue());
		assertEquals(
				"instructions missed count is 0, but expected maximum is -1",
				limit.check(new TestNode()));
	}

	@Test
	public void check_should_fail_on_value_coveredcount() {
		limit.setValue(CounterValue.COVEREDCOUNT.name());
		limit.setMaximum("-1");
		assertEquals(CounterValue.COVEREDCOUNT, limit.getValue());
		assertEquals(
				"instructions covered count is 0, but expected maximum is -1",
				limit.check(new TestNode()));
	}

	@Test
	public void check_should_fail_on_value_missedratio() {
		limit.setValue(CounterValue.MISSEDRATIO.name());
		limit.setMaximum("0.5");
		assertEquals(CounterValue.MISSEDRATIO, limit.getValue());
		assertEquals(
				"instructions missed ratio is 1.0, but expected maximum is 0.5",
				limit.check(new TestNode() {
					{
						instructionCounter = CounterImpl.COUNTER_1_0;
					}
				}));
	}

	@Test
	public void check_should_fail_on_value_coveredratio() {
		limit.setValue(CounterValue.COVEREDRATIO.name());
		limit.setMaximum("0.5");
		assertEquals(CounterValue.COVEREDRATIO, limit.getValue());
		assertEquals(
				"instructions covered ratio is 1.0, but expected maximum is 0.5",
				limit.check(new TestNode() {
					{
						instructionCounter = CounterImpl.COUNTER_0_1;
					}
				}));
	}

	@Test
	public void check_should_fail_on_counter_instruction() {
		limit.setValue(CounterValue.TOTALCOUNT.name());
		limit.setCounter(CounterEntity.INSTRUCTION.name());
		limit.setMaximum("-1");
		assertEquals(CounterEntity.INSTRUCTION, limit.getEntity());
		assertEquals(
				"instructions total count is 0, but expected maximum is -1",
				limit.check(new TestNode()));
	}

	@Test
	public void check_should_fail_on_check_counter_branch() {
		limit.setValue(CounterValue.TOTALCOUNT.name());
		limit.setCounter(CounterEntity.BRANCH.name());
		limit.setMaximum("-1");
		assertEquals(CounterEntity.BRANCH, limit.getEntity());
		assertEquals("branches total count is 0, but expected maximum is -1",
				limit.check(new TestNode()));
	}

	@Test
	public void check_should_fail_on_counter_line() {
		limit.setValue(CounterValue.TOTALCOUNT.name());
		limit.setCounter(CounterEntity.LINE.name());
		limit.setMaximum("-1");
		assertEquals(CounterEntity.LINE, limit.getEntity());
		assertEquals("lines total count is 0, but expected maximum is -1",
				limit.check(new TestNode()));
	}

	@Test
	public void check_should_fail_on_counter_complexity() {
		limit.setValue(CounterValue.TOTALCOUNT.name());
		limit.setCounter(CounterEntity.COMPLEXITY.name());
		limit.setMaximum("-1");
		assertEquals(CounterEntity.COMPLEXITY, limit.getEntity());
		assertEquals("complexity total count is 0, but expected maximum is -1",
				limit.check(new TestNode()));
	}

	@Test
	public void check_should_fail_on_counter_class() {
		limit.setValue(CounterValue.TOTALCOUNT.name());
		limit.setCounter(CounterEntity.CLASS.name());
		limit.setMaximum("-1");
		assertEquals(CounterEntity.CLASS, limit.getEntity());
		assertEquals("classes total count is 0, but expected maximum is -1",
				limit.check(new TestNode()));
	}

	@Test
	public void check_should_fail_on_counter_method() {
		limit.setValue(CounterValue.TOTALCOUNT.name());
		limit.setCounter(CounterEntity.METHOD.name());
		limit.setMaximum("-1");
		assertEquals(CounterEntity.METHOD, limit.getEntity());
		assertEquals("methods total count is 0, but expected maximum is -1",
				limit.check(new TestNode()));
	}

	@Test
	public void check_should_pass_with_NaN_ratio() {
		assertNull(limit.check(new TestNode() {
			{
				instructionCounter = CounterImpl.COUNTER_0_0;
			}
		}));
	}

	@Test
	public void check_should_pass_when_no_limits_given() {
		assertNull(limit.check(new TestNode() {
			{
				instructionCounter = CounterImpl.getInstance(1000, 0);
			}
		}));
	}

	@Test
	public void setMinimum_should_allow_null() {
		limit.setMinimum("0");
		limit.setMinimum((String) null);
		assertNull(limit.getMinimum());
	}

	@Test
	public void check_should_pass_when_minimum_is_fulfilled() {
		limit.setMinimum("0.35");
		assertEquals("0.35", limit.getMinimum());
		assertNull(limit.check(new TestNode() {
			{
				instructionCounter = CounterImpl.getInstance(65, 35);
			}
		}));
	}

	@Test
	public void check_should_fail_when_minimum_is_not_met() {
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
	public void check_should_report_actual_ratio_rounded_down_when_minimum_is_not_met() {
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
	public void check_should_report_counter_with_given_precision() {
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
	public void check_should_fail_when_minimum_ratio_is_smaller_than_0() {
		limit.setMinimum("-3");
		assertEquals("-3", limit.getMinimum());
		assertEquals(
				"given minimum ratio is -3, but must be between 0.0 and 1.0",
				limit.check(new TestNode()));
	}

	@Test
	public void check_should_fail_when_minimum_ratio_is_bigger_than_1() {
		limit.setMinimum("80");
		assertEquals("80", limit.getMinimum());
		assertEquals(
				"given minimum ratio is 80, but must be between 0.0 and 1.0",
				limit.check(new TestNode()));
	}

	@Test
	public void setMinimum_should_accept_percentage_string() {
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
	public void setMaximum_should_allow_null() {
		limit.setMaximum("0");
		limit.setMaximum((String) null);
		assertNull(limit.getMaximum());
	}

	@Test
	public void check_should_pass_when_maximum_counter_is_fulfilled() {
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
	public void check_should_pass_when_maximum_ratio_is_fulfilled() {
		limit.setMaximum("0.999");
		assertEquals("0.999", limit.getMaximum());
		assertNull(limit.check(new TestNode() {
			{
				instructionCounter = CounterImpl.getInstance(1, 99);
			}
		}));
	}

	@Test
	public void check_should_fail_when_maximum_is_not_met() {
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
	public void check_should_report_actual_ratio_rounded_up_when_maximum_is_not_met() {
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
	public void check_should_fail_when_maximum_ratio_is_smaller_than_0() {
		limit.setMaximum("-3");
		assertEquals("-3", limit.getMaximum());
		assertEquals(
				"given maximum ratio is -3, but must be between 0.0 and 1.0",
				limit.check(new TestNode()));
	}

	@Test
	public void check_should_fail_when_maximum_ratio_is_bigger_than_1() {
		limit.setMaximum("80");
		assertEquals("80", limit.getMaximum());
		assertEquals(
				"given maximum ratio is 80, but must be between 0.0 and 1.0",
				limit.check(new TestNode()));
	}

	@Test
	public void setMaximum_should_accept_percentage_string() {
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
