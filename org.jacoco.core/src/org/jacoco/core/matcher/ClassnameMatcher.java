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

import org.objectweb.asm.ClassReader;

/**
 * A ClassnameMatcher matches ClassReader objects based on their class name
 */
public class ClassnameMatcher implements Matcher<ClassReader> {
	private IncludeExcludeMatcher<String> matcher = new IncludeExcludeMatcher<String>();

	/**
	 * Includes the given pattern from the matches of this matcher
	 * @param pattern to include
	 * @return this object (for chaining)
	 */
	public ClassnameMatcher Include(String pattern) {
		matcher.Include(new WildcardMatcher(pattern));
		return this;
	}

	/**
	 * Excludes the given pattern from the matches of this matcher
	 * @param pattern pattern to exclude
	 * @return this object (for chaining)
	 */
	public ClassnameMatcher Exclude(String pattern) {
		matcher.Exclude(new WildcardMatcher(pattern));
		return this;
	}

	/**
	 * Tells whether this matcher matches this class reader
	 * @param classReader the reader to match
	 * @return whether this matcher matches
	 */
	public boolean matches(ClassReader classReader) {
		return this.matcher.matches(classReader.getClassName().replaceAll("/", "."));
	}

}
