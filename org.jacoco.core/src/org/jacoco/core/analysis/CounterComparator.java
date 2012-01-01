/*******************************************************************************
 * Copyright (c) Copyright (c) Copyright (c) 2009, 2012 Mountainminds GmbH & Co. KG and Contributors
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

import java.io.Serializable;
import java.util.Comparator;

import org.jacoco.core.analysis.ICoverageNode.CounterEntity;

/**
 * Collection of comparators to compare {@link ICounter} objects by different
 * criteria.
 */
public abstract class CounterComparator implements Comparator<ICounter>,
		Serializable {

	private static final long serialVersionUID = -3777463066252746748L;

	/**
	 * Compares the absolute number of total items.
	 */
	public static final CounterComparator TOTALITEMS = new CounterComparator() {

		private static final long serialVersionUID = 8824120489765405662L;

		public int compare(final ICounter c1, final ICounter c2) {
			return c1.getTotalCount() - c2.getTotalCount();
		}
	};

	/**
	 * Compares the absolute number of covered items.
	 */
	public static final CounterComparator COVEREDITEMS = new CounterComparator() {

		private static final long serialVersionUID = 1L;

		public int compare(final ICounter c1, final ICounter c2) {
			return c1.getCoveredCount() - c2.getCoveredCount();
		}
	};

	/**
	 * Compares the absolute number of missed items.
	 */
	public static final CounterComparator MISSEDITEMS = new CounterComparator() {

		private static final long serialVersionUID = -2991039557556551206L;

		public int compare(final ICounter c1, final ICounter c2) {
			return c1.getMissedCount() - c2.getMissedCount();
		}
	};

	/**
	 * Compares the ratio of covered items.
	 */
	public static final CounterComparator COVEREDRATIO = new CounterComparator() {

		private static final long serialVersionUID = 7897690710299613918L;

		public int compare(final ICounter c1, final ICounter c2) {
			return Double.compare(c1.getCoveredRatio(), c2.getCoveredRatio());
		}
	};

	/**
	 * Compares the ratio of missed items.
	 */
	public static final CounterComparator MISSEDRATIO = new CounterComparator() {

		private static final long serialVersionUID = -5014193668057469357L;

		public int compare(final ICounter c1, final ICounter c2) {
			return Double.compare(c1.getMissedRatio(), c2.getMissedRatio());
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

			private static final long serialVersionUID = 7703349549732801967L;

			public int compare(final ICounter o1, final ICounter o2) {
				return original.compare(o2, o1);
			}
		};
	}

	/**
	 * Creates a new comparator for {@link ICoverageNode} counters of the given
	 * entity based on this counter sorting criteria.
	 * 
	 * @param entity
	 *            counter entity to sort on
	 * @return comparator for {@link ICoverageNode} elements
	 */
	public NodeComparator on(final CounterEntity entity) {
		return new NodeComparator(this, entity);
	}

}
