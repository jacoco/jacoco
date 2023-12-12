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

import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link ExecutionDataStore}.
 */
public class ExecutionDataStoreTest implements IExecutionDataVisitor {

	private ExecutionDataStore store;

	private Map<Long, ExecutionData> dataOutput;

	@Before
	public void setup() {
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
		final boolean[] probes = new boolean[] { false, false, true };
		store.put(new ExecutionData(1000, "Sample", probes));
		final ExecutionData data = store.get(1000);
		assertSame(probes, data.getProbes());
		assertTrue(store.contains("Sample"));
		store.accept(this);
		assertEquals(Collections.singletonMap(Long.valueOf(1000), data),
				dataOutput);
	}

	@Test
	public void testReentrantAccept() {
		final boolean[] probes = new boolean[] { false, false, true };
		store.put(new ExecutionData(1000, "Sample0", probes));
		store.put(new ExecutionData(1001, "Sample1", probes));
		store.accept(new IExecutionDataVisitor() {
			public void visitClassExecution(ExecutionData data) {
				store.put(new ExecutionData(1002, "Sample2", probes));
				ExecutionDataStoreTest.this.visitClassExecution(data);
			}
		});
		assertEquals(2, dataOutput.size());
	}

	@Test
	public void testGetContents() {
		final boolean[] probes = new boolean[] {};
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
				new boolean[] {});
		store.put(data);
		assertSame(data, store.get(1000));
	}

	@Test
	public void testGetWithCreate() {
		final Long id = Long.valueOf(1000);
		final ExecutionData data = store.get(id, "Sample", 3);
		assertEquals(1000, data.getId());
		assertEquals("Sample", data.getName());
		assertEquals(3, data.getProbes().length);
		assertFalse(data.getProbes()[0]);
		assertFalse(data.getProbes()[1]);
		assertFalse(data.getProbes()[2]);
		assertSame(data, store.get(id, "Sample", 3));
		assertTrue(store.contains("Sample"));
	}

	@Test(expected = IllegalStateException.class)
	public void testGetNegative1() {
		final boolean[] data = new boolean[] { false, false, true };
		store.put(new ExecutionData(1000, "Sample", data));
		store.get(Long.valueOf(1000), "Other", 3);
	}

	@Test(expected = IllegalStateException.class)
	public void testGetNegative2() {
		final boolean[] data = new boolean[] { false, false, true };
		store.put(new ExecutionData(1000, "Sample", data));
		store.get(Long.valueOf(1000), "Sample", 4);
	}

	@Test(expected = IllegalStateException.class)
	public void testPutNegative() {
		final boolean[] data = new boolean[0];
		store.put(new ExecutionData(1000, "Sample1", data));
		store.put(new ExecutionData(1000, "Sample2", data));
	}

	@Test
	public void testMerge() {
		final boolean[] data1 = new boolean[] { false, true, false, true };
		store.visitClassExecution(new ExecutionData(1000, "Sample", data1));
		final boolean[] data2 = new boolean[] { false, true, true, false };
		store.visitClassExecution(new ExecutionData(1000, "Sample", data2));

		final boolean[] result = store.get(1000).getProbes();
		assertFalse(result[0]);
		assertTrue(result[1]);
		assertTrue(result[2]);
		assertTrue(result[3]);
	}

	@Test(expected = IllegalStateException.class)
	public void testMergeNegative() {
		final boolean[] data1 = new boolean[] { false, false };
		store.visitClassExecution(new ExecutionData(1000, "Sample", data1));
		final boolean[] data2 = new boolean[] { false, false, false };
		store.visitClassExecution(new ExecutionData(1000, "Sample", data2));
	}

	@Test
	public void testSubtract() {
		final boolean[] data1 = new boolean[] { false, true, false, true };
		store.put(new ExecutionData(1000, "Sample", data1));
		final boolean[] data2 = new boolean[] { false, false, true, true };
		store.subtract(new ExecutionData(1000, "Sample", data2));

		final boolean[] result = store.get(1000).getProbes();
		assertFalse(result[0]);
		assertTrue(result[1]);
		assertFalse(result[2]);
		assertFalse(result[3]);
	}

	@Test
	public void testSubtractOtherId() {
		final boolean[] data1 = new boolean[] { false, true };
		store.put(new ExecutionData(1000, "Sample1", data1));
		final boolean[] data2 = new boolean[] { true, true };
		store.subtract(new ExecutionData(2000, "Sample2", data2));

		final boolean[] result = store.get(1000).getProbes();
		assertFalse(result[0]);
		assertTrue(result[1]);

		assertNull(store.get(2000));
	}

	@Test
	public void testSubtractStore() {
		final boolean[] data1 = new boolean[] { false, true, false, true };
		store.put(new ExecutionData(1000, "Sample", data1));

		final ExecutionDataStore store2 = new ExecutionDataStore();
		final boolean[] data2 = new boolean[] { false, false, true, true };
		store2.put(new ExecutionData(1000, "Sample", data2));

		store.subtract(store2);

		final boolean[] result = store.get(1000).getProbes();
		assertFalse(result[0]);
		assertTrue(result[1]);
		assertFalse(result[2]);
		assertFalse(result[3]);
	}

	@Test
	public void testReset()
			throws InstantiationException, IllegalAccessException {
		final boolean[] data1 = new boolean[] { true, true, false };
		store.put(new ExecutionData(1000, "Sample", data1));
		store.reset();
		final boolean[] data2 = store.get(1000).getProbes();
		assertNotNull(data2);
		assertFalse(data2[0]);
		assertFalse(data2[1]);
		assertFalse(data2[2]);
	}

	// === IExecutionDataOutput ===

	public void visitClassExecution(final ExecutionData data) {
		dataOutput.put(Long.valueOf(data.getId()), data);
	}

}
