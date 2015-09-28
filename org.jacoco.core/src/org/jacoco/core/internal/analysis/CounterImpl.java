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

import org.jacoco.core.analysis.ICounter;

/**
 * {@link ICounter} implementations. Implementing a factory pattern allows to
 * share counter instances.
 */
public abstract class CounterImpl implements ICounter {

	/** Max counter value for which singletons are created */
	private static final int SINGLETON_LIMIT = 30;

	private static final CounterImpl[][] SINGLETONS = new CounterImpl[SINGLETON_LIMIT + 1][];

	static {
		for (int i = 0; i <= SINGLETON_LIMIT; i++) {
			SINGLETONS[i] = new CounterImpl[SINGLETON_LIMIT + 1];
			for (int j = 0; j <= SINGLETON_LIMIT; j++) {
				SINGLETONS[i][j] = new Fix(i, j, 0);
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
		public Var(final int missed, final int covered, final int executions) {
			super(missed, covered, executions);
		}

		@Override
		public CounterImpl increment(final int missed, final int covered,
				final int executions) {
			this.executions += executions;
			this.missed += missed;
			this.covered += covered;
			return this;
		}
	}

	/**
	 * Immutable version of the counter.
	 */
	private static class Fix extends CounterImpl {
		public Fix(final int missed, final int covered, final int executions) {
			super(missed, covered, executions);
		}

		@Override
		public CounterImpl increment(final int missed, final int covered,
				final int executions) {
			return getInstance(this.missed + missed, this.covered + covered,
					this.executions + executions);
		}
	}

	/**
	 * Factory method to retrieve a counter with the given number of items.
	 * 
	 * @param missed
	 *            number of missed items
	 * @param covered
	 *            number of covered items
	 * @param executions
	 *            number of executions on items
	 * @return counter instance
	 */
	public static CounterImpl getInstance(final int missed, final int covered,
			final int executions) {
		if (missed <= SINGLETON_LIMIT && covered <= SINGLETON_LIMIT
				&& executions == 0) {
			return SINGLETONS[missed][covered];
		} else {
			return new Var(missed, covered, executions);
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
		return getInstance(counter.getMissedCount(), counter.getCoveredCount(),
				counter.getExecutionCount());
	}

	/** number of item executions */
	protected int executions;

	/** number of missed items */
	protected int missed;

	/** number of covered items (items executed at least once) */
	protected int covered;

	/**
	 * Creates a new instance with the given numbers.
	 * 
	 * @param missed
	 *            number of missed items
	 * @param covered
	 *            number of covered items
	 * @param executions
	 *            number of executions
	 */
	protected CounterImpl(final int missed, final int covered,
			final int executions) {
		this.executions = executions;
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
		return increment(counter.getMissedCount(), counter.getCoveredCount(),
				counter.getExecutionCount());
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
	 * @param executions
	 *            number of executions on items
	 * @return counter instance with incremented values
	 */
	public abstract CounterImpl increment(int missed, int covered,
			int executions);

	// === ICounter implementation ===

	public double getValue(final CounterValue value) {
		switch (value) {
		case TOTALEXECCOUNT:
			return getExecutionCount();
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

	public int getExecutionCount() {
		return executions;
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
			return this.executions == that.getExecutionCount()
					&& this.missed == that.getMissedCount()
					&& this.covered == that.getCoveredCount();
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + covered;
		result = prime * result + executions;
		result = prime * result + missed;
		return result;
	}

	@Override
	public String toString() {
		final StringBuilder b = new StringBuilder("Counter["); //$NON-NLS-1$
		b.append(getMissedCount());
		b.append('/').append(getCoveredCount());
		b.append('/').append(getExecutionCount());
		b.append(']');
		return b.toString();
	}

}
