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
package org.jacoco.examples;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

import org.hamcrest.Matcher;
import org.junit.rules.ExternalResource;

/**
 * In-Memory buffer to assert console output.
 */
public class ConsoleOutput extends ExternalResource {

	private final ByteArrayOutputStream buffer;

	public final PrintStream stream;

	public ConsoleOutput() {
		this.buffer = new ByteArrayOutputStream();
		try {
			this.stream = new PrintStream(buffer, true, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new AssertionError(e.getMessage());
		}
	}

	@Override
	protected void after() {
		buffer.reset();
	}

	public static Matcher<String> containsLine(String line) {
		return containsString(String.format("%s%n", line));
	}

	public static Matcher<String> isEmpty() {
		return is("");
	}

	public String getContents() {
		try {
			return new String(buffer.toByteArray(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			fail(e.getMessage());
			return "";
		}
	}

	public void expect(Matcher<String> matcher) {
		assertThat(getContents(), matcher);
	}

}
