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
package org.jacoco.core.test.validation.java5;

import static org.jacoco.core.test.validation.targets.Stubs.nop;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Collection;

import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.analysis.ICounter;
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
 * Various tests for cyclomatic complexity of methods.
 */
public class CyclomaticComplexityTest {

	public interface Target {
		public void test(int arg);
	}

	private RuntimeData data;
	private IRuntime runtime;
	private byte[] bytes;
	private Target target;

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

	public static class Simple implements Target {
		public void test(int arg) {
			nop();
			nop();
			nop();
		}
	}

	@Test
	public void testSimple1() throws Exception {
		instrument(Simple.class);
		final ICounter complexity = analyze();
		assertEquals(CounterImpl.getInstance(1, 0), complexity);
	}

	@Test
	public void testSimple2() throws Exception {
		instrument(Simple.class);
		target.test(0);
		final ICounter complexity = analyze();
		assertEquals(CounterImpl.getInstance(0, 1), complexity);
	}

	public static class If implements Target {
		public void test(int arg) {
			if (arg == 0) {
				nop();
			}
		}
	}

	@Test
	public void testIf1() throws Exception {
		instrument(If.class);
		final ICounter complexity = analyze();
		assertEquals(CounterImpl.getInstance(2, 0), complexity);
	}

	@Test
	public void testIf2() throws Exception {
		instrument(If.class);
		target.test(0);
		final ICounter complexity = analyze();
		assertEquals(CounterImpl.getInstance(1, 1), complexity);
	}

	@Test
	public void testIf3() throws Exception {
		instrument(If.class);
		target.test(0);
		target.test(1);
		final ICounter complexity = analyze();
		assertEquals(CounterImpl.getInstance(0, 2), complexity);
	}

	public static class TwoIf implements Target {
		public void test(int arg) {
			if (arg < 0) {
				nop();
			}
			if (arg > 0) {
				nop();
			}
		}
	}

	@Test
	public void testTwoIf1() throws Exception {
		instrument(TwoIf.class);
		final ICounter complexity = analyze();
		assertEquals(CounterImpl.getInstance(3, 0), complexity);
	}

	@Test
	public void testTwoIf2() throws Exception {
		instrument(TwoIf.class);
		target.test(-1);
		final ICounter complexity = analyze();
		assertEquals(CounterImpl.getInstance(2, 1), complexity);
	}

	@Test
	public void testTwoIf3() throws Exception {
		instrument(TwoIf.class);
		target.test(-1);
		target.test(0);
		final ICounter complexity = analyze();
		assertEquals(CounterImpl.getInstance(1, 2), complexity);
	}

	@Test
	public void testTwoIf4() throws Exception {
		instrument(TwoIf.class);
		target.test(-1);
		target.test(+1);
		final ICounter complexity = analyze();
		assertEquals(CounterImpl.getInstance(0, 3), complexity);
	}

	public static class NestedIf implements Target {
		public void test(int arg) {
			if (arg >= 0) {
				if (arg == 0) {
					nop();
				}
				nop();
			}
		}
	}

	@Test
	public void testNestedIf1() throws Exception {
		instrument(NestedIf.class);
		final ICounter complexity = analyze();
		assertEquals(CounterImpl.getInstance(3, 0), complexity);
	}

	@Test
	public void testNestedIf2() throws Exception {
		instrument(NestedIf.class);
		target.test(-1);
		final ICounter complexity = analyze();
		assertEquals(CounterImpl.getInstance(2, 1), complexity);
	}

	@Test
	public void testNestedIf3() throws Exception {
		instrument(NestedIf.class);
		target.test(-1);
		target.test(0);
		final ICounter complexity = analyze();
		assertEquals(CounterImpl.getInstance(1, 2), complexity);
	}

	@Test
	public void testNestedIf4() throws Exception {
		instrument(NestedIf.class);
		target.test(-1);
		target.test(0);
		target.test(+1);
		final ICounter complexity = analyze();
		assertEquals(CounterImpl.getInstance(0, 3), complexity);
	}

	public static class Switch implements Target {
		public void test(int arg) {
			switch (arg) {
			case 1:
				nop();
				break;
			case 2:
				nop();
				break;
			}
		}
	}

	@Test
	public void testSwitch1() throws Exception {
		instrument(Switch.class);
		final ICounter complexity = analyze();
		assertEquals(CounterImpl.getInstance(3, 0), complexity);
	}

	@Test
	public void testSwitch2() throws Exception {
		instrument(Switch.class);
		target.test(0);
		final ICounter complexity = analyze();
		assertEquals(CounterImpl.getInstance(2, 1), complexity);
	}

	@Test
	public void testSwitch3() throws Exception {
		instrument(Switch.class);
		target.test(0);
		target.test(1);
		final ICounter complexity = analyze();
		assertEquals(CounterImpl.getInstance(1, 2), complexity);
	}

	@Test
	public void testSwitch4() throws Exception {
		instrument(Switch.class);
		target.test(0);
		target.test(1);
		target.test(2);
		final ICounter complexity = analyze();
		assertEquals(CounterImpl.getInstance(0, 3), complexity);
	}

	private void instrument(final Class<? extends Target> clazz)
			throws Exception {
		bytes = TargetLoader.getClassDataAsBytes(clazz);
		final byte[] instrumented = new Instrumenter(runtime).instrument(bytes,
				"TestTarget");
		final TargetLoader loader = new TargetLoader();
		target = (Target) loader.add(clazz, instrumented).newInstance();
	}

	private ICounter analyze() throws IOException {
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
				return m.getComplexityCounter();
			}
		}
		throw new AssertionError("No test() method.");
	}

}
