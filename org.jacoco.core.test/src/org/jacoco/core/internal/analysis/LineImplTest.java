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
package org.jacoco.core.internal.analysis;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

import java.util.BitSet;

import org.jacoco.core.analysis.ICounter;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link LineImpl}.
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
		assertEquals(0, line.coveredBranches);
	}

	@Test
	public void increment_with_detailed_information_about_covered_branches() {
		final BitSet bs = new BitSet();
		// From empty to fix
		line = LineImpl.EMPTY;
		bs.set(0, true);
		bs.set(1, false);
		LineImpl firstIncrement = line.increment( //
				CounterImpl.getInstance(0, 1), // instructions
				CounterImpl.getInstance(1, 1), // branches
				bs);
		LineImpl secondIncrement = line.increment( // same as above
				CounterImpl.getInstance(0, 1), // instructions
				CounterImpl.getInstance(1, 1), // branches
				bs);
		// singleton
		assertSame(firstIncrement, secondIncrement);
		assertNotSame(line, secondIncrement);
		line = firstIncrement;
		assertEquals(CounterImpl.getInstance(0, 1), //
				line.getInstructionCounter());
		assertEquals(CounterImpl.getInstance(1, 1), //
				line.getBranchCounter());
		assertEquals(/* 0b01 */ 1, line.coveredBranches);

		// From fix to fix
		bs.set(0, false);
		bs.set(1, true);
		firstIncrement = line.increment( //
				CounterImpl.getInstance(0, 1), // instructions
				CounterImpl.getInstance(1, 1), // branches
				bs);
		secondIncrement = line.increment( // same as above
				CounterImpl.getInstance(0, 1), // instructions
				CounterImpl.getInstance(1, 1), // branches
				bs);
		// singleton
		assertSame(firstIncrement, secondIncrement);
		assertNotSame(line, secondIncrement);
		line = firstIncrement;
		assertEquals(CounterImpl.getInstance(0, 1 + 1), //
				line.getInstructionCounter());
		assertEquals(CounterImpl.getInstance(1 + 1, 1 + 1), //
				line.getBranchCounter());
		assertEquals(/* 0b1001 */ 9, line.coveredBranches);

		// From fix to var
		bs.set(0, true);
		bs.set(1, true);
		bs.set(2, false);
		firstIncrement = line.increment( //
				CounterImpl.getInstance(0, 17), // instructions
				CounterImpl.getInstance(1, 2), // branches
				bs);
		secondIncrement = line.increment( // same as above
				CounterImpl.getInstance(0, 17), // instructions
				CounterImpl.getInstance(1, 2), // branches
				bs);
		// not singleton
		assertNotSame(firstIncrement, secondIncrement);
		line = firstIncrement;
		assertEquals(CounterImpl.getInstance(0, 1 + 1 + 17),
				line.getInstructionCounter());
		assertEquals(CounterImpl.getInstance(1 + 1 + 1, 1 + 1 + 2),
				line.getBranchCounter());
		assertEquals(/* 0b0111001 */ 57, line.coveredBranches);

		// From var to var
		bs.set(0, false);
		bs.set(1, true);
		bs.set(2, true);
		secondIncrement = line.increment( //
				CounterImpl.getInstance(0, 1), // instructions
				CounterImpl.getInstance(1, 2), // branches
				bs);
		// mutable
		assertSame(firstIncrement, secondIncrement);
		assertEquals(CounterImpl.getInstance(0, 1 + 1 + 17 + 1),
				line.getInstructionCounter());
		assertEquals(CounterImpl.getInstance(1 + 1 + 1 + 1, 1 + 1 + 2 + 2),
				line.getBranchCounter());
		assertEquals(/* 0b1100111001 */ 825, line.coveredBranches);
	}

	@Test
	public void increment_without_detailed_information_about_covered_branches() {
		// From empty to fix
		line = LineImpl.EMPTY;
		LineImpl firstIncrement = line.increment( //
				CounterImpl.getInstance(0, 1), // instructions
				CounterImpl.getInstance(1, 1), // branches
				null);
		LineImpl secondIncrement = line.increment( // same as above
				CounterImpl.getInstance(0, 1), // instructions
				CounterImpl.getInstance(1, 1), // branches
				null);
		// singleton
		assertSame(firstIncrement, secondIncrement);
		assertNotSame(line, secondIncrement);
		line = secondIncrement;
		assertEquals(CounterImpl.getInstance(0, 1), //
				line.getInstructionCounter());
		assertEquals(CounterImpl.getInstance(1, 1), //
				line.getBranchCounter());
		assertEquals(/* 0b01 */ 1, line.coveredBranches);

		// From fix to fix
		firstIncrement = line.increment( //
				CounterImpl.getInstance(0, 1), // instructions
				CounterImpl.getInstance(1, 1), // branches
				null);
		secondIncrement = line.increment( // same as above
				CounterImpl.getInstance(0, 1), // instructions
				CounterImpl.getInstance(1, 1), // branches
				null);
		// singleton
		assertSame(firstIncrement, secondIncrement);
		assertNotSame(line, secondIncrement);
		line = secondIncrement;
		assertEquals(CounterImpl.getInstance(0, 1 + 1), //
				line.getInstructionCounter());
		assertEquals(CounterImpl.getInstance(1 + 1, 1 + 1), //
				line.getBranchCounter());
		assertEquals(/* 0b0011 */ 3, line.coveredBranches);

		// From fix to var
		firstIncrement = line.increment( //
				CounterImpl.getInstance(0, 17), // instructions
				CounterImpl.getInstance(1, 2), // branches
				null);
		secondIncrement = line.increment( // same as above
				CounterImpl.getInstance(0, 17), // instructions
				CounterImpl.getInstance(1, 2), // branches
				null);
		// not singleton
		assertNotSame(firstIncrement, secondIncrement);
		line = firstIncrement;
		assertEquals(CounterImpl.getInstance(0, 1 + 1 + 17),
				line.getInstructionCounter());
		assertEquals(CounterImpl.getInstance(1 + 1 + 1, 1 + 1 + 2),
				line.getBranchCounter());
		assertEquals(/* 0b1111 */ 15, line.coveredBranches);

		// From var to var
		secondIncrement = line.increment( //
				CounterImpl.getInstance(0, 1), // instructions
				CounterImpl.getInstance(1, 2), // branches
				null);
		// mutable
		assertSame(firstIncrement, secondIncrement);
		assertEquals(CounterImpl.getInstance(0, 1 + 1 + 17 + 1),
				line.getInstructionCounter());
		assertEquals(CounterImpl.getInstance(1 + 1 + 1 + 1, 1 + 1 + 2 + 2),
				line.getBranchCounter());
		assertEquals(/* 0b111111 */ 63, line.coveredBranches);
	}

	@Test
	public void testIncrement1() {
		line = line.increment(CounterImpl.getInstance(1, 2),
				CounterImpl.getInstance(3, 4));
		assertEquals(CounterImpl.getInstance(1, 2),
				line.getInstructionCounter());
		assertEquals(CounterImpl.getInstance(3, 4), line.getBranchCounter());
	}

	@Test
	public void testIncrement2() {
		line = line.increment(CounterImpl.getInstance(1, 2),
				CounterImpl.getInstance(3, 4000));
		assertEquals(CounterImpl.getInstance(1, 2),
				line.getInstructionCounter());
		assertEquals(CounterImpl.getInstance(3, 4000), line.getBranchCounter());
	}

	@Test
	public void testIncrement3() {
		line = line.increment(CounterImpl.getInstance(1, 2),
				CounterImpl.getInstance(3000, 4000));
		assertEquals(CounterImpl.getInstance(1, 2),
				line.getInstructionCounter());
		assertEquals(CounterImpl.getInstance(3000, 4000),
				line.getBranchCounter());
	}

	@Test
	public void testIncrement4() {
		line = line.increment(CounterImpl.getInstance(1, 2000),
				CounterImpl.getInstance(3000, 4000));
		assertEquals(CounterImpl.getInstance(1, 2000),
				line.getInstructionCounter());
		assertEquals(CounterImpl.getInstance(3000, 4000),
				line.getBranchCounter());
	}

	@Test
	public void testIncrement5() {
		line = line.increment(CounterImpl.getInstance(1000, 2000),
				CounterImpl.getInstance(3000, 4000));
		assertEquals(CounterImpl.getInstance(1000, 2000),
				line.getInstructionCounter());
		assertEquals(CounterImpl.getInstance(3000, 4000),
				line.getBranchCounter());
	}

	@Test
	public void testGetStatus1() {
		line = line.increment(CounterImpl.getInstance(1, 0),
				CounterImpl.getInstance(0, 0));
		assertEquals(ICounter.NOT_COVERED, line.getStatus());
	}

	@Test
	public void testGetStatus2() {
		line = line.increment(CounterImpl.getInstance(0, 0),
				CounterImpl.getInstance(1, 0));
		assertEquals(ICounter.NOT_COVERED, line.getStatus());
	}

	@Test
	public void testGetStatus3() {
		line = line.increment(CounterImpl.getInstance(0, 1),
				CounterImpl.getInstance(0, 0));
		assertEquals(ICounter.FULLY_COVERED, line.getStatus());
	}

	@Test
	public void testGetStatus4() {
		line = line.increment(CounterImpl.getInstance(0, 0),
				CounterImpl.getInstance(0, 1));
		assertEquals(ICounter.FULLY_COVERED, line.getStatus());
	}

	@Test
	public void testGetStatus5() {
		line = line.increment(CounterImpl.getInstance(1, 1),
				CounterImpl.getInstance(0, 0));
		assertEquals(ICounter.PARTLY_COVERED, line.getStatus());
	}

	@Test
	public void testGetStatus6() {
		line = line.increment(CounterImpl.getInstance(0, 1),
				CounterImpl.getInstance(1, 1));
		assertEquals(ICounter.PARTLY_COVERED, line.getStatus());
	}

	@Test
	public void testHashCode() {
		line = line.increment(CounterImpl.getInstance(111, 222),
				CounterImpl.getInstance(333, 444));
		LineImpl line2 = LineImpl.EMPTY;
		line2 = line2.increment(CounterImpl.getInstance(111, 222),
				CounterImpl.getInstance(333, 444));
		assertEquals(line.hashCode(), line2.hashCode());
	}

	@Test
	public void testEquals1() {
		line = line.increment(CounterImpl.getInstance(111, 222),
				CounterImpl.getInstance(333, 444));
		LineImpl line2 = LineImpl.EMPTY;
		line2 = line2.increment(CounterImpl.getInstance(111, 222),
				CounterImpl.getInstance(333, 444));
		assertEquals(line, line2);
	}

	@Test
	public void testEquals2() {
		line = line.increment(CounterImpl.getInstance(111, 222),
				CounterImpl.getInstance(333, 444));
		assertFalse(line.equals(new Object()));
	}

	@Test
	public void testEquals3() {
		line = line.increment(CounterImpl.getInstance(111, 222),
				CounterImpl.getInstance(333, 444));
		LineImpl line2 = LineImpl.EMPTY;
		line2 = line2.increment(CounterImpl.getInstance(111, 2220),
				CounterImpl.getInstance(333, 444));
		assertFalse(line.equals(line2));
	}

	@Test
	public void testEquals4() {
		line = line.increment(CounterImpl.getInstance(111, 222),
				CounterImpl.getInstance(333, 4440));
		LineImpl line2 = LineImpl.EMPTY;
		line2 = line2.increment(CounterImpl.getInstance(111, 222),
				CounterImpl.getInstance(333, 444));
		assertFalse(line.equals(line2));
	}

}
