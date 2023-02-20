/*******************************************************************************
 * Copyright (c) 2009, 2023 Mountainminds GmbH & Co. KG and Contributors
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
		final AbstractInsnNode expectedTo = m.instructions.getLast();
		m.visitLabel(l2);
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
				"android/os/PowerManager$WakeLock", "acquire", "", false);

		filter.filter(m, context, output);

		assertIgnored(new Range(expectedFrom, expectedTo));
	}

	/**
	 * <pre>
	 * class Example {
	 *   private lateinit var x: String
	 *   fun example() = x
	 * }
	 * </pre>
	 */
	@Test
	public void should_filter_Kotlin_1_5() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"example", "()Ljava/lang/String;", null, null);
		Label label = new Label();
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitFieldInsn(Opcodes.GETFIELD, "Example", "x",
				"Ljava/lang/String;");
		m.visitVarInsn(Opcodes.ASTORE, 1);
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitJumpInsn(Opcodes.IFNONNULL, label);
		final AbstractInsnNode expectedFrom = m.instructions.getLast();
		m.visitLdcInsn("x");
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

}
