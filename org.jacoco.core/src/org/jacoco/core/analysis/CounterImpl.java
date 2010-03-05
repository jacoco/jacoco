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
 * $Id: $
 *******************************************************************************/
package org.jacoco.core.analysis;

/**
 * {@link ICounter} implementations. Implementing a factory pattern allows to
 * share counter instances.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public abstract class CounterImpl extends AbstractCounter {

	/** Max counter value for which singletons are created */
	private static final int SINGLETON_LIMIT = 10;

	private static final CounterImpl[][] SINGLETONS = new CounterImpl[SINGLETON_LIMIT + 1][];

	static {
		for (int i = 0; i <= SINGLETON_LIMIT; i++) {
			SINGLETONS[i] = new CounterImpl[i + 1];
			for (int j = 0; j <= i; j++) {
				SINGLETONS[i][j] = new Fix(i, j);
			}
		}
	}

	/** Constant for Counter with 0/0 values. */
	public static final CounterImpl COUNTER_0_0 = SINGLETONS[0][0];

	/**
	 * Mutable version of the counter.
	 */
	private static class Var extends CounterImpl {
		public Var(final int total, final int covered) {
			super(total, covered);
		}

		@Override
		public CounterImpl increment(final ICounter counter) {
			this.total += counter.getTotalCount();
			this.covered += counter.getCoveredCount();
			return this;
		}
	}

	/**
	 * Immutable version of the counter.
	 */
	private static class Fix extends CounterImpl {
		public Fix(final int total, final int covered) {
			super(total, covered);
		}

		@Override
		public CounterImpl increment(final ICounter counter) {
			return getInstance(this.total + counter.getTotalCount(),
					this.covered + counter.getCoveredCount());
		}
	}

	/**
	 * Factory method to retrieve a counter with the given number of items.
	 * 
	 * @param total
	 *            total number of items
	 * @param covered
	 *            covered number of items
	 * @return counter instance
	 */
	public static CounterImpl getInstance(final int total, final int covered) {
		if (total <= SINGLETON_LIMIT && covered <= total) {
			return SINGLETONS[total][covered];
		} else {
			return new Var(total, covered);
		}
	}

	/**
	 * Factory method to retrieve a clone ot the given counter.
	 * 
	 * @param counter
	 *            counter to copy
	 * @return counter instance
	 */
	public static CounterImpl getInstance(final ICounter counter) {
		return getInstance(counter.getTotalCount(), counter.getCoveredCount());
	}

	/**
	 * Factory method to retrieve a counter with the given number of items.
	 * 
	 * @param total
	 *            total number of items
	 * @param covered
	 *            <code>true</code>, if all items are covered
	 * @return counter instance
	 */
	public static CounterImpl getInstance(final int total, final boolean covered) {
		return getInstance(total, covered ? total : 0);
	}

	/**
	 * Factory method to retrieve a counter for a single item.
	 * 
	 * @param covered
	 *            <code>true</code>, if the item is covered
	 * @return counter instance
	 */
	public static CounterImpl getInstance(final boolean covered) {
		return getInstance(1, covered ? 1 : 0);
	}

	/**
	 * Creates a new instance with the given figures.
	 * 
	 * @param total
	 *            total number of items
	 * @param covered
	 *            covered number of items
	 */
	protected CounterImpl(final int total, final int covered) {
		super(total, covered);
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
