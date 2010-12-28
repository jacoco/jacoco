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

import static org.jacoco.core.analysis.ILines.FULLY_COVERED;
import static org.jacoco.core.analysis.ILines.NOT_COVERED;
import static org.jacoco.core.analysis.ILines.NO_CODE;
import static org.jacoco.core.analysis.ILines.PARTLY_COVERED;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Unit tests for {@link LinesImpl}.
 * 
 * @author Marc R. Hoffmann
 * @version $qualified.bundle.version$
 */
public class LinesImplTest {

	@Test
	public void testEmpty() {
		final ILines c = new LinesImpl();
		assertEquals(0, c.getTotalCount());
		assertEquals(0, c.getCoveredCount());
		assertEquals(-1, c.getFirstLine());
		assertEquals(-1, c.getLastLine());
		assertEquals(NO_CODE, c.getStatus(5));
		assertEquals(0, c.getTotalBranches(5));
		assertEquals(0, c.getMissedBranches(5));
		assertEquals(0, c.getCoveredBranches(5));
	}

	@Test
	public void testInitMissed() {
		final LinesImpl c = createNotCovered(5, 7, 10);
		assertEquals(3, c.getTotalCount());
		assertEquals(0, c.getCoveredCount());
		assertEquals(5, c.getFirstLine());
		assertEquals(10, c.getLastLine());
		assertEquals(NO_CODE, c.getStatus(4));
		assertEquals(NOT_COVERED, c.getStatus(5));
		assertEquals(NO_CODE, c.getStatus(6));
		assertEquals(NOT_COVERED, c.getStatus(7));
		assertEquals(NO_CODE, c.getStatus(8));
		assertEquals(NO_CODE, c.getStatus(9));
		assertEquals(NOT_COVERED, c.getStatus(10));
		assertEquals(NO_CODE, c.getStatus(11));
		assertEquals(0, c.getTotalBranches(0));
		assertEquals(0, c.getMissedBranches(0));
		assertEquals(0, c.getCoveredBranches(0));
	}

	@Test
	public void testInitCovered() {
		final LinesImpl c = createFullyCovered(5, 7, 10);
		assertEquals(3, c.getTotalCount());
		assertEquals(3, c.getCoveredCount());
		assertEquals(5, c.getFirstLine());
		assertEquals(10, c.getLastLine());
		assertEquals(NO_CODE, c.getStatus(4));
		assertEquals(FULLY_COVERED, c.getStatus(5));
		assertEquals(NO_CODE, c.getStatus(6));
		assertEquals(FULLY_COVERED, c.getStatus(7));
		assertEquals(NO_CODE, c.getStatus(8));
		assertEquals(NO_CODE, c.getStatus(9));
		assertEquals(FULLY_COVERED, c.getStatus(10));
		assertEquals(NO_CODE, c.getStatus(11));
		assertEquals(0, c.getTotalBranches(0));
		assertEquals(0, c.getMissedBranches(0));
		assertEquals(0, c.getCoveredBranches(0));
	}

	@Test
	public void testIncrement1() {
		// 1: N + N = N
		// 2: N + . = N
		// 3: . + . = .
		// 4: . + N = N
		// 5: N + N = N
		// ============
		// 4 total, 0 covered
		final LinesImpl c = createNotCovered(1, 2, 5);
		c.increment(createNotCovered(1, 4, 5));
		assertEquals(4, c.getTotalCount());
		assertEquals(0, c.getCoveredCount());
		assertEquals(1, c.getFirstLine());
		assertEquals(5, c.getLastLine());
		assertEquals(NOT_COVERED, c.getStatus(1));
		assertEquals(NOT_COVERED, c.getStatus(2));
		assertEquals(NO_CODE, c.getStatus(3));
		assertEquals(NOT_COVERED, c.getStatus(4));
		assertEquals(NOT_COVERED, c.getStatus(5));
		assertEquals(0, c.getTotalBranches(0));
		assertEquals(0, c.getMissedBranches(0));
		assertEquals(0, c.getCoveredBranches(0));
	}

