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

import java.util.ArrayList;
import java.util.List;

/**
 * Internal utility to parse and create command lines arguments.
 */
final class CommandLineSupport {

	private static final char BLANK = ' ';
	private static final char QUOTE = '"';
	private static final char SLASH = '\\';

	/**
	 * Quotes a single command line argument if necessary.
	 *
	 * @param arg
	 *            command line argument
	 * @return quoted argument
	 */
	static String quote(final String arg) {
		final StringBuilder escaped = new StringBuilder();
		for (final char c : arg.toCharArray()) {
			if (c == QUOTE || c == SLASH) {
				escaped.append(SLASH);
			}
			escaped.append(c);
		}
		if (arg.indexOf(BLANK) != -1 || arg.indexOf(QUOTE) != -1) {
			escaped.insert(0, QUOTE).append(QUOTE);
		}
		return escaped.toString();
	}

	/**
	 * Builds a single command line string from the given argument list.
	 * Arguments are quoted when necessary.
	 *
	 * @param args
	 *            command line arguments
	 * @return combined command line
	 */
	static String quote(final List<String> args) {
		final StringBuilder result = new StringBuilder();
		boolean separate = false;
		for (final String arg : args) {
			if (separate) {
				result.append(BLANK);
			}
			result.append(quote(arg));
			separate = true;
		}
		return result.toString();
	}

	/**
	 * Splits a command line into single arguments and removes quotes if
	 * present.
	 *
	 * @param commandline
	 *            combined command line
	 * @return list of arguments
	 */
	static List<String> split(final String commandline) {
		if (commandline == null || commandline.length() == 0) {
			return new ArrayList<String>();
		}
		final List<String> args = new ArrayList<String>();
		final StringBuilder current = new StringBuilder();
		int mode = M_STRIP_WHITESPACE;
		int endChar = BLANK;
		for (final char c : commandline.toCharArray()) {
			switch (mode) {
			case M_STRIP_WHITESPACE:
				if (!Character.isWhitespace(c)) {
					if (c == QUOTE) {
						endChar = QUOTE;
					} else {
						current.append(c);
						endChar = BLANK;
					}
					mode = M_PARSE_ARGUMENT;
				}
				break;
			case M_PARSE_ARGUMENT:
				if (c == endChar) {
					addArgument(args, current);
					mode = M_STRIP_WHITESPACE;
				} else if (c == SLASH) {
					current.append(SLASH);
					mode = M_ESCAPED;
				} else {
					current.append(c);
				}
				break;
			case M_ESCAPED:
				if (c == QUOTE || c == SLASH) {
					current.setCharAt(current.length() - 1, c);
				} else if (c == endChar) {
					addArgument(args, current);
					mode = M_STRIP_WHITESPACE;
				} else {
					current.append(c);
				}
				mode = M_PARSE_ARGUMENT;
				break;
			}
		}
		addArgument(args, current);
		return args;
	}

	private static void addArgument(final List<String> args,
			final StringBuilder current) {
		if (current.length() > 0) {
			args.add(current.toString());
			current.setLength(0);
		}
	}

	private static final int M_STRIP_WHITESPACE = 0;
	private static final int M_PARSE_ARGUMENT = 1;
	private static final int M_ESCAPED = 2;

	private CommandLineSupport() {
		// no instances
	}
}
