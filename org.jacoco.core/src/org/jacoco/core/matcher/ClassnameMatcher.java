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

import java.util.List;
import org.objectweb.asm.ClassReader;

/**
 * A ClassnameMatcher matches ClassReader objects based on their class name
 */
public class ClassnameMatcher implements Matcher<ClassReader> {
	private IncludeExcludeMatcher<String> matcher = new IncludeExcludeMatcher<String>();

	/**
	 * As the given pattern as an inclusion for this matcher
	 * @param pattern pattern to include
	 * @return this object (for chaining)
	 */
	public ClassnameMatcher Include(String pattern) {
		matcher.Include(new WildcardMatcher(pattern));
		return this;
	}

	/**
	 * As the given patterns as inclusions for this matcher
	 * @param patterns patterns to include
	 * @return this object (for chaining)
	 */
	public ClassnameMatcher Include(List<String> patterns) {
		for (String pattern : patterns) {
			Include(pattern);
		}
		return this;
	}

	/**
	 * As the given pattern as an exclusion for this matcher
	 * @param pattern pattern to exclude
	 * @return this object (for chaining)
	 */
	public ClassnameMatcher Exclude(String pattern) {
		matcher.Exclude(new WildcardMatcher(pattern));
		return this;
	}

	/**
	 * As the given patterns as exclusions for this matcher
	 * @param patterns patterns to include
	 * @return this object (for chaining)
	 */
	public ClassnameMatcher Exclude(List<String> patterns) {
		for (String pattern : patterns) {
			Exclude(pattern);
		}
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
