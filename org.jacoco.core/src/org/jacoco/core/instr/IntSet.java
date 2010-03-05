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
package org.jacoco.core.instr;

import java.util.Arrays;

/**
 * Lightweight set of sorted int values. The implementation is designed for a
 * small number of elements only.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
final class IntSet {

	private int[] store;

	private int size;

	/**
	 * Creates a new empty set with a initial capacity of 8 elements.
	 */
	public IntSet() {
		this(8);
	}

	/**
	 * Creates a new empty set with the given initial capacity.
	 * 
	 * @param initialCapacity
	 *            this is the initial capacity
	 */
	public IntSet(final int initialCapacity) {
		this.store = new int[initialCapacity];
		this.size = 0;
	}

	/**
	 * Adds the given value to the set if it is not already contained.
	 * 
	 * @param value
	 *            value to add
	 * @return <code>true</code> if the value has actually been added
	 */
	public boolean add(final int value) {
		if (contains(value)) {
			return false;
		}
		if (store.length == size) {
			final int[] newStore = new int[size * 2];
			System.arraycopy(store, 0, newStore, 0, size);
			store = newStore;
		}
		store[size++] = value;
		return true;
	}

	/**
	 * Clears all elements from the set.
	 */
	public void clear() {
		size = 0;
	}

	/**
	 * Tests whether the given value is in this set.
	 * 
	 * @param value
	 *            value to check
	 * @return <code>true</code> if the value is contained
	 */
	public boolean contains(final int value) {
		// search backwards as the last value is typically added again
		for (int i = size; --i >= 0;) {
			if (store[i] == value) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns a sorted array of the current content.
	 * 
	 * @return sorted array of the current content
	 */
	public int[] toArray() {
		final int[] result = new int[size];
		System.arraycopy(store, 0, result, 0, size);
		Arrays.sort(result);
		return result;
	}

}
