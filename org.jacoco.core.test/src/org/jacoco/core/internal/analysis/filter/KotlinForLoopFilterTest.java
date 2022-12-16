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
 * Unit tests for {@link KotlinForLoopFilter}.
 */
public class KotlinForLoopFilterTest extends FilterTestBase {

	private final KotlinForLoopFilter filter = new KotlinForLoopFilter();

	/**
	 * <pre>
	 * class Example {
	 *   fun example() {
	 *     for (j in 0 until i1()) {}
	 *   }
	 *   private fun i1() = 1
	 * }
	 * </pre>
	 */
	@Test
	public void should_filter_Kotlin_1_5_until() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"example", "()V", null, null);
		final Label label1 = new Label();
		final Label label2 = new Label();

		m.visitInsn(Opcodes.ICONST_0);
		m.visitVarInsn(Opcodes.ISTORE, 0);
		m.visitMethodInsn(Opcodes.INVOKESTATIC, "ExampleKt", "i1", "()I",
				false);
		m.visitVarInsn(Opcodes.ISTORE, 1);
		m.visitVarInsn(Opcodes.ILOAD, 0);
		m.visitVarInsn(Opcodes.ILOAD, 1);
		m.visitJumpInsn(Opcodes.IF_ICMPGE, label1);
		final AbstractInsnNode ignored1 = m.instructions.getLast();
		m.visitLabel(label2);
		m.visitVarInsn(Opcodes.ILOAD, 0);
		m.visitVarInsn(Opcodes.ISTORE, 2);
		m.visitInsn(Opcodes.IINC);
		m.visitVarInsn(Opcodes.ILOAD, 0);
		m.visitVarInsn(Opcodes.ILOAD, 1);
		m.visitJumpInsn(Opcodes.IF_ICMPLT, label2);
		final AbstractInsnNode ignored2 = m.instructions.getLast();
		m.visitLabel(label1);
		m.visitInsn(Opcodes.RETURN);

		filter.filter(m, context, output);
		assertIgnored(new Range(ignored1, ignored1),
				new Range(ignored2, ignored2));
	}

	/**
	 * <pre>
	 * class Example {
	 *   fun example() {
	 *     for (i in i1() downTo 0) {}
	 *   }
	 *   private fun i1() = 1
	 * }
	 * </pre>
	 */
	@Test
	public void should_filter_Kotlin_1_5_downTo() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"example", "()V", null, null);
		final Label label1 = new Label();
		final Label label2 = new Label();

		m.visitMethodInsn(Opcodes.INVOKESTATIC, "ExampleKt", "i1", "()I",
				false);
		m.visitVarInsn(Opcodes.ISTORE, 0);
		m.visitInsn(Opcodes.ICONST_0);
		m.visitVarInsn(Opcodes.ISTORE, 1);
		m.visitVarInsn(Opcodes.ILOAD, 1);
		m.visitJumpInsn(Opcodes.IF_ICMPGT, label1);
		final AbstractInsnNode ignored1 = m.instructions.getLast();
		m.visitLabel(label2);
		m.visitVarInsn(Opcodes.ILOAD, 0);
		m.visitVarInsn(Opcodes.ISTORE, 1);
		m.visitInsn(Opcodes.IINC);
		m.visitInsn(Opcodes.ICONST_0);
		m.visitVarInsn(Opcodes.ILOAD, 1);
		m.visitJumpInsn(Opcodes.IF_ICMPLE, label2);
		final AbstractInsnNode ignored2 = m.instructions.getLast();
		m.visitLabel(label1);
		m.visitInsn(Opcodes.RETURN);

		filter.filter(m, context, output);
		assertIgnored(new Range(ignored1, ignored1),
				new Range(ignored2, ignored2));
	}

	/**
	 * <pre>
	 * class Example {
	 *   fun example() {
	 *     val limit = 10
	 *     for (j in limit downTo i1()) {}
	 *   }
	 *   private fun i1() = 1
	 * }
	 * </pre>
	 */
	@Test
	public void should_filter_Kotlin_1_5_downTo_val() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"example", "()V", null, null);
		final Label label1 = new Label();
		final Label label2 = new Label();

		m.visitVarInsn(Opcodes.BIPUSH, 10);
		m.visitVarInsn(Opcodes.ISTORE, 0);
		m.visitVarInsn(Opcodes.ILOAD, 0);
		m.visitVarInsn(Opcodes.ISTORE, 1);
		m.visitMethodInsn(Opcodes.INVOKESTATIC, "ExampleKt", "i1", "()I",
				false);
		m.visitVarInsn(Opcodes.ISTORE, 2);
		m.visitVarInsn(Opcodes.ILOAD, 2);
		m.visitVarInsn(Opcodes.ILOAD, 1);
		m.visitJumpInsn(Opcodes.IF_ICMPGT, label1);
		final AbstractInsnNode ignored1 = m.instructions.getLast();
		m.visitLabel(label2);
		m.visitVarInsn(Opcodes.ILOAD, 1);
		m.visitVarInsn(Opcodes.ISTORE, 3);
		m.visitInsn(Opcodes.IINC);
		m.visitVarInsn(Opcodes.ILOAD, 3);
		m.visitVarInsn(Opcodes.ILOAD, 2);
		m.visitJumpInsn(Opcodes.IF_ICMPNE, label2);
		final AbstractInsnNode ignored2 = m.instructions.getLast();
		m.visitLabel(label1);
		m.visitInsn(Opcodes.RETURN);

		filter.filter(m, context, output);
		assertIgnored(new Range(ignored1, ignored1),
				new Range(ignored2, ignored2));
	}

}
