/*******************************************************************************
 * Copyright (c) 2009, 2025 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Fabian Mastenbroek - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.internal.analysis.filter;

import org.junit.Test;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * Unit tests for {@link KotlinLateinitFilter}.
 */
public class KotlinLateinitFilterTest extends FilterTestBase {

	private final KotlinLateinitFilter filter = new KotlinLateinitFilter();

	@Test
	public void should_filter_Kotlin_1_2() {
		final MethodNode m = new MethodNode(0, "read", "()Ljava/lang/String;",
				null, null);
		final Label label = new Label();
		m.visitInsn(Opcodes.DUP);
		m.visitJumpInsn(Opcodes.IFNONNULL, label);
		final AbstractInsnNode expectedFrom = m.instructions.getLast();
		m.visitLdcInsn("member");
		m.visitMethodInsn(Opcodes.INVOKESTATIC,
				"kotlin/jvm/internal/Intrinsics",
				"throwUninitializedPropertyAccessException",
				"(Ljava/lang/String;)V", false);
		final AbstractInsnNode expectedTo = m.instructions.getLast();
		m.visitLabel(label);

		filter.filter(m, context, output);

		assertIgnored(new Range(expectedFrom, expectedTo));
	}

	/**
	 * https://github.com/JetBrains/kotlin/commit/7c9578116bfa4aebac5a62c4bdaa1a11dd82426c
	 */
	@Test
	public void should_filter_Kotlin_1_5_0_public() {
		final MethodNode m = new MethodNode(0, "getMember",
				"()Ljava/lang/String;", null, null);
		final Label label = new Label();
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitFieldInsn(Opcodes.GETFIELD, "Example", "member",
				"Ljava/lang/String;");
		m.visitVarInsn(Opcodes.ASTORE, 1);
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitJumpInsn(Opcodes.IFNULL, label);
		final AbstractInsnNode branch = m.instructions.getLast();
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitInsn(Opcodes.ARETURN);
		m.visitLabel(label);
		final AbstractInsnNode expectedFrom = m.instructions.getLast();
		m.visitLdcInsn("member");
		m.visitMethodInsn(Opcodes.INVOKESTATIC,
				"kotlin/jvm/internal/Intrinsics",
				"throwUninitializedPropertyAccessException",
				"(Ljava/lang/String;)V", false);
		m.visitInsn(Opcodes.ACONST_NULL);
		m.visitInsn(Opcodes.ATHROW);
		final AbstractInsnNode expectedTo = m.instructions.getLast();

		filter.filter(m, context, output);

		assertIgnored(new Range(branch, branch),
				new Range(expectedFrom, expectedTo));
	}

	/**
	 * https://github.com/JetBrains/kotlin/commit/7c9578116bfa4aebac5a62c4bdaa1a11dd82426c
	 */
	@Test
	public void should_filter_Kotlin_1_5_0_private() {
		final MethodNode m = new MethodNode(0, "read", "()Ljava/lang/String;",
				null, null);
		final Label label = new Label();
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitFieldInsn(Opcodes.GETFIELD, "Example", "member",
				"Ljava/lang/String;");
		m.visitVarInsn(Opcodes.ASTORE, 1);
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitJumpInsn(Opcodes.IFNONNULL, label);
		final AbstractInsnNode expectedFrom = m.instructions.getLast();
		m.visitLdcInsn("member");
		m.visitMethodInsn(Opcodes.INVOKESTATIC,
				"kotlin/jvm/internal/Intrinsics",
				"throwUninitializedPropertyAccessException",
				"(Ljava/lang/String;)V", false);
		m.visitInsn(Opcodes.ACONST_NULL);
		m.visitInsn(Opcodes.ATHROW);
		final AbstractInsnNode expectedTo = m.instructions.getLast();
		m.visitLabel(label);
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitInsn(Opcodes.ARETURN);

		filter.filter(m, context, output);

		assertIgnored(new Range(expectedFrom, expectedTo));
	}

	/**
	 * https://github.com/JetBrains/kotlin/commit/81b09ca09f012ed42bb9e90d4b6d802c697c07b6
	 */
	@Test
	public void should_filter_Kotlin_1_5_30_public() {
		final MethodNode m = new MethodNode(0, "getMember",
				"()Ljava/lang/String;", null, null);
		final Label label = new Label();
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitFieldInsn(Opcodes.GETFIELD, "Example", "member",
				"Ljava/lang/String;");
		m.visitVarInsn(Opcodes.ASTORE, 1);
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitJumpInsn(Opcodes.IFNULL, label);
		final AbstractInsnNode branch = m.instructions.getLast();
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitInsn(Opcodes.ARETURN);
		m.visitLabel(label);
		final AbstractInsnNode expectedFrom = m.instructions.getLast();
		m.visitLdcInsn("member");
		m.visitMethodInsn(Opcodes.INVOKESTATIC,
				"kotlin/jvm/internal/Intrinsics",
				"throwUninitializedPropertyAccessException",
				"(Ljava/lang/String;)V", false);
		m.visitInsn(Opcodes.ACONST_NULL);
		m.visitInsn(Opcodes.ARETURN);
		final AbstractInsnNode expectedTo = m.instructions.getLast();

		filter.filter(m, context, output);

		assertIgnored(new Range(branch, branch),
				new Range(expectedFrom, expectedTo));
	}

