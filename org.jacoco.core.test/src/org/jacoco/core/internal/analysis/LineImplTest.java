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
import static org.junit.Assert.assertFalse;

import org.jacoco.core.analysis.ICounter;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link LineImplTest}.
 */
public class LineImplTest {

	private LineImpl line;

	@Before
	public void setup() {
		line = LineImpl.EMPTY;
	}

	@Test
	public void testEMPTY() {
		assertEquals(CounterImpl.COUNTER_0_0, line.getInstructionCounter());
		assertEquals(CounterImpl.COUNTER_0_0, line.getBranchCounter());
		assertEquals(ICounter.EMPTY, line.getStatus());
	}

	@Test
	public void testIncrement1() {
		line = line.increment(CounterImpl.getInstance(1, 2, 4),
				CounterImpl.getInstance(3, 4, 6));
		assertEquals(CounterImpl.getInstance(1, 2, 4),
				line.getInstructionCounter());
		assertEquals(CounterImpl.getInstance(3, 4, 6), line.getBranchCounter());
	}

	@Test
	public void testIncrement2() {
		line = line.increment(CounterImpl.getInstance(1, 2, 2),
				CounterImpl.getInstance(3, 4000, 4001));
		assertEquals(CounterImpl.getInstance(1, 2, 2),
				line.getInstructionCounter());
		assertEquals(CounterImpl.getInstance(3, 4000, 4001),
				line.getBranchCounter());
	}

	@Test
	public void testIncrement3() {
		line = line.increment(CounterImpl.getInstance(1, 2, 2),
				CounterImpl.getInstance(3000, 4000, 8000));
		assertEquals(CounterImpl.getInstance(1, 2, 2),
				line.getInstructionCounter());
		assertEquals(CounterImpl.getInstance(3000, 4000, 8000),
				line.getBranchCounter());
	}

	@Test
	public void testIncrement4() {
		line = line.increment(CounterImpl.getInstance(1, 2000, 6000),
				CounterImpl.getInstance(3000, 4000, 12000));
		assertEquals(CounterImpl.getInstance(1, 2000, 6000),
				line.getInstructionCounter());
		assertEquals(CounterImpl.getInstance(3000, 4000, 12000),
				line.getBranchCounter());
	}

	@Test
	public void testIncrement5() {
		line = line.increment(CounterImpl.getInstance(1000, 2000, 4000),
				CounterImpl.getInstance(3000, 4000, 8000));
		assertEquals(CounterImpl.getInstance(1000, 2000, 4000),
				line.getInstructionCounter());
		assertEquals(CounterImpl.getInstance(3000, 4000, 8000),
				line.getBranchCounter());
	}

	@Test
	public void testGetStatus1() {
		line = line.increment(CounterImpl.getInstance(1, 0, 0),
				CounterImpl.getInstance(0, 0, 0));
		assertEquals(ICounter.NOT_COVERED, line.getStatus());
	}

	@Test
	public void testGetStatus2() {
		line = line.increment(CounterImpl.getInstance(0, 0, 0),
				CounterImpl.getInstance(1, 0, 0));
		assertEquals(ICounter.NOT_COVERED, line.getStatus());
	}

	@Test
	public void testGetStatus3() {
		line = line.increment(CounterImpl.getInstance(0, 1, 3),
				CounterImpl.getInstance(0, 0, 0));
		assertEquals(ICounter.FULLY_COVERED, line.getStatus());
	}

	@Test
	public void testGetStatus4() {
		line = line.increment(CounterImpl.getInstance(0, 0, 0),
				CounterImpl.getInstance(0, 1, 3));
		assertEquals(ICounter.FULLY_COVERED, line.getStatus());
	}

	@Test
	public void testGetStatus5() {
		line = line.increment(CounterImpl.getInstance(1, 1, 3),
				CounterImpl.getInstance(0, 0, 0));
		assertEquals(ICounter.PARTLY_COVERED, line.getStatus());
	}

