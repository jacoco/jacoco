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
		m.visitTableSwitchInsn(97, 98, caseDefault, h1, h2);
		final AbstractInsnNode switchNode = m.instructions.getLast();

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
		final AbstractInsnNode expectedToInclusive = m.instructions.getLast();

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

		assertReplacedBranches(switchNode, expectedNewTargets);
		assertIgnored(new Range(switchNode.getNext(), expectedToInclusive));
	}

	@Test
	public void should_filter_when_default_is_first() {
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
		m.visitLookupSwitchInsn(caseDefault, new int[] { 97 },
				new Label[] { h1 });
		final AbstractInsnNode switchNode = m.instructions.getLast();

		m.visitLabel(h1);

		m.visitVarInsn(Opcodes.ALOAD, 2);
		m.visitLdcInsn("a");
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "equals",
				"(Ljava/lang/Object;)Z", false);
		// if equal "a", then goto its case
		m.visitJumpInsn(Opcodes.IFNE, case1);

		final AbstractInsnNode expectedToInclusive = m.instructions.getLast();

		m.visitLabel(caseDefault);
		m.visitInsn(Opcodes.RETURN);
		expectedNewTargets.add(m.instructions.getLast());
		m.visitLabel(case1);
		m.visitInsn(Opcodes.RETURN);
		expectedNewTargets.add(m.instructions.getLast());

		filter.filter(m, context, output);

		assertReplacedBranches(switchNode, expectedNewTargets);
		assertIgnored(new Range(switchNode.getNext(), expectedToInclusive));
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

		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "hashCode",
				"()I", false);
		m.visitTableSwitchInsn(97, 99, defaultCase, h1, h2, h3);

		m.visitLabel(h1);
		final AbstractInsnNode expectedFromInclusive = m.instructions.getLast();
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
		final AbstractInsnNode expectedToInclusive = m.instructions.getLast();

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

		assertIgnored(new Range(expectedFromInclusive, expectedToInclusive));
		assertReplacedBranches(expectedFromInclusive.getPrevious(),
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
