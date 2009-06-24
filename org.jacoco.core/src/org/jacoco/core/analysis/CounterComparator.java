/*******************************************************************************
 * Copyright (c) 2009 Mountainminds GmbH & Co. KG and others
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.jacoco.core.analysis.ICoverageDataSummary.CounterEntity;

/**
 * Collection of comparators to compare {@link ICounter} and
 * {@link ICoverageDataSummary} objects by different criteria.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public abstract class CounterComparator implements Comparator<ICounter> {

	/**
	 * Compares the absolute number of total items.
	 */
	public static final CounterComparator TOTALITEMS = new CounterComparator() {
		public int compare(final ICounter c1, final ICounter c2) {
			return c1.getTotalCount() - c2.getTotalCount();
		}
	};

	/**
	 * Compares the absolute number of covered items.
	 */
	public static final CounterComparator COVEREDITEMS = new CounterComparator() {
		public int compare(final ICounter c1, final ICounter c2) {
			return c1.getCoveredCount() - c2.getCoveredCount();
		}
	};

	/**
	 * Compares the absolute number of not covered items.
	 */
	public static final CounterComparator NOTCOVEREDITEMS = new CounterComparator() {
		public int compare(final ICounter c1, final ICounter c2) {
			return c1.getNotCoveredCount() - c2.getNotCoveredCount();
		}
	};

	/**
	 * Compares the ratio of covered items.
	 */
	public static final CounterComparator COVEREDRATIO = new CounterComparator() {
		public int compare(final ICounter c1, final ICounter c2) {
			return Double.compare(c1.getCoveredRatio(), c2.getCoveredRatio());
		}
	};

	/**
	 * Compares the ratio of not covered items.
	 */
	public static final CounterComparator NOTCOVEREDRATIO = new CounterComparator() {
		public int compare(final ICounter c1, final ICounter c2) {
			return Double.compare(c1.getNotCoveredRatio(), c2
					.getNotCoveredRatio());
		}
	};

	/**
	 * Creates a new version of this comparator that sorts in reverse order.
	 * 
	 * @return reverse comparator
	 */
	public CounterComparator reverse() {
		final CounterComparator original = this;
		return new CounterComparator() {
			public int compare(final ICounter o1, final ICounter o2) {
				return original.compare(o2, o1);
			}
		};
	}

	/**
	 * Creates a new comparator for {@link ICoverageDataSummary} counters of the
	 * given entity based on this counter sorting criteria.
	 * 
	 * @param entity
	 *            counter entity to sort on
	 * @return comparator for {@link ICoverageDataSummary} elements
	 */
	public Comparator<ICoverageDataSummary> getDataComparator(
			final CounterEntity entity) {
		return new Comparator<ICoverageDataSummary>() {
			public int compare(final ICoverageDataSummary n1,
					final ICoverageDataSummary n2) {
				final ICounter c1 = n1.getCounter(entity);
				final ICounter c2 = n2.getCounter(entity);
				return CounterComparator.this.compare(c1, c2);
			}
		};
	}

	/**
	 * Returns a sorted copy of the given collection of
	 * {@link ICoverageDataSummary} elements.
	 * 
	 * @param <T>
	 *            actual type of the elements
	 * @param summaries
	 *            collection to create a copy of
	 * @param entity
	 *            counter entity to sort
	 * @return sorted copy
	 */
	public <T extends ICoverageDataSummary> List<T> sort(
			final Collection<T> summaries, final CounterEntity entity) {
		final List<T> result = new ArrayList<T>(summaries);
		Collections.sort(result, getDataComparator(entity));
		return result;
	}

}
