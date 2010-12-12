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
	public void testEmpty1() {
		final ILines c = new LinesImpl();
		assertEquals(0, c.getTotalCount(), 0.0);
		assertEquals(0, c.getCoveredCount(), 0.0);
		assertEquals(-1, c.getFirstLine(), 0.0);
		assertEquals(-1, c.getLastLine(), 0.0);
		assertEquals(NO_CODE, c.getStatus(5), 0.0);
	}

	@Test
	public void testInitMissed() {
		final LinesImpl c = createNotCovered(5, 7, 10);
		assertEquals(3, c.getTotalCount(), 0.0);
		assertEquals(0, c.getCoveredCount(), 0.0);
		assertEquals(5, c.getFirstLine(), 0.0);
		assertEquals(10, c.getLastLine(), 0.0);
		assertEquals(NO_CODE, c.getStatus(4), 0.0);
		assertEquals(NOT_COVERED, c.getStatus(5), 0.0);
		assertEquals(NO_CODE, c.getStatus(6), 0.0);
		assertEquals(NOT_COVERED, c.getStatus(7), 0.0);
		assertEquals(NO_CODE, c.getStatus(8), 0.0);
		assertEquals(NO_CODE, c.getStatus(9), 0.0);
		assertEquals(NOT_COVERED, c.getStatus(10), 0.0);
		assertEquals(NO_CODE, c.getStatus(11), 0.0);
	}

	@Test
	public void testInitCovered() {
		final LinesImpl c = createFullyCovered(5, 7, 10);
		assertEquals(3, c.getTotalCount(), 0.0);
		assertEquals(3, c.getCoveredCount(), 0.0);
		assertEquals(5, c.getFirstLine(), 0.0);
		assertEquals(10, c.getLastLine(), 0.0);
		assertEquals(NO_CODE, c.getStatus(4), 0.0);
		assertEquals(FULLY_COVERED, c.getStatus(5), 0.0);
		assertEquals(NO_CODE, c.getStatus(6), 0.0);
		assertEquals(FULLY_COVERED, c.getStatus(7), 0.0);
		assertEquals(NO_CODE, c.getStatus(8), 0.0);
		assertEquals(NO_CODE, c.getStatus(9), 0.0);
		assertEquals(FULLY_COVERED, c.getStatus(10), 0.0);
		assertEquals(NO_CODE, c.getStatus(11), 0.0);
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
		assertEquals(4, c.getTotalCount(), 0.0);
		assertEquals(0, c.getCoveredCount(), 0.0);
		assertEquals(1, c.getFirstLine(), 0.0);
		assertEquals(5, c.getLastLine(), 0.0);
		assertEquals(NOT_COVERED, c.getStatus(1), 0.0);
		assertEquals(NOT_COVERED, c.getStatus(2), 0.0);
		assertEquals(NO_CODE, c.getStatus(3), 0.0);
		assertEquals(NOT_COVERED, c.getStatus(4), 0.0);
		assertEquals(NOT_COVERED, c.getStatus(5), 0.0);
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
		assertEquals(4, c.getTotalCount(), 0.0);
		assertEquals(4, c.getCoveredCount(), 0.0);
		assertEquals(1, c.getFirstLine(), 0.0);
		assertEquals(4, c.getLastLine(), 0.0);
		assertEquals(FULLY_COVERED, c.getStatus(1), 0.0);
		assertEquals(FULLY_COVERED, c.getStatus(2), 0.0);
		assertEquals(FULLY_COVERED, c.getStatus(3), 0.0);
		assertEquals(FULLY_COVERED, c.getStatus(4), 0.0);
	}

	@Test
	public void testIncrement3() {
		// 1: F + N = P
		// ============
		// 1 total, 1 covered
		final LinesImpl c = createFullyCovered(1);
		c.increment(createNotCovered(1));
		assertEquals(1, c.getTotalCount(), 0.0);
		assertEquals(1, c.getCoveredCount(), 0.0);
		assertEquals(1, c.getFirstLine(), 0.0);
		assertEquals(1, c.getLastLine(), 0.0);
		assertEquals(PARTLY_COVERED, c.getStatus(1), 0.0);
	}

	@Test
	public void testIncrement4() {
		// 1: N + F = P
		// ============
		// 1 total, 1 covered
		final LinesImpl c = createNotCovered(1);
		c.increment(createFullyCovered(1));
		assertEquals(1, c.getTotalCount(), 0.0);
		assertEquals(1, c.getCoveredCount(), 0.0);
		assertEquals(1, c.getFirstLine(), 0.0);
		assertEquals(1, c.getLastLine(), 0.0);
		assertEquals(PARTLY_COVERED, c.getStatus(1), 0.0);
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
		assertEquals(4, c.getTotalCount(), 0.0);
		assertEquals(4, c.getCoveredCount(), 0.0);
		assertEquals(1, c.getFirstLine(), 0.0);
		assertEquals(5, c.getLastLine(), 0.0);
		assertEquals(PARTLY_COVERED, c.getStatus(1), 0.0);
		assertEquals(PARTLY_COVERED, c.getStatus(2), 0.0);
		assertEquals(NO_CODE, c.getStatus(3), 0.0);
		assertEquals(PARTLY_COVERED, c.getStatus(4), 0.0);
		assertEquals(PARTLY_COVERED, c.getStatus(5), 0.0);
	}

	@Test
	public void testIncrement6() {
		// 1: P + N = P
		// ============
		// 1 total, 1 covered
		final LinesImpl c = createPartlyCovered(1);
		c.increment(createNotCovered(1));
		assertEquals(1, c.getTotalCount(), 0.0);
		assertEquals(1, c.getCoveredCount(), 0.0);
		assertEquals(1, c.getFirstLine(), 0.0);
		assertEquals(1, c.getLastLine(), 0.0);
		assertEquals(PARTLY_COVERED, c.getStatus(1), 0.0);
	}

	@Test
	public void testIncrement7() {
		// 1: N + P = P
		// ============
		// 1 total, 1 covered
		final LinesImpl c = createNotCovered(1);
		c.increment(createPartlyCovered(1));
		assertEquals(1, c.getTotalCount(), 0.0);
		assertEquals(1, c.getCoveredCount(), 0.0);
		assertEquals(1, c.getFirstLine(), 0.0);
		assertEquals(1, c.getLastLine(), 0.0);
		assertEquals(PARTLY_COVERED, c.getStatus(1), 0.0);
	}

	@Test
	public void testIncrement8() {
		// 1: P + F = P
		// ============
		// 1 total, 1 covered
		final LinesImpl c = createPartlyCovered(1);
		c.increment(createFullyCovered(1));
		assertEquals(1, c.getTotalCount(), 0.0);
		assertEquals(1, c.getCoveredCount(), 0.0);
		assertEquals(1, c.getFirstLine(), 0.0);
		assertEquals(1, c.getLastLine(), 0.0);
		assertEquals(PARTLY_COVERED, c.getStatus(1), 0.0);
	}

	@Test
	public void testIncrement9() {
		// 1: F + P = P
		// ============
		// 1 total, 1 covered
		final LinesImpl c = createFullyCovered(1);
		c.increment(createPartlyCovered(1));
		assertEquals(1, c.getTotalCount(), 0.0);
		assertEquals(1, c.getCoveredCount(), 0.0);
		assertEquals(1, c.getFirstLine(), 0.0);
		assertEquals(1, c.getLastLine(), 0.0);
		assertEquals(PARTLY_COVERED, c.getStatus(1), 0.0);
	}

	@Test
	public void testIncrementEmpty() {
		final LinesImpl c = createFullyCovered(1);
		c.increment(new LinesImpl());
		assertEquals(1, c.getTotalCount(), 0.0);
		assertEquals(1, c.getCoveredCount(), 0.0);
		assertEquals(1, c.getFirstLine(), 0.0);
		assertEquals(1, c.getLastLine(), 0.0);
		assertEquals(FULLY_COVERED, c.getStatus(1), 0.0);
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
		assertEquals(3, c.getTotalCount(), 0.0);
		assertEquals(3, c.getCoveredCount(), 0.0);
		assertEquals(1, c.getFirstLine(), 0.0);
		assertEquals(3, c.getLastLine(), 0.0);
		assertEquals(FULLY_COVERED, c.getStatus(1), 0.0);
		assertEquals(FULLY_COVERED, c.getStatus(2), 0.0);
		assertEquals(FULLY_COVERED, c.getStatus(3), 0.0);
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
		assertEquals(3, c.getTotalCount(), 0.0);
		assertEquals(3, c.getCoveredCount(), 0.0);
		assertEquals(1, c.getFirstLine(), 0.0);
		assertEquals(3, c.getLastLine(), 0.0);
		assertEquals(FULLY_COVERED, c.getStatus(1), 0.0);
		assertEquals(FULLY_COVERED, c.getStatus(2), 0.0);
		assertEquals(FULLY_COVERED, c.getStatus(3), 0.0);
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
		assertEquals(3, c.getTotalCount(), 0.0);
		assertEquals(3, c.getCoveredCount(), 0.0);
		assertEquals(1, c.getFirstLine(), 0.0);
		assertEquals(3, c.getLastLine(), 0.0);
		assertEquals(FULLY_COVERED, c.getStatus(1), 0.0);
		assertEquals(FULLY_COVERED, c.getStatus(2), 0.0);
		assertEquals(FULLY_COVERED, c.getStatus(3), 0.0);
	}

	private LinesImpl createNotCovered(final int... lines) {
		final LinesImpl c = new LinesImpl();
		for (int l : lines) {
			c.increment(l, false);
		}
		return c;
	}

	private LinesImpl createFullyCovered(final int... lines) {
		final LinesImpl c = new LinesImpl();
		for (int l : lines) {
			c.increment(l, true);
		}
		return c;
	}

	private LinesImpl createPartlyCovered(final int... lines) {
		final LinesImpl c = new LinesImpl();
		for (int l : lines) {
			c.increment(l, false);
			c.increment(l, true);
		}
		return c;
	}

}
