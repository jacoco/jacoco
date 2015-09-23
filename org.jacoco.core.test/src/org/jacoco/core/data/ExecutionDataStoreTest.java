/*******************************************************************************
 * Copyright (c) 2009, 2015 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.core.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jacoco.core.internal.instr.IProbeArray;
import org.jacoco.core.internal.instr.ProbeArrayService;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Unit tests for {@link ExecutionDataStore}.
 */
public class ExecutionDataStoreTest {

	public static class ExecutionExistsDataStoreTest extends
			ExecutionDataStoreTestBase {
		@BeforeClass
		public static void setup() {
			ProbeArrayService.reset();
			ProbeArrayService.configure(ProbeMode.exists);
		}

		@AfterClass
		public static void teardown() {
			ProbeArrayService.reset();
		}
	}

	public static class ExecutionCountDataStoreTest extends
			ExecutionDataStoreTestBase {
		@BeforeClass
		public static void setup() {
			ProbeArrayService.reset();
			ProbeArrayService.configure(ProbeMode.count);
		}

		@AfterClass
		public static void teardown() {
			ProbeArrayService.reset();
		}
	}

	public static class ExecutionParallelDataStoreTest extends
			ExecutionDataStoreTestBase {
		@BeforeClass
		public static void setup() {
			ProbeArrayService.reset();
			ProbeArrayService.configure(ProbeMode.parallelcount);
		}

		@AfterClass
		public static void teardown() {
			ProbeArrayService.reset();
		}
	}

