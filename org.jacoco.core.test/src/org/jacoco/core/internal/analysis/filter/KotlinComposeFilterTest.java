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

import static org.objectweb.asm.Opcodes.*;

import org.jacoco.core.internal.instr.InstrSupport;
import org.junit.Test;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodNode;

/**
 * Unit tests for {@link KotlinComposeFilter}.
 */
public class KotlinComposeFilterTest extends FilterTestBase {

	private final IFilter filter = new KotlinComposeFilter();

	/**
	 * <pre>
	 * &#064;androidx.compose.runtime.Composable
	 * fun example(x: Int) {
	 *   if (x < 0)
	 *     return
	 *   example(x - 1)
	 * }
	 * </pre>
	 *
	 * transformed by <a href=
	 * "https://github.com/JetBrains/kotlin/tree/v2.0.0-RC2/plugins/compose">Compose
	 * Kotlin compiler plugin version 2.0.0</a> into
	 *
	 * <pre>
	 * fun example(x: Int, $composer: Composer?, $changed: Int) {
	 *   $composer = $composer.startRestartGroup(...)
	 *   sourceInformation($composer, ...)
	 *   val $dirty = $changed
	 *   if ($changed and 0b0110 == 0) {
	 *     $dirty = $dirty or if ($composer.changed(x)) 0b0100 else 0b0010
	 *   }
	 *   if ($dirty and 0b0011 != 0b0010 || !$composer.skipping) {
	 *     if (isTraceInProgress()) {
	 *       traceEventStart(...)
	 *     }
	 *
	 *     if (x < 0) {
	 *       if (isTraceInProgress()) {
	 *         traceEventEnd()
	 *       }
	 *       $composer.endRestartGroup()?.updateScope { ... }
	 *       return
	 *     }
	 *
	 *     example(x, $composer, 0)
	 *
	 *     if (isTraceInProgress()) {
	 *       traceEventEnd()
	 *     }
	 *   } else {
	 *     $composer.skipToGroupEnd()
	 *   }
	 *   $composer.endRestartGroup()?.updateScope { ... }
	 * }
	 * </pre>
	 */
	@Test
	public void should_filter() {
		context.classAnnotations
				.add(KotlinGeneratedFilter.KOTLIN_METADATA_DESC);
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"f", "(Landroidx/compose/runtime/Composer;I)V", null, null);
		m.visitAnnotation("Landroidx/compose/runtime/Composable;", false);

		final Range range1 = new Range();
		final Range range2 = new Range();
		final Range range3 = new Range();
		final Range range4 = new Range();
		final Range range5 = new Range();
		final Range range6 = new Range();
		final Range range7 = new Range();

		m.visitVarInsn(ALOAD, 1);
		range1.fromInclusive = m.instructions.getLast();
		m.visitLdcInsn(Integer.valueOf(-974630231));
		m.visitMethodInsn(INVOKEINTERFACE, "androidx/compose/runtime/Composer",
				"startRestartGroup", "(I)Landroidx/compose/runtime/Composer;",
				true);
		m.visitVarInsn(ASTORE, 1);
		m.visitVarInsn(ILOAD, 2);
		m.visitVarInsn(ISTORE, 3);
		Label label1 = new Label();
		m.visitLabel(label1);
		m.visitVarInsn(ILOAD, 2);
		m.visitIntInsn(BIPUSH, 14);
		m.visitInsn(IAND);
		Label label2 = new Label();
		m.visitJumpInsn(IFNE, label2);
		m.visitVarInsn(ILOAD, 3);
		m.visitVarInsn(ALOAD, 1);
		m.visitVarInsn(ILOAD, 0);
		m.visitMethodInsn(INVOKEINTERFACE, "androidx/compose/runtime/Composer",
				"changed", "(I)Z", true);
		Label label3 = new Label();
		m.visitJumpInsn(IFEQ, label3);
		m.visitInsn(ICONST_4);
		Label label4 = new Label();
		m.visitJumpInsn(GOTO, label4);
		m.visitLabel(label3);
		m.visitInsn(ICONST_2);
		m.visitLabel(label4);
		m.visitInsn(IOR);
		m.visitVarInsn(ISTORE, 3);
		m.visitLabel(label2);
		m.visitVarInsn(ILOAD, 3);
		m.visitIntInsn(BIPUSH, 11);
		m.visitInsn(IAND);
		m.visitInsn(ICONST_2);
		Label label5 = new Label();
		m.visitJumpInsn(IF_ICMPNE, label5);
		m.visitVarInsn(ALOAD, 1);
		m.visitMethodInsn(INVOKEINTERFACE, "androidx/compose/runtime/Composer",
				"getSkipping", "()Z", true);
		Label label6 = new Label();
		m.visitJumpInsn(IFNE, label6);
		range1.toInclusive = m.instructions.getLast();

