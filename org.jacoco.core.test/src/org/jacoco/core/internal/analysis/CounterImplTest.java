/*******************************************************************************
 * Copyright (c) 2009, 2023 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.internal.analysis;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

import org.jacoco.core.analysis.ICounter;
import org.jacoco.core.analysis.ICounter.CounterValue;
import org.junit.Test;

/**
 * Unit tests for {@link CounterImpl}.
 */
public class CounterImplTest {

	@Test
	public void testGetInstance1() {
		ICounter c = CounterImpl.getInstance(0, 0);
		assertEquals(0, c.getTotalCount());
		assertEquals(0.0, c.getValue(CounterValue.TOTALCOUNT), 0.0);
		assertEquals(0, c.getMissedCount());
		assertEquals(0.0, c.getValue(CounterValue.MISSEDCOUNT), 0.0);
		assertEquals(0, c.getCoveredCount());
		assertEquals(0.0, c.getValue(CounterValue.COVEREDCOUNT), 0.0);
	}

	@Test
	public void testGetInstance2() {
		ICounter c = CounterImpl.getInstance(33, 15);
		assertEquals(48, c.getTotalCount());
		assertEquals(48.0, c.getValue(CounterValue.TOTALCOUNT), 0.0);
		assertEquals(33, c.getMissedCount());
		assertEquals(33.0, c.getValue(CounterValue.MISSEDCOUNT), 0.0);
		assertEquals(15, c.getCoveredCount());
		assertEquals(15.0, c.getValue(CounterValue.COVEREDCOUNT), 0.0);
	}

	@Test
	public void testGetInstance3() {
		ICounter c = CounterImpl.getInstance(15, 12);
		ICounter copy = CounterImpl.getInstance(c);
		assertEquals(27, copy.getTotalCount());
		assertEquals(27.0, c.getValue(CounterValue.TOTALCOUNT), 0.0);
		assertEquals(15, copy.getMissedCount());
		assertEquals(15.0, c.getValue(CounterValue.MISSEDCOUNT), 0.0);
		assertEquals(12, copy.getCoveredCount());
		assertEquals(12.0, c.getValue(CounterValue.COVEREDCOUNT), 0.0);
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
		assertEquals(3, c.getMissedCount());
		assertEquals(2, c.getCoveredCount());
	}

	@Test
	public void testIncrement2() {
		CounterImpl c = CounterImpl.getInstance(31, 5);
		c = c.increment(CounterImpl.getInstance(7, 3));
		assertEquals(38, c.getMissedCount());
		assertEquals(8, c.getCoveredCount());
	}

	@Test
	public void testGetCoveredRatio1() {
		ICounter c = CounterImpl.getInstance(30, 10);
		assertEquals(0.25, c.getCoveredRatio(), 0.0);
		assertEquals(0.25, c.getValue(CounterValue.COVEREDRATIO), 0.0);
	}

	@Test
	public void testGetCoveredRatio2() {
		ICounter c = CounterImpl.getInstance(20, 0);
		assertEquals(0.0, c.getCoveredRatio(), 0.0);
		assertEquals(0.0, c.getValue(CounterValue.COVEREDRATIO), 0.0);
	}

	@Test
	public void testGetCoveredRatio3() {
		ICounter c = CounterImpl.getInstance(0, 0);
		assertEquals(Double.NaN, c.getCoveredRatio(), 0.0);
		assertEquals(Double.NaN, c.getValue(CounterValue.COVEREDRATIO), 0.0);
	}

	@Test
	public void testGetMissedRatio1() {
		ICounter c = CounterImpl.getInstance(10, 30);
		assertEquals(0.25, c.getMissedRatio(), 0.0);
		assertEquals(0.25, c.getValue(CounterValue.MISSEDRATIO), 0.0);
	}

	@Test
	public void testGetMissedRatio2() {
		ICounter c = CounterImpl.getInstance(0, 20);
		assertEquals(0.0, c.getMissedRatio(), 0.0);
		assertEquals(0.0, c.getValue(CounterValue.MISSEDRATIO), 0.0);
	}

	@Test
	public void testGetMissedRatio3() {
		ICounter c = CounterImpl.getInstance(0, 0);
		assertEquals(Double.NaN, c.getMissedRatio(), 0.0);
		assertEquals(Double.NaN, c.getValue(CounterValue.MISSEDRATIO), 0.0);
	}

	@Test
	public void testGetMissedStatus1() {
		ICounter c = CounterImpl.getInstance(0, 0);
		assertEquals(ICounter.EMPTY, c.getStatus());
	}

	@Test
	public void testGetMissedStatus2() {
		ICounter c = CounterImpl.getInstance(5, 0);
		assertEquals(ICounter.NOT_COVERED, c.getStatus());
	}

	@Test
	public void testGetMissedStatus3() {
		ICounter c = CounterImpl.getInstance(0, 5);
		assertEquals(ICounter.FULLY_COVERED, c.getStatus());
	}

	@Test
	public void testGetMissedStatus4() {
		ICounter c = CounterImpl.getInstance(2, 3);
		assertEquals(ICounter.PARTLY_COVERED, c.getStatus());
	}

	@Test
	public void testEquals1() {
		ICounter c1 = CounterImpl.getInstance(300, 123);
		ICounter c2 = CounterImpl.getInstance(300, 123);
		assertEquals(c1, c2);
	}

	@Test
	public void testEquals2() {
		ICounter c1 = CounterImpl.getInstance(300, 123);
		ICounter c2 = CounterImpl.getInstance(400, 123);
		assertFalse(c1.equals(c2));
	}

	@Test
	public void testEquals3() {
		ICounter c1 = CounterImpl.getInstance(300, 123);
		ICounter c2 = CounterImpl.getInstance(300, 124);
		assertFalse(c1.equals(c2));
	}

	@Test
	public void testEquals4() {
		ICounter c = CounterImpl.getInstance(300, 123);
		assertFalse(c.equals(new Integer(123)));
	}

	@Test
	public void testHashCode1() {
		ICounter c1 = CounterImpl.getInstance(300, 123);
		ICounter c2 = CounterImpl.getInstance(300, 123);
		assertEquals(c1.hashCode(), c2.hashCode());
	}

	@Test
	public void testHashCode2() {
		ICounter c1 = CounterImpl.getInstance(300, 123);
		ICounter c2 = CounterImpl.getInstance(400, 123);
		assertFalse(c1.hashCode() == c2.hashCode());
	}

	@Test
	public void testHashCode3() {
		ICounter c1 = CounterImpl.getInstance(300, 123);
		ICounter c2 = CounterImpl.getInstance(300, 124);
		assertFalse(c1.hashCode() == c2.hashCode());
	}

	@Test
	public void testToString() {
		ICounter c = CounterImpl.getInstance(300, 123);
		assertEquals("Counter[300/123]", c.toString());
	}

}
