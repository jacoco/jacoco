/*******************************************************************************
 * Copyright (c) 2009, 2022 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Łukasz Suski - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.internal.analysis.filter;

import org.jacoco.core.internal.instr.InstrSupport;
import org.junit.Test;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodNode;

public class KotlinComposeFilterTest extends FilterTestBase {

	private final IFilter filter = new KotlinComposeFilter();

	/**
	 * Source code:
	 *
	 * <pre>
	 * <code>@Composable</code>
	 * fun MyComposable(text:String, modifier: Modifier = Modifier){
	 *     if (text=="qwerty"){
	 *         Text(text = text, modifier = modifier)
	 *     }
	 * }
	 * </pre>
	 *
	 * Decompiled:
	 *
	 * <pre>
	 *    <code>@Composable</code>
	 *    public static final void MyComposableWithIf(@NotNull String text, @Nullable Modifier modifier, @Nullable Composer $composer, int $changed, int var4) {
	 *       Intrinsics.checkNotNullParameter(text, "text");
	 *       $composer = $composer.startRestartGroup(229561689);
	 *       ComposerKt.sourceInformation($composer, "C(MyComposableWithIf)P(1):MyComposable.kt#6ane5v");
	 *       int $dirty = $changed;
	 *       if ((var4 & 1) != 0) {
	 *          $dirty = $changed | 6;
	 *       } else if (($changed & 14) == 0) {
	 *          $dirty = $changed | ($composer.changed(text) ? 4 : 2);
	 *       }
	 *
	 *       if ((var4 & 2) != 0) {
	 *          $dirty |= 48;
	 *       } else if (($changed & 112) == 0) {
	 *          $dirty |= $composer.changed(modifier) ? 32 : 16;
	 *       }
	 *
	 *       if (($dirty & 91 ^ 18) == 0 && $composer.getSkipping()) {
	 *          $composer.skipToGroupEnd();
	 *       } else {
	 *          if ((var4 & 2) != 0) {
	 *             modifier = (Modifier)Modifier.Companion;
	 *          }
	 *
	 *          if (Intrinsics.areEqual(text, com.example.android.compose.LiveLiterals.MyComposableKt.INSTANCE.String$arg-1$call-EQEQ$cond$if$fun-MyComposableWithIf())) {
	 *             $composer.startReplaceableGroup(229561780);
	 *             ComposerKt.sourceInformation($composer, "10@277L38");
	 *             TextKt.Text-fLXpl1I(text, modifier, 0L, 0L, (FontStyle)null, (FontWeight)null, (FontFamily)null, 0L, (TextDecoration)null, (TextAlign)null, 0L, 0, false, 0, (Function1)null, (TextStyle)null, $composer, 14 & $dirty | 112 & $dirty, 64, 65532);
	 *             $composer.endReplaceableGroup();
	 *          } else {
	 *             $composer.startReplaceableGroup(229561834);
	 *             $composer.endReplaceableGroup();
	 *          }
	 *       }
	 *
	 *       ScopeUpdateScope var6 = $composer.endRestartGroup();
	 *       if (var6 != null) {
	 *          var6.updateScope((Function2)(new 1(text, modifier, $changed, var4)));
	 *       }
	 *
	 *    }
	 * </pre>
	 */
	@Test
	public void should_filter_labeled_groups_with_only_composer_invocations() {

		// region given

		final MethodNode m = getComposableMethod();

		// L3
		m.visitLabel(new Label());
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitLdcInsn("text");
		m.visitMethodInsn(Opcodes.INVOKESTATIC,
				"kotlin/jvm/internal/Intrinsics", "checkNotNullParameter",
				"(Ljava/lang/Object;Ljava/lang/String;)V", false);

		// L4
		Label label4 = new Label();
		m.visitLabel(label4);
		m.visitVarInsn(Opcodes.ALOAD, 2);
		m.visitLdcInsn(1842867868);
		m.visitMethodInsn(Opcodes.INVOKEINTERFACE,
				"androidx/compose/runtime/Composer", "startRestartGroup",
				"(I)Landroidx/compose/runtime/Composer;", true);
		m.visitVarInsn(Opcodes.ASTORE, 2);
		m.visitVarInsn(Opcodes.ALOAD, 2);
		m.visitLdcInsn("C(MyComposable)P(1):MyComposable.kt#6ane5v");
		m.visitMethodInsn(Opcodes.INVOKESTATIC,
				"androidx/compose/runtime/ComposerKt", "sourceInformation",
				"(Landroidx/compose/runtime/Composer;Ljava/lang/String;)V",
				false);
		m.visitVarInsn(Opcodes.ILOAD, 3);
		m.visitVarInsn(Opcodes.ISTORE, 5);

		// L1
		m.visitLabel(new Label());
		m.visitVarInsn(Opcodes.ILOAD, 4);
		m.visitInsn(Opcodes.ICONST_1);
		m.visitInsn(Opcodes.IAND);
		Label label5 = new Label();
		m.visitJumpInsn(Opcodes.IFEQ, label5);
		m.visitVarInsn(Opcodes.ILOAD, 5);
		m.visitIntInsn(Opcodes.BIPUSH, 6);
		m.visitInsn(Opcodes.IOR);
		m.visitVarInsn(Opcodes.ISTORE, 5);
		Label label6 = new Label();
		m.visitJumpInsn(Opcodes.GOTO, label6);

		// L5
		m.visitLabel(label5);
		m.visitFrame(Opcodes.F_NEW, 0, new Object[] {}, 0, new Object[] {});
		m.visitVarInsn(Opcodes.ILOAD, 3);
		m.visitIntInsn(Opcodes.BIPUSH, 14);
		m.visitInsn(Opcodes.IAND);
		m.visitJumpInsn(Opcodes.IFNE, label6);
		m.visitVarInsn(Opcodes.ILOAD, 5);
		m.visitVarInsn(Opcodes.ALOAD, 2);
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitMethodInsn(Opcodes.INVOKEINTERFACE,
				"androidx/compose/runtime/Composer", "changed",
				"(Ljava/lang/Object;)Z", true);
		Label label7 = new Label();
		m.visitJumpInsn(Opcodes.IFEQ, label7);
		m.visitInsn(Opcodes.ICONST_4);
		Label label8 = new Label();
		m.visitJumpInsn(Opcodes.GOTO, label8);

		// L7
		m.visitLabel(label7);
		m.visitFrame(Opcodes.F_NEW, 0, new Object[] {}, 0, new Object[] {});
		m.visitInsn(Opcodes.ICONST_2);

		// L8
		m.visitLabel(label8);
		m.visitFrame(Opcodes.F_NEW, 0, new Object[] {}, 0, new Object[] {});
		m.visitInsn(Opcodes.IOR);
		m.visitVarInsn(Opcodes.ISTORE, 5);

		// L6
		m.visitLabel(label6);
		m.visitFrame(Opcodes.F_NEW, 0, new Object[] {}, 0, new Object[] {});
		m.visitVarInsn(Opcodes.ILOAD, 4);
		m.visitInsn(Opcodes.ICONST_2);
		m.visitInsn(Opcodes.IAND);
		Label label9 = new Label();
		m.visitJumpInsn(Opcodes.IFEQ, label9);
		m.visitVarInsn(Opcodes.ILOAD, 5);
		m.visitIntInsn(Opcodes.BIPUSH, 48);
		m.visitInsn(Opcodes.IOR);
		m.visitVarInsn(Opcodes.ISTORE, 5);
		Label label10 = new Label();
		m.visitJumpInsn(Opcodes.GOTO, label10);

		// L9
		m.visitLabel(label9);
		m.visitFrame(Opcodes.F_NEW, 0, new Object[] {}, 0, new Object[] {});
		m.visitVarInsn(Opcodes.ILOAD, 3);
		m.visitIntInsn(Opcodes.BIPUSH, 112);
		m.visitInsn(Opcodes.IAND);
		m.visitJumpInsn(Opcodes.IFNE, label10);
		m.visitVarInsn(Opcodes.ILOAD, 5);
		m.visitVarInsn(Opcodes.ALOAD, 2);
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitMethodInsn(Opcodes.INVOKEINTERFACE,
				"androidx/compose/runtime/Composer", "changed",
				"(Ljava/lang/Object;)Z", true);
		Label label11 = new Label();
		m.visitJumpInsn(Opcodes.IFEQ, label11);
		m.visitIntInsn(Opcodes.BIPUSH, 32);
		Label label12 = new Label();
		m.visitJumpInsn(Opcodes.GOTO, label12);

		// L11
		m.visitLabel(label11);
		m.visitFrame(Opcodes.F_NEW, 0, new Object[] {}, 0, new Object[] {});
		m.visitIntInsn(Opcodes.BIPUSH, 16);

		// L12
		m.visitLabel(label12);
		m.visitFrame(Opcodes.F_NEW, 0, new Object[] {}, 0, new Object[] {});
		m.visitInsn(Opcodes.IOR);
		m.visitVarInsn(Opcodes.ISTORE, 5);

		// L10
		m.visitLabel(label10);
		m.visitFrame(Opcodes.F_NEW, 0, new Object[] {}, 0, new Object[] {});
		m.visitVarInsn(Opcodes.ILOAD, 5);
		m.visitIntInsn(Opcodes.BIPUSH, 91);
		m.visitInsn(Opcodes.IAND);
		m.visitIntInsn(Opcodes.BIPUSH, 18);
		m.visitInsn(Opcodes.IXOR);
		Label label13 = new Label();
		m.visitJumpInsn(Opcodes.IFNE, label13);
		m.visitVarInsn(Opcodes.ALOAD, 2);
		m.visitMethodInsn(Opcodes.INVOKEINTERFACE,
				"androidx/compose/runtime/Composer", "getSkipping", "Z", true);
		Label label14 = new Label();
		m.visitJumpInsn(Opcodes.IFNE, label14);

		// L13
		m.visitLabel(label13);
		m.visitFrame(Opcodes.F_NEW, 0, new Object[] {}, 0, new Object[] {});
		m.visitVarInsn(Opcodes.ILOAD, 4);
		m.visitInsn(Opcodes.ICONST_2);
		m.visitInsn(Opcodes.IAND);
		Label label15 = new Label();
		label15.info = new LabelNode();
		m.visitJumpInsn(Opcodes.IFEQ, label15);
		m.visitFieldInsn(Opcodes.GETSTATIC, "androidx/compose/ui/Modifier",
				"Companion", "androidx.compose.ui.Modifier$Companion");
		m.visitVarInsn(Opcodes.ASTORE, 1);

		// L15
		m.visitLabel(label15);
		m.visitFrame(Opcodes.F_NEW, 0, new Object[] {}, 0, new Object[] {});
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitFieldInsn(Opcodes.GETSTATIC,
				"com/example/android/compose/LiveLiterals$MyComposableKt",
				"INSTANCE",
				"com.example.android.compose.LiveLiterals$MyComposableKt");
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
				"com/example/android/compose/LiveLiterals$MyComposableKt",
				"String$arg-1$call-EQEQ$cond$if$fun-MyComposable",
				"()Ljava/lang/String;", false);
		m.visitMethodInsn(Opcodes.INVOKESTATIC,
				"kotlin/jvm/internal/Intrinsics", "areEqual",
				"(Ljava/lang/Object;Ljava/lang/Object;)Z", false);
		Label label16 = new Label();
		m.visitJumpInsn(Opcodes.IFEQ, label16);
		m.visitVarInsn(Opcodes.ALOAD, 2);
		m.visitLdcInsn(1842867953);
		m.visitMethodInsn(Opcodes.INVOKEINTERFACE,
				"androidx/compose/runtime/Composer", "startReplaceableGroup",
				"(I)V", true);
		m.visitVarInsn(Opcodes.ALOAD, 2);
		m.visitLdcInsn("10@271L38");
		m.visitMethodInsn(Opcodes.INVOKESTATIC,
				"androidx/compose/runtime/ComposerKt", "sourceInformation",
				"(Landroidx/compose/runtime/Composer;Ljava/lang/String;)V",
				false);

		// L17
		Label label17 = new Label();
		m.visitLabel(label17);
		m.visitVarInsn(Opcodes.ALOAD, 2);
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitInsn(Opcodes.ICONST_0);
		visitComposableMethodInvocation(m);

		// L18
		Label label18 = new Label();
		m.visitLabel(label18);
		m.visitVarInsn(Opcodes.ALOAD, 2);
		m.visitMethodInsn(Opcodes.INVOKEINTERFACE,
				"androidx/compose/runtime/Composer", "endReplaceableGroup",
				"()V", true);
		Label label19 = new Label();
		m.visitJumpInsn(Opcodes.GOTO, label19);

		// L16
		m.visitLabel(label16);
		m.visitFrame(Opcodes.F_NEW, 0, new Object[] {}, 0, new Object[] {});
		m.visitVarInsn(Opcodes.ALOAD, 2);
		m.visitLdcInsn(1842868007);
		m.visitMethodInsn(Opcodes.INVOKEINTERFACE,
				"androidx/compose/runtime/Composer", "startReplaceableGroup",
				"(I)V", true);
		m.visitVarInsn(Opcodes.ALOAD, 2);
		m.visitMethodInsn(Opcodes.INVOKEINTERFACE,
				"androidx/compose/runtime/Composer", "endReplaceableGroup",
				"()V", true);
		m.visitJumpInsn(Opcodes.GOTO, label19);

		// L14
		m.visitLabel(label14);
		m.visitFrame(Opcodes.F_NEW, 0, new Object[] {}, 0, new Object[] {});
		m.visitVarInsn(Opcodes.ALOAD, 2);
		m.visitMethodInsn(Opcodes.INVOKEINTERFACE,
				"androidx/compose/runtime/Composer", "skipToGroupEnd", "()V",
				true);

		// L19
		m.visitLabel(label19);
		m.visitFrame(Opcodes.F_NEW, 0, new Object[] {}, 0, new Object[] {});
		m.visitVarInsn(Opcodes.ALOAD, 2);
		m.visitMethodInsn(Opcodes.INVOKEINTERFACE,
				"androidx/compose/runtime/Composer", "endRestartGroup",
				"()Landroidx/compose/runtime/ScopeUpdateScope;", true);
		m.visitVarInsn(Opcodes.ASTORE, 6);
		m.visitVarInsn(Opcodes.ALOAD, 6);
		Label label20 = new Label();
		m.visitJumpInsn(Opcodes.IFNONNULL, label20);
		Label label21 = new Label();
		m.visitJumpInsn(Opcodes.GOTO, label21);

		// L20
		m.visitLabel(label20);
		m.visitFrame(Opcodes.F_NEW, 0, new Object[] {}, 0, new Object[] {});
		m.visitVarInsn(Opcodes.ALOAD, 6);
		m.visitTypeInsn(Opcodes.NEW,
				"com/example/android/compose/MyComposableKt$MyComposable$1");
		m.visitInsn(Opcodes.DUP);
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitMethodInsn(Opcodes.INVOKESPECIAL,
				"com/example/android/compose/MyComposableKt$MyComposable$1",
				"<init>",
				"(Ljava/lang/String;Landroidx/compose/ui/Modifier;II)V", false);
		m.visitMethodInsn(Opcodes.INVOKEINTERFACE,
				"androidx/compose/runtime/ScopeUpdateScope", "updateScope",
				"(Lkotlin/jvm/functions/Function2;)V", true);

		// L21
		m.visitLabel(label21);
		m.visitFrame(Opcodes.F_NEW, 0, new Object[] {}, 0, new Object[] {});
		m.visitInsn(Opcodes.RETURN);

		// endregion

		// when

		filter.filter(m, context, output);

		// then

		Range range1 = new Range((AbstractInsnNode) label4.info,
				((LabelNode) label15.info).getPrevious());
		Range range2 = new Range((AbstractInsnNode) label18.info,
				((LabelNode) label21.info).getPrevious());

		mergeIgnoredRanges();
		assertIgnored(range1, range2);
	}

	private void visitComposableMethodInvocation(MethodNode m) {
		m.visitMethodInsn(Opcodes.INVOKESTATIC,
				"androidx/compose/material/TextKt", "Text-fLXpl1I",
				"(Ljava/lang/String;Landroidx/compose/ui/Modifier;JJLandroidx/compose/ui/text/font/FontStyle;Landroidx/compose/ui/text/font/FontWeight;Landroidx/compose/ui/text/font/FontFamily;JLandroidx/compose/ui/text/style/TextDecoration;Landroidx/compose/ui/text/style/TextAlign;JIZILkotlin/jvm/functions/Function1;Landroidx/compose/ui/text/TextStyle;Landroidx/compose/runtime/Composer;III)V",
				false);
	}

	private MethodNode getComposableMethod() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"MyComposable", "(Ljava/lang/Object;)Ljava/lang/Object;", null,
				null);
		context.classAnnotations
				.add(KotlinGeneratedFilter.KOTLIN_METADATA_DESC);
		m.visitAnnotation(KotlinComposeFilter.COMPOSABLE_ANNOTATION_DESCRIPTOR,
				false);
		return m;
	}

	/**
	 * Not sure if this can happen but there are null checks which have to be
	 * covered
	 */
	@Test
	public void ignore_entire_method_if_no_start_and_end_label_and_no_non_composer_invocation() {
		final MethodNode m = getComposableMethod();

		m.visitFrame(Opcodes.F_NEW, 0, new Object[] {}, 0, new Object[] {});
		m.visitVarInsn(Opcodes.ALOAD, 2);
		m.visitMethodInsn(Opcodes.INVOKEINTERFACE,
				"androidx/compose/runtime/Composer", "skipToGroupEnd", "()V",
				true);
		m.visitInsn(Opcodes.RETURN);

		filter.filter(m, context, output);

		assertIgnored(new Range(m.instructions.getFirst(),
				m.instructions.getLast().getPrevious()));
	}

	@Test
	public void does_not_ignore_entire_method_if_no_start_and_end_label_but_has_non_composer_invocations() {
		final MethodNode m = getComposableMethod();

		m.visitFrame(Opcodes.F_NEW, 0, new Object[] {}, 0, new Object[] {});
		m.visitVarInsn(Opcodes.ALOAD, 2);
		m.visitMethodInsn(Opcodes.INVOKEINTERFACE,
				"androidx/compose/runtime/Composer", "skipToGroupEnd", "()V",
				true);
		visitComposableMethodInvocation(m);
		m.visitInsn(Opcodes.RETURN);

		filter.filter(m, context, output);

		assertIgnored();
	}
}
