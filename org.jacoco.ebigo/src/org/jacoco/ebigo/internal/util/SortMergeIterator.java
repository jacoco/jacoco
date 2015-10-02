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
package org.jacoco.ebigo.internal.util;

import java.lang.reflect.Array;
import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Helps the Empirical Big-O analysis walk through multiple coverage analysis in
 * lock-step.
 * 
 * @author Omer Azmon
 */
public class SortMergeIterator<T> implements Iterator<T[]> {

	private final Comparator<T> comparator;
	private final Iterator<T>[] objectIterators;
	private final T[] nextSet;
	private final T[] thisSet;
	private boolean hasNext;

	@SuppressWarnings("unchecked")
	private T[] newArrayT(int size) {
		final ParameterizedType pType = (ParameterizedType) this.getClass()
				.getGenericSuperclass();
		final Class<T> classT = (Class<T>) pType.getActualTypeArguments()[0];
		return (T[]) Array.newInstance(classT, size);
	}

	/**
	 * Constructor
	 * 
	 * @param comparator
	 *            the comparator that will be used to order this (@code List}. If
	 *            {@code null}, the {@linkplain Comparable natural ordering} of
	 *            the elements will be used.
	 * @param listofList
	 *            the list of lists of objects of type T
	 * @throws IllegalArgumentException
	 *             if the list is {@code null} or empty (no elements).
	 */
	@SuppressWarnings("unchecked")
	public SortMergeIterator(final Comparator<T> comparator,
			final List<Collection<T>> listofList) {
		if (listofList == null || listofList.isEmpty()) {
			throw new IllegalArgumentException("null or empty object list");
		}
		this.comparator = comparator;

		// Create iterator array
		final Iterator<T>[] iterators = new Iterator[listofList.size()];
		objectIterators = iterators;
		for (int idx = 0; idx < listofList.size(); idx++) {
			final SortedSet<T> sortedList = new TreeSet<T>(comparator);
			sortedList.addAll(listofList.get(idx));
			objectIterators[idx] = sortedList.iterator();
		}

		thisSet = newArrayT(listofList.size());
		nextSet = newArrayT(listofList.size());

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
		for (int idx = 0; idx < objectIterators.length; idx++) {
			if (!objectIterators[idx].hasNext()) {
				hasNext = false;
				break;
			}
			nextSet[idx] = objectIterators[idx].next();
		}
	}

	private T findGreatest() {
		T greatest = null;
		for (int idx = 0; idx < objectIterators.length; idx++) {
			if (comparator.compare(nextSet[idx], greatest) > 0) {
				greatest = nextSet[idx];
			}
		}
		return greatest;
	}

	private boolean nextUntilMatch(final T greatest) {
		boolean matchFound = true;
		for (int idx = 0; idx < objectIterators.length; idx++) {
			// while < greatest, get next
			while (comparator.compare(nextSet[idx], greatest) < 0) {
				// if no next, we are done with the iteration
				if (!objectIterators[idx].hasNext()) {
					hasNext = false;
					return true;
				}
				matchFound = false; // Oh, well
				nextSet[idx] = objectIterators[idx].next();
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
			T greatest = findGreatest();
			fullMatchFound = nextUntilMatch(greatest);
		}

		return;
	}

	/**
	 * Returns the next lock-step object (one per list in the list of object
	 * lists in the constructor. The order of elements in the array matches the
	 * order in the list provided during object construction.
	 *
	 * @return the next set in the iteration
	 * @throws NoSuchElementException
	 *             if the iteration has no more sets in the iteration.
	 */
	public T[] next() {
		if (!hasNext()) {
			throw new NoSuchElementException();
		}

		System.arraycopy(nextSet, 0, thisSet, 0, nextSet.length);
		findNextMatchingSet();
		return thisSet;
	}

	/**
	 * Not Supported HERE
	 * 
	 * @throws UnsupportedOperationException unconditionally
	 */
	public void remove() {
		throw new UnsupportedOperationException();
	}


}