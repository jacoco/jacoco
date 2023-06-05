/*******************************************************************************
 * Copyright (c) 2009, 2023 Mountainminds GmbH & Co. KG and Contributors
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

import org.jacoco.core.internal.instr.InstrSupport;
import org.junit.Test;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.HashSet;
import java.util.Set;

/**
 * Unit tests for {@link ExhaustiveSwitchFilter}.
 */
public class ExhaustiveSwitchFilterTest extends FilterTestBase {

	private final IFilter filter = new ExhaustiveSwitchFilter();

	/**
	 * <pre>
	 *   enum E {
	 *     A, B, C
	 *   }
	 *
	 *   int example(E e) {
	 *     return switch (e) {
	 *       case A -> 1;
	 *       case B -> 2;
	 *       case C -> 3;
	 *     };
	 *   }
	 * </pre>
	 */
	@Test
	public void should_filter_when_default_branch_has_LineNumber_of_switch() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"Example", "()I", null, null);

		final Label start = new Label();
		final Label end = new Label();
		m.visitLabel(start);
		m.visitLineNumber(0, start);
		m.visitFieldInsn(Opcodes.GETSTATIC, "Example$1", "$SwitchMap$Example$E",
				"[I");
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "Example$E", "ordinal", "()I",
				false);
		m.visitInsn(Opcodes.IALOAD);

		final Label dflt = new Label();
		final Label case1 = new Label();
		final Label case2 = new Label();
		final Label case3 = new Label();
		m.visitLookupSwitchInsn(dflt, new int[] { 1, 2, 3 },
				new Label[] { case1, case2, case3 });
		final AbstractInsnNode switchNode = m.instructions.getLast();
		final Set<AbstractInsnNode> newTargets = new HashSet<AbstractInsnNode>();

		m.visitLabel(dflt);
		final Range range = new Range();
		range.fromInclusive = m.instructions.getLast();
		m.visitLineNumber(0, dflt);
		m.visitTypeInsn(Opcodes.NEW, "java/lang/IncompatibleClassChangeError");
		m.visitInsn(Opcodes.DUP);
		m.visitMethodInsn(Opcodes.INVOKESPECIAL,
				"java/lang/IncompatibleClassChangeError", "<init>", "()V",
				false);
		m.visitInsn(Opcodes.ATHROW);
		range.toInclusive = m.instructions.getLast();

		m.visitLabel(case1);
		m.visitInsn(Opcodes.ICONST_1);
		newTargets.add(m.instructions.getLast());
		m.visitJumpInsn(Opcodes.GOTO, end);

		m.visitLabel(case2);
		m.visitInsn(Opcodes.ICONST_2);
		newTargets.add(m.instructions.getLast());

		m.visitLabel(case3);
		m.visitInsn(Opcodes.ICONST_3);
		newTargets.add(m.instructions.getLast());

		m.visitLabel(end);
		m.visitInsn(Opcodes.IRETURN);

		filter.filter(m, context, output);

		assertIgnored(range);
		assertReplacedBranches(switchNode, newTargets);
	}

	/**
	 * <pre>
	 *   enum E {
	 *     A, B, C
	 *   }
	 *
	 *   int example(E e) {
	 *     return switch (e) {
	 *       case A -> 1;
	 *       case B -> 2;
	 *       case C -> 3;
	 *     };
	 *   }
	 * </pre>
	 */
	@Test
	public void should_filter_when_default_branch_has_no_LineNumber() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"Example", "()I", null, null);

		final Label start = new Label();
		final Label end = new Label();
		m.visitLabel(start);
		m.visitLineNumber(0, start);
		m.visitFieldInsn(Opcodes.GETSTATIC, "Example$1", "$SwitchMap$Example$E",
				"[I");
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "Example$E", "ordinal", "()I",
				false);
		m.visitInsn(Opcodes.IALOAD);

		final Label dflt = new Label();
		final Label case1 = new Label();
		final Label case2 = new Label();
		final Label case3 = new Label();
		m.visitLookupSwitchInsn(dflt, new int[] { 1, 2, 3 },
				new Label[] { case1, case2, case3 });
		final AbstractInsnNode switchNode = m.instructions.getLast();
		final Set<AbstractInsnNode> newTargets = new HashSet<AbstractInsnNode>();

		m.visitLabel(dflt);
		final Range range = new Range();
		range.fromInclusive = m.instructions.getLast();
		m.visitTypeInsn(Opcodes.NEW, "java/lang/IncompatibleClassChangeError");
		m.visitInsn(Opcodes.DUP);
		m.visitMethodInsn(Opcodes.INVOKESPECIAL,
				"java/lang/IncompatibleClassChangeError", "<init>", "()V",
				false);
		m.visitInsn(Opcodes.ATHROW);
		range.toInclusive = m.instructions.getLast();

		m.visitLabel(case1);
		m.visitInsn(Opcodes.ICONST_1);
		newTargets.add(m.instructions.getLast());
		m.visitJumpInsn(Opcodes.GOTO, end);

		m.visitLabel(case2);
		m.visitInsn(Opcodes.ICONST_2);
		newTargets.add(m.instructions.getLast());

		m.visitLabel(case3);
		m.visitInsn(Opcodes.ICONST_3);
		newTargets.add(m.instructions.getLast());

		m.visitLabel(end);
		m.visitInsn(Opcodes.IRETURN);

		filter.filter(m, context, output);

		assertIgnored(range);
		assertReplacedBranches(switchNode, newTargets);
	}

	/**
	 * <pre>
	 *   enum E {
	 *     A, B, C
	 *   }
	 *
	 *   int example(E e) {
	 *     return switch (e) {
	 *       case A -> 1;
	 *       case B -> 2;
	 *       case C -> 3;
	 *     };
	 *   }
	 * </pre>
	 */
	@Test
	public void should_filter_when_default_branch_throws_Java_21_MatchException() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"Example", "()I", null, null);

		final Label start = new Label();
		final Label end = new Label();
		m.visitLabel(start);
		m.visitLineNumber(0, start);
		m.visitFieldInsn(Opcodes.GETSTATIC, "Example$1", "$SwitchMap$Example$E",
				"[I");
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "Example$E", "ordinal", "()I",
				false);
		m.visitInsn(Opcodes.IALOAD);

		final Label dflt = new Label();
		final Label case1 = new Label();
		final Label case2 = new Label();
		final Label case3 = new Label();
		m.visitLookupSwitchInsn(dflt, new int[] { 1, 2, 3 },
				new Label[] { case1, case2, case3 });
		final AbstractInsnNode switchNode = m.instructions.getLast();
		final Set<AbstractInsnNode> newTargets = new HashSet<AbstractInsnNode>();

		m.visitLabel(dflt);
		final Range range = new Range();
		range.fromInclusive = m.instructions.getLast();
		m.visitTypeInsn(Opcodes.NEW, "java/lang/MatchException");
		m.visitInsn(Opcodes.DUP);
		m.visitInsn(Opcodes.ACONST_NULL);
		m.visitInsn(Opcodes.ACONST_NULL);
		m.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/MatchException",
				"<init>", "(Ljava/lang/String;Ljava/lang/Throwable;)V", false);
		m.visitInsn(Opcodes.ATHROW);
		range.toInclusive = m.instructions.getLast();

		m.visitLabel(case1);
		m.visitInsn(Opcodes.ICONST_1);
		newTargets.add(m.instructions.getLast());
		m.visitJumpInsn(Opcodes.GOTO, end);

		m.visitLabel(case2);
		m.visitInsn(Opcodes.ICONST_2);
		newTargets.add(m.instructions.getLast());

		m.visitLabel(case3);
		m.visitInsn(Opcodes.ICONST_3);
		newTargets.add(m.instructions.getLast());

		m.visitLabel(end);
		m.visitInsn(Opcodes.IRETURN);

		filter.filter(m, context, output);

		assertIgnored(range);
		assertReplacedBranches(switchNode, newTargets);
	}

	/**
	 * <pre>
	 *   enum E {
	 *     A, B, C
	 *   }
	 *
	 *   int example(E e) {
	 *     return switch (e) {
	 *       case A -> 1;
	 *       case B -> 2;
	 *       default -> throw new IncompatibleClassChangeError();
	 *     };
	 *   }
	 * </pre>
	 */
	@Test
	public void should_not_filter_when_default_branch_has_LineNumber_different_from_switch() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"Example", "()I", null, null);

		final Label start = new Label();
		final Label end = new Label();
		m.visitLabel(start);
		m.visitLineNumber(0, start);
		m.visitFieldInsn(Opcodes.GETSTATIC, "Example$1", "$SwitchMap$Example$E",
				"[I");
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "Example$E", "ordinal", "()I",
				false);
		m.visitInsn(Opcodes.IALOAD);

		final Label dflt = new Label();
		final Label case1 = new Label();
		final Label case2 = new Label();
		m.visitLookupSwitchInsn(dflt, new int[] { 1, 2 },
				new Label[] { case1, case2 });

		m.visitLabel(dflt);
		m.visitLineNumber(1, dflt);
		m.visitTypeInsn(Opcodes.NEW, "java/lang/IncompatibleClassChangeError");
		m.visitInsn(Opcodes.DUP);
		m.visitMethodInsn(Opcodes.INVOKESPECIAL,
				"java/lang/IncompatibleClassChangeError", "<init>", "()V",
				false);
		m.visitInsn(Opcodes.ATHROW);

		m.visitLabel(case1);
		m.visitInsn(Opcodes.ICONST_1);
		m.visitJumpInsn(Opcodes.GOTO, end);

		m.visitLabel(case2);
		m.visitInsn(Opcodes.ICONST_2);

		m.visitLabel(end);
		m.visitInsn(Opcodes.IRETURN);

		filter.filter(m, context, output);

		assertIgnored();
	}

}
