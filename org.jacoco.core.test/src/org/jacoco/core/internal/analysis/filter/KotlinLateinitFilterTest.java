/*******************************************************************************
 * Copyright (c) 2009, 2022 Mountainminds GmbH & Co. KG and Contributors
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

import org.jacoco.core.internal.instr.InstrSupport;
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

	private final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
			"name", "()V", null, null);

	@Test
	public void testLateinitBranchIsFiltered() {
		final Label l1 = new Label();
		final Label l2 = new Label();

		m.visitLabel(l1);
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitFieldInsn(Opcodes.GETFIELD,
				"com/better/alarm/background/VibrationService", "wakeLock",
				"Landroid/os/PowerManager$WakeLock;");
		m.visitInsn(Opcodes.DUP);
		m.visitJumpInsn(Opcodes.IFNONNULL, l2);

		final AbstractInsnNode expectedFrom = m.instructions.getLast();

		m.visitLdcInsn("wakelock");
		m.visitMethodInsn(Opcodes.INVOKESTATIC,
				"kotlin/jvm/internal/Intrinsics",
				"throwUninitializedPropertyAccessException",
				"(Ljava/lang/String;)V", false);
		m.visitLabel(l2);
		final AbstractInsnNode expectedTo = m.instructions.getLast();
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
				"android/os/PowerManager$WakeLock", "acquire", "", false);

		filter.filter(m, context, output);

		assertIgnored(new Range(expectedFrom, expectedTo));
	}

	/**
	 * <pre>
	 * class LateinitStringPrivate {
	 *     private lateinit var member: String
	 *
	 *     fun get(): String = member
	 * }
	 * </pre>
	 */
	@Test
	public void should_filter_Kotlin_1_5_private() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"get", "()Ljava/lang/String;", null, null);
		Label label = new Label();
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitFieldInsn(Opcodes.GETFIELD, "LateinitStringPrivate", "member",
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
		m.visitLabel(label);
		final AbstractInsnNode expectedTo = m.instructions.getLast();
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitInsn(Opcodes.ARETURN);

		filter.filter(m, context, output);

		assertIgnored(new Range(expectedFrom, expectedTo));
	}

	/**
	 * <pre>
	 * class LateinitStringPublic {
	 *     lateinit var member: String
	 * }
	 * </pre>
	 */
	@Test
	public void should_filter_Kotlin_1_5_public() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"getMember", "()Ljava/lang/String;", null, null);
		Label label = new Label();
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitFieldInsn(Opcodes.GETFIELD, "LateinitStringPublic", "member",
				"Ljava/lang/String;");
		m.visitVarInsn(Opcodes.ASTORE, 1);
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitJumpInsn(Opcodes.IFNULL, label);
		final AbstractInsnNode expectedFrom = m.instructions.getLast();
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitInsn(Opcodes.ARETURN);
		m.visitLabel(label);
		m.visitLdcInsn("member");
		m.visitMethodInsn(Opcodes.INVOKESTATIC,
				"kotlin/jvm/internal/Intrinsics",
				"throwUninitializedPropertyAccessException",
				"(Ljava/lang/String;)V", false);
		m.visitInsn(Opcodes.ACONST_NULL);
		m.visitInsn(Opcodes.ATHROW);
		final AbstractInsnNode expectedTo = m.instructions.getLast();

		filter.filter(m, context, output);

		assertIgnored(new Range(expectedFrom, expectedTo));
	}

	/**
	 * <pre>
	 * class LateinitStringPrivate {
	 *     private lateinit var member: String
	 *
	 *     fun get(): String = member
	 * }
	 * </pre>
	 */
	@Test
	public void should_filter_Kotlin_1_5_30_private() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"get", "()Ljava/lang/String;", null, null);
		Label l1 = new Label();
		Label l2 = new Label();
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitFieldInsn(Opcodes.GETFIELD, "LateinitStringPrivate", "member",
				"Ljava/lang/String;");
		m.visitVarInsn(Opcodes.ASTORE, 1);
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitJumpInsn(Opcodes.IFNONNULL, l1);
		final AbstractInsnNode expectedFrom = m.instructions.getLast();
		m.visitLdcInsn("member");
		m.visitMethodInsn(Opcodes.INVOKESTATIC,
				"kotlin/jvm/internal/Intrinsics",
				"throwUninitializedPropertyAccessException",
				"(Ljava/lang/String;)V", false);
		m.visitInsn(Opcodes.ACONST_NULL);
		m.visitJumpInsn(Opcodes.GOTO, l2);
		m.visitLabel(l1);
		final AbstractInsnNode expectedTo = m.instructions.getLast();
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitLabel(l2);
		m.visitInsn(Opcodes.ARETURN);

		filter.filter(m, context, output);

		assertIgnored(new Range(expectedFrom, expectedTo));
	}

	/**
	 * <pre>
	 * class LateinitStringPublic {
	 *     lateinit var member: String
	 * }
	 * </pre>
	 */
	@Test
	public void should_filter_Kotlin_1_5_30_public() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"getMember", "()Ljava/lang/String;", null, null);
		Label label = new Label();
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitFieldInsn(Opcodes.GETFIELD, "LateinitStringPublic", "member",
				"Ljava/lang/String;");
		m.visitVarInsn(Opcodes.ASTORE, 1);
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitJumpInsn(Opcodes.IFNULL, label);
		final AbstractInsnNode expectedFrom = m.instructions.getLast();
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitInsn(Opcodes.ARETURN);
		m.visitLabel(label);
		m.visitLdcInsn("member");
		m.visitMethodInsn(Opcodes.INVOKESTATIC,
				"kotlin/jvm/internal/Intrinsics",
				"throwUninitializedPropertyAccessException",
				"(Ljava/lang/String;)V", false);
		m.visitInsn(Opcodes.ACONST_NULL);
		m.visitInsn(Opcodes.ARETURN);
		final AbstractInsnNode expectedTo = m.instructions.getLast();

		filter.filter(m, context, output);

		assertIgnored(new Range(expectedFrom, expectedTo));
	}

	/**
	 * <pre>
	 * class LateinitGenericPrivate<T : Any> {
	 *     private lateinit var member: T
	 *
	 *     fun get(): T = member
	 * }
	 * </pre>
	 */
	@Test
	public void should_filter_Kotlin_1_5_30_private_generic() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"get", "()Ljava/lang/String;", null, null);
		Label l1 = new Label();
		Label l2 = new Label();
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitFieldInsn(Opcodes.GETFIELD, "LateinitGenericPrivate", "member",
				"Ljava/lang/Object;");
		m.visitVarInsn(Opcodes.ASTORE, 1);
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitJumpInsn(Opcodes.IFNONNULL, l1);
		final AbstractInsnNode expectedFrom = m.instructions.getLast();
		m.visitLdcInsn("member");
		m.visitMethodInsn(Opcodes.INVOKESTATIC,
				"kotlin/jvm/internal/Intrinsics",
				"throwUninitializedPropertyAccessException",
				"(Ljava/lang/String;)V", false);
		m.visitFieldInsn(Opcodes.GETSTATIC, "kotlin/Unit", "INSTANCE",
				"Lkotlin/Unit;");
		m.visitJumpInsn(Opcodes.GOTO, l2);
		m.visitLabel(l1);
		final AbstractInsnNode expectedTo = m.instructions.getLast();
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitLabel(l2);
		m.visitInsn(Opcodes.ARETURN);

		filter.filter(m, context, output);

		assertIgnored(new Range(expectedFrom, expectedTo));
	}

	/**
	 * <pre>
	 * class LateinitGenericPublic<T : Any> {
	 *     lateinit var member: T
	 * }
	 * </pre>
	 */
	@Test
	public void should_filter_Kotlin_1_5_30_public_generic() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"getMember", "()Ljava/lang/String;", null, null);
		Label label = new Label();
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitFieldInsn(Opcodes.GETFIELD, "LateinitGenericPublic", "member",
				"Ljava/lang/Object;");
		m.visitVarInsn(Opcodes.ASTORE, 1);
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitJumpInsn(Opcodes.IFNULL, label);
		final AbstractInsnNode expectedFrom = m.instructions.getLast();
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitInsn(Opcodes.ARETURN);
		m.visitLabel(label);
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

		assertIgnored(new Range(expectedFrom, expectedTo));
	}

	/**
	 * <pre>
	 * class LateinitStringPrivate {
	 *     private lateinit var member: String
	 *
	 *     fun get(): String = member
	 * }
	 * </pre>
	 */
	@Test
	public void should_filter_Kotlin_1_6_private() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"get", "()Ljava/lang/String;", null, null);
		Label label = new Label();
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitFieldInsn(Opcodes.GETFIELD, "LateinitStringPrivate", "member",
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
		m.visitLabel(label);
		final AbstractInsnNode expectedTo = m.instructions.getLast();
		m.visitInsn(Opcodes.ARETURN);

		filter.filter(m, context, output);

		assertIgnored(new Range(expectedFrom, expectedTo));
	}

	/**
	 * <pre>
	 * class LateinitGenericPrivate<T : Any> {
	 *     private lateinit var member: T
	 *
	 *     fun get(): T = member
	 * }
	 * </pre>
	 */
	@Test
	public void should_filter_Kotlin_1_6_private_generic() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"get", "()Ljava/lang/String;", null, null);
		Label label = new Label();
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitFieldInsn(Opcodes.GETFIELD, "LateinitGenericPrivate", "member",
				"Ljava/lang/Object;");
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
		m.visitLabel(label);
		final AbstractInsnNode expectedTo = m.instructions.getLast();
		m.visitInsn(Opcodes.ARETURN);

		filter.filter(m, context, output);

		assertIgnored(new Range(expectedFrom, expectedTo));
	}

	/**
	 * <pre>
	 * class LateinitStringPublic {
	 *     lateinit var member: String
	 * }
	 * </pre>
	 *
	 * Kotlin 1.7.21 contains an additional frame node before the option pop.
	 */
	@Test
	public void should_filter_Kotlin_1_7_21() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"get", "()Ljava/lang/String;", null, null);
		Label label = new Label();
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitFieldInsn(Opcodes.GETFIELD, "LateinitStringPublic", "member",
				"Ljava/lang/String;");
		m.visitInsn(Opcodes.DUP);
		m.visitJumpInsn(Opcodes.IFNULL, label);
		final AbstractInsnNode expectedFrom = m.instructions.getLast();
		m.visitInsn(Opcodes.ARETURN);
		m.visitLabel(label);
		m.visitFrame(Opcodes.F_SAME1, 0, new Object[] {}, 1,
				new String[] { "java/lang/String" });
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

		assertIgnored(new Range(expectedFrom, expectedTo));
	}
}
