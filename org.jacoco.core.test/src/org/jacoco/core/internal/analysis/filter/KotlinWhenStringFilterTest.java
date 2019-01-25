/*******************************************************************************
 * Copyright (c) 2009, 2019 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
 * Unit tests for {@link KotlinWhenStringFilter}.
 */
public class KotlinWhenStringFilterTest extends FilterTestBase {

	private final IFilter filter = new KotlinWhenStringFilter();

	@Test
	public void should_filter() {
		final Set<AbstractInsnNode> expectedNewTargets = new HashSet<AbstractInsnNode>();

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

		// case "a"
		m.visitLabel(h1);
		final AbstractInsnNode expectedFromInclusive = m.instructions.getLast();

		m.visitVarInsn(Opcodes.ALOAD, 2);
		m.visitLdcInsn("a");
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "equals",
				"(Ljava/lang/Object;)Z", false);
		m.visitJumpInsn(Opcodes.IFEQ, sameHash);
		m.visitJumpInsn(Opcodes.GOTO, case1);

		// case "\u0000a"
		m.visitLabel(sameHash);
		m.visitVarInsn(Opcodes.ALOAD, 2);
		m.visitLdcInsn("\u0000a");
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "equals",
				"(Ljava/lang/Object;)Z", false);
		m.visitJumpInsn(Opcodes.IFEQ, defaultCase);
		m.visitJumpInsn(Opcodes.GOTO, case2);

		// case "b"
		m.visitLabel(h2);
		m.visitVarInsn(Opcodes.ALOAD, 2);
		m.visitLdcInsn("\u0000a");
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "equals",
				"(Ljava/lang/Object;)Z", false);
		m.visitJumpInsn(Opcodes.IFEQ, defaultCase);
		m.visitJumpInsn(Opcodes.GOTO, case3);
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
		m.visitLabel(defaultCase);
		m.visitInsn(Opcodes.RETURN);
		expectedNewTargets.add(m.instructions.getLast());

		filter.filter(m, context, output);

		assertReplacedBranches(expectedFromInclusive.getPrevious(),
				expectedNewTargets);
		assertIgnored(new Range(expectedFromInclusive, expectedToInclusive));
	}

}
