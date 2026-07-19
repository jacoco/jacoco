/*******************************************************************************
 * Copyright (c) 2009, 2026 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0
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
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

/**
 * Unit tests for {@link ExecutionData}.
 */
public class ExecutionDataTest {

	@Test
	public void testCreateEmpty() {
		final ExecutionData e = new ExecutionData(5, "Example", 3);
		assertEquals(5, e.getId());
		assertEquals("Example", e.getName());
		assertEquals(3, e.getProbes().length);
		assertFalse(e.getProbes()[0]);
		assertFalse(e.getProbes()[1]);
		assertFalse(e.getProbes()[2]);
	}

	@Test
	public void testGetters() {
		final boolean[] data = new boolean[0];
		final ExecutionData e = new ExecutionData(5, "Example", data);
		assertEquals(5, e.getId());
		assertEquals("Example", e.getName());
		assertSame(data, e.getProbes());
	}

	@Test
	public void testReset() {
		final ExecutionData e = new ExecutionData(5, "Example",
				new boolean[] { true, false, true });
		e.reset();
		assertFalse(e.getProbes()[0]);
		assertFalse(e.getProbes()[1]);
		assertFalse(e.getProbes()[2]);
	}

	@Test
	public void testHasHits() {
		final boolean[] probes = new boolean[] { false, false, false };
		final ExecutionData e = new ExecutionData(5, "Example", probes);
		assertFalse(e.hasHits());
		probes[1] = true;
		assertTrue(e.hasHits());
	}

	@Test
	public void testHasHits_empty() {
		final boolean[] probes = new boolean[] {};
		final ExecutionData e = new ExecutionData(5, "Example", probes);
		assertFalse(e.hasHits());
	}

	@Test
	public void testMerge() {
		final ExecutionData a = new ExecutionData(5, "Example",
				new boolean[] { false, true, false, true });
		final ExecutionData b = new ExecutionData(5, "Example",
				new boolean[] { false, false, true, true });
		a.merge(b);

		// b is merged into a:
		assertFalse(a.getProbes()[0]);
		assertTrue(a.getProbes()[1]);
		assertTrue(a.getProbes()[2]);
		assertTrue(a.getProbes()[3]);

		// b must not be modified:
		assertFalse(b.getProbes()[0]);
		assertFalse(b.getProbes()[1]);
		assertTrue(b.getProbes()[2]);
		assertTrue(b.getProbes()[3]);
	}

	@Test
	public void testMergeSubtract() {
		final ExecutionData a = new ExecutionData(5, "Example",
				new boolean[] { false, true, false, true });
		final ExecutionData b = new ExecutionData(5, "Example",
				new boolean[] { false, false, true, true });
		a.merge(b, false);

		// b is subtracted from a:
		assertFalse(a.getProbes()[0]);
		assertTrue(a.getProbes()[1]);
		assertFalse(a.getProbes()[2]);
		assertFalse(a.getProbes()[3]);

		// b must not be modified:
		assertFalse(b.getProbes()[0]);
		assertFalse(b.getProbes()[1]);
		assertTrue(b.getProbes()[2]);
		assertTrue(b.getProbes()[3]);
	}

	@Test
	public void testMergeLongerData() {
		final ExecutionData a = new ExecutionData(5, "Example",
				new boolean[] { false, true });
		final ExecutionData b = new ExecutionData(5, "Example",
				new boolean[] { true, false, false, true });
		a.merge(b);

		// a is extended to the length of b:
		assertEquals(4, a.getProbes().length);
		assertTrue(a.getProbes()[0]);
		assertTrue(a.getProbes()[1]);
		assertFalse(a.getProbes()[2]);
		assertTrue(a.getProbes()[3]);

		// b must not be modified:
		assertTrue(b.getProbes()[0]);
		assertFalse(b.getProbes()[1]);
		assertFalse(b.getProbes()[2]);
		assertTrue(b.getProbes()[3]);
	}

	@Test
	public void testMergeShorterData() {
		final ExecutionData a = new ExecutionData(5, "Example",
				new boolean[] { false, false, false, true });
		final ExecutionData b = new ExecutionData(5, "Example",
				new boolean[] { true, false });
		a.merge(b);

		// probes of a beyond the length of b are kept:
		assertEquals(4, a.getProbes().length);
		assertTrue(a.getProbes()[0]);
		assertFalse(a.getProbes()[1]);
		assertFalse(a.getProbes()[2]);
		assertTrue(a.getProbes()[3]);
	}

	@Test
	public void testMergeSubtractLongerData() {
		final ExecutionData a = new ExecutionData(5, "Example",
				new boolean[] { true, true });
		final ExecutionData b = new ExecutionData(5, "Example",
				new boolean[] { true, false, true });
		a.merge(b, false);

		// subtraction never extends the probe array:
		assertEquals(2, a.getProbes().length);
		assertFalse(a.getProbes()[0]);
		assertTrue(a.getProbes()[1]);
	}

	@Test
	public void testMergeDifferentName() {
		final ExecutionData a = new ExecutionData(5, "Example",
				new boolean[] { true });
		final ExecutionData b = new ExecutionData(5, "Other",
				new boolean[] { true });
		try {
			a.merge(b);
			fail("IllegalStateException expected");
		} catch (final IllegalStateException e) {
			// expected
		}
	}

	@Test
	public void testAssertCompatibility() {
		final ExecutionData a = new ExecutionData(5, "Example",
				new boolean[] { true });
		a.assertCompatibility(5, "Example", 1);
	}

	@Test
	public void testAssertCompatibilityNegative1() {
		final ExecutionData a = new ExecutionData(5, "Example",
				new boolean[] { true });
		try {
			a.assertCompatibility(55, "Example", 1);
			fail("IllegalStateException expected");
		} catch (final IllegalStateException e) {
			// expected
		}
	}

	@Test
	public void testAssertCompatibilityNegative2() {
		final ExecutionData a = new ExecutionData(5, "Example",
				new boolean[] { true });
		try {
			a.assertCompatibility(5, "Exxxample", 1);
			fail("IllegalStateException expected");
		} catch (final IllegalStateException e) {
			// expected
		}
	}

	@Test
	public void testAssertCompatibilityNegative3() {
		final ExecutionData a = new ExecutionData(5, "Example",
				new boolean[] { true });
		try {
			a.assertCompatibility(5, "Example", 3);
			fail("IllegalStateException expected");
		} catch (final IllegalStateException e) {
			// expected
		}
	}

	@Test
	public void testToString() {
		final ExecutionData a = new ExecutionData(Long.MAX_VALUE, "Example",
				new boolean[] { true });
		assertEquals("ExecutionData[name=Example, id=7fffffffffffffff]",
				a.toString());
	}

}