	@Test
	public void testIncrement2() {
		// 1: F + F = F
		// 2: F + . = F
		// 3: . + F = F
		// 4: F + F = F
		// ============
		// 4 total, 4 covered
		final LinesImpl c = createFullyCovered(1, 2, 4);
		c.increment(createFullyCovered(1, 3, 4));
		assertEquals(4, c.getTotalCount());
		assertEquals(4, c.getCoveredCount());
		assertEquals(1, c.getFirstLine());
		assertEquals(4, c.getLastLine());
		assertEquals(FULLY_COVERED, c.getStatus(1));
		assertEquals(FULLY_COVERED, c.getStatus(2));
		assertEquals(FULLY_COVERED, c.getStatus(3));
		assertEquals(FULLY_COVERED, c.getStatus(4));
		assertEquals(0, c.getTotalBranches(0));
		assertEquals(0, c.getMissedBranches(0));
		assertEquals(0, c.getCoveredBranches(0));
	}

	@Test
	public void testIncrement3() {
		// 1: F + N = P
		// ============
		// 1 total, 1 covered
		final LinesImpl c = createFullyCovered(1);
		c.increment(createNotCovered(1));
		assertEquals(1, c.getTotalCount());
		assertEquals(1, c.getCoveredCount());
		assertEquals(1, c.getFirstLine());
		assertEquals(1, c.getLastLine());
		assertEquals(PARTLY_COVERED, c.getStatus(1));
		assertEquals(0, c.getTotalBranches(0));
		assertEquals(0, c.getMissedBranches(0));
		assertEquals(0, c.getCoveredBranches(0));
	}

	@Test
	public void testIncrement4() {
		// 1: N + F = P
		// ============
		// 1 total, 1 covered
		final LinesImpl c = createNotCovered(1);
		c.increment(createFullyCovered(1));
		assertEquals(1, c.getTotalCount());
		assertEquals(1, c.getCoveredCount());
		assertEquals(1, c.getFirstLine());
		assertEquals(1, c.getLastLine());
		assertEquals(PARTLY_COVERED, c.getStatus(1));
		assertEquals(0, c.getTotalBranches(0));
		assertEquals(0, c.getMissedBranches(0));
		assertEquals(0, c.getCoveredBranches(0));
	}

	@Test
	public void testIncrement5() {
		// 1: P + P = P
		// 2: P + . = P
		// 3: . + . = .
		// 4: . + P = P
		// 5: P + P = P
		// ============
		// 4 total, 4 covered
		final LinesImpl c = createPartlyCovered(1, 2, 5);
		c.increment(createPartlyCovered(1, 4, 5));
		assertEquals(4, c.getTotalCount());
		assertEquals(4, c.getCoveredCount());
		assertEquals(1, c.getFirstLine());
		assertEquals(5, c.getLastLine());
		assertEquals(PARTLY_COVERED, c.getStatus(1));
		assertEquals(PARTLY_COVERED, c.getStatus(2));
		assertEquals(NO_CODE, c.getStatus(3));
		assertEquals(PARTLY_COVERED, c.getStatus(4));
		assertEquals(PARTLY_COVERED, c.getStatus(5));
		assertEquals(0, c.getTotalBranches(0));
		assertEquals(0, c.getMissedBranches(0));
		assertEquals(0, c.getCoveredBranches(0));
	}

	@Test
	public void testIncrement6() {
		// 1: P + N = P
		// ============
		// 1 total, 1 covered
		final LinesImpl c = createPartlyCovered(1);
		c.increment(createNotCovered(1));
		assertEquals(1, c.getTotalCount());
		assertEquals(1, c.getCoveredCount());
		assertEquals(1, c.getFirstLine());
		assertEquals(1, c.getLastLine());
		assertEquals(PARTLY_COVERED, c.getStatus(1));
		assertEquals(0, c.getTotalBranches(0));
		assertEquals(0, c.getMissedBranches(0));
		assertEquals(0, c.getCoveredBranches(0));
	}

