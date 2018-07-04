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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class StringParserTest {

	private StringParser parser;

	@Test
	public void hasNext_should_return_true_when_characters_available() {
		parser = new StringParser("abc");
		assertTrue(parser.hasNext());
	}

	@Test
	public void hasNext_should_return_false_when_no_characters_available() {
		parser = new StringParser("");
		assertFalse(parser.hasNext());
	}

	@Test
	public void isNext_should_return_true_and_consume_character_when_match() {
		parser = new StringParser("x");
		assertTrue(parser.isNext('x'));
		assertFalse(parser.hasNext());
	}

	@Test
	public void isNext_should_return_false_and_leave_character_when_no_match() {
		parser = new StringParser("x");
		assertFalse(parser.isNext('y'));
		assertTrue(parser.hasNext());
	}

	@Test
	public void isNext_should_return_false_when_no_characters_available() {
		parser = new StringParser("");
		assertFalse(parser.isNext('x'));
	}

	@Test
	public void getNext_should_return_and_consume_character_when_characters_available() {
		parser = new StringParser("abc");
		assertEquals('a', parser.getNext());
		assertEquals('b', parser.getNext());
		assertEquals('c', parser.getNext());
	}

	@Test(expected = IllegalArgumentException.class)
	public void getNext_should_throw_exception_when_no_characters_available() {
		parser = new StringParser("");
		parser.getNext();
	}

	@Test
	public void expectNext_should_return_without_exception_when_expected_character_is_present() {
		parser = new StringParser("?");
		parser.expectNext('?');
	}

	@Test(expected = IllegalArgumentException.class)
	public void expectNext_should_throw_exception_when_expected_character_is_not_present() {
		parser = new StringParser("?");
		parser.expectNext('!');
	}

	@Test
	public void read_should_return_all_characters_upto_limiter() {
		parser = new StringParser("abc>x");
		assertEquals("abc", parser.read('>'));
		assertEquals('x', parser.getNext());
	}

	@Test
	public void read_should_return_empty_string_when_limiter_follows_directly() {
		parser = new StringParser(">x");
		assertEquals("", parser.read('>'));
		assertEquals('x', parser.getNext());
	}

	@Test(expected = IllegalArgumentException.class)
	public void read_should_throw_exception_when_limiter_is_missing() {
		parser = new StringParser("abc");
		parser.read('>');
	}

}
