/*******************************************************************************
 * Copyright (c) 2009, 2010 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.core.instr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link IntSet}.
 * 
 * @author Marc R. Hoffmann
 * @version $qualified.bundle.version$
 */
public class IntSetTest {

	private IntSet intset;

	@Before
	public void setup() {
		intset = new IntSet();
	}

	@Test
	public void testEmptySet() {
		assertFalse(intset.contains(15));
		assertContents();
	}

	@Test
	public void testAdd1() {
		intset.add(17);
		intset.add(-1);
		intset.add(0);
		intset.add(20);
		assertContents(-1, 0, 17, 20);
	}

	@Test
	public void testAdd2() {
		intset.add(11);
		intset.add(33);
		intset.add(11);
		intset.add(33);
		assertContents(11, 33);
	}

	@Test
	public void testAdd3() {
		final int[] expected = new int[100];
		for (int i = 0; i < expected.length; i++) {
			expected[i] = i;
			intset.add(i);
		}
		assertContents(expected);
	}

	@Test
	public void testContains() {
		intset.add(11);
		intset.add(15);
		assertFalse(intset.contains(-11));
		assertTrue(intset.contains(11));
		assertTrue(intset.contains(15));
		assertFalse(intset.contains(16));
	}

	@Test
	public void testClear() {
		intset.add(3);
		intset.add(17);
		intset.clear();
		assertContents();
	}

	private void assertContents(int... expected) {
		final int[] actual = intset.toArray();
		assertEquals(expected.length, actual.length, 0.0);
		for (int i = 0; i < expected.length; i++) {
			final Integer e = Integer.valueOf(expected[i]);
			final Integer a = Integer.valueOf(actual[i]);
			assertEquals("element at " + i, e, a);
		}
	}

}
