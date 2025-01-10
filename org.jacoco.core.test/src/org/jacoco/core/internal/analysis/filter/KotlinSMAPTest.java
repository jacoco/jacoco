/*******************************************************************************
 * Copyright (c) 2009, 2025 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.internal.analysis.filter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

/**
 * Unit test for {@link KotlinSMAP}.
 */
public class KotlinSMAPTest {

	/**
	 * <pre>
	 * // A.kt
	 * package a
	 * inline fun f() {} // line 3
	 * </pre>
	 *
	 * <pre>
	 * // B.kt
	 * package b
	 * fun callsite() = f() // line 3
	 * </pre>
	 */
	@Test
	public void should_parse() {
		final KotlinSMAP smap = new KotlinSMAP("B.kt", "SMAP\n" //
				+ "B.kt\n" // OutputFileName=B.kt
				+ "Kotlin\n" // DefaultStratumId=Kotlin
				+ "*S Kotlin\n" // StratumID=Kotlin
				+ "*F\n" // FileSection
				+ "+ 1 B.kt\n" // FileID=1,FileName=B.kt
				+ "b/BKt\n" // ClassName=b/BKt
				+ "+ 2 A.kt\n" // FileID=2,FileName=A.kt
				+ "a/AKt\n" // ClassName=a/AKt
				+ "*L\n" // LineSection
				+ "1#1,4:1\n" // InputStartLine=1,LineFileID=1,RepeatCount=4,OutputStartLine=1
				+ "3#2:5\n" // InputStartLine=3,LineFileID=2,RepeatCount=,OutputStartLine=5
				+ "*E\n"); // EndSection
		assertEquals(2, smap.mappings().size());
		KotlinSMAP.Mapping mapping = smap.mappings().get(0);
		assertEquals("b/BKt", mapping.inputClassName());
		assertEquals(1, mapping.inputStartLine());
		assertEquals(4, mapping.repeatCount());
		assertEquals(1, mapping.outputStartLine());
		mapping = smap.mappings().get(1);
		assertEquals("a/AKt", mapping.inputClassName());
		assertEquals(3, mapping.inputStartLine());
		assertEquals(1, mapping.repeatCount());
		assertEquals(5, mapping.outputStartLine());
	}

	/**
	 * See <a href="https://youtrack.jetbrains.com/issue/KT-37704">KT-37704</a>
	 *
	 * <pre>
	 * inline fun f() {}
	 * fun g() = f()
	 * </pre>
	 */
	@Test
	public void should_stop_parsing_at_KotlinDebug_stratum() {
		final KotlinSMAP smap = new KotlinSMAP("Example.kt", "SMAP\n" //
				+ "Example.kt\n" // OutputFileName=Example.kt
				+ "Kotlin\n" // DefaultStratumId=Kotlin
				+ "*S Kotlin\n" // StratumID=Kotlin
				+ "*F\n" // FileSection
				+ "+ 1 Example.kt\n" // FileID=1,FileName=Example.kt
				+ "ExampleKt\n" //
				+ "*L\n" // LineSection
				+ "1#1,3:1\n" // InputStartLine=1,LineFileID=1,RepeatCount=3,OutputStartLine=1
				+ "1#1:4\n" // InputStartLine=1,LineFileID=1,OutputStartLine=4
				+ "*S KotlinDebug\n" // StratumID=KotlinDebug
				+ "xxx");
		assertEquals(2, smap.mappings().size());
		KotlinSMAP.Mapping mapping = smap.mappings().get(0);
		assertEquals("ExampleKt", mapping.inputClassName());
		assertEquals(1, mapping.inputStartLine());
		assertEquals(3, mapping.repeatCount());
		assertEquals(1, mapping.outputStartLine());
		mapping = smap.mappings().get(1);
		assertEquals("ExampleKt", mapping.inputClassName());
		assertEquals(1, mapping.inputStartLine());
		assertEquals(1, mapping.repeatCount());
		assertEquals(4, mapping.outputStartLine());
	}

	@Test
	public void should_throw_exception_when_not_an_SMAP_Header() {
		try {
			new KotlinSMAP("", "xxx");
			fail("exception expected");
		} catch (final IllegalStateException e) {
			assertEquals("Unexpected SMAP line: xxx", e.getMessage());
		}
	}

	@Test
	public void should_throw_exception_when_OutputFileName_does_not_match_SourceFileName() {
		try {
			new KotlinSMAP("", "SMAP\n" //
					+ "Example.kt\n");
			fail("exception expected");
		} catch (final IllegalStateException e) {
			assertEquals("Unexpected SMAP line: Example.kt", e.getMessage());
		}
	}

	@Test
	public void should_throw_exception_when_DefaultStratumId_is_not_Kotlin() {
		try {
			new KotlinSMAP("Servlet.java", "SMAP\n" //
					+ "Servlet.java\n" // OutputFileName=Servlet.java
					+ "JSP\n"); // DefaultStratumId=JSP
			fail("exception expected");
		} catch (final IllegalStateException e) {
			assertEquals("Unexpected SMAP line: JSP", e.getMessage());
		}
	}

	@Test
	public void should_throw_exception_when_first_StratumId_is_not_Kotlin() {
		try {
			new KotlinSMAP("Example.kt", "SMAP\n" //
					+ "Example.kt\n" // OutputFileName=Example.kt
					+ "Kotlin\n" // DefaultStratumId=Kotlin
					+ "*S KotlinDebug\n"); // StratumID=KotlinDebug
			fail("exception expected");
		} catch (final IllegalStateException e) {
			assertEquals("Unexpected SMAP line: *S KotlinDebug",
					e.getMessage());
		}
	}

	@Test
	public void should_throw_exception_when_StratumSection_does_not_start_with_FileSection() {
		try {
			new KotlinSMAP("Example.kt", "SMAP\n" //
					+ "Example.kt\n" //
					+ "Kotlin\n" //
					+ "*S Kotlin\n" //
					+ "xxx"); //
			fail("exception expected");
		} catch (final IllegalStateException e) {
			assertEquals("Unexpected SMAP line: xxx", e.getMessage());
		}
	}

	@Test
	public void should_throw_exception_when_FileSection_contains_unexpected_FileInfo() {
		try {
			new KotlinSMAP("Example.kt", "SMAP\n" //
					+ "Example.kt\n" //
					+ "Kotlin\n" //
					+ "*S Kotlin\n" //
					+ "*F\n" //
					+ "xxx"); //
			fail("exception expected");
		} catch (final IllegalStateException e) {
			assertEquals("Unexpected SMAP line: xxx", e.getMessage());
		}
	}

	@Test
	public void should_throw_exception_when_LineSection_contains_unexpected_LineInfo() {
		try {
			new KotlinSMAP("Example.kt", "SMAP\n" //
					+ "Example.kt\n" //
					+ "Kotlin\n" //
					+ "*S Kotlin\n" //
					+ "*F\n" //
					+ "*L\n" //
					+ "xxx"); //
			fail("exception expected");
		} catch (final IllegalStateException e) {
			assertEquals("Unexpected SMAP line: xxx", e.getMessage());
		}
	}

	@Test
	public void should_throw_exception_when_LineInfo_does_not_have_FileID() {
		try {
			new KotlinSMAP("Example.kt", "SMAP\n" //
					+ "Example.kt\n" //
					+ "Kotlin\n" //
					+ "*S Kotlin\n" //
					+ "*F\n" //
					+ "*L\n" //
					+ "1:1\n"); // InputStartLine=1,OutputStartLine=1
			fail("exception expected");
		} catch (final NullPointerException e) {
			// expected
		}
	}

}