		m.visitLabel(label5);
		m.visitMethodInsn(INVOKESTATIC, "androidx/compose/runtime/ComposerKt",
				"isTraceInProgress", "()Z", false);
		Label label7 = new Label();
		m.visitJumpInsn(IFEQ, label7);
		range3.fromInclusive = m.instructions.getLast();
		m.visitLdcInsn(Integer.valueOf(-974630231));
		m.visitVarInsn(ILOAD, 3);
		m.visitInsn(ICONST_M1);
		m.visitLdcInsn("org.example.example (Example.kt:19)");
		m.visitMethodInsn(INVOKESTATIC, "androidx/compose/runtime/ComposerKt",
				"traceEventStart", "(IIILjava/lang/String;)V", false);
		m.visitLabel(label7);
		range3.toInclusive = m.instructions.getLast();

		m.visitVarInsn(ILOAD, 0);
		Label label8 = new Label();
		m.visitJumpInsn(IFGE, label8);
		m.visitMethodInsn(INVOKESTATIC, "androidx/compose/runtime/ComposerKt",
				"isTraceInProgress", "()Z", false);
		Label label9 = new Label();
		m.visitJumpInsn(IFEQ, label9);
		range4.fromInclusive = m.instructions.getLast();
		m.visitMethodInsn(INVOKESTATIC, "androidx/compose/runtime/ComposerKt",
				"traceEventEnd", "()V", false);
		m.visitLabel(label9);
		range4.toInclusive = m.instructions.getLast();

		m.visitVarInsn(ALOAD, 1);
		m.visitMethodInsn(INVOKEINTERFACE, "androidx/compose/runtime/Composer",
				"endRestartGroup",
				"()Landroidx/compose/runtime/ScopeUpdateScope;", true);
		m.visitInsn(DUP);
		Label label10 = new Label();
		m.visitJumpInsn(IFNULL, label10);
		range5.fromInclusive = m.instructions.getLast();
		m.visitTypeInsn(NEW, "org/example/ExampleKt$example$4");
		m.visitInsn(DUP);
		m.visitVarInsn(ILOAD, 0);
		m.visitVarInsn(ILOAD, 2);
		m.visitMethodInsn(INVOKESPECIAL, "org/example/ExampleKt$example$4",
				"<init>", "(II)V", false);
		m.visitTypeInsn(CHECKCAST, "kotlin/jvm/functions/Function2");
		m.visitMethodInsn(INVOKEINTERFACE,
				"androidx/compose/runtime/ScopeUpdateScope", "updateScope",
				"(Lkotlin/jvm/functions/Function2;)V", true);
		Label label11 = new Label();
		m.visitJumpInsn(GOTO, label11);
		m.visitLabel(label10);
		m.visitFrame(Opcodes.F_SAME1, 0, null, 1,
				new Object[] { "androidx/compose/runtime/ScopeUpdateScope" });
		m.visitInsn(POP);
		range5.toInclusive = m.instructions.getLast();

		m.visitLabel(label11);
		m.visitInsn(Opcodes.RETURN);

		m.visitLabel(label8);
		m.visitVarInsn(ILOAD, 0);
		m.visitInsn(ICONST_1);
		m.visitInsn(ISUB);
		m.visitVarInsn(ALOAD, 1);
		m.visitInsn(ICONST_0);
		m.visitMethodInsn(INVOKESTATIC, "org/example/ExampleKt", "example",
				"(ILandroidx/compose/runtime/Composer;I)V", false);

		m.visitMethodInsn(INVOKESTATIC, "androidx/compose/runtime/ComposerKt",
				"isTraceInProgress", "()Z", false);
		Label label12 = new Label();
		m.visitJumpInsn(IFEQ, label12);
		range6.fromInclusive = m.instructions.getLast();
		m.visitMethodInsn(INVOKESTATIC, "androidx/compose/runtime/ComposerKt",
				"traceEventEnd", "()V", false);
		m.visitJumpInsn(GOTO, label12);
		m.visitLabel(label6);
		range2.fromInclusive = m.instructions.getLast();
		m.visitVarInsn(ALOAD, 1);
		m.visitMethodInsn(INVOKEINTERFACE, "androidx/compose/runtime/Composer",
				"skipToGroupEnd", "()V", true);
		m.visitLabel(label12);
		range6.toInclusive = m.instructions.getLast();

		m.visitVarInsn(ALOAD, 1);
		m.visitMethodInsn(INVOKEINTERFACE, "androidx/compose/runtime/Composer",
				"endRestartGroup",
				"()Landroidx/compose/runtime/ScopeUpdateScope;", true);
		m.visitInsn(DUP);
		Label label13 = new Label();
		m.visitJumpInsn(IFNULL, label13);
		range7.fromInclusive = m.instructions.getLast();
		m.visitTypeInsn(NEW, "org/example/ExampleKt$example$5");
		m.visitInsn(DUP);
		m.visitVarInsn(ILOAD, 0);
		m.visitVarInsn(ILOAD, 2);
		m.visitMethodInsn(INVOKESPECIAL, "org/example/ExampleKt$example$5",
				"<init>", "(II)V", false);
		m.visitTypeInsn(CHECKCAST, "kotlin/jvm/functions/Function2");
		m.visitMethodInsn(INVOKEINTERFACE,
				"androidx/compose/runtime/ScopeUpdateScope", "updateScope",
				"(Lkotlin/jvm/functions/Function2;)V", true);
		Label label14 = new Label();
		m.visitJumpInsn(GOTO, label14);
		m.visitLabel(label13);
		m.visitFrame(Opcodes.F_SAME1, 0, null, 1,
				new Object[] { "androidx/compose/runtime/ScopeUpdateScope" });
		m.visitInsn(POP);
		range7.toInclusive = m.instructions.getLast();

