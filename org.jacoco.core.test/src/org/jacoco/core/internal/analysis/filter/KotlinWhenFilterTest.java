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
	 * enum class E { A, B }
	 * fun example(e: E?) = when (e) {
	 *     E.A -> "a"
	 *     E.B -> "b"
	 *     null -> "null"
	 * }
	 * </pre>
	 */
	@Test
	public void should_filter_when_by_nullable_enum_with_null_case_and_without_else() {
		final Range range1 = new Range();
		final Range range2 = new Range();
		final HashSet<AbstractInsnNode> newTargets = new HashSet<AbstractInsnNode>();
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"example", "(LE;)Ljava/lang/String;", null, null);
		final Label l1 = new Label();
		final Label l2 = new Label();
		final Label caseNull = new Label();
		final Label caseElse = new Label();
		final Label caseA = new Label();
		final Label caseB = new Label();
		final Label after = new Label();

		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitInsn(Opcodes.DUP);
		range1.fromInclusive = m.instructions.getLast();
		m.visitJumpInsn(Opcodes.IFNONNULL, l1);
		m.visitInsn(Opcodes.POP);
		m.visitInsn(Opcodes.ICONST_M1);
		m.visitJumpInsn(Opcodes.GOTO, l2);
		m.visitLabel(l1);
		m.visitFieldInsn(Opcodes.GETSTATIC, "ExampleKt$WhenMappings",
				"$EnumSwitchMapping$0", "[I");
		m.visitInsn(Opcodes.SWAP);
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "ExampleKt$Enum", "ordinal",
				"()I", false);
		m.visitInsn(Opcodes.IALOAD);
		m.visitLabel(l2);
		range1.toInclusive = m.instructions.getLast();
		m.visitTableSwitchInsn(-1, 2, caseElse, caseNull, caseElse, caseA,
				caseB);
		final AbstractInsnNode switchNode = m.instructions.getLast();

		m.visitLabel(caseA);
		m.visitLdcInsn("a");
		newTargets.add(m.instructions.getLast());
		m.visitJumpInsn(Opcodes.GOTO, after);

		m.visitLabel(caseB);
		m.visitLdcInsn("b");
		newTargets.add(m.instructions.getLast());
		m.visitJumpInsn(Opcodes.GOTO, after);

		m.visitLabel(caseNull);
		m.visitLdcInsn("null");
		newTargets.add(m.instructions.getLast());
		m.visitJumpInsn(Opcodes.GOTO, after);

		m.visitLabel(caseElse);
		range2.fromInclusive = m.instructions.getLast();
		m.visitTypeInsn(Opcodes.NEW, "kotlin/NoWhenBranchMatchedException");
		m.visitInsn(Opcodes.DUP);
		m.visitMethodInsn(Opcodes.INVOKESPECIAL,
				"kotlin/NoWhenBranchMatchedException", "<init>", "()V", false);
		m.visitInsn(Opcodes.ATHROW);
		range2.toInclusive = m.instructions.getLast();

		m.visitLabel(after);
		m.visitInsn(Opcodes.ARETURN);

		filter.filter(m, context, output);

		assertIgnored(range1, range2);
		assertReplacedBranches(switchNode, newTargets);
	}

	/**
	 * <pre>
	 * enum class E { A, B }
	 * fun example(e: E?) = when (e) {
	 *     E.A -> "a"
	 *     E.B -> "b"
	 *     else -> "else"
	 * }
	 * </pre>
	 */
	@Test
	public void should_filter_when_by_nullable_enum_without_null_case_and_with_else() {
		final Range range1 = new Range();
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"example", "(LE;)Ljava/lang/String;", null, null);
		final Label l1 = new Label();
		final Label l2 = new Label();
		final Label caseElse = new Label();
		final Label caseA = new Label();
		final Label caseB = new Label();
		final Label after = new Label();

		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitInsn(Opcodes.DUP);
		range1.fromInclusive = m.instructions.getLast();
		m.visitJumpInsn(Opcodes.IFNONNULL, l1);
		m.visitInsn(Opcodes.POP);
		m.visitInsn(Opcodes.ICONST_M1);
		m.visitJumpInsn(Opcodes.GOTO, l2);
		m.visitLabel(l1);
		m.visitFieldInsn(Opcodes.GETSTATIC, "ExampleKt$WhenMappings",
				"$EnumSwitchMapping$0", "[I");
		m.visitInsn(Opcodes.SWAP);
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "ExampleKt$Enum", "ordinal",
				"()I", false);
		m.visitInsn(Opcodes.IALOAD);
		m.visitLabel(l2);
		range1.toInclusive = m.instructions.getLast();
		m.visitTableSwitchInsn(1, 2, caseElse, caseA, caseB);

		m.visitLabel(caseA);
		m.visitLdcInsn("a");
		m.visitJumpInsn(Opcodes.GOTO, after);

		m.visitLabel(caseB);
		m.visitLdcInsn("b");
		m.visitJumpInsn(Opcodes.GOTO, after);

		m.visitLabel(caseElse);
		m.visitLdcInsn("else");

		m.visitLabel(after);
		m.visitInsn(Opcodes.ARETURN);

		filter.filter(m, context, output);

		assertIgnored(range1);
		assertNoReplacedBranches();
	}

	/**
	 * <pre>
	 * enum class E { A, B }
	 * fun example(e: E?) = when (e) {
	 *     E.A -> "a"
	 *     null -> "null"
	 *     else -> "else"
	 * }
	 * </pre>
	 */
	@Test
	public void should_filter_when_by_nullable_enum_with_null_and_else_cases() {
		final Range range1 = new Range();
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"example", "(LE;)Ljava/lang/String;", null, null);
		final Label l1 = new Label();
		final Label l2 = new Label();
		final Label caseNull = new Label();
		final Label caseElse = new Label();
		final Label caseA = new Label();
		final Label after = new Label();

		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitInsn(Opcodes.DUP);
		range1.fromInclusive = m.instructions.getLast();
		m.visitJumpInsn(Opcodes.IFNONNULL, l1);
		m.visitInsn(Opcodes.POP);
		m.visitInsn(Opcodes.ICONST_M1);
		m.visitJumpInsn(Opcodes.GOTO, l2);
		m.visitLabel(l1);
		m.visitFieldInsn(Opcodes.GETSTATIC, "ExampleKt$WhenMappings",
				"$EnumSwitchMapping$0", "[I");
		m.visitInsn(Opcodes.SWAP);
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "ExampleKt$Enum", "ordinal",
				"()I", false);
		m.visitInsn(Opcodes.IALOAD);
		m.visitLabel(l2);
		range1.toInclusive = m.instructions.getLast();
		m.visitTableSwitchInsn(-1, 1, caseElse, caseNull, caseA);

		m.visitLabel(caseA);
		m.visitLdcInsn("a");
		m.visitJumpInsn(Opcodes.GOTO, after);

		m.visitLabel(caseNull);
		m.visitLdcInsn("null");
		m.visitJumpInsn(Opcodes.GOTO, after);

		m.visitLabel(caseElse);
		m.visitLdcInsn("else");

		m.visitLabel(after);
		m.visitInsn(Opcodes.ARETURN);

		filter.filter(m, context, output);

		assertIgnored(range1);
		assertNoReplacedBranches();
	}

}
