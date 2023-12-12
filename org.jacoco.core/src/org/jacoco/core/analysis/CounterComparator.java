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
package org.jacoco.core.analysis;

import java.io.Serializable;
import java.util.Comparator;

import org.jacoco.core.analysis.ICounter.CounterValue;
import org.jacoco.core.analysis.ICoverageNode.CounterEntity;

/**
 * Collection of comparators to compare {@link ICounter} objects by different
 * criteria.
 */
public class CounterComparator implements Comparator<ICounter>, Serializable {

	private static final long serialVersionUID = -3777463066252746748L;

	/**
	 * Compares the absolute number of total items.
	 */
	public static final CounterComparator TOTALITEMS = new CounterComparator(
			CounterValue.TOTALCOUNT);

	/**
	 * Compares the absolute number of covered items.
	 */
	public static final CounterComparator COVEREDITEMS = new CounterComparator(
			CounterValue.COVEREDCOUNT);

	/**
	 * Compares the absolute number of missed items.
	 */
	public static final CounterComparator MISSEDITEMS = new CounterComparator(
			CounterValue.MISSEDCOUNT);

	/**
	 * Compares the ratio of covered items.
	 */
	public static final CounterComparator COVEREDRATIO = new CounterComparator(
			CounterValue.COVEREDRATIO);

	/**
	 * Compares the ratio of missed items.
	 */
	public static final CounterComparator MISSEDRATIO = new CounterComparator(
			CounterValue.MISSEDRATIO);

	private final CounterValue value;
	private final boolean reverse;

	private CounterComparator(final CounterValue value) {
		this(value, false);
	}

	private CounterComparator(final CounterValue value, final boolean reverse) {
		this.value = value;
		this.reverse = reverse;
	}

	public int compare(final ICounter c1, final ICounter c2) {
		final int cmp = Double.compare(c1.getValue(value), c2.getValue(value));
		return reverse ? -cmp : cmp;
	}

	/**
	 * Creates a new version of this comparator that sorts in reverse order.
	 *
	 * @return reverse comparator
	 */
	public CounterComparator reverse() {
		return new CounterComparator(value, !reverse);
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
