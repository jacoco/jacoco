/*******************************************************************************
 * Copyright (c) 2009, 2010 Mountainminds GmbH & Co. KG and others
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
package org.jacoco.core.runtime;

import static org.junit.Assert.assertSame;

import java.util.Collections;
import java.util.Map;

import org.jacoco.core.data.ExecutionDataStore;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link MapAdapter}.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public class MapAdapterTest {

	private ExecutionDataStore store;

	private Map<Long, boolean[]> map;

	@Before
	public void setup() {
		store = new ExecutionDataStore();
		map = new MapAdapter(store);
	}

	@Test
	public void testGet() {
		boolean[] arr = new boolean[] { false, true, false };
		store.put(123, "Foo", arr);
		assertSame(arr, map.get(Long.valueOf(123)));
	}

	@Test(expected = IllegalStateException.class)
	public void testGetNegative() {
		map.get(Long.valueOf(123));
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testClear() {
		map.clear();
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testContainsKey() {
		map.containsKey(Long.valueOf(1));
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testContainsValue() {
		map.containsValue(Long.valueOf(1));
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testEntrySet() {
		map.entrySet();
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testIsEmpty() {
		map.isEmpty();
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testKeySet() {
		map.keySet();
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testPut() {
		map.put(Long.valueOf(0), new boolean[0]);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testPutAll() {
		final Map<Long, boolean[]> other = Collections.emptyMap();
		map.putAll(other);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testRemove() {
		map.remove(Long.valueOf(0));
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testValues() {
		map.values();
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testSize() {
		map.size();
	}

}
