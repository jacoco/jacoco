/*******************************************************************************
 * Copyright (c) 2009, 2026 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.test.validation.java5;

import static org.jacoco.core.test.validation.targets.Stubs.nop;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Collection;

import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.analysis.ICounter;
import org.jacoco.core.analysis.ICoverageNode.CounterEntity;
import org.jacoco.core.analysis.IMethodCoverage;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.data.SessionInfoStore;
import org.jacoco.core.instr.Instrumenter;
import org.jacoco.core.internal.analysis.CounterImpl;
import org.jacoco.core.runtime.IRuntime;
import org.jacoco.core.runtime.RuntimeData;
import org.jacoco.core.runtime.SystemPropertiesRuntime;
import org.jacoco.core.test.TargetLoader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests of the boundary counter, which reports whether an ordered numeric
 * comparison was ever evaluated with equal values.
 *
 * Every target here is compiled by the compiler under test and then executed,
 * so these tests pin down the bytecode shapes that the boundary probe has to
 * recognize. A compiler that emits a different shape for the same source shows
 * up as a changed counter rather than as silently missing data.
 */
public class BoundaryTest {

	public interface Target {
		public void test(int arg);
	}

	public interface LongTarget {
		public void test(long arg);
	}

	public interface DoubleTarget {
		public void test(double arg);
	}

	public interface StringTarget {
		public void test(String arg);
	}

	private RuntimeData data;
	private IRuntime runtime;
	private byte[] bytes;
	private Object target;

	@Before
	public void setup() throws Exception {
		data = new RuntimeData();
		runtime = new SystemPropertiesRuntime();
		runtime.startup(data);
	}

	@After
	public void teardown() {
		runtime.shutdown();
	}

	// === Ordered comparison of int values ===

	public static class GreaterThan implements Target {
		public void test(int arg) {
			if (arg > 6) {
				nop();
			}
		}
	}

	@Test
	public void boundary_should_be_empty_when_no_branch_is_covered()
			throws Exception {
		instrument(GreaterThan.class);
		assertBoundary(0, 0);
	}

	@Test
	public void boundary_should_be_empty_when_only_one_branch_is_covered()
			throws Exception {
		instrument(GreaterThan.class);
		test(7);
		assertBoundary(0, 0);
	}

	@Test
	public void boundary_should_be_missed_when_equal_values_are_not_used()
			throws Exception {
		instrument(GreaterThan.class);
		test(5);
		test(7);
		assertBoundary(1, 0);
	}

	@Test
	public void boundary_should_be_covered_when_equal_values_are_used()
			throws Exception {
		instrument(GreaterThan.class);
		test(5);
		test(6);
		test(7);
		assertBoundary(0, 1);
	}

	public static class GreaterOrEqual implements Target {
		public void test(int arg) {
			if (arg >= 6) {
				nop();
			}
		}
	}

	@Test
	public void non_strict_comparison_should_report_boundary()
			throws Exception {
		instrument(GreaterOrEqual.class);
		test(5);
		test(7);
		assertBoundary(1, 0);
	}

	@Test
	public void non_strict_comparison_should_cover_boundary_with_equal_value()
			throws Exception {
		instrument(GreaterOrEqual.class);
		test(5);
		test(6);
		assertBoundary(0, 1);
	}

	// === Comparison against zero, which javac emits without a constant ===

	public static class GreaterThanZero implements Target {
		public void test(int arg) {
			if (arg > 0) {
				nop();
			}
		}
	}

	@Test
	public void comparison_against_zero_should_report_boundary()
			throws Exception {
		instrument(GreaterThanZero.class);
		test(-1);
		test(1);
		assertBoundary(1, 0);
	}

	@Test
	public void comparison_against_zero_should_cover_boundary_with_zero()
			throws Exception {
		instrument(GreaterThanZero.class);
		test(-1);
		test(0);
		test(1);
		assertBoundary(0, 1);
	}

	// === Equality has no boundary ===

	public static class EqualTo implements Target {
		public void test(int arg) {
			if (arg == 6) {
				nop();
			}
		}
	}

	@Test
	public void equality_comparison_should_not_report_boundary()
			throws Exception {
		instrument(EqualTo.class);
		test(5);
		test(6);
		assertBoundary(0, 0);
	}

	public static class NotEqualTo implements Target {
		public void test(int arg) {
			if (arg != 6) {
				nop();
			}
		}
	}

	@Test
	public void inequality_comparison_should_not_report_boundary()
			throws Exception {
		instrument(NotEqualTo.class);
		test(5);
		test(6);
		assertBoundary(0, 0);
	}

	// === Reference comparison has no boundary ===

	public static class NullCheck implements StringTarget {
		public void test(String arg) {
			if (arg != null) {
				nop();
			}
		}
	}

	@Test
	public void null_check_should_not_report_boundary() throws Exception {
		instrument(NullCheck.class);
		testString("a");
		testString(null);
		assertBoundary(0, 0);
	}

	// === long values, compiled to LCMP followed by a comparison ===

	public static class LongGreaterThan implements LongTarget {
		public void test(long arg) {
			if (arg > 6L) {
				nop();
			}
		}
	}

	@Test
	public void long_comparison_should_report_missed_boundary()
			throws Exception {
		instrument(LongGreaterThan.class);
		testLong(5);
		testLong(7);
		assertBoundary(1, 0);
	}

