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

/**
 * {@link ICounter} implementations. Implementing a factory pattern allows to
 * share counter instances.
 * 
 * @author Marc R. Hoffmann
 * @version $qualified.bundle.version$
 */
public abstract class CounterImpl extends AbstractCounter {

	/** Max counter value for which singletons are created */
	private static final int SINGLETON_LIMIT = 30;

	private static final CounterImpl[][] SINGLETONS = new CounterImpl[SINGLETON_LIMIT + 1][];

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
		public CounterImpl increment(final ICounter counter) {
			this.missed += counter.getMissedCount();
			this.covered += counter.getCoveredCount();
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
		public CounterImpl increment(final ICounter counter) {
			return getInstance(this.missed + counter.getMissedCount(),
					this.covered + counter.getCoveredCount());
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

	/**
	 * Factory method to retrieve a counter for a single item.
	 * 
	 * @param covered
	 *            <code>true</code>, if the item is covered
	 * @return counter instance
	 */
	public static CounterImpl getInstance(final boolean covered) {
		return covered ? COUNTER_0_1 : COUNTER_1_0;
	}

	/**
	 * Creates a new instance with the given numbers.
	 * 
	 * @param missed
	 *            number of missed items
	 * @param covered
	 *            number of covered items
	 */
	protected CounterImpl(final int missed, final int covered) {
		super(missed, covered);
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
	public abstract CounterImpl increment(final ICounter counter);

}
