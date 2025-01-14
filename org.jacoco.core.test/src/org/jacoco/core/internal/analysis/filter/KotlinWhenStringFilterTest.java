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

import java.util.ArrayList;

import org.jacoco.core.internal.instr.InstrSupport;
import org.junit.Test;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * Unit tests for {@link KotlinWhenStringFilter}.
 */
public class KotlinWhenStringFilterTest extends FilterTestBase {

	private final IFilter filter = new KotlinWhenStringFilter();

	private final ArrayList<Replacement> replacements = new ArrayList<Replacement>();

	@Test
	public void should_filter() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"name", "()V", null, null);

		final Label h1 = new Label();
		final Label sameHash = new Label();
		final Label h2 = new Label();
		final Label case1 = new Label();
		final Label case2 = new Label();
		final Label case3 = new Label();
		final Label defaultCase = new Label();

		// filter should not remember this unrelated slot
		m.visitLdcInsn("");
		m.visitVarInsn(Opcodes.ASTORE, 1);
		m.visitVarInsn(Opcodes.ALOAD, 1);

		// switch (...)
		m.visitVarInsn(Opcodes.ASTORE, 2);
		m.visitVarInsn(Opcodes.ALOAD, 2);
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "hashCode",
				"()I", false);
		m.visitTableSwitchInsn(97, 98, defaultCase, h1, h2);
		replacements.add(new Replacement(0, m.instructions.getLast(), 0));

		// case "a"
		m.visitLabel(h1);
		final AbstractInsnNode expectedFromInclusive = m.instructions.getLast();

		m.visitVarInsn(Opcodes.ALOAD, 2);
		m.visitLdcInsn("a");
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "equals",
				"(Ljava/lang/Object;)Z", false);
		m.visitJumpInsn(Opcodes.IFEQ, sameHash);
		m.visitJumpInsn(Opcodes.GOTO, case1);
		replacements.add(new Replacement(1, m.instructions.getLast(), 0));

		// case "\u0000a"
		m.visitLabel(sameHash);
		m.visitVarInsn(Opcodes.ALOAD, 2);
		m.visitLdcInsn("\u0000a");
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "equals",
				"(Ljava/lang/Object;)Z", false);
		m.visitJumpInsn(Opcodes.IFEQ, defaultCase);
		replacements.add(new Replacement(0, m.instructions.getLast(), 1));
		m.visitJumpInsn(Opcodes.GOTO, case2);
		replacements.add(new Replacement(2, m.instructions.getLast(), 0));

		// case "b"
		m.visitLabel(h2);
		m.visitVarInsn(Opcodes.ALOAD, 2);
		m.visitLdcInsn("\u0000a");
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "equals",
				"(Ljava/lang/Object;)Z", false);
		m.visitJumpInsn(Opcodes.IFEQ, defaultCase);
		replacements.add(new Replacement(0, m.instructions.getLast(), 1));
		m.visitJumpInsn(Opcodes.GOTO, case3);
		final AbstractInsnNode expectedToInclusive = m.instructions.getLast();
		replacements.add(new Replacement(3, m.instructions.getLast(), 0));

		m.visitLabel(case1);
		m.visitInsn(Opcodes.RETURN);
		m.visitLabel(case2);
		m.visitInsn(Opcodes.RETURN);
		m.visitLabel(case3);
		m.visitInsn(Opcodes.RETURN);
		m.visitLabel(defaultCase);
		m.visitInsn(Opcodes.RETURN);

		filter.filter(m, context, output);

		assertReplacedBranches(m, expectedFromInclusive.getPrevious(),
				replacements);
		assertIgnored(new Range(expectedFromInclusive, expectedToInclusive));
	}

	/**
	 * <pre>
	 * fun example(p: String) {
	 *   when (p) {
	 *     "b" -> return
	 *     "a" -> return
	 *     "\u0000a" -> return
	 *   }
	 * }
	 * </pre>
	 */
	@Test
	public void should_filter_when_biggest_hashCode_first() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"example", "(Ljava/lang/String;)V", null, null);

		final Label h1 = new Label();
		final Label sameHash = new Label();
		final Label h2 = new Label();
		final Label case1 = new Label();
		final Label case2 = new Label();
		final Label case3 = new Label();
		final Label defaultCase = new Label();

		m.visitVarInsn(Opcodes.ALOAD, 2);
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "hashCode",
				"()I", false);
		m.visitTableSwitchInsn(97, 98, defaultCase, h1, h2);
		replacements.add(new Replacement(0, m.instructions.getLast(), 0));

		m.visitLabel(h1);
		final AbstractInsnNode expectedFromInclusive = m.instructions.getLast();
		m.visitVarInsn(Opcodes.ALOAD, 2);
		m.visitLdcInsn("a");
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "equals",
				"(Ljava/lang/Object;)Z", false);
		m.visitJumpInsn(Opcodes.IFEQ, sameHash);
		m.visitJumpInsn(Opcodes.GOTO, case2);
		replacements.add(new Replacement(1, m.instructions.getLast(), 0));

		m.visitLabel(sameHash);
		m.visitVarInsn(Opcodes.ALOAD, 2);
		m.visitLdcInsn("\u0000a");
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "equals",
				"(Ljava/lang/Object;)Z", false);
		m.visitJumpInsn(Opcodes.IFEQ, defaultCase);
		replacements.add(new Replacement(0, m.instructions.getLast(), 1));
		m.visitJumpInsn(Opcodes.GOTO, case3);
		replacements.add(new Replacement(2, m.instructions.getLast(), 0));

		m.visitLabel(h2);
		m.visitVarInsn(Opcodes.ALOAD, 2);
		m.visitLdcInsn("b");
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "equals",
				"(Ljava/lang/Object;)Z", false);
		m.visitJumpInsn(Opcodes.IFEQ, defaultCase);
		final AbstractInsnNode expectedToInclusive = m.instructions.getLast();
		replacements.add(new Replacement(0, m.instructions.getLast(), 1));
		replacements.add(new Replacement(3, m.instructions.getLast(), 0));

		m.visitLabel(case1);
		m.visitInsn(Opcodes.RETURN);
		m.visitLabel(case2);
		m.visitInsn(Opcodes.RETURN);
		m.visitLabel(case3);
		m.visitInsn(Opcodes.RETURN);
		m.visitLabel(defaultCase);
		m.visitInsn(Opcodes.RETURN);

		filter.filter(m, context, output);

		assertIgnored(new Range(expectedFromInclusive, expectedToInclusive));
		assertReplacedBranches(m, expectedFromInclusive.getPrevious(),
				replacements);
	}

	@Test
	public void should_not_filter_empty_lookup_switch() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"name", "(Ljava/lang/String;)V", null, null);
		m.visitVarInsn(Opcodes.ALOAD, 1);
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