	@Test
	public void testIncrement7() {
		// 1: N + P = P
		// ============
		// 1 total, 1 covered
		final LinesImpl c = createNotCovered(1);
		c.increment(createPartlyCovered(1));
		assertEquals(1, c.getTotalCount());
		assertEquals(1, c.getCoveredCount());
		assertEquals(1, c.getFirstLine());
		assertEquals(1, c.getLastLine());
		assertEquals(PARTLY_COVERED, c.getStatus(1));
		assertEquals(0, c.getTotalBranches(0));
		assertEquals(0, c.getMissedBranches(0));
		assertEquals(0, c.getCoveredBranches(0));
	}

	@Test
	public void testIncrement8() {
		// 1: P + F = P
		// ============
		// 1 total, 1 covered
		final LinesImpl c = createPartlyCovered(1);
		c.increment(createFullyCovered(1));
		assertEquals(1, c.getTotalCount());
		assertEquals(1, c.getCoveredCount());
		assertEquals(1, c.getFirstLine());
		assertEquals(1, c.getLastLine());
		assertEquals(PARTLY_COVERED, c.getStatus(1));
		assertEquals(0, c.getTotalBranches(0));
		assertEquals(0, c.getMissedBranches(0));
		assertEquals(0, c.getCoveredBranches(0));
	}

	@Test
	public void testIncrement9() {
		// 1: F + P = P
		// ============
		// 1 total, 1 covered
		final LinesImpl c = createFullyCovered(1);
		c.increment(createPartlyCovered(1));
		assertEquals(1, c.getTotalCount());
		assertEquals(1, c.getCoveredCount());
		assertEquals(1, c.getFirstLine());
		assertEquals(1, c.getLastLine());
		assertEquals(PARTLY_COVERED, c.getStatus(1));
		assertEquals(0, c.getTotalBranches(0));
		assertEquals(0, c.getMissedBranches(0));
		assertEquals(0, c.getCoveredBranches(0));
	}

	@Test
	public void testIncrementEmpty() {
		final LinesImpl c = createFullyCovered(1);
		c.increment(new LinesImpl());
		assertEquals(1, c.getTotalCount());
		assertEquals(1, c.getCoveredCount());
		assertEquals(1, c.getFirstLine());
		assertEquals(1, c.getLastLine());
		assertEquals(FULLY_COVERED, c.getStatus(1));
		assertEquals(0, c.getTotalBranches(0));
		assertEquals(0, c.getMissedBranches(0));
		assertEquals(0, c.getCoveredBranches(0));
	}

	@Test
	public void testIncreaseLeft() {
		// 1: . + F = F
		// 2: F + F = F
		// 3: F + F = F
		// ============
		// 3 total, 3 covered
		final LinesImpl c = createFullyCovered(2, 3);
		c.increment(createFullyCovered(1, 2, 3));
		assertEquals(3, c.getTotalCount());
		assertEquals(3, c.getCoveredCount());
		assertEquals(1, c.getFirstLine());
		assertEquals(3, c.getLastLine());
		assertEquals(FULLY_COVERED, c.getStatus(1));
		assertEquals(FULLY_COVERED, c.getStatus(2));
		assertEquals(FULLY_COVERED, c.getStatus(3));
		assertEquals(0, c.getTotalBranches(0));
		assertEquals(0, c.getMissedBranches(0));
		assertEquals(0, c.getCoveredBranches(0));
	}