	public static abstract class ExecutionDataStoreTestBase implements
			IExecutionDataVisitor {

		private ExecutionDataStore store;

		private Map<Long, ExecutionData> dataOutput;

		@Before
		public void setupMethod() {
			store = new ExecutionDataStore();
			dataOutput = new HashMap<Long, ExecutionData>();
		}

		@Test
		public void testEmpty() {
			assertNull(store.get(123));
			assertFalse(store.contains("org/jacoco/example/Foo"));
			store.accept(this);
			assertEquals(Collections.emptyMap(), dataOutput);
		}

		@Test
		public void testPut() {
			final IProbeArray<?> probes = createProbes(new int[] { 0, 0, 1 });
			store.put(new ExecutionData(1000, "Sample", probes));
			final ExecutionData data = store.get(1000);
			assertEquals(probes, data.getProbes());
			assertTrue(store.contains("Sample"));
			store.accept(this);
			assertEquals(Collections.singletonMap(Long.valueOf(1000), data),
					dataOutput);
		}

		@Test
		public void testGetContents() {
			final IProbeArray<?> probes = createProbes(new int[] {});
			final ExecutionData a = new ExecutionData(1000, "A", probes);
			store.put(a);
			final ExecutionData aa = new ExecutionData(1000, "A", probes);
			store.put(aa);
			final ExecutionData b = new ExecutionData(1001, "B", probes);
			store.put(b);
			final Set<ExecutionData> actual = new HashSet<ExecutionData>(
					store.getContents());
			final Set<ExecutionData> expected = new HashSet<ExecutionData>(
					Arrays.asList(a, b));
			assertEquals(expected, actual);
		}

		@Test
		public void testGetWithoutCreate() {
			final ExecutionData data = new ExecutionData(1000, "Sample",
					createProbes(new int[] {}));
			store.put(data);
			assertSame(data, store.get(1000));
		}

		@Test
		public void testGetWithCreate() {
			final Long id = Long.valueOf(1000);
			final ExecutionData data = store.get(id, "Sample", 3);
			assertEquals(1000, data.getId());
			assertEquals("Sample", data.getName());
			assertEquals(3, data.getProbes().length());
			assertFalse(data.getProbes().isProbeCovered(0));
			assertFalse(data.getProbes().isProbeCovered(1));
			assertFalse(data.getProbes().isProbeCovered(2));
			assertSame(data, store.get(id, "Sample", 3));
			assertTrue(store.contains("Sample"));
		}

		@Test(expected = IllegalStateException.class)
		public void testGetNegative1() {
			final IProbeArray<?> data = createProbes(new int[] { 0, 0, 1 });
			store.put(new ExecutionData(1000, "Sample", data));
			store.get(Long.valueOf(1000), "Other", 3);
		}

		@Test(expected = IllegalStateException.class)
		public void testGetNegative2() {
			final IProbeArray<?> data = createProbes(new int[] { 0, 0, 1 });
			store.put(new ExecutionData(1000, "Sample", data));
			store.get(Long.valueOf(1000), "Sample", 4);
		}

		@Test(expected = IllegalStateException.class)
		public void testPutNegative() {
			final IProbeArray<?> data = createProbes(new int[0]);
			store.put(new ExecutionData(1000, "Sample1", data));
			store.put(new ExecutionData(1000, "Sample2", data));
		}

		@Test
		public void testMerge() {
			final IProbeArray<?> data1 = createProbes(new int[] { 0, 5, 0, 5 });
			store.visitClassExecution(new ExecutionData(1000, "Sample", data1));
			final IProbeArray<?> data2 = createProbes(new int[] { 0, 5, 5, 0 });
			store.visitClassExecution(new ExecutionData(1000, "Sample", data2));

			final IProbeArray<?> result = store.get(1000).getProbes();
			assertFalse(result.isProbeCovered(0));
			assertTrue(result.isProbeCovered(1));
			assertTrue(result.isProbeCovered(2));
			assertTrue(result.isProbeCovered(3));
		}

		@Test(expected = IllegalStateException.class)
		public void testMergeNegative() {
			final IProbeArray<?> data1 = createProbes(new int[] { 0, 0 });
			store.visitClassExecution(new ExecutionData(1000, "Sample", data1));
			final IProbeArray<?> data2 = createProbes(new int[] { 0, 0, 0 });
			store.visitClassExecution(new ExecutionData(1000, "Sample", data2));
		}

		@Test
		public void testSubtract() {
			final IProbeArray<?> data1 = createProbes(new int[] { 0, 4, 4, 4 });
			store.put(new ExecutionData(1000, "Sample", data1));
			final IProbeArray<?> data2 = createProbes(new int[] { 0, 0, 4, 4 });
			store.subtract(new ExecutionData(1000, "Sample", data2));

			final IProbeArray<?> result = store.get(1000).getProbes();
			assertFalse(result.isProbeCovered(0));
			assertTrue(result.isProbeCovered(1));
			assertFalse(result.isProbeCovered(2));
			assertFalse(result.isProbeCovered(3));
		}

		@Test
		public void testSubtractOtherId() {
			final IProbeArray<?> data1 = createProbes(new int[] { 0, 3 });
			store.put(new ExecutionData(1000, "Sample1", data1));
			final IProbeArray<?> data2 = createProbes(new int[] { 3, 3 });
			store.subtract(new ExecutionData(2000, "Sample2", data2));

			final IProbeArray<?> result = store.get(1000).getProbes();
			assertFalse(result.isProbeCovered(0));
			assertTrue(result.isProbeCovered(1));

			assertNull(store.get(2000));
		}

		@Test
		public void testSubtractStore() {
			final IProbeArray<?> data1 = createProbes(new int[] { 0, 2, 0, 1 });
			store.put(new ExecutionData(1000, "Sample", data1));

			final ExecutionDataStore store2 = new ExecutionDataStore();
			final IProbeArray<?> data2 = createProbes(new int[] { 0, 0, 1, 1 });
			store2.put(new ExecutionData(1000, "Sample", data2));

			store.subtract(store2);

			final IProbeArray<?> result = store.get(1000).getProbes();
			assertFalse(result.isProbeCovered(0));
			assertTrue(result.isProbeCovered(1));
			assertFalse(result.isProbeCovered(2));
			assertFalse(result.isProbeCovered(3));
		}

		@Test
		public void testReset() throws InstantiationException,
				IllegalAccessException {
			final IProbeArray<?> data1 = createProbes(new int[] { 1, 1, 0 });
			store.put(new ExecutionData(1000, "Sample", data1));
			store.reset();
			final IProbeArray<?> data2 = store.get(1000).getProbes();
			assertNotNull(data2);
			assertFalse(data2.isProbeCovered(0));
			assertFalse(data2.isProbeCovered(1));
			assertFalse(data2.isProbeCovered(2));
		}

		// === IExecutionDataOutput ===

		public void visitClassExecution(final ExecutionData data) {
			dataOutput.put(Long.valueOf(data.getId()), data);
		}

		private IProbeArray<?> createProbes(int[] data) {
			IProbeArray<?> probes = ProbeArrayService
					.newProbeArray(data.length);
			for (int i = 0; i < data.length; i++) {
				for (int j = data[i]; j > 0; j--) {
					probes.increment(i);
				}
			}
			return probes;
		}
	}
}
