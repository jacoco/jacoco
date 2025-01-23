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
 * Unit tests for {@link StringSwitchFilter}.
 */
public class StringSwitchFilterTest extends FilterTestBase {

	private final IFilter filter = new StringSwitchFilter();

	@Test
	public void should_filter() {
		final Range range = new Range();
		final Set<AbstractInsnNode> expectedNewTargets = new HashSet<AbstractInsnNode>();

		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"name", "()V", null, null);

		final Label case1 = new Label();
		final Label case2 = new Label();
		final Label case3 = new Label();
		final Label caseDefault = new Label();
		final Label h1 = new Label();
		final Label h2 = new Label();

		// filter should not remember this unrelated slot
		m.visitLdcInsn("");
		m.visitVarInsn(Opcodes.ASTORE, 1);
		m.visitVarInsn(Opcodes.ALOAD, 1);

		// switch (...)
		m.visitInsn(Opcodes.DUP);
		m.visitVarInsn(Opcodes.ASTORE, 2);
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "hashCode",
				"()I", false);
		range.fromInclusive = m.instructions.getLast();
		m.visitTableSwitchInsn(97, 98, caseDefault, h1, h2);

		m.visitLabel(h1);

		m.visitVarInsn(Opcodes.ALOAD, 2);
		m.visitLdcInsn("a");
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "equals",
				"(Ljava/lang/Object;)Z", false);
		// if equal "a", then goto its case
		m.visitJumpInsn(Opcodes.IFNE, case1);

		m.visitVarInsn(Opcodes.ALOAD, 2);
		m.visitLdcInsn("\0a");
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "equals",
				"(Ljava/lang/Object;)Z", false);
		// if equal "\0a", then goto its case
		m.visitJumpInsn(Opcodes.IFNE, case2);

		// goto default case
		m.visitJumpInsn(Opcodes.GOTO, caseDefault);

		m.visitLabel(h2);

		m.visitVarInsn(Opcodes.ALOAD, 2);
		m.visitLdcInsn("b");
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "equals",
				"(Ljava/lang/Object;)Z", false);
		// if equal "b", then goto its case
		m.visitJumpInsn(Opcodes.IFNE, case3);

		// goto default case
		m.visitJumpInsn(Opcodes.GOTO, caseDefault);
		range.toInclusive = m.instructions.getLast();

		m.visitLabel(case1);
		m.visitInsn(Opcodes.RETURN);
		expectedNewTargets.add(m.instructions.getLast());
		m.visitLabel(case2);
		m.visitInsn(Opcodes.RETURN);
		expectedNewTargets.add(m.instructions.getLast());
		m.visitLabel(case3);
		m.visitInsn(Opcodes.RETURN);
		expectedNewTargets.add(m.instructions.getLast());
		m.visitLabel(caseDefault);
		m.visitInsn(Opcodes.RETURN);
		expectedNewTargets.add(m.instructions.getLast());

		filter.filter(m, context, output);

		assertIgnored(range);
		assertReplacedBranches(range.fromInclusive.getPrevious(),
				expectedNewTargets);
	}

	@Test
	public void should_filter_when_default_is_first() {
		final Range range = new Range();
		final Set<AbstractInsnNode> expectedNewTargets = new HashSet<AbstractInsnNode>();

		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"name", "()V", null, null);

		final Label case1 = new Label();
		final Label caseDefault = new Label();
		final Label h1 = new Label();

		// filter should not remember this unrelated slot
		m.visitLdcInsn("");
		m.visitVarInsn(Opcodes.ASTORE, 1);
		m.visitVarInsn(Opcodes.ALOAD, 1);

		// switch (...)
		m.visitInsn(Opcodes.DUP);
		m.visitVarInsn(Opcodes.ASTORE, 2);
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "hashCode",
				"()I", false);
		range.fromInclusive = m.instructions.getLast();
		m.visitLookupSwitchInsn(caseDefault, new int[] { 97 },
				new Label[] { h1 });

		m.visitLabel(h1);
		m.visitVarInsn(Opcodes.ALOAD, 2);
		m.visitLdcInsn("a");
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "equals",
				"(Ljava/lang/Object;)Z", false);
		// if equal "a", then goto its case
		m.visitJumpInsn(Opcodes.IFNE, case1);
		range.toInclusive = m.instructions.getLast();

		m.visitLabel(caseDefault);
		m.visitInsn(Opcodes.RETURN);
		expectedNewTargets.add(m.instructions.getLast());
		m.visitLabel(case1);
		m.visitInsn(Opcodes.RETURN);
		expectedNewTargets.add(m.instructions.getLast());

		filter.filter(m, context, output);

		assertIgnored(range);
		assertReplacedBranches(range.fromInclusive.getPrevious(),
				expectedNewTargets);
	}

	/**
	 * <pre>
	 * fun example(p: String) {
	 *   when (p) {
	 *     "a" -> return
	 *     "\u0000a" -> return
	 *     "b" -> return
	 *     "\u0000b" -> return
	 *     "c" -> return
	 *     "\u0000c" -> return
	 *   }
	 * }
	 * </pre>
	 */
	@Test
	public void should_filter_Kotlin_1_5() {
		final Range range = new Range();
		final Set<AbstractInsnNode> expectedNewTargets = new HashSet<AbstractInsnNode>();

		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"example", "()V", null, null);

		final Label h1 = new Label();
		final Label h2 = new Label();
		final Label h3 = new Label();
		final Label defaultCase = new Label();
		final Label case1 = new Label();
		final Label case2 = new Label();
		final Label case3 = new Label();
		final Label case4 = new Label();
		final Label case5 = new Label();
		final Label case6 = new Label();

		m.visitVarInsn(Opcodes.ASTORE, 1);
		m.visitVarInsn(Opcodes.ALOAD, 1);
		range.fromInclusive = m.instructions.getLast();
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "hashCode",
				"()I", false);
		m.visitTableSwitchInsn(97, 99, defaultCase, h1, h2, h3);

		m.visitLabel(h1);
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitLdcInsn("a");
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "equals",
				"(Ljava/lang/Object;)Z", false);
		m.visitJumpInsn(Opcodes.IFNE, case1);

		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitLdcInsn("\u0000a");
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "equals",
				"(Ljava/lang/Object;)Z", false);
		m.visitJumpInsn(Opcodes.IFNE, case2);

		m.visitJumpInsn(Opcodes.GOTO, defaultCase);

		m.visitLabel(h2);
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitLdcInsn("b");
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "equals",
				"(Ljava/lang/Object;)Z", false);
		m.visitJumpInsn(Opcodes.IFNE, case3);

		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitLdcInsn("\u0000b");
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "equals",
				"(Ljava/lang/Object;)Z", false);
		m.visitJumpInsn(Opcodes.IFNE, case4);

		m.visitJumpInsn(Opcodes.GOTO, defaultCase);

		m.visitLabel(h3);
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitLdcInsn("c");
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "equals",
				"(Ljava/lang/Object;)Z", false);
		m.visitJumpInsn(Opcodes.IFNE, case5);

		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitLdcInsn("\u0000c");
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "equals",
				"(Ljava/lang/Object;)Z", false);
		m.visitJumpInsn(Opcodes.IFNE, case6);

		m.visitJumpInsn(Opcodes.GOTO, defaultCase);
		range.toInclusive = m.instructions.getLast();

		m.visitLabel(case1);
		m.visitInsn(Opcodes.RETURN);
		expectedNewTargets.add(m.instructions.getLast());
		m.visitLabel(case2);
		m.visitInsn(Opcodes.RETURN);
		expectedNewTargets.add(m.instructions.getLast());
		m.visitLabel(case3);
		m.visitInsn(Opcodes.RETURN);
		expectedNewTargets.add(m.instructions.getLast());
		m.visitLabel(case4);
		m.visitInsn(Opcodes.RETURN);
		expectedNewTargets.add(m.instructions.getLast());
		m.visitLabel(case5);
		m.visitInsn(Opcodes.RETURN);
		expectedNewTargets.add(m.instructions.getLast());
		m.visitLabel(case6);
		m.visitInsn(Opcodes.RETURN);
		expectedNewTargets.add(m.instructions.getLast());
		m.visitLabel(defaultCase);
		m.visitInsn(Opcodes.RETURN);
		expectedNewTargets.add(m.instructions.getLast());

		filter.filter(m, context, output);

		assertIgnored(range);
		assertReplacedBranches(range.fromInclusive.getPrevious(),
				expectedNewTargets);
	}

	/**
	 * <pre>
	 * fun example(s: String?) = when (s) {
	 *     "a" -> "case a"
	 *     "b" -> "case b"
	 *     "c" -> "case c"
	 *     else -> "else"
	 * }
	 * </pre>
	 */
	@Test
	public void should_filter_Kotlin_nullable_else() {
		final Range range = new Range();
		final Set<AbstractInsnNode> expectedNewTargets = new HashSet<AbstractInsnNode>();

		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"example", "(Ljava/lang/String;)Ljava/lang/String;", null,
				null);
		final Label hashA = new Label();
		final Label hashB = new Label();
		final Label hashC = new Label();
		final Label caseA = new Label();
		final Label caseB = new Label();
		final Label caseC = new Label();
		final Label caseElse = new Label();
		final Label end = new Label();

		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitVarInsn(Opcodes.ASTORE, 2);
		m.visitVarInsn(Opcodes.ALOAD, 2);
		range.fromInclusive = m.instructions.getLast();
		m.visitJumpInsn(Opcodes.IFNULL, caseElse);
		m.visitVarInsn(Opcodes.ALOAD, 2);
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "hashCode",
				"()I", false);
		m.visitTableSwitchInsn(97, 99, caseElse,
				new Label[] { hashA, hashB, hashC });

		m.visitLabel(hashA);
		m.visitVarInsn(Opcodes.ALOAD, 2);
		m.visitLdcInsn("a");
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "equals",
				"(Ljava/lang/Object;)Z", false);
		m.visitJumpInsn(Opcodes.IFNE, caseA);
		m.visitJumpInsn(Opcodes.GOTO, caseElse);

		m.visitLabel(hashB);
		m.visitVarInsn(Opcodes.ALOAD, 2);
		m.visitLdcInsn("b");
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "equals",
				"(Ljava/lang/Object;)Z", false);
		m.visitJumpInsn(Opcodes.IFNE, caseB);
		m.visitJumpInsn(Opcodes.GOTO, caseElse);

		m.visitLabel(hashC);
		m.visitVarInsn(Opcodes.ALOAD, 2);
		m.visitLdcInsn("c");
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "equals",
				"(Ljava/lang/Object;)Z", false);
		m.visitJumpInsn(Opcodes.IFNE, caseC);
		m.visitJumpInsn(Opcodes.GOTO, caseElse);
		range.toInclusive = m.instructions.getLast();

		m.visitLabel(caseA);
		m.visitLdcInsn("case a");
		expectedNewTargets.add(m.instructions.getLast());
		m.visitJumpInsn(Opcodes.GOTO, end);

		m.visitLabel(caseB);
		m.visitLdcInsn("case b");
		expectedNewTargets.add(m.instructions.getLast());
		m.visitJumpInsn(Opcodes.GOTO, end);

		m.visitLabel(caseC);
		m.visitLdcInsn("case c");
		expectedNewTargets.add(m.instructions.getLast());
		m.visitJumpInsn(Opcodes.GOTO, end);

		m.visitLabel(caseElse);
		m.visitLdcInsn("else");
		expectedNewTargets.add(m.instructions.getLast());

		m.visitLabel(end);
		m.visitInsn(Opcodes.ARETURN);

		filter.filter(m, context, output);

		assertIgnored(range);
		assertReplacedBranches(range.fromInclusive.getPrevious(),
				expectedNewTargets);
	}

	/**
	 * <pre>
	 * fun example(s: String?) = when (s) {
	 *     "a" -> "case a"
	 *     "b" -> "case b"
	 *     "c" -> "case c"
	 *     null -> "null"
	 *     else -> "else"
	 * }
	 * </pre>
	 */
	@Test
	public void should_filter_Kotlin_nullable_case() {
		final Range range = new Range();
		final Set<AbstractInsnNode> expectedNewTargets = new HashSet<AbstractInsnNode>();

		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"example", "(Ljava/lang/String;)Ljava/lang/String;", null,
				null);
		final Label hashA = new Label();
		final Label hashB = new Label();
		final Label hashC = new Label();
		final Label caseA = new Label();
		final Label caseB = new Label();
		final Label caseC = new Label();
		final Label caseNull = new Label();
		final Label caseElse = new Label();
		final Label end = new Label();

		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitVarInsn(Opcodes.ASTORE, 2);
		m.visitVarInsn(Opcodes.ALOAD, 2);
		range.fromInclusive = m.instructions.getLast();
		m.visitInsn(Opcodes.DUP);
		m.visitJumpInsn(Opcodes.IFNULL, caseNull);
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "hashCode",
				"()I", false);
		m.visitTableSwitchInsn(97, 99, caseElse,
				new Label[] { hashA, hashB, hashC });

		m.visitLabel(hashA);
		m.visitVarInsn(Opcodes.ALOAD, 2);
		m.visitLdcInsn("a");
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "equals",
				"(Ljava/lang/Object;)Z", false);
		m.visitJumpInsn(Opcodes.IFNE, caseA);
		m.visitJumpInsn(Opcodes.GOTO, caseElse);

		m.visitLabel(hashB);
		m.visitVarInsn(Opcodes.ALOAD, 2);
		m.visitLdcInsn("b");
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "equals",
				"(Ljava/lang/Object;)Z", false);
		m.visitJumpInsn(Opcodes.IFNE, caseB);
		m.visitJumpInsn(Opcodes.GOTO, caseElse);

		m.visitLabel(hashC);
		m.visitVarInsn(Opcodes.ALOAD, 2);
		m.visitLdcInsn("c");
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "equals",
				"(Ljava/lang/Object;)Z", false);
		m.visitJumpInsn(Opcodes.IFNE, caseC);
		m.visitJumpInsn(Opcodes.GOTO, caseElse);
		range.toInclusive = m.instructions.getLast();

		m.visitLabel(caseA);
		m.visitLdcInsn("case a");
		expectedNewTargets.add(m.instructions.getLast());
		m.visitJumpInsn(Opcodes.GOTO, end);

		m.visitLabel(caseB);
		m.visitLdcInsn("case b");
		expectedNewTargets.add(m.instructions.getLast());
		m.visitJumpInsn(Opcodes.GOTO, end);

		m.visitLabel(caseC);
		m.visitLdcInsn("case c");
		expectedNewTargets.add(m.instructions.getLast());
		m.visitJumpInsn(Opcodes.GOTO, end);

		m.visitLabel(caseNull);
		m.visitInsn(Opcodes.POP);
		expectedNewTargets.add(m.instructions.getLast());
		m.visitLdcInsn("null");
		m.visitJumpInsn(Opcodes.GOTO, end);

		m.visitLabel(caseElse);
		m.visitLdcInsn("else");
		expectedNewTargets.add(m.instructions.getLast());

		m.visitLabel(end);
		m.visitInsn(Opcodes.ARETURN);

		filter.filter(m, context, output);

		assertIgnored(range);
		assertReplacedBranches(range.fromInclusive.getPrevious(),
				expectedNewTargets);
	}

	@Test
	public void should_not_filter_empty_lookup_switch() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"name", "(Ljava/lang/String;)V", null, null);
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitVarInsn(Opcodes.ASTORE, 2);
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "hashCode",
				"()I", false);
		final Label defaultCase = new Label();
		m.visitLookupSwitchInsn(defaultCase, null, new Label[] {});
		m.visitLabel(defaultCase);
		m.visitInsn(Opcodes.RETURN);

		filter.filter(m, context, output);

		assertIgnored();
	}

}