	/**
	 * https://github.com/JetBrains/kotlin/commit/81b09ca09f012ed42bb9e90d4b6d802c697c07b6
	 */
	@Test
	public void should_filter_Kotlin_1_5_30_public_generic() {
		final MethodNode m = new MethodNode(0, "getMember",
				"()Ljava/lang/Object;", null, null);
		final Label label = new Label();
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitFieldInsn(Opcodes.GETFIELD, "Example", "member",
				"Ljava/lang/String;");
		m.visitVarInsn(Opcodes.ASTORE, 1);
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitJumpInsn(Opcodes.IFNULL, label);
		final AbstractInsnNode branch = m.instructions.getLast();
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitInsn(Opcodes.ARETURN);
		m.visitLabel(label);
		final AbstractInsnNode expectedFrom = m.instructions.getLast();
		m.visitLdcInsn("member");
		m.visitMethodInsn(Opcodes.INVOKESTATIC,
				"kotlin/jvm/internal/Intrinsics",
				"throwUninitializedPropertyAccessException",
				"(Ljava/lang/String;)V", false);
		m.visitFieldInsn(Opcodes.GETSTATIC, "kotlin/Unit", "INSTANCE",
				"Lkotlin/Unit;");
		m.visitInsn(Opcodes.ARETURN);
		final AbstractInsnNode expectedTo = m.instructions.getLast();

		filter.filter(m, context, output);

		assertIgnored(new Range(branch, branch),
				new Range(expectedFrom, expectedTo));
	}

	/**
	 * https://github.com/JetBrains/kotlin/commit/81b09ca09f012ed42bb9e90d4b6d802c697c07b6
	 */
	@Test
	public void should_filter_Kotlin_1_5_30_private() {
		final MethodNode m = new MethodNode(0, "read", "()Ljava/lang/String;",
				null, null);
		final Label label = new Label();
		final Label label2 = new Label();
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitFieldInsn(Opcodes.GETFIELD, "Example", "member",
				"Ljava/lang/String;");
		m.visitVarInsn(Opcodes.ASTORE, 1);
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitJumpInsn(Opcodes.IFNONNULL, label);
		final AbstractInsnNode expectedFrom = m.instructions.getLast();
		m.visitLdcInsn("member");
		m.visitMethodInsn(Opcodes.INVOKESTATIC,
				"kotlin/jvm/internal/Intrinsics",
				"throwUninitializedPropertyAccessException",
				"(Ljava/lang/String;)V", false);
		m.visitInsn(Opcodes.ACONST_NULL);
		m.visitJumpInsn(Opcodes.GOTO, label2);
		final AbstractInsnNode expectedTo = m.instructions.getLast();
		m.visitLabel(label);
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitLabel(label2);
		m.visitInsn(Opcodes.ARETURN);

		filter.filter(m, context, output);

		assertIgnored(new Range(expectedFrom, expectedTo));
	}

	/**
	 * https://github.com/JetBrains/kotlin/commit/81b09ca09f012ed42bb9e90d4b6d802c697c07b6
	 */
	@Test
	public void should_filter_Kotlin_1_5_30_private_generic() {
		final MethodNode m = new MethodNode(0, "read", "()Ljava/lang/Object;",
				null, null);
		final Label label = new Label();
		final Label label2 = new Label();
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitFieldInsn(Opcodes.GETFIELD, "Example", "member",
				"Ljava/lang/Object;");
		m.visitVarInsn(Opcodes.ASTORE, 1);
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitJumpInsn(Opcodes.IFNONNULL, label);
		final AbstractInsnNode expectedFrom = m.instructions.getLast();
		m.visitLdcInsn("member");
		m.visitMethodInsn(Opcodes.INVOKESTATIC,
				"kotlin/jvm/internal/Intrinsics",
				"throwUninitializedPropertyAccessException",
				"(Ljava/lang/String;)V", false);
		m.visitFieldInsn(Opcodes.GETSTATIC, "kotlin/Unit", "INSTANCE",
				"Lkotlin/Unit;");
		m.visitJumpInsn(Opcodes.GOTO, label2);
		final AbstractInsnNode expectedTo = m.instructions.getLast();
		m.visitLabel(label);
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitLabel(label2);
		m.visitInsn(Opcodes.ARETURN);

		filter.filter(m, context, output);

		assertIgnored(new Range(expectedFrom, expectedTo));
	}

