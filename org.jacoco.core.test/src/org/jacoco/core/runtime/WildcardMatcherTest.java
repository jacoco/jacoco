/*******************************************************************************
 * Copyright (c) 2009, 2015 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
	public void testEmpty() {
		assertTrue(new WildcardMatcher("").matches(""));
		assertFalse(new WildcardMatcher("").matches("abc"));
	}

	@Test
	public void testExact() {
		assertTrue(new WildcardMatcher("abc/def.txt").matches("abc/def.txt"));
	}

	@Test
	public void testCaseSensitive() {
		assertFalse(new WildcardMatcher("abcdef").matches("abcDef"));
		assertFalse(new WildcardMatcher("ABCDEF").matches("AbCDEF"));
	}

	@Test
	public void testQuote() {
		assertFalse(new WildcardMatcher("rst.xyz").matches("rstAxyz"));
		assertTrue(new WildcardMatcher("(x)+").matches("(x)+"));
	}

	@Test
	public void testWildcards() {
		assertTrue(new WildcardMatcher("*").matches(""));
		assertTrue(new WildcardMatcher("*").matches("java/lang/Object"));
		assertTrue(new WildcardMatcher("*Test").matches("jacoco/MatcherTest"));
		assertTrue(new WildcardMatcher("Matcher*").matches("Matcher"));
		assertTrue(new WildcardMatcher("Matcher*").matches("MatcherTest"));
		assertTrue(new WildcardMatcher("a*b*a").matches("a-b-b-a"));
		assertFalse(new WildcardMatcher("a*b*a").matches("alaska"));
		assertTrue(new WildcardMatcher("Hello?orld").matches("HelloWorld"));
		assertFalse(new WildcardMatcher("Hello?orld").matches("HelloWWWorld"));
		assertTrue(new WildcardMatcher("?aco*").matches("jacoco"));
	}

	@Test
	public void testMultiExpression() {
		assertTrue(new WildcardMatcher("Hello:World").matches("World"));
		assertTrue(new WildcardMatcher("Hello:World").matches("World"));
		assertTrue(new WildcardMatcher("*Test:*Foo").matches("UnitTest"));
	}

	@Test
	public void testDollar() {
		assertTrue(new WildcardMatcher("*$*").matches("java/util/Map$Entry"));
		assertTrue(new WildcardMatcher("*$$$*")
				.matches("org/example/Enity$$$generated123"));
	}

}
