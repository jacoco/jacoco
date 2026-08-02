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
package org.jacoco.core.internal.analysis.filter;

import java.util.ArrayList;
import java.util.List;

import org.jacoco.core.internal.instr.InstrSupport;
import org.jacoco.core.test.TextBlock;
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
		context.kotlinSMAP = new KotlinSMAP("callsite.kt", TextBlock.lines( //
				"SMAP", //
				"callsite.kt", // OutputFileName=callsite.kt
				"Kotlin", // DefaultStratumId=Kotlin
				"*S Kotlin", // StratumID=Kotlin
				"*F", // FileSection
				"+ 1 callsite.kt", // FileID=1,FileName=callsite.kt
				"CallsiteKt", //
				"+ 2 a.kt", // FileID=2,FileName=a.kt
				"AKt", //
				"+ 3 b.kt", // FileID=3,FileName=b.kt
				"BKt", //
				"*L", // LineSection
				"1#1,8:1", // InputStartLine=1,LineFileID=1,RepeatCount=8,OutputStartLine=1
				"2#2,2:9", // InputStartLine=2,LineFileID=2,RepeatCount=2,OutputStartLine=9
				"2#3,2:11", // InputStartLine=2,LineFileID=3,RepeatCount=2,OutputStartLine=11
				"*E")); // EndSection

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

		assertIgnored(m, expectedRanges.toArray(new Range[0]));

		// should not re-process SMAP:
		context.kotlinSMAP = null;
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
		context.kotlinSMAP = new KotlinSMAP("example.kt", TextBlock.lines( //
				"SMAP", //
				"example.kt", // OutputFileName=example.kt
				"Kotlin", // DefaultStratumId=Kotlin
				"*S Kotlin", // StratumID=Kotlin
				"*F", // FileSection
				"+ 1 example.kt", // FileID=1,FileName=example.kt
				"Callsite", //
				"+ 2 example.kt", // FileID=2,FileName=example.kt
				"ExampleKt", //
				"*L", // LineSection
				"1#1,15:1", // InputStartLine=1,LineFileID=1,RepeatCount=10,OutputStartLine=1
				"7#1,2:18", // InputStartLine=7,LineFileID=1,RepeatCount=2,OutputStartLine=18
				"2#2,2:16", // InputStartLine=2,LineFileID=2,RepeatCount=2,OutputStartLine=16
				"*E")); // EndSection

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

		assertIgnored(m, expectedRanges.toArray(new Range[0]));
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
		context.kotlinSMAP = new KotlinSMAP("Example.kt", TextBlock.lines( //
				"SMAP", //
				"Example.kt", // OutputFileName=Example.kt
				"Kotlin", // DefaultStratumId=Kotlin
				"*S Kotlin", // StratumID=Kotlin
				"*F", // FileSection
				"+ 1 Example.kt", // FileID=1,FileName=Example.kt
				"ExampleKt", //
				"+ 2 Example.kt", // FileID=2,FileName=Example.kt
				"a/ExampleKt", //
				"*L", // LineSection
				"1#1,6:1", // InputStartLine=1,LineFileID=1,RepeatCount=6,OutputStartLine=1
				"7#2:7", // InputStartLine=7,LineFileID=2,OutputStartLine=7
				"*S KotlinDebug")); // StratumID=KotlinDebug

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

		assertIgnored(m, expectedRanges.toArray(new Range[0]));
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
		context.kotlinSMAP = new KotlinSMAP("Example.kt", TextBlock.lines(//
				"SMAP", //
				"Example.kt", // OutputFileName=Example.kt
				"Kotlin", // DefaultStratumId=Kotlin
				"*S Kotlin", // StratumID=Kotlin
				"*F", // FileSection
				"+ 1 Example.kt", // FileID=1
				"ExampleKt$example$1", //
				"+ 2 Example.kt", // FileID=2
				"ExampleKt", //
				"*L", // LineSection
				"1#1,11:1", // InputStartLine=1,LineFileID=1,RepeatCount=11,OutputStartLine=1
				"9#2:12", // InputStartLine=9,LineFileID=2,OutputStartLine=12
				"*E")); // EndSection

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

		assertIgnored(m, expectedRanges.toArray(new Range[0]));
	}

	@Test
	public void should_not_filter_when_no_SourceDebugExtension_attribute() {
		m.visitLineNumber(1, new Label());
		m.visitInsn(Opcodes.RETURN);

		filter.filter(m, context, output);

		assertIgnored(m);
	}

	private final List<Range> expectedRanges = new ArrayList<Range>();

	private void shouldIgnorePrevious(final MethodNode m) {
		expectedRanges.add(
				new Range(m.instructions.getLast(), m.instructions.getLast()));
	}

}
