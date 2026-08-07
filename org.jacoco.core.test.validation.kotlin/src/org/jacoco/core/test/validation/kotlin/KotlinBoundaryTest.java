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
package org.jacoco.core.test.validation.kotlin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.lang.reflect.Method;

import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.IClassCoverage;
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
import org.jacoco.core.test.validation.kotlin.targets.KotlinBoundaryTarget;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests of the boundary counter for Kotlin. Kotlin routes several constructs
 * through comparisons that do not appear in the source, and its filters remove
 * or rewrite instructions during analysis, so the shapes the boundary probe
 * sees here differ from the ones javac produces.
 */
public class KotlinBoundaryTest {

	private RuntimeData data;
	private IRuntime runtime;
	private byte[] bytes;
	private Object target;

	@Before
	public void setup() throws Exception {
		data = new RuntimeData();
		runtime = new SystemPropertiesRuntime();
		runtime.startup(data);

		bytes = TargetLoader.getClassDataAsBytes(KotlinBoundaryTarget.class);
		final byte[] instrumented = new Instrumenter(runtime).instrument(bytes,
				"TestTarget");
		final TargetLoader loader = new TargetLoader();
		target = loader.add(KotlinBoundaryTarget.class, instrumented)
				.newInstance();
	}

	@After
	public void teardown() {
		runtime.shutdown();
	}

	@Test
	public void comparison_should_report_missed_boundary() throws Exception {
		call("greaterThan", int.class, Integer.valueOf(5));
		call("greaterThan", int.class, Integer.valueOf(7));

		assertBoundary("greaterThan", 1, 0);
	}

	@Test
	public void comparison_should_report_covered_boundary() throws Exception {
		call("greaterThan", int.class, Integer.valueOf(5));
		call("greaterThan", int.class, Integer.valueOf(6));
		call("greaterThan", int.class, Integer.valueOf(7));

		assertBoundary("greaterThan", 0, 1);
	}

	/**
	 * A range check is two comparisons, one against each end, so both ends are
	 * reported separately. Note that Kotlin normalizes the upper end to an
	 * exclusive bound: <code>arg in 1..10</code> compiles to
	 * <code>1 &gt; arg</code> and <code>arg &gt;= 11</code>, so the boundary
	 * value of the upper comparison is 11 and not 10.
	 */
	@Test
	public void range_check_should_report_both_ends() throws Exception {
		call("inRange", int.class, Integer.valueOf(0));
		call("inRange", int.class, Integer.valueOf(5));
		call("inRange", int.class, Integer.valueOf(12));

		assertBoundary("inRange", 2, 0);
	}

	@Test
	public void range_check_should_cover_both_ends() throws Exception {
		call("inRange", int.class, Integer.valueOf(0));
		call("inRange", int.class, Integer.valueOf(1));
		call("inRange", int.class, Integer.valueOf(10));
		call("inRange", int.class, Integer.valueOf(11));

		assertBoundary("inRange", 0, 2);
	}

	@Test
	public void when_with_comparison_should_report_boundary() throws Exception {
		call("whenWithComparison", int.class, Integer.valueOf(5));
		call("whenWithComparison", int.class, Integer.valueOf(7));

		assertBoundary("whenWithComparison", 1, 0);
	}

	@Test
	public void when_with_range_should_report_boundary() throws Exception {
		call("whenWithRange", int.class, Integer.valueOf(0));
		call("whenWithRange", int.class, Integer.valueOf(5));
		call("whenWithRange", int.class, Integer.valueOf(12));

		assertBoundary("whenWithRange", 2, 0);
	}

	@Test
	public void elvis_then_comparison_should_report_boundary()
			throws Exception {
		call("elvisThenComparison", Integer.class, null);
		call("elvisThenComparison", Integer.class, Integer.valueOf(7));

		assertBoundary("elvisThenComparison", 1, 0);
	}

	@Test
	public void nullable_comparison_should_report_boundary() throws Exception {
		call("nullableComparison", Integer.class, null);
		call("nullableComparison", Integer.class, Integer.valueOf(5));
		call("nullableComparison", Integer.class, Integer.valueOf(7));

		assertBoundary("nullableComparison", 1, 0);
	}

	@Test
	public void long_comparison_should_report_boundary() throws Exception {
		call("longGreaterThan", long.class, Long.valueOf(5));
		call("longGreaterThan", long.class, Long.valueOf(6));
		call("longGreaterThan", long.class, Long.valueOf(7));

		assertBoundary("longGreaterThan", 0, 1);
	}

	/**
	 * The comparison operator on a {@link Comparable} compiles to compareTo()
	 * followed by a comparison of its result against zero, where zero means
	 * that the two operands are equal.
	 */
	@Test
	public void comparable_operator_should_report_covered_boundary()
			throws Exception {
		callTwoStrings("a", "b");
		callTwoStrings("b", "b");
		callTwoStrings("c", "b");

		assertBoundary("comparableOperator", 0, 1);
	}

	@Test
	public void comparable_operator_should_report_missed_boundary()
			throws Exception {
		callTwoStrings("a", "b");
		callTwoStrings("c", "b");

		assertBoundary("comparableOperator", 1, 0);
	}

	/**
	 * The body of an inline function is copied into the caller, but
	 * KotlinInlineFilter ignores every instruction that the SMAP attributes to
	 * the inline function, so the caller does not own that comparison and
	 * reports no boundary for it.
	 */
	@Test
	public void inlined_comparison_should_not_be_reported_in_the_caller()
			throws Exception {
		call("callsInline", int.class, Integer.valueOf(5));
		call("callsInline", int.class, Integer.valueOf(7));

		assertBoundary("callsInline", 0, 0);
		assertBoundary("isBig", 0, 0);
	}

	@Test
	public void inline_function_should_report_boundary_when_called_directly()
			throws Exception {
		call("isBig", int.class, Integer.valueOf(5));
		call("isBig", int.class, Integer.valueOf(6));
		call("isBig", int.class, Integer.valueOf(7));

		assertBoundary("isBig", 0, 1);
	}

	private void call(final String name, final Class<?> argType,
			final Object arg) throws Exception {
		final Method method = target.getClass().getMethod(name, argType);
		method.invoke(target, arg);
	}

	private void callTwoStrings(final String a, final String b)
			throws Exception {
		final Method method = target.getClass().getMethod("comparableOperator",
				String.class, String.class);
		method.invoke(target, a, b);
	}

	private void assertBoundary(final String methodName, final int missed,
			final int covered) throws IOException {
		final CoverageBuilder builder = new CoverageBuilder();
		final ExecutionDataStore store = new ExecutionDataStore();
		data.collect(store, new SessionInfoStore(), false);
		new Analyzer(store, builder).analyzeClass(bytes, "TestTarget");
		for (final IClassCoverage c : builder.getClasses()) {
			for (final IMethodCoverage m : c.getMethods()) {
				if (methodName.equals(m.getName())) {
					assertEquals("Boundaries in " + methodName,
							CounterImpl.getInstance(missed, covered),
							m.getCounter(CounterEntity.BOUNDARY));
					return;
				}
			}
		}
		fail("Method not found: " + methodName);
	}

}
