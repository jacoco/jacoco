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

import static org.junit.Assert.assertArrayEquals;
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
		final int[] probes = new int[] { 0, 0, 1 };
		store.put(new ExecutionData(1000, "Sample", probes));
		final ExecutionData data = store.get(1000);
		assertArrayEquals(probes, data.getProbes());
		assertTrue(store.contains("Sample"));
		store.accept(this);
		assertEquals(Collections.singletonMap(Long.valueOf(1000), data),
				dataOutput);
	}

	@Test
	public void testGetContents() {
		final int[] probes = new int[] {};
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
				new int[] {});
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
		assertFalse(data.getProbes()[0] != 0);
		assertFalse(data.getProbes()[1] != 0);
		assertFalse(data.getProbes()[2] != 0);
		assertSame(data, store.get(id, "Sample", 3));
		assertTrue(store.contains("Sample"));
	}

	@Test(expected = IllegalStateException.class)
	public void testGetNegative1() {
		final int[] data = new int[] { 0, 0, 1 };
		store.put(new ExecutionData(1000, "Sample", data));
		store.get(Long.valueOf(1000), "Other", 3);
	}

	@Test(expected = IllegalStateException.class)
	public void testGetNegative2() {
		final int[] data = new int[] { 0, 0, 1 };
		store.put(new ExecutionData(1000, "Sample", data));
		store.get(Long.valueOf(1000), "Sample", 4);
	}

	@Test(expected = IllegalStateException.class)
	public void testPutNegative() {
		final int[] data = new int[0];
		store.put(new ExecutionData(1000, "Sample1", data));
		store.put(new ExecutionData(1000, "Sample2", data));
	}

	@Test
	public void testMerge() {
		final int[] data1 = new int[] { 0, 5, 0, 5 };
		store.visitClassExecution(new ExecutionData(1000, "Sample", data1));
		final int[] data2 = new int[] { 0, 5, 5, 0 };
		store.visitClassExecution(new ExecutionData(1000, "Sample", data2));

		final int[] result = store.get(1000).getProbes();
		assertFalse(result[0] != 0);
		assertTrue(result[1] != 0);
		assertTrue(result[2] != 0);
		assertTrue(result[3] != 0);
	}

	@Test(expected = IllegalStateException.class)
	public void testMergeNegative() {
		final int[] data1 = new int[] { 0, 0 };
		store.visitClassExecution(new ExecutionData(1000, "Sample", data1));
		final int[] data2 = new int[] { 0, 0, 0 };
		store.visitClassExecution(new ExecutionData(1000, "Sample", data2));
	}

	@Test
	public void testSubtract() {
		final int[] data1 = new int[] { 0, 4, 4, 4 };
		store.put(new ExecutionData(1000, "Sample", data1));
		final int[] data2 = new int[] { 0, 0, 4, 4 };
		store.subtract(new ExecutionData(1000, "Sample", data2));

		final int[] result = store.get(1000).getProbes();
		assertFalse(result[0] != 0);
		assertTrue(result[1] != 0);
		assertFalse(result[2] != 0);
		assertFalse(result[3] != 0);
	}

	@Test
	public void testSubtractOtherId() {
		final int[] data1 = new int[] { 0, 3 };
		store.put(new ExecutionData(1000, "Sample1", data1));
		final int[] data2 = new int[] { 3, 3 };
		store.subtract(new ExecutionData(2000, "Sample2", data2));

		final int[] result = store.get(1000).getProbes();
		assertFalse(result[0] != 0);
		assertTrue(result[1] != 0);

		assertNull(store.get(2000));
	}

	@Test
	public void testSubtractStore() {
		final int[] data1 = new int[] { 0, 2, 0, 1 };
		store.put(new ExecutionData(1000, "Sample", data1));

		final ExecutionDataStore store2 = new ExecutionDataStore();
		final int[] data2 = new int[] { 0, 0, 1, 1 };
		store2.put(new ExecutionData(1000, "Sample", data2));

		store.subtract(store2);

		final int[] result = store.get(1000).getProbes();
		assertFalse(result[0] != 0);
		assertTrue(result[1] != 0);
		assertFalse(result[2] != 0);
		assertFalse(result[3] != 0);
	}

	@Test
	public void testReset() throws InstantiationException,
			IllegalAccessException {
		final int[] data1 = new int[] { 1, 1, 0 };
		store.put(new ExecutionData(1000, "Sample", data1));
		store.reset();
		final int[] data2 = store.get(1000).getProbes();
		assertNotNull(data2);
		assertFalse(data2[0] != 0);
		assertFalse(data2[1] != 0);
		assertFalse(data2[2] != 0);
	}

	// === IExecutionDataOutput ===

	public void visitClassExecution(final ExecutionData data) {
		dataOutput.put(Long.valueOf(data.getId()), data);
	}

}
