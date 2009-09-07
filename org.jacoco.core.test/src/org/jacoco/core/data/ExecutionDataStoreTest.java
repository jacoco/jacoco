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

	private HashMap<Long, boolean[][]> output;

	@Before
	public void setup() {
		store = new ExecutionDataStore();
		output = new HashMap<Long, boolean[][]>();
	}

	@Test
	public void testEmpty() {
		assertNull(store.get(123));
		store.accept(this);
		assertEquals(Collections.emptyMap(), output);
	}

	@Test
	public void testPut() {
		boolean[][] data = new boolean[][] { new boolean[] { false },
				new boolean[] { false, true } };
		store.put(1000, data);
		assertSame(data, store.get(1000));
		store.accept(this);
		assertEquals(Collections.singletonMap(Long.valueOf(1000), data), output);
	}

	@Test
	public void testMerge() {
		boolean[][] data1 = new boolean[][] { new boolean[] { false, true },
				new boolean[] { false, true } };
		store.visitClassExecution(1000, data1);
		boolean[][] data2 = new boolean[][] { new boolean[] { false, true },
				new boolean[] { true, false } };
		store.visitClassExecution(1000, data2);

		final boolean[][] result = store.get(1000);
		assertFalse(result[0][0]);
		assertTrue(result[0][1]);
		assertTrue(result[1][0]);
		assertTrue(result[1][1]);
	}

	@Test(expected = IllegalStateException.class)
	public void testNegative1() {
		boolean[][] data1 = new boolean[][] { new boolean[] { false },
				new boolean[] { false } };
		store.visitClassExecution(1000, data1);
		boolean[][] data2 = new boolean[][] { new boolean[] { false },
				new boolean[] { false }, new boolean[] { false } };
		store.visitClassExecution(1000, data2);
	}

	@Test(expected = IllegalStateException.class)
	public void testNegative2() {
		boolean[][] data1 = new boolean[][] { new boolean[] { false, false } };
		store.visitClassExecution(1000, data1);
		boolean[][] data2 = new boolean[][] { new boolean[] { false, false,
				false } };
		store.visitClassExecution(1000, data2);
	}

	@Test
	public void testReset() throws InstantiationException,
			IllegalAccessException {
		final boolean[][] data1 = new boolean[1][];
		data1[0] = new boolean[] { true, true, true };
		store.put(1000, data1);
		store.reset();
		boolean[][] data2 = store.get(1000);
		assertNotNull(data2);
		assertFalse(data2[0][0]);
		assertFalse(data2[0][1]);
		assertFalse(data2[0][2]);
	}

	// === IExecutionDataOutput ===

	public void visitClassExecution(long id, boolean[][] blockdata) {
		output.put(Long.valueOf(id), blockdata);
	}

}
