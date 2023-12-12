/*******************************************************************************
 * Copyright (c) 2009, 2023 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Brock Janiczak - analysis and concept
 *    Marc R. Hoffmann - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.internal.analysis;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility to normalize {@link String} instances in a way that if
 * <code>equals()</code> is <code>true</code> for two strings they will be
 * represented the same instance. While this is exactly what
 * {@link String#intern()} does, this implementation avoids VM specific side
 * effects and is supposed to be faster, as neither native code is called nor
 * synchronization is required for concurrent lookup.
 */
public final class StringPool {

	private static final String[] EMPTY_ARRAY = new String[0];

	private final Map<String, String> pool = new HashMap<String, String>(1024);

	/**
	 * Returns a normalized instance that is equal to the given {@link String} .
	 *
	 * @param s
	 *            any string or <code>null</code>
	 * @return normalized instance or <code>null</code>
	 */
	public String get(final String s) {
		if (s == null) {
			return null;
		}
		final String norm = pool.get(s);
		if (norm == null) {
			pool.put(s, s);
			return s;
		}
		return norm;
	}

	/**
	 * Returns a modified version of the array with all string slots normalized.
	 * It is up to the implementation to replace strings in the array instance
	 * or return a new array instance.
	 *
	 * @param arr
	 *            String array or <code>null</code>
	 * @return normalized instance or <code>null</code>
	 */
	public String[] get(final String[] arr) {
		if (arr == null) {
			return null;
		}
		if (arr.length == 0) {
			return EMPTY_ARRAY;
		}
		for (int i = 0; i < arr.length; i++) {
			arr[i] = get(arr[i]);
		}
		return arr;
	}

}
