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
 * Unit tests for {@link StringSwitchEcjFilter}.
 */
public class StringSwitchEcjFilterTest extends FilterTestBase {

	private final IFilter filter = new StringSwitchEcjFilter();

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