	@Test
	public void testGetStatus6() {
		line = line.increment(CounterImpl.getInstance(0, 1, 3),
				CounterImpl.getInstance(1, 1, 3));
		assertEquals(ICounter.PARTLY_COVERED, line.getStatus());
	}

	@Test
	public void testHashCode() {
		line = line.increment(CounterImpl.getInstance(111, 222, 222),
				CounterImpl.getInstance(333, 444, 444));
		LineImpl line2 = LineImpl.EMPTY;
		line2 = line2.increment(CounterImpl.getInstance(111, 222, 222),
				CounterImpl.getInstance(333, 444, 444));
		assertEquals(line.hashCode(), line2.hashCode());
	}

	@Test
	public void testEquals1() {
		line = line.increment(CounterImpl.getInstance(111, 222, 222),
				CounterImpl.getInstance(333, 444, 444));
		LineImpl line2 = LineImpl.EMPTY;
		line2 = line2.increment(CounterImpl.getInstance(111, 222, 222),
				CounterImpl.getInstance(333, 444, 444));
		assertEquals(line, line2);
	}

	@Test
	public void testEquals2() {
		line = line.increment(CounterImpl.getInstance(111, 222, 333),
				CounterImpl.getInstance(333, 444, 555));
		assertFalse(line.equals(new Object()));
	}

	@Test
	public void testEquals3a() {
		line = line.increment(CounterImpl.getInstance(111, 222, 333),
				CounterImpl.getInstance(333, 444, 555));
		LineImpl line2 = LineImpl.EMPTY;
		line2 = line2.increment(CounterImpl.getInstance(111, 2220, 333),
				CounterImpl.getInstance(333, 444, 555));
		assertFalse(line.equals(line2));
	}

	@Test
	public void testEquals3b() {
		line = line.increment(CounterImpl.getInstance(111, 222, 333),
				CounterImpl.getInstance(333, 444, 555));
		LineImpl line2 = LineImpl.EMPTY;
		line2 = line2.increment(CounterImpl.getInstance(1110, 222, 333),
				CounterImpl.getInstance(333, 444, 555));
		assertFalse(line.equals(line2));
	}

	@Test
	public void testEquals3c() {
		line = line.increment(CounterImpl.getInstance(111, 222, 333),
				CounterImpl.getInstance(333, 444, 555));
		LineImpl line2 = LineImpl.EMPTY;
		line2 = line2.increment(CounterImpl.getInstance(111, 222, 3330),
				CounterImpl.getInstance(333, 444, 555));
		assertFalse(line.equals(line2));
	}

	@Test
	public void testEquals4a() {
		line = line.increment(CounterImpl.getInstance(111, 222, 222),
				CounterImpl.getInstance(333, 4440, 555));
		LineImpl line2 = LineImpl.EMPTY;
		line2 = line2.increment(CounterImpl.getInstance(111, 222, 222),
				CounterImpl.getInstance(333, 444, 555));
		assertFalse(line.equals(line2));
	}

	@Test
	public void testEquals4b() {
		line = line.increment(CounterImpl.getInstance(111, 222, 222),
				CounterImpl.getInstance(3330, 444, 555));
		LineImpl line2 = LineImpl.EMPTY;
		line2 = line2.increment(CounterImpl.getInstance(111, 222, 222),
				CounterImpl.getInstance(333, 444, 555));
		assertFalse(line.equals(line2));
	}

	@Test
	public void testEquals4c() {
		line = line.increment(CounterImpl.getInstance(111, 222, 222),
				CounterImpl.getInstance(333, 444, 5550));
		LineImpl line2 = LineImpl.EMPTY;
		line2 = line2.increment(CounterImpl.getInstance(111, 222, 222),
				CounterImpl.getInstance(333, 444, 555));
		assertFalse(line.equals(line2));
	}

}
