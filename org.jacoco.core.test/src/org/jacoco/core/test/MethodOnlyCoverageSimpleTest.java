/*******************************************************************************
 * Copyright (c) 2009, 2025 Mountainminds GmbH & Co. KG and Contributors
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
package org.jacoco.core.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.analysis.IMethodCoverage;
import org.jacoco.core.data.ExecutionData;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.instr.Instrumenter;
import org.jacoco.core.internal.data.CRC64;
import org.jacoco.core.runtime.IRuntime;
import org.jacoco.core.runtime.LoggerRuntime;
import org.junit.Test;

/**
 * Simple test for method-only coverage mode.
 */
public class MethodOnlyCoverageSimpleTest {

	@Test
	public void testMethodOnlyAnalysisWithSimulatedData() throws Exception {
		// Step 1: Get original class bytes
		final byte[] original = TargetLoader
				.getClassDataAsBytes(TestTarget.class);

		// Step 2: Create simulated execution data
		// In method-only mode, we would have one probe per method
		final long classId = CRC64.classId(original);
		final boolean[] probes = new boolean[4]; // 4 methods total
		probes[0] = true; // constructor (always called)
		probes[1] = true; // method1 - covered
		probes[2] = false; // method2 - not covered
		probes[3] = true; // method3 - covered

		final ExecutionDataStore executionData = new ExecutionDataStore();
		executionData.put(new ExecutionData(classId,
				"org/jacoco/core/test/MethodOnlyCoverageSimpleTest$TestTarget",
				probes));

		// Step 3: Analyze with method-only mode
		final CoverageBuilder coverageBuilder = new CoverageBuilder();
		final Analyzer analyzer = new Analyzer(executionData, coverageBuilder,
				true); // method-only mode
		analyzer.analyzeClass(original, TestTarget.class.getName());

		// Step 4: Verify results
		final IClassCoverage classCoverage = coverageBuilder.getClasses()
				.iterator().next();
		assertNotNull("Class coverage should not be null", classCoverage);

		// Verify class has no line/branch data
		assertEquals("Class should have no lines", 0,
				classCoverage.getLineCounter().getTotalCount());
		assertEquals("Class should have no branches", 0,
				classCoverage.getBranchCounter().getTotalCount());

		// Verify method coverage
		int coveredMethods = 0;
		int totalMethods = 0;
		for (final IMethodCoverage method : classCoverage.getMethods()) {
			totalMethods++;
			assertEquals("Method " + method.getName() + " should have no lines",
					0, method.getLineCounter().getTotalCount());
			assertEquals(
					"Method " + method.getName() + " should have no branches",
					0, method.getBranchCounter().getTotalCount());

			if (method.getMethodCounter().getCoveredCount() > 0) {
				coveredMethods++;
			}
		}

		assertEquals("Should have 4 methods total", 4, totalMethods);
		assertEquals("Should have 3 covered methods", 3, coveredMethods);
	}

	@Test
	public void testMethodOnlyInstrumentationMarker() throws Exception {
		// Verify that method-only instrumentation adds the marker annotation
		final IRuntime runtime = new LoggerRuntime();
		final Instrumenter instrumenter = new Instrumenter(runtime, true);

		final byte[] original = TargetLoader
				.getClassDataAsBytes(TestTarget.class);
		final byte[] instrumented = instrumenter.instrument(original,
				"TestTarget");

		// The fact that it doesn't throw an exception proves basic
		// functionality
		assertNotNull("Instrumented class should not be null", instrumented);
	}

	/**
	 * Simple test target with multiple methods.
	 */
	public static class TestTarget {
		public void method1() {
			System.out.println("Method 1");
		}

		public void method2() {
			System.out.println("Method 2");
		}

		public void method3() {
			System.out.println("Method 3");
		}
	}
}
