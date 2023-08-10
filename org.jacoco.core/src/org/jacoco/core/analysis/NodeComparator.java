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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.jacoco.core.analysis.ICoverageNode.CounterEntity;

/**
 * Comparator to compare {@link ICoverageNode} objects by different counter
 * criteria.
 *
 * @see CounterComparator#on(ICoverageNode.CounterEntity)
 */
public class NodeComparator implements Comparator<ICoverageNode>, Serializable {

	private static final long serialVersionUID = 8550521643608826519L;

	private final Comparator<ICounter> counterComparator;

	private final CounterEntity entity;

	NodeComparator(final Comparator<ICounter> counterComparator,
			final CounterEntity entity) {
		this.counterComparator = counterComparator;
		this.entity = entity;
	}

	/**
	 * Creates a new composite comparator with a second search criterion.
	 *
	 * @param second
	 *            second criterion comparator
	 *
	 * @return composite comparator
	 */
	public NodeComparator second(final Comparator<ICoverageNode> second) {
		final Comparator<ICoverageNode> first = this;
		return new NodeComparator(null, null) {

			private static final long serialVersionUID = -5515272752138802838L;

			@Override
			public int compare(final ICoverageNode o1, final ICoverageNode o2) {
				final int result = first.compare(o1, o2);
				return result == 0 ? second.compare(o1, o2) : result;
			}
		};
	}

	/**
	 * Returns a sorted copy of the given collection of {@link ICoverageNode}
	 * elements.
	 *
	 * @param <T>
	 *            actual type of the elements
	 * @param summaries
	 *            collection to create a copy of
	 * @return sorted copy
	 */
	public <T extends ICoverageNode> List<T> sort(
			final Collection<T> summaries) {
		final List<T> result = new ArrayList<T>(summaries);
		Collections.sort(result, this);
		return result;
	}

	public int compare(final ICoverageNode n1, final ICoverageNode n2) {
		final ICounter c1 = n1.getCounter(entity);
		final ICounter c2 = n2.getCounter(entity);
		return counterComparator.compare(c1, c2);
	}

}