	/**
	 * https://github.com/JetBrains/kotlin/commit/041773fd2584bc279813361eb7fc11ae84c214fd
	 */
	@Test
	public void should_filter_Kotlin_1_6_0_private() {
		final MethodNode m = new MethodNode(0, "read", "()Ljava/lang/String;",
				null, null);
		final Label label = new Label();
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitFieldInsn(Opcodes.GETFIELD, "Example", "member",
				"Ljava/lang/String;");
		m.visitInsn(Opcodes.DUP);
		m.visitJumpInsn(Opcodes.IFNONNULL, label);
		final AbstractInsnNode expectedFrom = m.instructions.getLast();
		m.visitInsn(Opcodes.POP);
		m.visitLdcInsn("member");
		m.visitMethodInsn(Opcodes.INVOKESTATIC,
				"kotlin/jvm/internal/Intrinsics",
				"throwUninitializedPropertyAccessException",
				"(Ljava/lang/String;)V", false);
		m.visitInsn(Opcodes.ACONST_NULL);
		final AbstractInsnNode expectedTo = m.instructions.getLast();
		m.visitLabel(label);
		m.visitInsn(Opcodes.ARETURN);

		filter.filter(m, context, output);

		assertIgnored(new Range(expectedFrom, expectedTo));
	}

	/**
	 * https://github.com/JetBrains/kotlin/commit/041773fd2584bc279813361eb7fc11ae84c214fd
	 */
	@Test
	public void should_filter_Kotlin_1_6_0_private_generic() {
		final MethodNode m = new MethodNode(0, "read", "()Ljava/lang/Object;",
				null, null);
		final Label label = new Label();
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitFieldInsn(Opcodes.GETFIELD, "Example", "member",
				"Ljava/lang/String;");
		m.visitInsn(Opcodes.DUP);
		m.visitJumpInsn(Opcodes.IFNONNULL, label);
		final AbstractInsnNode expectedFrom = m.instructions.getLast();
		m.visitInsn(Opcodes.POP);
		m.visitLdcInsn("member");
		m.visitMethodInsn(Opcodes.INVOKESTATIC,
				"kotlin/jvm/internal/Intrinsics",
				"throwUninitializedPropertyAccessException",
				"(Ljava/lang/String;)V", false);
		m.visitFieldInsn(Opcodes.GETSTATIC, "kotlin/Unit", "INSTANCE",
				"Lkotlin/Unit;");
		final AbstractInsnNode expectedTo = m.instructions.getLast();
		m.visitLabel(label);
		m.visitInsn(Opcodes.ARETURN);

		filter.filter(m, context, output);

		assertIgnored(new Range(expectedFrom, expectedTo));
	}

	/**
	 * https://github.com/jetBrains/kotlin/commit/0a67ab54fec635f82e0507cbdd4299ae0dbe71b0
	 */
	@Test
	public void should_filter_Kotlin_1_6_20_public() {
		final MethodNode m = new MethodNode(0, "getMember",
				"()Ljava/lang/String;", null, null);
		final Label label = new Label();
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitFieldInsn(Opcodes.GETFIELD, "Example", "member",
				"Ljava/lang/String;");
		m.visitInsn(Opcodes.DUP);
		m.visitJumpInsn(Opcodes.IFNULL, label);
		final AbstractInsnNode branch = m.instructions.getLast();
		m.visitInsn(Opcodes.ARETURN);
		m.visitLabel(label);
		final AbstractInsnNode expectedFrom = m.instructions.getLast();
		m.visitInsn(Opcodes.POP);
		m.visitLdcInsn("member");
		m.visitMethodInsn(Opcodes.INVOKESTATIC,
				"kotlin/jvm/internal/Intrinsics",
				"throwUninitializedPropertyAccessException",
				"(Ljava/lang/String;)V", false);
		m.visitInsn(Opcodes.ACONST_NULL);
		m.visitInsn(Opcodes.ARETURN);
		final AbstractInsnNode expectedTo = m.instructions.getLast();

		filter.filter(m, context, output);

		assertIgnored(new Range(branch, branch),
				new Range(expectedFrom, expectedTo));
	}

	/**
	 * https://github.com/jetBrains/kotlin/commit/0a67ab54fec635f82e0507cbdd4299ae0dbe71b0
	 */
	@Test
	public void should_filter_Kotlin_1_6_20_public_generic() {
		final MethodNode m = new MethodNode(0, "getMember",
				"()Ljava/lang/Object;", null, null);
		final Label label = new Label();
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitFieldInsn(Opcodes.GETFIELD, "Example", "member",
				"Ljava/lang/String;");
		m.visitInsn(Opcodes.DUP);
		m.visitJumpInsn(Opcodes.IFNULL, label);
		final AbstractInsnNode branch = m.instructions.getLast();
		m.visitInsn(Opcodes.ARETURN);
		m.visitLabel(label);
		final AbstractInsnNode expectedFrom = m.instructions.getLast();
		m.visitInsn(Opcodes.POP);
		m.visitLdcInsn("member");
		m.visitMethodInsn(Opcodes.INVOKESTATIC,
				"kotlin/jvm/internal/Intrinsics",
				"throwUninitializedPropertyAccessException",
				"(Ljava/lang/String;)V", false);
		m.visitFieldInsn(Opcodes.GETSTATIC, "kotlin/Unit", "INSTANCE",
				"Lkotlin/Unit;");
		m.visitInsn(Opcodes.ARETURN);
		final AbstractInsnNode expectedTo = m.instructions.getLast();

		filter.filter(m, context, output);

		assertIgnored(new Range(branch, branch),
				new Range(expectedFrom, expectedTo));
	}

}
