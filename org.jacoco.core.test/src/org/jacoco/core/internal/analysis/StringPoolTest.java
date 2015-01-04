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
package org.jacoco.core.internal.analysis;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link StringPool}.
 */
public class StringPoolTest {

	private StringPool pool;

	@Before
	public void setup() {
		pool = new StringPool();
	}

	@Test
	public void testGetStringNull() {
		assertNull(pool.get((String) null));
	}

	@Test
	public void testGetString() {
		final String a = pool.get(new String("JaCoCo"));
		final String b = pool.get(new String("JaCoCo"));

		assertEquals("JaCoCo", a);
		assertEquals("JaCoCo", b);
		assertSame(a, b);
	}

	@Test
	public void testGetArrayNull() {
		assertNull(pool.get((String[]) null));
	}

	@Test
	public void testGetEmptyArray() {
		final String[] arr1 = pool.get(new String[0]);
		final String[] arr2 = pool.get(new String[0]);

		assertEquals(0, arr1.length);
		assertSame(arr1, arr2);
	}

	@Test
	public void testGetArray() {
		final String[] arr1 = pool.get(new String[] { new String("JaCoCo") });
		final String[] arr2 = pool.get(new String[] { new String("JaCoCo") });

		assertEquals(1, arr1.length);
		assertEquals("JaCoCo", arr1[0]);
		assertSame(arr1[0], arr2[0]);
	}

}
