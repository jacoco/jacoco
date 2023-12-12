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

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Test;

/**
 * Unit tests for {@link CommandLineSupport}.
 */
public class CommandLineSupportTest {

	@Test
	public void testQuote1() {
		assertEquals("aBc", CommandLineSupport.quote("aBc"));
	}

	@Test
	public void testQuote2() {
		assertEquals("\"a c\"", CommandLineSupport.quote("a c"));
	}

	@Test
	public void testQuote3() {
		assertEquals("\"a\\\"c\"", CommandLineSupport.quote("a\"c"));
	}

	@Test
	public void testQuote4() {
		assertEquals("\" xy \"", CommandLineSupport.quote(" xy "));
	}

	@Test
	public void testQuote5() {
		assertEquals("a\\\\b", CommandLineSupport.quote("a\\b"));
	}

	@Test
	public void testQuoteList1() {
		assertEquals("", CommandLineSupport.quote(Arrays.<String> asList()));
	}

	@Test
	public void testQuoteList2() {
		assertEquals("a", CommandLineSupport.quote(Arrays.asList("a")));
	}

	@Test
	public void testQuoteList3() {
		assertEquals("a b c",
				CommandLineSupport.quote(Arrays.asList("a", "b", "c")));
	}

	@Test
	public void testQuoteList4() {
		assertEquals("a \"b b\" c",
				CommandLineSupport.quote(Arrays.asList("a", "b b", "c")));
	}

	@Test
	public void testSplit1() {
		assertEquals(Arrays.asList(), CommandLineSupport.split(null));
	}

	@Test
	public void testSplit2() {
		assertEquals(Arrays.asList(), CommandLineSupport.split(""));
	}

	@Test
	public void testSplit3() {
		assertEquals(Arrays.asList("abc"), CommandLineSupport.split("abc"));
	}

	@Test
	public void testSplit4() {
		assertEquals(Arrays.asList("aa", "bbbb", "cccccc"),
				CommandLineSupport.split("  aa  bbbb  cccccc   "));
	}

	@Test
	public void testSplit5() {
		assertEquals(Arrays.asList("a a", "b b "),
				CommandLineSupport.split("\"a a\" \"b b \" "));
	}

	@Test
	public void testSplit6() {
		assertEquals(Arrays.asList("a\"c"), CommandLineSupport.split("a\\\"c"));
	}

	@Test
	public void testSplit7() {
		assertEquals(Arrays.asList("a\\c"), CommandLineSupport.split("a\\c"));
	}

	@Test
	public void testSplit8() {
		assertEquals(Arrays.asList("a\\"), CommandLineSupport.split("a\\"));
	}

	@Test
	public void testSplit9() {
		assertEquals(Arrays.asList("a\\", "b"),
				CommandLineSupport.split("a\\ b"));
	}

	@Test
	public void testSplit10() {
		assertEquals(Arrays.asList("a\\b"), CommandLineSupport.split("a\\\\b"));
	}

}