		m.visitLabel(label14);
		m.visitInsn(RETURN);
		range2.toInclusive = m.instructions.getLast();

		filter.filter(m, context, output);

		assertIgnored(m, range1, range2, range3, range4, range5, range6,
				range7);
	}

	/**
	 * <pre>
	 * &#064;androidx.compose.runtime.Composable
	 * fun example() {
	 *   ... // body
	 * }
	 * </pre>
	 *
	 * after <a href=
	 * "https://github.com/JetBrains/kotlin/commit/ee9217f8f0f37967684fbfe4a568c2b3c8707507">
	 * change in Compose Kotlin compiler plugin version 2.1.0</a> transformed
	 * into
	 *
	 * <pre>
	 * fun example($composer: Composer?, $changed: Int) {
	 *   $composer = $composer.startRestartGroup(...)
	 *   sourceInformation($composer, ...)
	 *   if ($composer.shouldExecute(...)) {
	 *     ... // body
	 *   } else {
	 *     $composer.skipToGroupEnd()
	 *   }
	 *   $composer.endRestartGroup()?.updateScope { ... }
	 * }
	 * </pre>
	 *
	 * This <a href=
	 * "https://github.com/JetBrains/kotlin/commit/5f7e5d1518e839c1f8514a5c145aad52b1ce1739">became
	 * the default in Compose Kotlin compiler plugin version 2.2.0</a>
	 */
	@Test
	public void should_filter_pausable_composition() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"example", "(Landroidx/compose/runtime/Composer;I)V", null,
				null);
		m.visitAnnotation("Landroidx/compose/runtime/Composable;", false);

		final Range range1 = new Range();
		final Range range2 = new Range();

		m.visitVarInsn(ALOAD, 0);
		range1.fromInclusive = m.instructions.getLast();
		m.visitLdcInsn(Integer.valueOf(-933543558));
		m.visitMethodInsn(INVOKEINTERFACE, "androidx/compose/runtime/Composer",
				"startRestartGroup", "(I)Landroidx/compose/runtime/Composer;",
				true);
		m.visitVarInsn(ASTORE, 0);
		m.visitVarInsn(ALOAD, 0);
		m.visitVarInsn(ILOAD, 1);
		final Label label1 = new Label();
		m.visitJumpInsn(IFEQ, label1);
		m.visitInsn(ICONST_1);
		final Label label2 = new Label();
		m.visitJumpInsn(GOTO, label2);
		m.visitLabel(label1);
		m.visitInsn(ICONST_0);
		m.visitLabel(label2);
		m.visitVarInsn(ILOAD, 1);
		m.visitInsn(ICONST_1);
		m.visitInsn(IAND);
		m.visitMethodInsn(INVOKEINTERFACE, "androidx/compose/runtime/Composer",
				"shouldExecute", "(ZI)Z", true);
		final Label label3 = new Label();
		m.visitJumpInsn(IFEQ, label3);
		range1.toInclusive = m.instructions.getLast();

		// body
		final Label label5 = new Label();
		m.visitJumpInsn(GOTO, label5);

		m.visitLabel(label3);
		m.visitVarInsn(ALOAD, 0);
		m.visitMethodInsn(INVOKEINTERFACE, "androidx/compose/runtime/Composer",
				"skipToGroupEnd", "()V", true);
		m.visitLabel(label5);
		m.visitVarInsn(ALOAD, 0);
		m.visitMethodInsn(INVOKEINTERFACE, "androidx/compose/runtime/Composer",
				"endRestartGroup",
				"()Landroidx/compose/runtime/ScopeUpdateScope;", true);
		m.visitInsn(DUP);
		final Label label6 = new Label();
		m.visitJumpInsn(IFNULL, label6);
		range2.fromInclusive = m.instructions.getLast();
		m.visitVarInsn(ILOAD, 1);
		m.visitInvokeDynamicInsn("invoke",
				"(I)Lkotlin/jvm/functions/Function2;", null);
		m.visitMethodInsn(INVOKEINTERFACE,
				"androidx/compose/runtime/ScopeUpdateScope", "updateScope",
				"(Lkotlin/jvm/functions/Function2;)V", true);
		final Label label7 = new Label();
		m.visitJumpInsn(GOTO, label7);
		m.visitLabel(label6);
		m.visitInsn(POP);
		range2.toInclusive = m.instructions.getLast();

		m.visitLabel(label7);
		m.visitInsn(RETURN);

		filter.filter(m, context, output);

		assertIgnored(m, range1, range2);
	}

}
