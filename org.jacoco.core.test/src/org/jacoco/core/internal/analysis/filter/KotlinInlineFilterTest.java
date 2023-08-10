/*******************************************************************************
 * Copyright (c) 2009, 2023 Mountainminds GmbH & Co. KG and Contributors
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

import java.util.ArrayList;
import java.util.List;

import org.jacoco.core.internal.instr.InstrSupport;
import org.junit.Test;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodNode;

/**
 * Unit tests for {@link KotlinInlineFilter}.
 */
public class KotlinInlineFilterTest extends FilterTestBase {

	private final KotlinInlineFilter filter = new KotlinInlineFilter();

	private final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
			"callsite", "()V", null, null);

	@Test
	public void should_filter() {
		context.sourceFileName = "callsite.kt";
		context.sourceDebugExtension = "" //
				+ "SMAP\n" //
				+ "callsite.kt\n" // OutputFileName=callsite.kt
				+ "Kotlin\n" // DefaultStratumId=Kotlin
				+ "*S Kotlin\n" // StratumID=Kotlin
				+ "*F\n" // FileSection
				+ "+ 1 callsite.kt\n" // FileID=1,FileName=callsite.kt
				+ "CallsiteKt\n" //
				+ "+ 2 a.kt\n" // FileID=2,FileName=a.kt
				+ "AKt\n" //
				+ "+ 3 b.kt\n" // FileID=3,FileName=b.kt
				+ "BKt\n" //
				+ "*L\n" // LineSection
				+ "1#1,8:1\n" // InputStartLine=1,LineFileID=1,RepeatCount=8,OutputStartLine=1
				+ "2#2,2:9\n" // InputStartLine=2,LineFileID=2,RepeatCount=2,OutputStartLine=9
				+ "2#3,2:11\n" // InputStartLine=2,LineFileID=3,RepeatCount=2,OutputStartLine=11
				+ "*E\n"; // EndSection
		context.classAnnotations
				.add(KotlinGeneratedFilter.KOTLIN_METADATA_DESC);

		m.visitLineNumber(2, new Label());
		m.visitInsn(Opcodes.NOP);

		m.visitLineNumber(9, new Label());
		shouldIgnorePrevious(m);
		m.visitMethodInsn(Opcodes.INVOKESTATIC, "Stubs", "nop", "()V", false);
		shouldIgnorePrevious(m);
		m.visitLineNumber(10, new Label());
		shouldIgnorePrevious(m);
		m.visitInsn(Opcodes.NOP);
		shouldIgnorePrevious(m);

		m.visitLineNumber(3, new Label());
		m.visitInsn(Opcodes.NOP);

		m.visitLineNumber(11, new Label());
		shouldIgnorePrevious(m);
		m.visitMethodInsn(Opcodes.INVOKESTATIC, "Stubs", "nop", "()V", false);
		shouldIgnorePrevious(m);
		m.visitLineNumber(12, new Label());
		shouldIgnorePrevious(m);
		m.visitInsn(Opcodes.NOP);
		shouldIgnorePrevious(m);

		m.visitLineNumber(4, new Label());
		m.visitInsn(Opcodes.RETURN);

		filter.filter(m, context, output);

		assertIgnored(expectedRanges.toArray(new Range[0]));

		// should not reparse:
		context.sourceDebugExtension = "";
		filter.filter(m, context, output);
	}

	/**
	 * <pre>
	 *     inline fun inlined_top_level() {
	 *       Stubs.nop()
	 *     }
	 *
	 *     class Callsite {
	 *       fun inlined() {
	 *           Stubs.nop()
	 *       }
	 *
	 *       fun callsite {
	 *         inlined_top_level()
	 *         inlined()
	 *       }
	 *     }
	 * </pre>
	 */
	@Test
	public void should_filter_when_in_same_file() {
		context.sourceFileName = "example.kt";
		context.sourceDebugExtension = "" //
				+ "SMAP\n" //
				+ "example.kt\n" // OutputFileName=example.kt
				+ "Kotlin\n" // DefaultStratumId=Kotlin
				+ "*S Kotlin\n" // StratumID=Kotlin
				+ "*F\n" // FileSection
				+ "+ 1 example.kt\n" // FileID=1,FileName=example.kt
				+ "Callsite\n" //
				+ "+ 2 example.kt\n" // FileID=2,FileName=example.kt
				+ "ExampleKt\n" //
				+ "*L\n" // LineSection
				+ "1#1,15:1\n" // InputStartLine=1,LineFileID=1,RepeatCount=10,OutputStartLine=1
				+ "7#1,2:18\n" // InputStartLine=7,LineFileID=1,RepeatCount=2,OutputStartLine=18
				+ "2#2,2:16\n" // InputStartLine=2,LineFileID=2,RepeatCount=2,OutputStartLine=16
				+ "*E\n"; // EndSection
		context.classAnnotations
				.add(KotlinGeneratedFilter.KOTLIN_METADATA_DESC);

		m.visitLineNumber(11, new Label());
		m.visitInsn(Opcodes.NOP);
		m.visitLineNumber(16, new Label());
		shouldIgnorePrevious(m);
		m.visitMethodInsn(Opcodes.INVOKESTATIC, "Stubs", "nop", "()V", false);
		shouldIgnorePrevious(m);
		m.visitLineNumber(17, new Label());
		shouldIgnorePrevious(m);
		m.visitInsn(Opcodes.NOP);
		shouldIgnorePrevious(m);

		m.visitLineNumber(12, new Label());
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitVarInsn(Opcodes.ASTORE, 1);
		m.visitLineNumber(18, new Label());
		shouldIgnorePrevious(m);
		m.visitMethodInsn(Opcodes.INVOKESTATIC, "Stubs", "nop", "()V", false);
		shouldIgnorePrevious(m);
		m.visitLineNumber(19, new Label());
		shouldIgnorePrevious(m);
		m.visitInsn(Opcodes.NOP);
		shouldIgnorePrevious(m);

		m.visitLineNumber(13, new Label());
		m.visitInsn(Opcodes.RETURN);

		filter.filter(m, context, output);

		assertIgnored(expectedRanges.toArray(new Range[0]));
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
	public void should_filter_without_parsing_KotlinDebug_stratum() {
		context.sourceFileName = "Example.kt";
		context.sourceDebugExtension = "" //
				+ "SMAP\n" //
				+ "Example.kt\n" // OutputFileName=Example.kt
				+ "Kotlin\n" // DefaultStratumId=Kotlin
				+ "*S Kotlin\n" // StratumID=Kotlin
				+ "*F\n" // FileSection
				+ "+ 1 Example.kt\n" // FileID=1,FileName=Example.kt
				+ "ExampleKt\n" //
				+ "*L\n" // LineSection
				+ "1#1,3:1\n" // InputStartLine=1,LineFileID=1,RepeatCount=3,OutputStartLine=1
				+ "1#1:4\n" // InputStartLine=1,LineFileID=1,OutputStartLine=4
				+ "*S KotlinDebug\n"; // StratumID=KotlinDebug
		context.classAnnotations
				.add(KotlinGeneratedFilter.KOTLIN_METADATA_DESC);

		m.visitLineNumber(2, new Label());
		m.visitInsn(Opcodes.ICONST_0);
		m.visitVarInsn(Opcodes.ISTORE, 0);
		m.visitLineNumber(4, new Label());
		shouldIgnorePrevious(m);
		m.visitInsn(Opcodes.NOP);
		shouldIgnorePrevious(m);
		m.visitLineNumber(3, new Label());
		m.visitInsn(Opcodes.RETURN);

		filter.filter(m, context, output);

		assertIgnored(expectedRanges.toArray(new Range[0]));
	}

	@Test
	public void should_not_parse_SourceDebugExtension_attribute_when_no_kotlin_metadata_annotation() {
		context.sourceDebugExtension = "SMAP";

		m.visitLineNumber(1, new Label());
		m.visitInsn(Opcodes.RETURN);

		filter.filter(m, context, output);

		assertIgnored();
	}

	@Test
	public void should_not_filter_when_no_SourceDebugExtension_attribute() {
		context.classAnnotations
				.add(KotlinGeneratedFilter.KOTLIN_METADATA_DESC);

		m.visitLineNumber(1, new Label());
		m.visitInsn(Opcodes.RETURN);

		filter.filter(m, context, output);

		assertIgnored();
	}

	@Test
	public void should_throw_exception_when_SMAP_incomplete() {
		context.sourceDebugExtension = "" //
				+ "SMAP\n";
		context.classAnnotations
				.add(KotlinGeneratedFilter.KOTLIN_METADATA_DESC);

		try {
			filter.filter(m, context, output);
			fail("exception expected");
		} catch (final IllegalStateException e) {
			assertEquals("Unexpected SMAP line: null", e.getMessage());
		}
	}

	@Test
	public void should_throw_exception_when_unexpected_FileInfo() {
		context.sourceFileName = "callsite.kt";
		context.sourceDebugExtension = "" //
				+ "SMAP\n" //
				+ "callsite.kt\n" //
				+ "Kotlin\n" //
				+ "*S Kotlin\n" //
				+ "*F\n" //
				+ "xxx";
		context.classAnnotations
				.add(KotlinGeneratedFilter.KOTLIN_METADATA_DESC);

		try {
			filter.filter(m, context, output);
			fail("exception expected");
		} catch (final IllegalStateException e) {
			assertEquals("Unexpected SMAP line: xxx", e.getMessage());
		}
	}

	@Test
	public void should_throw_exception_when_no_SourceFileId_for_SourceFile() {
		context.sourceFileName = "example.kt";
		context.classAnnotations
				.add(KotlinGeneratedFilter.KOTLIN_METADATA_DESC);
		context.sourceDebugExtension = "" //
				+ "SMAP\n" //
				+ "example.kt\n" //
				+ "Kotlin\n" //
				+ "*S Kotlin\n" //
				+ "*F\n" //
				+ "+ 1 another.kt\n" //
				+ "AnotherKt\n" //
				+ "*L\n" //
				+ "*E\n";

		try {
			filter.filter(m, context, output);
			fail("exception expected");
		} catch (final IllegalStateException e) {
			assertEquals("Unexpected SMAP FileSection", e.getMessage());
		}
	}

	@Test
	public void should_throw_exception_when_unexpected_LineInfo() {
		context.sourceFileName = "callsite.kt";
		context.sourceDebugExtension = "" //
				+ "SMAP\n" //
				+ "callsite.kt\n" //
				+ "Kotlin\n" //
				+ "*S Kotlin\n" //
				+ "*F\n" //
				+ "+ 1 callsite.kt\n" //
				+ "Callsite\n" //
				+ "*L\n" //
				+ "xxx";
		context.classAnnotations
				.add(KotlinGeneratedFilter.KOTLIN_METADATA_DESC);

		try {
			filter.filter(m, context, output);
			fail("exception expected");
		} catch (final IllegalStateException e) {
			assertEquals("Unexpected SMAP line: xxx", e.getMessage());
		}
	}

	private final List<Range> expectedRanges = new ArrayList<Range>();

	private void shouldIgnorePrevious(final MethodNode m) {
		expectedRanges.add(
				new Range(m.instructions.getLast(), m.instructions.getLast()));
	}

}
