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
package org.jacoco.core.test.validation.scala;

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
import org.jacoco.core.test.validation.scala.targets.ScalaBoundaryTarget;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests of the boundary counter for Scala. Several Scala constructs compile to
 * comparisons that do not appear in the source, and others that look like
 * comparisons compile to a library call with no comparison at all.
 */
public class ScalaBoundaryTest {

	private RuntimeData data;
	private IRuntime runtime;
	private byte[] bytes;
	private Object target;

	@Before
	public void setup() throws Exception {
		data = new RuntimeData();
		runtime = new SystemPropertiesRuntime();
		runtime.startup(data);

		bytes = TargetLoader.getClassDataAsBytes(ScalaBoundaryTarget.class);
		final byte[] instrumented = new Instrumenter(runtime).instrument(bytes,
				"TestTarget");
		final TargetLoader loader = new TargetLoader();
		target = loader.add(ScalaBoundaryTarget.class, instrumented)
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

	@Test
	public void non_strict_comparison_should_report_boundary()
			throws Exception {
		call("greaterOrEqual", int.class, Integer.valueOf(5));
		call("greaterOrEqual", int.class, Integer.valueOf(6));

		assertBoundary("greaterOrEqual", 0, 1);
	}

	@Test
	public void equality_comparison_should_not_report_boundary()
			throws Exception {
		call("equalTo", int.class, Integer.valueOf(5));
		call("equalTo", int.class, Integer.valueOf(6));

		assertBoundary("equalTo", 0, 0);
	}

	@Test
	public void match_with_guard_should_report_boundary() throws Exception {
		call("matchWithGuard", int.class, Integer.valueOf(5));
		call("matchWithGuard", int.class, Integer.valueOf(7));

		assertBoundary("matchWithGuard", 1, 0);
	}

	@Test
	public void match_with_guard_should_cover_boundary() throws Exception {
		call("matchWithGuard", int.class, Integer.valueOf(5));
		call("matchWithGuard", int.class, Integer.valueOf(6));
		call("matchWithGuard", int.class, Integer.valueOf(7));

		assertBoundary("matchWithGuard", 0, 1);
	}

	@Test
	public void two_comparisons_should_be_counted_separately()
			throws Exception {
		call("twoComparisons", int.class, Integer.valueOf(0));
		call("twoComparisons", int.class, Integer.valueOf(1));
		call("twoComparisons", int.class, Integer.valueOf(5));
		call("twoComparisons", int.class, Integer.valueOf(11));

		assertBoundary("twoComparisons", 1, 1);
	}

	/**
	 * A range membership test is a library call, so there is no comparison in
	 * this method to report a boundary for.
	 */
	@Test
	public void range_contains_should_not_report_boundary() throws Exception {
		call("rangeContains", int.class, Integer.valueOf(0));
		call("rangeContains", int.class, Integer.valueOf(5));
		call("rangeContains", int.class, Integer.valueOf(11));

		assertBoundary("rangeContains", 0, 0);
	}

	@Test
	public void long_comparison_should_report_boundary() throws Exception {
		call("longGreaterThan", long.class, Long.valueOf(5));
		call("longGreaterThan", long.class, Long.valueOf(6));
		call("longGreaterThan", long.class, Long.valueOf(7));

		assertBoundary("longGreaterThan", 0, 1);
	}

	@Test
	public void double_comparison_should_not_treat_nan_as_boundary()
			throws Exception {
		call("doubleLessThan", double.class, Double.valueOf(5));
		call("doubleLessThan", double.class, Double.valueOf(Double.NaN));

		assertBoundary("doubleLessThan", 1, 0);
	}

	/**
	 * Scala compiles the comparison operator on strings to a call of
	 * StringOps.$greater$extension, so the comparison happens inside the Scala
	 * library and not in this method. The counter only sees comparisons that
	 * are present in the analyzed method, so it reports nothing here.
	 */
	@Test
	public void string_operator_should_not_report_boundary() throws Exception {
		callTwoStrings("stringGreaterThan", "a", "b");
		callTwoStrings("stringGreaterThan", "b", "b");
		callTwoStrings("stringGreaterThan", "c", "b");

		assertBoundary("stringGreaterThan", 0, 0);
	}

	/**
	 * Calling compareTo() explicitly puts the comparison back into the method,
	 * where zero means that the two operands are equal.
	 */
	@Test
	public void compare_to_should_report_covered_boundary() throws Exception {
		callTwoStrings("stringCompareTo", "a", "b");
		callTwoStrings("stringCompareTo", "b", "b");
		callTwoStrings("stringCompareTo", "c", "b");

		assertBoundary("stringCompareTo", 0, 1);
	}

	@Test
	public void compare_to_should_report_missed_boundary() throws Exception {
		callTwoStrings("stringCompareTo", "a", "b");
		callTwoStrings("stringCompareTo", "c", "b");

		assertBoundary("stringCompareTo", 1, 0);
	}

	/**
	 * The loop condition is a comparison like any other, and its boundary is
	 * the value that ends the loop.
	 */
	@Test
	public void loop_condition_should_report_covered_boundary()
			throws Exception {
		call("countUpTo", int.class, Integer.valueOf(3));

		assertBoundary("countUpTo", 0, 1);
	}

	@Test
	public void loop_that_never_runs_should_not_report_boundary()
			throws Exception {
		call("countUpTo", int.class, Integer.valueOf(0));

		assertBoundary("countUpTo", 0, 0);
	}

	private void call(final String name, final Class<?> argType,
			final Object arg) throws Exception {
		final Method method = target.getClass().getMethod(name, argType);
		method.invoke(target, arg);
	}

	private void callTwoStrings(final String name, final String a,
			final String b) throws Exception {
		final Method method = target.getClass().getMethod(name, String.class,
				String.class);
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
