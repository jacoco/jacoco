/*******************************************************************************
 * Copyright (c) 2009, 2018 Mountainminds GmbH & Co. KG and Contributors
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
public class StringSwitchEcjFilterTest {

	private final IFilter filter = new StringSwitchEcjFilter();

	private final FilterContextMock context = new FilterContextMock();

	private AbstractInsnNode fromInclusive;
	private AbstractInsnNode toInclusive;

	private AbstractInsnNode source;
	private Set<AbstractInsnNode> newTargets;

	private final IFilterOutput output = new IFilterOutput() {
		public void ignore(final AbstractInsnNode fromInclusive,
				final AbstractInsnNode toInclusive) {
			assertNull(StringSwitchEcjFilterTest.this.fromInclusive);
			StringSwitchEcjFilterTest.this.fromInclusive = fromInclusive;
			StringSwitchEcjFilterTest.this.toInclusive = toInclusive;
		}

		public void merge(final AbstractInsnNode i1,
				final AbstractInsnNode i2) {
			fail();
		}

		public void replaceBranches(final AbstractInsnNode source,
				final Set<AbstractInsnNode> newTargets) {
			assertNull(StringSwitchEcjFilterTest.this.source);
			StringSwitchEcjFilterTest.this.source = source;
			StringSwitchEcjFilterTest.this.newTargets = newTargets;
		}
	};

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

		m.visitVarInsn(Opcodes.ALOAD, 1);

		m.visitVarInsn(Opcodes.ASTORE, 2);
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "hashCode",
				"()I", false);
		m.visitTableSwitchInsn(97, 98, caseDefault, h1, h2);

		m.visitLabel(h1);
		final AbstractInsnNode expectedFromInclusive = m.instructions.getLast();

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

		m.visitLabel(caseDefault);
		m.visitInsn(Opcodes.RETURN);
		expectedNewTargets.add(m.instructions.getLast());
		m.visitLabel(case1);
		m.visitInsn(Opcodes.RETURN);
		expectedNewTargets.add(m.instructions.getLast());
		m.visitLabel(case2);
		m.visitInsn(Opcodes.RETURN);
		expectedNewTargets.add(m.instructions.getLast());
		m.visitLabel(case3);
		m.visitInsn(Opcodes.RETURN);
		expectedNewTargets.add(m.instructions.getLast());

		filter.filter(m, context, output);

		assertEquals(expectedFromInclusive.getPrevious(), source);
		assertEquals(expectedNewTargets, newTargets);
		assertEquals(expectedFromInclusive, fromInclusive);
		assertEquals(expectedToInclusive, toInclusive);
	}

}
