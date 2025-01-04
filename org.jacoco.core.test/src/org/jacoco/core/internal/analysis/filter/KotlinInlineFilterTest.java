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

import java.util.ArrayList;
import java.util.List;

import org.jacoco.core.internal.instr.InstrSupport;
import org.junit.Test;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
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
		context.className = "CallsiteKt";
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
		context.className = "Callsite";
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
	 * <pre>
	 * package a;
	 *
	 * inline fun testInline() {} // line 7
	 * </pre>
	 *
	 * <pre>
	 * import a.testInline
	 *
	 * fun main() {
	 *   testInline() // line 4
	 * }
	 * </pre>
	 */
	@Test
	public void should_filter_when_inlined_with_same_file_name_and_line_number() {
		context.className = "ExampleKt";
		context.sourceFileName = "Example.kt";
		context.sourceDebugExtension = "" //
				+ "SMAP\n" //
				+ "Example.kt\n" // OutputFileName=Example.kt
				+ "Kotlin\n" // DefaultStratumId=Kotlin
				+ "*S Kotlin\n" // StratumID=Kotlin
				+ "*F\n" // FileSection
				+ "+ 1 Example.kt\n" // FileID=1,FileName=Example.kt
				+ "ExampleKt\n" //
				+ "+ 2 Example.kt\n" // FileID=2,FileName=Example.kt
				+ "a/ExampleKt\n" //
				+ "*L\n" // LineSection
				+ "1#1,6:1\n" // InputStartLine=1,LineFileID=1,RepeatCount=6,OutputStartLine=1
				+ "7#2:7\n" // InputStartLine=7,LineFileID=2,OutputStartLine=7
				+ "*S KotlinDebug"; // StratumID=KotlinDebug
		context.classAnnotations
				.add(KotlinGeneratedFilter.KOTLIN_METADATA_DESC);

		Label label0 = new Label();
		m.visitLabel(label0);
		m.visitLineNumber(4, label0);
		m.visitInsn(Opcodes.ICONST_0);
		m.visitVarInsn(Opcodes.ISTORE, 0);
		Label label1 = new Label();
		m.visitLabel(label1);
		m.visitLineNumber(7, label1);
		shouldIgnorePrevious(m);
		m.visitInsn(Opcodes.NOP);
		shouldIgnorePrevious(m);
		Label label2 = new Label();
		m.visitLabel(label2);
		shouldIgnorePrevious(m);
		m.visitLineNumber(5, label2);
		m.visitInsn(Opcodes.RETURN);

		filter.filter(m, context, output);

		assertIgnored(expectedRanges.toArray(new Range[0]));
	}

	/**
	 * <pre>
	 * inline fun example(crossinline lambda: () -> Unit): () -> Unit {
	 *   return {
	 *     lambda()
	 *   }
	 * }
	 *
	 * fun callsite() {
	 *   example {
	 *   }()
	 * }
	 * </pre>
	 */
	@Test
	public void should_filter_all_lines() {
		context.className = "ExampleKt$callsite$$inlined$example$1";
		context.sourceFileName = "Example.kt";
		context.sourceDebugExtension = "" //
				+ "SMAP\n" //
				+ "Example.kt\n" // OutputFileName=Example.kt
				+ "Kotlin\n" // DefaultStratumId=Kotlin
				+ "*S Kotlin\n" // StratumID=Kotlin
				+ "*F\n" // FileSection
				+ "+ 1 Example.kt\n" // FileID=1
				+ "ExampleKt$example$1\n" //
				+ "+ 2 Example.kt\n" // FileID=2
				+ "ExampleKt\n" //
				+ "*L\n" // LineSection
				+ "1#1,11:1\n" // InputStartLine=1,LineFileID=1,RepeatCount=11,OutputStartLine=1
				+ "9#2:12\n" // InputStartLine=9,LineFileID=2,OutputStartLine=12
				+ "*E\n"; // EndSection
		context.classAnnotations
				.add(KotlinGeneratedFilter.KOTLIN_METADATA_DESC);

		Label label0 = new Label();
		m.visitLabel(label0);
		m.visitLineNumber(3, label0);
		m.visitInsn(Opcodes.ICONST_0);
		m.visitVarInsn(Opcodes.ISTORE, 1);
		Label label1 = new Label();
		m.visitLabel(label1);
		m.visitLineNumber(12, label1);
		m.visitInsn(Opcodes.NOP);
		Label label2 = new Label();
		m.visitLabel(label2);
		m.visitLineNumber(3, label2);
		m.visitInsn(Opcodes.NOP);
		Label label3 = new Label();
		m.visitLabel(label3);
		m.visitLineNumber(4, label3);
		m.visitInsn(Opcodes.RETURN);

		for (AbstractInsnNode i = m.instructions.getFirst()
				.getNext(); i != null; i = i.getNext()) {
			expectedRanges.add(new Range(i, i));
		}

		filter.filter(m, context, output);

		assertIgnored(expectedRanges.toArray(new Range[0]));
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

	private final List<Range> expectedRanges = new ArrayList<Range>();

	private void shouldIgnorePrevious(final MethodNode m) {
		expectedRanges.add(
				new Range(m.instructions.getLast(), m.instructions.getLast()));
	}

}
