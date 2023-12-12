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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class WildcardMatcherTest {

	@Test
	public void empty_expression_should_match_any_string() {
		assertTrue(new WildcardMatcher("").matches(""));
		assertFalse(new WildcardMatcher("").matches("abc"));
	}

	@Test
	public void expressions_without_wildcards_should_match_exactly() {
		assertTrue(new WildcardMatcher("abc/def.txt").matches("abc/def.txt"));
		assertFalse(new WildcardMatcher("abc/def.txt").matches("/abc/def.txt"));
	}

	@Test
	public void should_match_case_sensitive() {
		assertFalse(new WildcardMatcher("abcdef").matches("abcDef"));
		assertFalse(new WildcardMatcher("ABCDEF").matches("AbCDEF"));
	}

	@Test
	public void should_not_use_regex_characters() {
		assertFalse(new WildcardMatcher("rst.xyz").matches("rstAxyz"));
		assertTrue(new WildcardMatcher("(x)+").matches("(x)+"));
	}

	@Test
	public void asterix_should_match_any_number_of_any_character() {
		assertTrue(new WildcardMatcher("*").matches(""));
		assertTrue(new WildcardMatcher("*").matches("java/lang/Object"));
		assertTrue(new WildcardMatcher("*Test").matches("jacoco/MatcherTest"));
		assertTrue(new WildcardMatcher("Matcher*").matches("Matcher"));
		assertTrue(new WildcardMatcher("Matcher*").matches("MatcherTest"));
		assertTrue(new WildcardMatcher("a*b*a").matches("a-b-b-a"));
		assertFalse(new WildcardMatcher("a*b*a").matches("alaska"));
	}

	@Test
	public void questionmark_should_match_any_single_character() {
		assertTrue(new WildcardMatcher("Hello?orld").matches("HelloWorld"));
		assertFalse(new WildcardMatcher("Hello?orld").matches("Helloorld"));
		assertFalse(new WildcardMatcher("Hello?orld").matches("HelloWWWorld"));
	}

	@Test
	public void should_match_any_expression_when_multiple_expressions_are_given() {
		assertTrue(new WildcardMatcher("Hello:World").matches("World"));
		assertTrue(new WildcardMatcher("*Test:*Foo").matches("UnitTest"));
		assertFalse(new WildcardMatcher("foo:bar").matches("foo:bar"));
	}

	@Test
	public void should_match_dollar_sign() {
		assertTrue(new WildcardMatcher("*$*").matches("java/util/Map$Entry"));
		assertTrue(new WildcardMatcher("*$$$*")
				.matches("org/example/Enity$$$generated123"));
	}

}
