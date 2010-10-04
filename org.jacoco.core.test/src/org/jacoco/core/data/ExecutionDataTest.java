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
package org.jacoco.core.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Unit tests for {@link ExecutionData}.
 * 
 * @author Marc R. Hoffmann
 * @version $qualified.bundle.version$
 */
public class ExecutionDataTest {

	@Test
	public void testCreateEmpty() {
		final ExecutionData e = new ExecutionData(5, "Example", 3);
		assertEquals(5, e.getId());
		assertEquals("Example", e.getName());
		assertEquals(3, e.getData().length);
		assertFalse(e.getData()[0]);
		assertFalse(e.getData()[1]);
		assertFalse(e.getData()[2]);
	}

	@Test
	public void testGetters() {
		final boolean[] data = new boolean[0];
		final ExecutionData e = new ExecutionData(5, "Example", data);
		assertEquals(5, e.getId());
		assertEquals("Example", e.getName());
		assertSame(data, e.getData());
	}

	@Test
	public void testReset() {
		final ExecutionData e = new ExecutionData(5, "Example", new boolean[] {
				true, false, true });
		e.reset();
		assertFalse(e.getData()[0]);
		assertFalse(e.getData()[1]);
		assertFalse(e.getData()[2]);
	}

	@Test
	public void testMerge() {
		final ExecutionData a = new ExecutionData(5, "Example", new boolean[] {
				false, true, false, true });
		final ExecutionData b = new ExecutionData(5, "Example", new boolean[] {
				false, false, true, true });
		a.merge(b);

		// b is merged into a:
		assertFalse(a.getData()[0]);
		assertTrue(a.getData()[1]);
		assertTrue(a.getData()[2]);
		assertTrue(a.getData()[3]);

		// b must not be modified:
		assertFalse(b.getData()[0]);
		assertFalse(b.getData()[1]);
		assertTrue(b.getData()[2]);
		assertTrue(b.getData()[3]);
	}

	@Test
	public void testAssertCompatibility() {
		final ExecutionData a = new ExecutionData(5, "Example",
				new boolean[] { true });
		a.assertCompatibility(5, "Example", 1);
	}

	@Test(expected = IllegalStateException.class)
	public void testAssertCompatibilityNegative1() {
		final ExecutionData a = new ExecutionData(5, "Example",
				new boolean[] { true });
		a.assertCompatibility(55, "Example", 1);
	}

	@Test(expected = IllegalStateException.class)
	public void testAssertCompatibilityNegative2() {
		final ExecutionData a = new ExecutionData(5, "Example",
				new boolean[] { true });
		a.assertCompatibility(5, "Exxxample", 1);
	}

	@Test(expected = IllegalStateException.class)
	public void testAssertCompatibilityNegative3() {
		final ExecutionData a = new ExecutionData(5, "Example",
				new boolean[] { true });
		a.assertCompatibility(5, "Example", 3);
	}

	@Test
	public void testToString() {
		final ExecutionData a = new ExecutionData(Long.MAX_VALUE, "Example",
				new boolean[] { true });
		assertEquals("ExecutionData [name=Example, id=7fffffffffffffff]",
				a.toString());
	}

}
