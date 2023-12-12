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
package org.jacoco.cli.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import org.junit.Before;

/**
 * Base class for command tests.
 */
public abstract class CommandTestBase {

	protected StringWriter out;
	protected StringWriter err;
	protected int result;

	@Before
	public void before() {
		out = new StringWriter();
		err = new StringWriter();
	}

	protected int execute(String... args) throws Exception {
		result = new Main(args).execute(new PrintWriter(out),
				new PrintWriter(err));
		return result;
	}

	protected void assertOk() {
		assertEquals(err.toString(), 0, result);
	}

	protected void assertFailure() {
		assertEquals(-1, result);
	}

	protected void assertNoOutput(StringWriter buffer) {
		assertEquals("", buffer.toString());
	}

	protected void assertContains(String expected, StringWriter buffer) {
		final String content = buffer.toString();
		assertTrue(content, content.contains(expected));
	}

	protected void assertContainsNot(String expected, StringWriter buffer) {
		final String content = buffer.toString();
		assertFalse(content, content.contains(expected));
	}

	protected String getClassPath() {
		final String name = getClass().getName();
		final String res = "/" + name.replace('.', '/') + ".class";
		String loc = getClass().getResource(res).getFile();
		try {
			loc = URLDecoder.decode(loc, "UTF-8");
		} catch (UnsupportedEncodingException e) {
		}
		return loc.substring(0, loc.length() - res.length());
	}

}
