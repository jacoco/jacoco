/*******************************************************************************
 * Copyright (c) 2009, 2016 Mountainminds GmbH & Co. KG and Contributors
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
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import org.jacoco.core.internal.instr.IProbeArray;
import org.jacoco.core.internal.instr.ProbeArrayService;
import org.jacoco.core.internal.instr.ProbeBooleanArray;
import org.jacoco.core.internal.instr.ProbeIntArray;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Unit tests for {@link ExecutionData}.
 */
public class ExecutionDataTest {
	public static class ExecutionExistsDataTest extends ExecutionExistsTestBase {
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

	public static class ExecutionCountDataTest extends ExecutionExistsTestBase {
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

	public static class ExecutionParallelDataTest extends
			ExecutionExistsTestBase {
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

	public static abstract class ExecutionExistsTestBase {
		@Test
		public void testCreateEmpty() {
			final ExecutionData e = new ExecutionData(5, "Example", 3);
			assertEquals(5, e.getId());
			assertEquals("Example", e.getName());
			assertEquals(3, e.getProbes().length());
			assertFalse(e.getProbes().isProbeCovered(0));
			assertFalse(e.getProbes().isProbeCovered(1));
			assertFalse(e.getProbes().isProbeCovered(2));
		}

		@Test
		public void testGetters() {
			final IProbeArray<?> data = createProbes(new int[0]);
			final ExecutionData e = new ExecutionData(5, "Example", data);
			assertEquals(5, e.getId());
			assertEquals("Example", e.getName());
			assertEquals(data, e.getProbes());
		}

		@Test
		public void testReset() {
			final ExecutionData e = new ExecutionData(5, "Example",
					createProbes(new int[] { 1, 0, 1 }));
			e.reset();
			assertFalse(e.getProbes().isProbeCovered(0));
			assertFalse(e.getProbes().isProbeCovered(1));
			assertFalse(e.getProbes().isProbeCovered(2));
		}

		@Test
		public void testMerge() {
			final ExecutionData a = new ExecutionData(5, "Example",
					createProbes(new int[] { 0, 1, 0, 1 }));
			final ExecutionData b = new ExecutionData(5, "Example",
					createProbes(new int[] { 0, 0, 1, 1 }));
			a.merge(b);

			// b is merged into a:
			assertFalse(a.getProbes().isProbeCovered(0));
			assertTrue(a.getProbes().isProbeCovered(1));
			assertTrue(a.getProbes().isProbeCovered(2));
			assertTrue(a.getProbes().isProbeCovered(3));

			// b must not be modified:
			assertFalse(b.getProbes().isProbeCovered(0));
			assertFalse(b.getProbes().isProbeCovered(1));
			assertTrue(b.getProbes().isProbeCovered(2));
			assertTrue(b.getProbes().isProbeCovered(3));
		}

		@Test
		public void testMergeSubtract() {
			final ExecutionData a = new ExecutionData(5, "Example",
					createProbes(new int[] { 0, 1, 0, 1 }));
			final ExecutionData b = new ExecutionData(5, "Example",
					createProbes(new int[] { 0, 0, 1, 1 }));
			a.merge(b, false);

			// b is subtracted from a:
			assertFalse(a.getProbes().isProbeCovered(0));
			assertTrue(a.getProbes().isProbeCovered(1));
			assertFalse(a.getProbes().isProbeCovered(2));
			assertFalse(a.getProbes().isProbeCovered(3));

			// b must not be modified:
			assertFalse(b.getProbes().isProbeCovered(0));
			assertFalse(b.getProbes().isProbeCovered(1));
			assertTrue(b.getProbes().isProbeCovered(2));
			assertTrue(b.getProbes().isProbeCovered(3));
		}

		@Test
		public void testAssertCompatibility() {
			final ExecutionData a = new ExecutionData(5, "Example",
					createProbes(new int[] { 1 }));
			a.assertCompatibility(5, "Example", 1);
		}

		@Test(expected = IllegalStateException.class)
		public void testAssertCompatibilityNegative1() {
			final ExecutionData a = new ExecutionData(5, "Example",
					createProbes(new int[] { 1 }));
			a.assertCompatibility(55, "Example", 1);
		}

		@Test(expected = IllegalStateException.class)
		public void testAssertCompatibilityNegative2() {
			final ExecutionData a = new ExecutionData(5, "Example",
					createProbes(new int[] { 1 }));
			a.assertCompatibility(5, "Exxxample", 1);
		}

		@Test(expected = IllegalStateException.class)
		public void testAssertCompatibilityNegative3() {
			final ExecutionData a = new ExecutionData(5, "Example",
					createProbes(new int[] { 1 }));
			a.assertCompatibility(5, "Example", 3);
		}

		@Test(expected = IllegalStateException.class)
		public void testAssertCompatibilityNegative4() {
			final ExecutionData a = new ExecutionData(5, "Example",
					createProbes(new int[] { 1 }));
			a.assertCompatibility(55, "Example", createProbes(new int[] { 1 }));
		}

		@Test(expected = IllegalStateException.class)
		public void testAssertCompatibilityNegative5() {
			final ExecutionData a = new ExecutionData(5, "Example",
					createProbes(new int[] { 1 }));
			a.assertCompatibility(5, "Exxxample", createProbes(new int[] { 1 }));
		}

		@Test(expected = IllegalArgumentException.class)
		public void testAssertCompatibilityNegative6() {
			final ExecutionData a = new ExecutionData(5, "Example",
					createProbes(new int[] { 1 }));
			IProbeArray<?> probeArray;
			if (ProbeArrayService.getProbeMode() == ProbeMode.exists) {
				probeArray = new ProbeIntArray(2);
			} else {
				probeArray = new ProbeBooleanArray(2);
			}
			a.assertCompatibility(5, "Example", probeArray);
		}

		@Test
		public void testToString() {
			final ExecutionData a = new ExecutionData(Long.MAX_VALUE,
					"Example", createProbes(new int[] { 1 }));
			assertEquals("ExecutionData[name=Example, id=7fffffffffffffff]",
					a.toString());
		}

		@Test
		public void testDeepCopy() {
			final ExecutionData a = new ExecutionData(Long.MAX_VALUE,
					"Example", createProbes(new int[] { 1 }));
			final ExecutionData result = a.deepCopy();
			assertEquals(a.getId(), result.getId());
			assertEquals(a.getName(), result.getName());
			assertNotSame(a.getProbes(), result.getProbes());
			assertEquals(a.getProbes(), result.getProbes());
		}

		private IProbeArray<?> createProbes(int[] data) {
			final IProbeArray<?> probes = ProbeArrayService
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