	@Test
	public void long_comparison_should_report_covered_boundary()
			throws Exception {
		instrument(LongGreaterThan.class);
		testLong(5);
		testLong(6);
		testLong(7);
		assertBoundary(0, 1);
	}

	public static class LongEqualTo implements LongTarget {
		public void test(long arg) {
			if (arg == 6L) {
				nop();
			}
		}
	}

	@Test
	public void long_equality_should_not_report_boundary() throws Exception {
		instrument(LongEqualTo.class);
		testLong(5);
		testLong(6);
		assertBoundary(0, 0);
	}

	// === double values, compiled to DCMP followed by a comparison ===

	public static class DoubleLessThan implements DoubleTarget {
		public void test(double arg) {
			if (arg < 6d) {
				nop();
			}
		}
	}

	@Test
	public void double_comparison_should_report_covered_boundary()
			throws Exception {
		instrument(DoubleLessThan.class);
		testDouble(5);
		testDouble(6);
		assertBoundary(0, 1);
	}

	@Test
	public void double_comparison_should_not_treat_nan_as_boundary()
			throws Exception {
		instrument(DoubleLessThan.class);
		testDouble(5);
		testDouble(Double.NaN);
		assertBoundary(1, 0);
	}

	// === compareTo(), where zero means that the operands are equal ===

	public static class CompareTo implements StringTarget {
		public void test(String arg) {
			if (arg.compareTo("b") < 0) {
				nop();
			}
		}
	}

	@Test
	public void compare_to_should_report_missed_boundary() throws Exception {
		instrument(CompareTo.class);
		testString("a");
		testString("c");
		assertBoundary(1, 0);
	}

	@Test
	public void compare_to_should_report_covered_boundary() throws Exception {
		instrument(CompareTo.class);
		testString("a");
		testString("b");
		assertBoundary(0, 1);
	}

	// === Two comparisons in one method ===

	public static class TwoComparisons implements Target {
		public void test(int arg) {
			if (arg > 6) {
				nop();
			}
			if (arg < 20) {
				nop();
			}
		}
	}

	@Test
	public void two_comparisons_should_be_counted_separately()
			throws Exception {
		instrument(TwoComparisons.class);
		test(6);
		test(7);
		test(25);
		// arg > 6 is evaluated with the boundary value 6, arg < 20 is not
		// evaluated with 20
		assertBoundary(1, 1);
	}

	@Test
	public void comparison_with_one_missed_branch_should_not_be_counted()
			throws Exception {
		instrument(TwoComparisons.class);
		test(6);
		test(7);
		// arg < 20 is true for both values, so one of its branches is missed
		assertBoundary(0, 1);
	}

	// === Operands of a conditional expression ===

	public static class AndCondition implements Target {
		public void test(int arg) {
			if (arg > 6 && arg < 20) {
				nop();
			}
		}
	}

	/**
	 * For plain Java each operand of <code>&amp;&amp;</code> keeps its own two
	 * branches, which branch coverage already reports as four branches on the
	 * line. Each operand therefore also gets its own boundary. This is not the
	 * case where
	 * {@link org.jacoco.core.internal.analysis.Instruction#replaceBranches}
	 * applies, which is why that method drops the boundary.
	 */
	@Test
	public void operands_of_conditional_expression_should_be_counted_separately()
			throws Exception {
		instrument(AndCondition.class);
		test(5);
		test(6);
		test(7);
		test(20);
		assertBoundary(0, 2);
	}

	@Test
	public void short_circuited_operand_should_not_be_counted()
			throws Exception {
		instrument(AndCondition.class);
		test(5);
		test(7);
		// arg < 20 is only evaluated for 7, so one of its branches is missed,
		// while arg > 6 is fully covered without ever seeing the value 6
		assertBoundary(1, 0);
	}

	private void test(final int arg) {
		((Target) target).test(arg);
	}

	private void testLong(final long arg) {
		((LongTarget) target).test(arg);
	}

	private void testDouble(final double arg) {
		((DoubleTarget) target).test(arg);
	}

	private void testString(final String arg) {
		((StringTarget) target).test(arg);
	}

	private void instrument(final Class<?> clazz) throws Exception {
		bytes = TargetLoader.getClassDataAsBytes(clazz);
		final byte[] instrumented = new Instrumenter(runtime).instrument(bytes,
				"TestTarget");
		final TargetLoader loader = new TargetLoader();
		target = loader.add(clazz, instrumented).newInstance();
	}

	private void assertBoundary(final int missed, final int covered)
			throws IOException {
		final CoverageBuilder builder = new CoverageBuilder();
		final ExecutionDataStore store = new ExecutionDataStore();
		data.collect(store, new SessionInfoStore(), false);
		final Analyzer analyzer = new Analyzer(store, builder);
		analyzer.analyzeClass(bytes, "TestTarget");
		final Collection<IClassCoverage> classes = builder.getClasses();
		assertEquals(1, classes.size(), 0.0);
		final IClassCoverage classCoverage = classes.iterator().next();
		for (final IMethodCoverage m : classCoverage.getMethods()) {
			if (m.getName().equals("test")) {
				assertEquals(CounterImpl.getInstance(missed, covered),
						m.getCounter(CounterEntity.BOUNDARY));
				return;
			}
		}
		throw new AssertionError("No test() method.");
	}

}
