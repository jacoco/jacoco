/*******************************************************************************
 * Copyright (c) 2009, 2026 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    JaCoCo contributors - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.internal.instr;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.jacoco.core.data.ExecutionData;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.data.SessionInfoStore;
import org.jacoco.core.instr.Instrumenter;
import org.jacoco.core.runtime.RuntimeData;
import org.jacoco.core.runtime.SystemPropertiesRuntime;
import org.jacoco.core.test.TargetLoader;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OperationsPerInvocation;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;

/**
 * Measures actual instrumentation with a workload and probe array shared by all
 * benchmark threads. Run with separate forks at 1, 2, 4, 8 and available-core
 * thread counts, for example:
 *
 * <pre>
 * java -jar target/benchmarks.jar ProbeContentionBenchmark -t 1 -f 2 -wi 5 -i 5
 * </pre>
 *
 * Compare both workloads and all modes; conditional probes can also slow down
 * execution. There is deliberately no machine-specific performance threshold.
 */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 1)
public class ProbeContentionBenchmark {

	@Param({ "uninstrumented", "default", "conditional" })
	public String mode;

	@Param({ "false", "true" })
	public boolean large;

	private Callable<Integer> workload;

	private SystemPropertiesRuntime runtime;

	private RuntimeData data;

	@Setup
	@SuppressWarnings("unchecked")
	public void setup() throws Exception {
		runtime = new SystemPropertiesRuntime();
		data = new RuntimeData();
		runtime.startup(data);
		byte[] bytes = TargetLoader.getClassDataAsBytes(Workload.class);
		if (!"uninstrumented".equals(mode)) {
			Instrumenter instrumenter = new Instrumenter(runtime);
			instrumenter.setConditionalProbes("conditional".equals(mode));
			bytes = instrumenter.instrument(bytes, Workload.class.getName());
		}
		workload = (Callable<Integer>) new TargetLoader()
				.add(Workload.class, bytes).getConstructor(Boolean.TYPE)
				.newInstance(Boolean.valueOf(large));
	}

	@Benchmark
	@OperationsPerInvocation(10000)
	public Integer hotCalls() throws Exception {
		return workload.call();
	}

	@TearDown
	public void collectCoverage() {
		try {
			ExecutionDataStore store = new ExecutionDataStore();
			data.collect(store, new SessionInfoStore(), false);
			boolean hit = false;
			for (ExecutionData entry : store.getContents()) {
				for (boolean probe : entry.getProbes()) {
					hit |= probe;
				}
			}
			if (!"uninstrumented".equals(mode) && !hit) {
				throw new AssertionError("No coverage recorded");
			}
		} finally {
			runtime.shutdown();
		}
	}

	/**
	 * Work is repeated inside the loaded class, outside reflection overhead.
	 */
	public static class Workload implements Callable<Integer> {

		private final boolean large;

		public Workload(final boolean large) {
			this.large = large;
		}

		public Integer call() {
			int value = (int) System.nanoTime();
			for (int i = 0; i < 10000; i++) {
				value = large ? large(value) : small(value);
			}
			return Integer.valueOf(value);
		}

		private int small(final int value) {
			return value * 31 + 1;
		}

		private int large(int value) {
			// Enough application bytecode to expose changes to inlining
			// decisions.
			if ((value & 1) != 0) {
				value ^= 0x12345678;
			}
			if ((value & 2) != 0) {
				value += 17;
			}
			if ((value & 4) != 0) {
				value *= 31;
			}
			if ((value & 8) != 0) {
				value ^= value >>> 3;
			}
			if ((value & 16) != 0) {
				value += 101;
			}
			if ((value & 32) != 0) {
				value *= 13;
			}
			if ((value & 64) != 0) {
				value ^= value << 7;
			}
			if ((value & 128) != 0) {
				value -= 71;
			}
			if ((value & 256) != 0) {
				value *= 7;
			}
			if ((value & 512) != 0) {
				value ^= value >>> 11;
			}
			if ((value & 1024) != 0) {
				value += 103;
			}
			if ((value & 2048) != 0) {
				value *= 19;
			}
			if ((value & 4096) != 0) {
				value ^= value << 13;
			}
			if ((value & 8192) != 0) {
				value -= 107;
			}
			if ((value & 16384) != 0) {
				value *= 23;
			}
			if ((value & 32768) != 0) {
				value ^= value >>> 17;
			}
			if ((value & 65536) != 0) {
				value += 109;
			}
			if ((value & 131072) != 0) {
				value *= 29;
			}
			if ((value & 262144) != 0) {
				value ^= value << 19;
			}
			if ((value & 524288) != 0) {
				value -= 113;
			}
			if ((value & 1048576) != 0) {
				value *= 37;
			}
			if ((value & 2097152) != 0) {
				value ^= value >>> 23;
			}
			if ((value & 4194304) != 0) {
				value += 127;
			}
			if ((value & 8388608) != 0) {
				value *= 41;
			}
			return value * 31 + 1;
		}
	}
}
