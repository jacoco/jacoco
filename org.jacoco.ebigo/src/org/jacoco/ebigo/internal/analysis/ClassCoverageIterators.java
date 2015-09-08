/*******************************************************************************
 * Copyright (c) 2009, 2015 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Omer Azmon - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.ebigo.internal.analysis;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.TreeSet;

import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.IClassCoverage;

/**
 * Helps the Empirical Big-O analysis walk through multiple coverage analysis in
 * lock-step.
 * 
 * @author Omer Azmon
 */
public class ClassCoverageIterators implements Iterator<IClassCoverage[]> {
	private static class ClassCoverageSortedSet extends TreeSet<IClassCoverage> {
		private static final long serialVersionUID = -3743987701961283390L;

		public ClassCoverageSortedSet(Collection<IClassCoverage> c) {
			super(new Comparator<IClassCoverage>() {
				public int compare(IClassCoverage left, IClassCoverage right) {
					return left.getName().compareTo(right.getName());
				}
			});
			addAll(c);
		}

	}

	private final Iterator<IClassCoverage>[] classCoverageIterators;
	private final IClassCoverage[] nextccs;
	private final IClassCoverage[] thisccs;
	private boolean hasNext;

	/**
	 * Constructor
	 * 
	 * @param coverageBuilderList
	 *            the list of coverage analysis to walk in lock-step.
	 * @throws IllegalArgumentException
	 *             if the {@code analyzerList} is {@code null} or empty (no
	 *             elements).
	 */
	public ClassCoverageIterators(
			final List<CoverageBuilder> coverageBuilderList) {
		if (coverageBuilderList == null || coverageBuilderList.isEmpty()) {
			throw new IllegalArgumentException("null or empty analyzerList");
		}

		// Create iterator array
		@SuppressWarnings("unchecked")
		final Iterator<IClassCoverage>[] iterators = new Iterator[coverageBuilderList
				.size()];
		classCoverageIterators = iterators;
		for (int idx = 0; idx < coverageBuilderList.size(); idx++) {
			ClassCoverageSortedSet ccs = new ClassCoverageSortedSet(
					coverageBuilderList.get(idx).getClasses());
			classCoverageIterators[idx] = ccs.iterator();
		}

		thisccs = new IClassCoverage[coverageBuilderList.size()];
		nextccs = new IClassCoverage[coverageBuilderList.size()];

		hasNext = true;

		// Find first element
		findNextMatchingSet();
	}

	/**
	 * Returns {@code true} if their are more class coverage elements. (In other
	 * words, returns {@code true} if {@link #next} would return an element
	 * rather than throwing an exception.)
	 *
	 * @return {@code true} if the iteration has more class coverage elements
	 */
	public boolean hasNext() {
		return hasNext;
	}

	private void nextAllIterators() {
		for (int idx = 0; idx < classCoverageIterators.length; idx++) {
			if (!classCoverageIterators[idx].hasNext()) {
				hasNext = false;
				break;
			}
			nextccs[idx] = classCoverageIterators[idx].next();
		}
	}

	private String findGreatestClassName() {
		String greatestString = "";
		for (int idx = 0; idx < classCoverageIterators.length; idx++) {
			String className = nextccs[idx].getName();
			if (className.compareTo(greatestString) > 0) {
				greatestString = className;
			}
		}
		return greatestString;
	}

	private boolean nextUntilMatchClassName(final String greatestClassName) {
		boolean matchFound = true;
		for (int idx = 0; idx < classCoverageIterators.length; idx++) {
			// while < greatest, get next
			while (nextccs[idx].getName().compareTo(greatestClassName) < 0) {
				// if no next, we are done with the iteration
				if (!classCoverageIterators[idx].hasNext()) {
					hasNext = false;
					return true;
				}
				matchFound = false; // Oh, well
				nextccs[idx] = classCoverageIterators[idx].next();
			}
		}
		return matchFound;
	}

	private void findNextMatchingSet() {
		nextAllIterators();
		if (!hasNext()) {
			return;
		}

		boolean fullMatchFound = false;
		while (!fullMatchFound) {
			String greatestClassName = findGreatestClassName();
			fullMatchFound = nextUntilMatchClassName(greatestClassName);
		}

		return;
	}

	/**
	 * Returns the next lock-step {@code IClassCoverage[]} (one per analysis
	 * object in the constructor. The order of elements in the array matches the
	 * order in the list provided during object construction.
	 *
	 * @return the next {@code IClassCoverage[]} in the iteration
	 * @throws NoSuchElementException
	 *             if the iteration has no more {@code IClassCoverage[]} in the
	 *             iteration.
	 */
	public IClassCoverage[] next() {
		if (!hasNext()) {
			throw new NoSuchElementException();
		}

		System.arraycopy(nextccs, 0, thisccs, 0, nextccs.length);
		findNextMatchingSet();
		return thisccs;
	}
}