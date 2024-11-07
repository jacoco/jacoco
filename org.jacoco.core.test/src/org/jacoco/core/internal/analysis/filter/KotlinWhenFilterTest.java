/*******************************************************************************
 * Copyright (c) 2009, 2024 Mountainminds GmbH & Co. KG and Contributors
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

import java.util.HashSet;
import java.util.Set;

import org.jacoco.core.internal.instr.InstrSupport;
import org.junit.Test;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * Unit tests for {@link KotlinWhenFilter}.
 */
public class KotlinWhenFilterTest extends FilterTestBase {

	private final KotlinWhenFilter filter = new KotlinWhenFilter();

	private final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
			"name", "()V", null, null);

	@Test
	public void should_filter_implicit_else() {
		final Label label = new Label();

		final Range range1 = new Range();

		m.visitInsn(Opcodes.NOP);

		m.visitJumpInsn(Opcodes.IFEQ, label);
		range1.fromInclusive = m.instructions.getLast();
		range1.toInclusive = m.instructions.getLast();

		m.visitInsn(Opcodes.NOP);

		final Range range2 = new Range();
		m.visitLabel(label);
		range2.fromInclusive = m.instructions.getLast();
		m.visitTypeInsn(Opcodes.NEW, "kotlin/NoWhenBranchMatchedException");
		m.visitInsn(Opcodes.DUP);
		m.visitMethodInsn(Opcodes.INVOKESPECIAL,
				"kotlin/NoWhenBranchMatchedException", "<init>", "()V", false);
		m.visitInsn(Opcodes.ATHROW);
		range2.toInclusive = m.instructions.getLast();

		filter.filter(m, context, output);

		assertIgnored(range1, range2);
		assertNoReplacedBranches();
	}

	@Test
	public void should_not_filter_explicit_else() {
		final Label label = new Label();

		m.visitInsn(Opcodes.NOP);

		m.visitJumpInsn(Opcodes.IFEQ, label);

		m.visitInsn(Opcodes.NOP);

		m.visitLabel(label);
		m.visitTypeInsn(Opcodes.NEW, "kotlin/NoWhenBranchMatchedException");
		m.visitInsn(Opcodes.DUP);
		m.visitMethodInsn(Opcodes.INVOKESPECIAL,
				"kotlin/NoWhenBranchMatchedException", "<init>", "()V", false);
		m.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Throwable");
		m.visitInsn(Opcodes.ATHROW);

		filter.filter(m, context, output);

		assertIgnored();
		assertNoReplacedBranches();
	}

	@Test
	public void should_filter_implicit_default() {
		final Label case1 = new Label();
		final Label caseDefault = new Label();
		final Label after = new Label();

		m.visitInsn(Opcodes.NOP);

		m.visitTableSwitchInsn(0, 0, caseDefault, case1);
		final AbstractInsnNode switchNode = m.instructions.getLast();
		final Set<AbstractInsnNode> newTargets = new HashSet<AbstractInsnNode>();

		m.visitLabel(case1);
		m.visitInsn(Opcodes.ICONST_1);
		newTargets.add(m.instructions.getLast());
		m.visitJumpInsn(Opcodes.GOTO, after);

		final Range range1 = new Range();
		m.visitLabel(caseDefault);
		range1.fromInclusive = m.instructions.getLast();
		m.visitTypeInsn(Opcodes.NEW, "kotlin/NoWhenBranchMatchedException");
		m.visitInsn(Opcodes.DUP);
		m.visitMethodInsn(Opcodes.INVOKESPECIAL,
				"kotlin/NoWhenBranchMatchedException", "<init>", "()V", false);
		m.visitInsn(Opcodes.ATHROW);
		range1.toInclusive = m.instructions.getLast();

		m.visitLabel(after);

		filter.filter(m, context, output);

		assertIgnored(range1);
		assertReplacedBranches(switchNode, newTargets);
	}

	/**
	 * <pre>
	 * fun example(b: Boolean) = when (b) {
	 *   true -> "t"
	 *   false -> "f"
	 * }
	 * </pre>
	 */
	@Test
	public void should_filter_instruction_ifne() {
		final Range range1 = new Range();
		final Range range2 = new Range();
		final Label falseCase = new Label();
		final Label after = new Label();
		final Label exception = new Label();
		m.visitVarInsn(Opcodes.ILOAD, 1);
		m.visitInsn(Opcodes.ICONST_1);
		m.visitJumpInsn(Opcodes.IF_ICMPNE, falseCase);
		m.visitLdcInsn("t");
		m.visitJumpInsn(Opcodes.GOTO, after);
		m.visitLabel(falseCase);
		m.visitVarInsn(Opcodes.ILOAD, 1);
		m.visitJumpInsn(Opcodes.IFNE, exception);
		range1.fromInclusive = m.instructions.getLast();
		range1.toInclusive = m.instructions.getLast();
		m.visitLdcInsn("f");
		m.visitJumpInsn(Opcodes.GOTO, after);
		m.visitLabel(exception);
		range2.fromInclusive = m.instructions.getLast();
		m.visitTypeInsn(Opcodes.NEW, "kotlin/NoWhenBranchMatchedException");
		m.visitInsn(Opcodes.DUP);
		m.visitMethodInsn(Opcodes.INVOKESPECIAL,
				"kotlin/NoWhenBranchMatchedException", "<init>", "()V", false);
		m.visitInsn(Opcodes.ATHROW);
		range2.toInclusive = m.instructions.getLast();
		m.visitLabel(after);

		filter.filter(m, context, output);

		assertIgnored(range1, range2);
		assertNoReplacedBranches();
	}

	/**
	 * <pre>
	 * fun example(b: Boolean) = when(b) {
	 *   false -> "f"
	 *   true -> "t"
	 * }
	 * </pre>
	 *
	 * <pre>
	 * fun example(e: EnumWithSingleValue) = when (e) {
	 *   EnumWithSingleValue.A -> "a"
	 * }
	 * </pre>
	 */
	@Test
	public void should_filter_instruction_ificmpne() {
		final Range range1 = new Range();
		final Range range2 = new Range();
		final Label trueCase = new Label();
		final Label after = new Label();
		final Label exception = new Label();
		m.visitVarInsn(Opcodes.ILOAD, 1);
		m.visitJumpInsn(Opcodes.IFNE, trueCase);
		m.visitLdcInsn("f");
		m.visitJumpInsn(Opcodes.GOTO, after);
		m.visitLabel(trueCase);
		m.visitVarInsn(Opcodes.ILOAD, 1);
		m.visitInsn(Opcodes.ICONST_1);
		m.visitJumpInsn(Opcodes.IF_ICMPNE, exception);
		range1.fromInclusive = m.instructions.getLast();
		range1.toInclusive = m.instructions.getLast();
		m.visitLdcInsn("t");
		m.visitJumpInsn(Opcodes.GOTO, after);
		m.visitLabel(exception);
		range2.fromInclusive = m.instructions.getLast();
		m.visitTypeInsn(Opcodes.NEW, "kotlin/NoWhenBranchMatchedException");
		m.visitInsn(Opcodes.DUP);
		m.visitMethodInsn(Opcodes.INVOKESPECIAL,
				"kotlin/NoWhenBranchMatchedException", "<init>", "()V", false);
		m.visitInsn(Opcodes.ATHROW);
		range2.toInclusive = m.instructions.getLast();
		m.visitLabel(after);

		filter.filter(m, context, output);

		assertIgnored(range1, range2);
		assertNoReplacedBranches();
	}

	/**
	 * <pre>
	 * fun example(b: Boolean?) = when(b) {
	 *   true -> "t"
	 *   false -> "f"
	 *   null -> "n"
	 * }
	 * </pre>
	 */
	@Test
	public void should_filter_instruction_ifnonnull() {
		final Range range1 = new Range();
		final Range range2 = new Range();
		final Label falseCase = new Label();
		final Label nullCase = new Label();
		final Label after = new Label();
		final Label exception = new Label();
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitInsn(Opcodes.ICONST_1);
		m.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Boolean", "valueOf",
				"(Z)Ljava/lang/Boolean;", false);
		m.visitMethodInsn(Opcodes.INVOKESTATIC,
				"kotlin/jvm/internal/Intrinsics", "areEqual",
				"(Ljava/lang/Object;Ljava/lang/Object;)Z", false);
		m.visitJumpInsn(Opcodes.IFEQ, falseCase);
		m.visitLdcInsn("t");
		m.visitJumpInsn(Opcodes.GOTO, after);
		m.visitLabel(falseCase);
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitInsn(Opcodes.ICONST_0);
		m.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Boolean", "valueOf",
				"(Z)Ljava/lang/Boolean;", false);
		m.visitMethodInsn(Opcodes.INVOKESTATIC,
				"kotlin/jvm/internal/Intrinsics", "areEqual",
				"(Ljava/lang/Object;Ljava/lang/Object;)Z", false);
		m.visitJumpInsn(Opcodes.IFEQ, nullCase);
		m.visitLdcInsn("f");
		m.visitJumpInsn(Opcodes.GOTO, after);
		m.visitLabel(nullCase);
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitJumpInsn(Opcodes.IFNONNULL, exception);
		range1.fromInclusive = m.instructions.getLast();
		range1.toInclusive = m.instructions.getLast();
		m.visitLdcInsn("n");
		m.visitJumpInsn(Opcodes.GOTO, after);
		m.visitLabel(exception);
		range2.fromInclusive = m.instructions.getLast();
		m.visitTypeInsn(Opcodes.NEW, "kotlin/NoWhenBranchMatchedException");
		m.visitInsn(Opcodes.DUP);
		m.visitMethodInsn(Opcodes.INVOKESPECIAL,
				"kotlin/NoWhenBranchMatchedException", "<init>", "()V", false);
		m.visitInsn(Opcodes.ATHROW);
		range2.toInclusive = m.instructions.getLast();
		m.visitLabel(after);

		filter.filter(m, context, output);

		assertIgnored(range1, range2);
		assertNoReplacedBranches();
	}

}
