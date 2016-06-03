/*******************************************************************************
 * Copyright (c) 2009, 2017 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.report.internal.html.table;

/**
 * Very simple stateful utility to parse through a given string.
 */
class StringParser {

	private final String input;
	private int pos;

	StringParser(final String input) {
		this.input = input;
		this.pos = 0;
	}

	boolean hasNext() {
		return input.length() > pos;
	}

	boolean isNext(final char c) {
		if (input.length() > pos && input.charAt(pos) == c) {
			pos++;
			return true;
		}
		return false;
	}

	char getNext() {
		if (input.length() > pos) {
			return input.charAt(pos++);
		}
		throw new IllegalArgumentException(
				"Unexpected end of table definition: " + input);
	}

	void expectNext(final char expected) {
		final char c = getNext();
		if (c != expected) {
			throw new IllegalArgumentException(
					"Unexpected character in table definition: " + input);
		}
	}

	String read(final char limiter) {
		final StringBuilder buffer = new StringBuilder();
		char c;
		while ((c = getNext()) != limiter) {
			buffer.append(c);
		}
		return buffer.toString();
	}
}
