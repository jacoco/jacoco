/*******************************************************************************
 * Copyright (c) 2009 Mountainminds GmbH & Co. KG and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 * $Id: $
 *******************************************************************************/
package org.jacoco.core.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link ExecutionDataStore}.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public class ExecutionDataStoreTest implements IExecutionDataVisitor {

	private ExecutionDataStore store;

	private Map<Long, boolean[]> dataOutput;

	private Map<Long, String> nameOutput;

	@Before
	public void setup() {
		store = new ExecutionDataStore();
		dataOutput = new HashMap<Long, boolean[]>();
		nameOutput = new HashMap<Long, String>();
	}

	@Test
	public void testEmpty() {
		assertNull(store.getData(123));
		store.accept(this);
		assertEquals(Collections.emptyMap(), dataOutput);
	}

	@Test
	public void testPut() {
		boolean[] data = new boolean[] { false, false, true };
		store.put(1000, "Sample", data);
		assertSame(data, store.getData(1000));
		assertEquals("Sample", store.getName(1000));
		store.accept(this);
		assertEquals(Collections.singletonMap(Long.valueOf(1000), data),
				dataOutput);
		assertEquals(Collections.singletonMap(Long.valueOf(1000), "Sample"),
				nameOutput);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testIdClash() {
		boolean[] data = new boolean[0];
		store.put(1000, "Sample1", data);
		store.put(1000, "Sample2", data);
	}

	@Test
	public void testMerge() {
		boolean[] data1 = new boolean[] { false, true, false, true };
		store.visitClassExecution(1000, "Sample", data1);
		boolean[] data2 = new boolean[] { false, true, true, false };
		store.visitClassExecution(1000, "Sample", data2);

		final boolean[] result = store.getData(1000);
		assertFalse(result[0]);
		assertTrue(result[1]);
		assertTrue(result[2]);
		assertTrue(result[3]);
	}

	@Test(expected = IllegalStateException.class)
	public void testNegative1() {
		boolean[] data1 = new boolean[] { false, false };
		store.visitClassExecution(1000, "Sample", data1);
		boolean[] data2 = new boolean[] { false, false, false };
		store.visitClassExecution(1000, "Sample", data2);
	}

	@Test
	public void testReset() throws InstantiationException,
			IllegalAccessException {
		final boolean[] data1 = new boolean[] { true, true, false };
		store.put(1000, "Sample", data1);
		store.reset();
		boolean[] data2 = store.getData(1000);
		assertNotNull(data2);
		assertFalse(data2[0]);
		assertFalse(data2[1]);
		assertFalse(data2[2]);
	}

	// === IExecutionDataOutput ===

	public void visitClassExecution(long id, String name, boolean[] blockdata) {
		Long key = Long.valueOf(id);
		dataOutput.put(key, blockdata);
		nameOutput.put(key, name);
	}

}