	@Test
	public void testIncreaseRight() {
		// 1: F + F = F
		// 2: F + F = F
		// 3: . + F = F
		// ============
		// 3 total, 3 covered
		final LinesImpl c = createFullyCovered(1, 2);
		c.increment(createFullyCovered(1, 2, 3));
		assertEquals(3, c.getTotalCount());
		assertEquals(3, c.getCoveredCount());
		assertEquals(1, c.getFirstLine());
		assertEquals(3, c.getLastLine());
		assertEquals(FULLY_COVERED, c.getStatus(1));
		assertEquals(FULLY_COVERED, c.getStatus(2));
		assertEquals(FULLY_COVERED, c.getStatus(3));
		assertEquals(0, c.getTotalBranches(0));
		assertEquals(0, c.getMissedBranches(0));
		assertEquals(0, c.getCoveredBranches(0));
	}

	@Test
	public void testIncreaseBoth() {
		// 1: . + F = F
		// 2: F + F = F
		// 3: . + F = F
		// ============
		// 3 total, 3 covered
		final LinesImpl c = createFullyCovered(2);
		c.increment(createFullyCovered(1, 2, 3));
		assertEquals(3, c.getTotalCount());
		assertEquals(3, c.getCoveredCount());
		assertEquals(1, c.getFirstLine());
		assertEquals(3, c.getLastLine());
		assertEquals(FULLY_COVERED, c.getStatus(1));
		assertEquals(FULLY_COVERED, c.getStatus(2));
		assertEquals(FULLY_COVERED, c.getStatus(3));
		assertEquals(0, c.getTotalBranches(0));
		assertEquals(0, c.getMissedBranches(0));
		assertEquals(0, c.getCoveredBranches(0));
	}

	private LinesImpl createNotCovered(final int... lines) {
		final LinesImpl c = new LinesImpl();
		for (int l : lines) {
			c.incrementInsn(l, false);
		}
		return c;
	}

	private LinesImpl createFullyCovered(final int... lines) {
		final LinesImpl c = new LinesImpl();
		for (int l : lines) {
			c.incrementInsn(l, true);
		}
		return c;
	}

	private LinesImpl createPartlyCovered(final int... lines) {
		final LinesImpl c = new LinesImpl();
		for (int l : lines) {
			c.incrementInsn(l, false);
			c.incrementInsn(l, true);
		}
		return c;
	}

	@Test
	public void testIncrementBranches1() {
		final LinesImpl c = new LinesImpl();
		c.incrementBranches(5, 3, 5);
		assertEquals(5, c.getFirstLine());
		assertEquals(5, c.getLastLine());

		assertEquals(0, c.getTotalBranches(4));
		assertEquals(0, c.getCoveredBranches(4));
		assertEquals(0, c.getMissedBranches(4));

		assertEquals(8, c.getTotalBranches(5));
		assertEquals(3, c.getCoveredBranches(5));
		assertEquals(5, c.getMissedBranches(5));
		assertEquals(NO_CODE, c.getStatus(5));

		assertEquals(0, c.getTotalBranches(6));
		assertEquals(0, c.getCoveredBranches(6));
		assertEquals(0, c.getMissedBranches(6));
	}

	@Test
	public void testIncrementBranches2() {
		final LinesImpl c = new LinesImpl();
		c.incrementBranches(5, 3, 5);

		final LinesImpl c2 = new LinesImpl();
		c2.incrementBranches(50, 30, 5);

		c.increment(c2);

		assertEquals(5, c.getFirstLine());
		assertEquals(5, c.getLastLine());
		assertEquals(88, c.getTotalBranches(5));
		assertEquals(33, c.getCoveredBranches(5));
		assertEquals(55, c.getMissedBranches(5));
		assertEquals(NO_CODE, c.getStatus(5));
	}

	@Test
	public void testIncrementBranchesOverflow() {
		final LinesImpl c = new LinesImpl();
		c.incrementBranches(1000, 0, 5);

		assertEquals(5, c.getFirstLine());
		assertEquals(5, c.getLastLine());
		assertEquals(127, c.getTotalBranches(5));
		assertEquals(0, c.getCoveredBranches(5));
		assertEquals(127, c.getMissedBranches(5));
		assertEquals(NO_CODE, c.getStatus(5));
	}

}
