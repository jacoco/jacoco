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
package org.jacoco.core.runtime;

import java.util.regex.Pattern;

/**
 * Matches strings against glob like wildcard expressions where <code>?</code>
 * matches any single character and <code>*</code> matches any number of any
 * character. Multiple expressions can be separated with a colon (:). In this
 * case the expression matches if at least one part matches.
 */
public class WildcardMatcher {

	private final Pattern pattern;

	/**
	 * Creates a new matcher with the given expression.
	 *
	 * @param expression
	 *            wildcard expressions
	 */
	public WildcardMatcher(final String expression) {
		final String[] parts = expression.split("\\:");
		final StringBuilder regex = new StringBuilder(expression.length() * 2);
		boolean next = false;
		for (final String part : parts) {
			if (next) {
				regex.append('|');
			}
			regex.append('(').append(toRegex(part)).append(')');
			next = true;
		}
		pattern = Pattern.compile(regex.toString());
	}

	private static CharSequence toRegex(final String expression) {
		final StringBuilder regex = new StringBuilder(expression.length() * 2);
		for (final char c : expression.toCharArray()) {
			switch (c) {
			case '?':
				regex.append(".");
				break;
			case '*':
				regex.append(".*");
				break;
			default:
				regex.append(Pattern.quote(String.valueOf(c)));
				break;
			}
		}
		return regex;
	}

	/**
	 * Matches the given string against the expressions of this matcher.
	 *
	 * @param s
	 *            string to test
	 * @return <code>true</code>, if the expression matches
	 */
	public boolean matches(final String s) {
		return pattern.matcher(s).matches();
	}

}
