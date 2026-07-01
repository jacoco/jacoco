/*******************************************************************************
 * Copyright (c) 2009, 2026 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.test.snapshot;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.io.StringReader;

import org.junit.Test;

/**
 * Unit test for {@link MethodSnapshotParser.Lexer}
 */
public class MethodSnapshotLexerTest {

	@Test
	public void whitespace() throws Exception {
		final MethodSnapshotParser.Lexer lexer = newLexer(" \r\n");
		assertNull(lexer.nextToken());
	}

	@Test
	public void comment() throws Exception {
		final MethodSnapshotParser.Lexer lexer = newLexer(
				"// comment 1\n// comment 2");
		assertEquals("// comment 1", lexer.nextToken());
		assertEquals("// comment 2", lexer.nextToken());
		assertNull(lexer.nextToken());
	}

	/**
	 * @see org.objectweb.asm.util.Printer#appendString(StringBuilder, String)
	 */
	@Test
	public void string() throws Exception {
		final MethodSnapshotParser.Lexer lexer = newLexer(
				MethodSnapshotParserTest.text( //
						" \"\" ", //
						" \"\\r\\n\" ", //
						" \"\\uF000\" ", //
						" \"\\\"\" ", //
						" \"\\\\\" "));
		assertEquals("\"\"", lexer.nextToken());
		assertEquals("CRLF", "\"\r\n\"", lexer.nextToken());
		assertEquals("unicode", "\"\uF000\"", lexer.nextToken());
		assertEquals("double quote", "\"\"\"", lexer.nextToken());
		assertEquals("backslash", "\"\\\"", lexer.nextToken());
		assertNull(lexer.nextToken());

		try {
			newLexer("\" \\ \"").nextToken();
			fail("expected IllegalStateException");
		} catch (final IllegalStateException e) {
			assertEquals("Invalid escape in string token", e.getMessage());
		}
		try {
			newLexer("\"").nextToken();
			fail("expected IllegalStateException");
		} catch (final IllegalStateException e) {
			assertEquals("Unterminated string token", e.getMessage());
		}
	}

	/** For example in arguments of INVOKEDYNAMIC */
	@Test
	public void string_followed_by_comma() throws Exception {
		final MethodSnapshotParser.Lexer lexer = newLexer("\"\",");
		assertEquals("\"\"", lexer.nextToken());
		assertNull(lexer.nextToken());

		try {
			newLexer("\"\"s").nextToken();
			fail("expected IllegalStateException");
		} catch (final IllegalStateException e) {
			assertEquals("Improperly terminated string token", e.getMessage());
		}
	}

	@Test
	public void word() throws Exception {
		final MethodSnapshotParser.Lexer lexer = newLexer("word1,word2 word3");
		assertEquals("word1", lexer.nextToken());
		assertEquals("word2", lexer.nextToken());
		assertEquals("word3", lexer.nextToken());
		assertNull(lexer.nextToken());
	}

	private static MethodSnapshotParser.Lexer newLexer(final String input) {
		return new MethodSnapshotParser.Lexer(new StringReader(input));
	}

}
