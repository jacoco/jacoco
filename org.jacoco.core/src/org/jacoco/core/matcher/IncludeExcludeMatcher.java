/*******************************************************************************
 * Copyright (c) 2009, 2017 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Jeffry Gaston - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.matcher;

import java.util.ArrayList;
import java.util.List;

/**
 * An IncludeExcludeMatcher matches a given input if
 * at least one inclusion matches and no exclusions match.
 */
public class IncludeExcludeMatcher<T> implements Matcher<T> {
	private List<Matcher<T>> inclusions = new ArrayList<Matcher<T>>();
	private List<Matcher<T>> exclusions = new ArrayList<Matcher<T>>();

	/**
	 * Includes the given matcher
	 * @param inclusion new matcher to include
	 * @return this object (for chaining several calls)
	 */
	public IncludeExcludeMatcher Include(Matcher<T> inclusion) {
		inclusions.add(inclusion);
		return this;
	}

	/**
	 * Excludes a given matcher
	 * @param exclusion
	 * @return this object (for chaining several calls)
	 */
	public IncludeExcludeMatcher Exclude(Matcher<T> exclusion) {
		exclusions.add(exclusion);
		return this;
	}

	/**
	 * Tells whether this matcher matches this string
	 * @param input the string match
	 * @return whether the matcher matches
	 */
	public boolean matches(T input) {
		// doesn't match if an exclusion matches
		for (Matcher<T> exclusion : exclusions) {
			if (exclusion.matches(input)) {
				return false;
			}
		}
		// does match if an inclusion matches
		for (Matcher<T> inclusion : inclusions) {
			if (inclusion.matches(input)) {
				return true;
			}
		}
		// no match; choose a default based on whether any includes were given
		return (inclusions.size() == 0);
	}
}