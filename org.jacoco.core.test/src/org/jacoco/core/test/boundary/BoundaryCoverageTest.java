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
package org.jacoco.core.test.boundary;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.lang.reflect.Method;

import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.analysis.ICounter;
import org.jacoco.core.analysis.ICoverageNode.CounterEntity;
import org.jacoco.core.analysis.IMethodCoverage;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.internal.analysis.CounterImpl;
import org.jacoco.core.test.InstrumentingLoader;
import org.jacoco.core.test.TargetLoader;
import org.jacoco.core.test.boundary.targets.BoundaryTarget;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests that boundary probes survive instrumentation and record whether the two
 * values of an ordered comparison were ever equal. Every test executes the
 * instrumented target, so a broken probe shows up as a {@link VerifyError} or a
 * wrong result rather than as a wrong counter only.
 */
public class BoundaryCoverageTest {

	private InstrumentingLoader loader;

	private Class<?> target;

	@Before
	public void setup() throws Exception {
		loader = new InstrumentingLoader(BoundaryTarget.class);
		target = loader.loadClass(BoundaryTarget.class.getName());
	}

	@Test
	public void should_report_missed_boundary_when_equal_values_are_not_used()
			throws Exception {
		call("greaterThan", int.class, Integer.valueOf(5));
		call("greaterThan", int.class, Integer.valueOf(7));

		assertBoundary("greaterThan", 1, 0);
	}

	@Test
	public void should_report_covered_boundary_when_equal_values_are_used()
			throws Exception {
		call("greaterThan", int.class, Integer.valueOf(5));
		call("greaterThan", int.class, Integer.valueOf(6));
		call("greaterThan", int.class, Integer.valueOf(7));

		assertBoundary("greaterThan", 0, 1);
	}

	@Test
	public void should_not_report_boundary_when_only_one_branch_is_covered()
			throws Exception {
		call("greaterThan", int.class, Integer.valueOf(7));

		assertBoundary("greaterThan", 0, 0);
	}

	@Test
	public void should_not_report_boundary_for_equality_comparison()
			throws Exception {
		call("equalTo", int.class, Integer.valueOf(5));
		call("equalTo", int.class, Integer.valueOf(6));

		assertBoundary("equalTo", 0, 0);
	}

	@Test
	public void should_report_missed_boundary_for_long_comparison()
			throws Exception {
		call("longGreaterThan", long.class, Long.valueOf(5));
		call("longGreaterThan", long.class, Long.valueOf(7));

		assertBoundary("longGreaterThan", 1, 0);
	}

	@Test
	public void should_report_covered_boundary_for_long_comparison()
			throws Exception {
		call("longGreaterThan", long.class, Long.valueOf(5));
		call("longGreaterThan", long.class, Long.valueOf(6));
		call("longGreaterThan", long.class, Long.valueOf(7));

		assertBoundary("longGreaterThan", 0, 1);
	}

	@Test
	public void should_report_covered_boundary_for_double_comparison()
			throws Exception {
		call("doubleLessThan", double.class, Double.valueOf(5));
		call("doubleLessThan", double.class, Double.valueOf(6));

		assertBoundary("doubleLessThan", 0, 1);
	}

	@Test
	public void should_not_report_boundary_for_double_comparison_with_nan()
			throws Exception {
		call("doubleLessThan", double.class, Double.valueOf(5));
		call("doubleLessThan", double.class, Double.valueOf(Double.NaN));

		assertBoundary("doubleLessThan", 1, 0);
	}

	private void call(final String name, final Class<?> argType,
			final Object arg) throws Exception {
		final Method method = target.getMethod(name, argType);
		method.invoke(null, arg);
	}

	private void assertBoundary(final String methodName, final int missed,
			final int covered) throws Exception {
		final ExecutionDataStore store = loader.collect();
		final CoverageBuilder builder = new CoverageBuilder();
		new Analyzer(store, builder).analyzeClass(
				TargetLoader.getClassDataAsBytes(BoundaryTarget.class),
				BoundaryTarget.class.getName());
		for (final IClassCoverage c : builder.getClasses()) {
			for (final IMethodCoverage m : c.getMethods()) {
				if (methodName.equals(m.getName())) {
					final ICounter expected = CounterImpl.getInstance(missed,
							covered);
					assertEquals("Boundaries in " + methodName, expected,
							m.getCounter(CounterEntity.BOUNDARY));
					return;
				}
			}
		}
		fail("Method not found: " + methodName);
	}

}
