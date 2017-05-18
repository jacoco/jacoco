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
package org.jacoco.cli.internal;

import static org.junit.Assert.assertEquals;
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
