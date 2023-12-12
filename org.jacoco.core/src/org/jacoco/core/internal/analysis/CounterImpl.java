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

import org.jacoco.core.analysis.ICounter;

/**
 * {@link ICounter} implementations. Implementing a factory pattern allows to
 * share counter instances.
 */
public abstract class CounterImpl implements ICounter {

	/** Max counter value for which singletons are created */
	private static final int SINGLETON_LIMIT = 30;

	private static final CounterImpl[][] SINGLETONS = new CounterImpl[SINGLETON_LIMIT
			+ 1][];

	static {
		for (int i = 0; i <= SINGLETON_LIMIT; i++) {
			SINGLETONS[i] = new CounterImpl[SINGLETON_LIMIT + 1];
			for (int j = 0; j <= SINGLETON_LIMIT; j++) {
				SINGLETONS[i][j] = new Fix(i, j);
			}
		}
	}

	/** Constant for Counter with 0/0 values. */
	public static final CounterImpl COUNTER_0_0 = SINGLETONS[0][0];

	/** Constant for Counter with 1/0 values. */
	public static final CounterImpl COUNTER_1_0 = SINGLETONS[1][0];

	/** Constant for Counter with 0/1 values. */
	public static final CounterImpl COUNTER_0_1 = SINGLETONS[0][1];

	/**
	 * Mutable version of the counter.
	 */
	private static class Var extends CounterImpl {
		public Var(final int missed, final int covered) {
			super(missed, covered);
		}

		@Override
		public CounterImpl increment(final int missed, final int covered) {
			this.missed += missed;
			this.covered += covered;
			return this;
		}
	}

	/**
	 * Immutable version of the counter.
	 */
	private static class Fix extends CounterImpl {
		public Fix(final int missed, final int covered) {
			super(missed, covered);
		}

		@Override
		public CounterImpl increment(final int missed, final int covered) {
			return getInstance(this.missed + missed, this.covered + covered);
		}
	}

	/**
	 * Factory method to retrieve a counter with the given number of items.
	 *
	 * @param missed
	 *            number of missed items
	 * @param covered
	 *            number of covered items
	 * @return counter instance
	 */
	public static CounterImpl getInstance(final int missed, final int covered) {
		if (missed <= SINGLETON_LIMIT && covered <= SINGLETON_LIMIT) {
			return SINGLETONS[missed][covered];
		} else {
			return new Var(missed, covered);
		}
	}

	/**
	 * Factory method to retrieve a clone of the given counter.
	 *
	 * @param counter
	 *            counter to copy
	 * @return counter instance
	 */
	public static CounterImpl getInstance(final ICounter counter) {
		return getInstance(counter.getMissedCount(), counter.getCoveredCount());
	}

	/** number of missed items */
	protected int missed;

	/** number of covered items */
	protected int covered;

	/**
	 * Creates a new instance with the given numbers.
	 *
	 * @param missed
	 *            number of missed items
	 * @param covered
	 *            number of covered items
	 */
	protected CounterImpl(final int missed, final int covered) {
		this.missed = missed;
		this.covered = covered;
	}

	/**
	 * Returns a counter with values incremented by the numbers of the given
	 * counter. It is up to the implementation whether this counter instance is
	 * modified or a new instance is returned.
	 *
	 * @param counter
	 *            number of additional total and covered items
	 * @return counter instance with incremented values
	 */
	public CounterImpl increment(final ICounter counter) {
		return increment(counter.getMissedCount(), counter.getCoveredCount());
	}

	/**
	 * Returns a counter with values incremented by the given numbers. It is up
	 * to the implementation whether this counter instance is modified or a new
	 * instance is returned.
	 *
	 * @param missed
	 *            number of missed items
	 * @param covered
	 *            number of covered items
	 * @return counter instance with incremented values
	 */
	public abstract CounterImpl increment(int missed, int covered);

	// === ICounter implementation ===

	public double getValue(final CounterValue value) {
		switch (value) {
		case TOTALCOUNT:
			return getTotalCount();
		case MISSEDCOUNT:
			return getMissedCount();
		case COVEREDCOUNT:
			return getCoveredCount();
		case MISSEDRATIO:
			return getMissedRatio();
		case COVEREDRATIO:
			return getCoveredRatio();
		default:
			throw new AssertionError(value);
		}
	}

	public int getTotalCount() {
		return missed + covered;
	}

	public int getCoveredCount() {
		return covered;
	}

	public int getMissedCount() {
		return missed;
	}

	public double getCoveredRatio() {
		return (double) covered / (missed + covered);
	}

	public double getMissedRatio() {
		return (double) missed / (missed + covered);
	}

	public int getStatus() {
		int status = covered > 0 ? FULLY_COVERED : EMPTY;
		if (missed > 0) {
			status |= NOT_COVERED;
		}
		return status;
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj instanceof ICounter) {
			final ICounter that = (ICounter) obj;
			return this.missed == that.getMissedCount()
					&& this.covered == that.getCoveredCount();
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return missed ^ covered * 17;
	}

	@Override
	public String toString() {
		final StringBuilder b = new StringBuilder("Counter["); //$NON-NLS-1$
		b.append(getMissedCount());
		b.append('/').append(getCoveredCount());
		b.append(']');
		return b.toString();
	}

}
