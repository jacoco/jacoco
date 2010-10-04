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
package org.jacoco.core.analysis;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

import org.junit.Test;

/**
 * Unit tests for {@link CounterImpl}.
 * 
 * @author Marc R. Hoffmann
 * @version $qualified.bundle.version$
 */
public class CounterImplTest {

	@Test
	public void testGetInstance1() {
		ICounter c = CounterImpl.getInstance(0, 0);
		assertEquals(0, c.getTotalCount(), 0.0);
		assertEquals(0, c.getCoveredCount(), 0.0);
	}

	@Test
	public void testGetInstance2() {
		ICounter c = CounterImpl.getInstance(33, 15);
		assertEquals(33, c.getTotalCount(), 0.0);
		assertEquals(15, c.getCoveredCount(), 0.0);
	}

	@Test
	public void testGetInstance3() {
		ICounter c = CounterImpl.getInstance(17, true);
		assertEquals(17, c.getTotalCount(), 0.0);
		assertEquals(17, c.getCoveredCount(), 0.0);
	}

	@Test
	public void testGetInstance4() {
		ICounter c = CounterImpl.getInstance(17, false);
		assertEquals(17, c.getTotalCount(), 0.0);
		assertEquals(0, c.getCoveredCount(), 0.0);
	}

	@Test
	public void testGetInstance5() {
		ICounter c = CounterImpl.getInstance(true);
		assertEquals(1, c.getTotalCount(), 0.0);
		assertEquals(1, c.getCoveredCount(), 0.0);
	}

	@Test
	public void testGetInstance6() {
		ICounter c = CounterImpl.getInstance(false);
		assertEquals(1, c.getTotalCount(), 0.0);
		assertEquals(0, c.getCoveredCount(), 0.0);
	}

	@Test
	public void testGetInstance7() {
		ICounter c = CounterImpl.getInstance(15, 12);
		ICounter copy = CounterImpl.getInstance(c);
		assertEquals(15, copy.getTotalCount(), 0.0);
		assertEquals(12, copy.getCoveredCount(), 0.0);
	}

	@Test
	public void testFixInstance() {
		ICounter c1 = CounterImpl.getInstance(30, 30);
		ICounter c2 = CounterImpl.getInstance(30, 30);
		assertSame(c1, c2);
	}

	@Test
	public void testVarInstance() {
		ICounter c1 = CounterImpl.getInstance(31, 30);
		ICounter c2 = CounterImpl.getInstance(31, 30);
		assertNotSame(c1, c2);
	}

	@Test
	public void testIncrement1() {
		CounterImpl c = CounterImpl.getInstance(1, 1);
		c = c.increment(CounterImpl.getInstance(2, 1));
		assertEquals(3, c.getTotalCount(), 0.0);
		assertEquals(2, c.getCoveredCount(), 0.0);
	}

	@Test
	public void testIncrement2() {
		CounterImpl c = CounterImpl.getInstance(31, 5);
		c = c.increment(CounterImpl.getInstance(7, 3));
		assertEquals(38, c.getTotalCount(), 0.0);
		assertEquals(8, c.getCoveredCount(), 0.0);
	}

}
