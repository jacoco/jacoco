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

import org.jacoco.core.internal.instr.InstrSupport;
import org.junit.Test;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodNode;

public class StringSwitchJavacFilterTest implements IFilterOutput {

	private final IFilter filter = new StringSwitchJavacFilter();

	private final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
			"name", "()V", null, null);

	private AbstractInsnNode fromInclusive;
	private AbstractInsnNode toInclusive;

	@Test
	public void should_filter_code_generated_by_javac() {
		final Label h1 = new Label();
		final Label h1_2 = new Label();
		final Label h2 = new Label();
		final Label secondSwitch = new Label();
		final Label cases = new Label();

		m.visitInsn(Opcodes.ICONST_M1);
		m.visitVarInsn(Opcodes.ISTORE, 2);

		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "hashCode",
				"()I", false);
		m.visitLookupSwitchInsn(secondSwitch, new int[] { 97, 98 },
				new Label[] { h1, h2 });
		final AbstractInsnNode fromInclusive = m.instructions.getLast();

		m.visitLabel(h1);
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitLdcInsn("a");
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "equals",
				"(Ljava/lang/Object;)Z", false);
		// if not equal "a", then goto next comparison
		m.visitJumpInsn(Opcodes.IFEQ, h1_2);
		m.visitInsn(Opcodes.ICONST_0);
		m.visitVarInsn(Opcodes.ISTORE, 2);

		// goto secondSwitch
		m.visitJumpInsn(Opcodes.GOTO, secondSwitch);

		m.visitLabel(h1_2);
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitLdcInsn("\0a");
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "equals",
				"(Ljava/lang/Object;)Z", false);
		// if not equal "\0a", then goto second switch
		m.visitJumpInsn(Opcodes.IFEQ, secondSwitch);
		m.visitInsn(Opcodes.ICONST_1);
		m.visitVarInsn(Opcodes.ISTORE, 2);

		// goto secondSwitch
		m.visitJumpInsn(Opcodes.GOTO, secondSwitch);

		m.visitLabel(h2);
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitLdcInsn("b");
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "equals",
				"(Ljava/lang/Object;)Z", false);
		// if not equal "b", then goto second switch
		m.visitJumpInsn(Opcodes.IFEQ, secondSwitch);
		m.visitInsn(Opcodes.ICONST_2);
		m.visitVarInsn(Opcodes.ISTORE, 2);

		m.visitLabel(secondSwitch);
		final AbstractInsnNode toInclusive = m.instructions.getLast();
		m.visitVarInsn(Opcodes.ILOAD, 2);
		m.visitTableSwitchInsn(0, 2, cases);
		m.visitLabel(cases);

		filter.filter("Foo", "java/lang/Object", m, this);

		assertEquals(fromInclusive, this.fromInclusive);
		assertEquals(toInclusive, this.toInclusive);
	}

	@Test
	public void should_not_filter_code_generated_by_ECJ() {
		final Label h1 = new Label();
		final Label h2 = new Label();
		final Label cases = new Label();

		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "hashCode",
				"()I", false);
		m.visitTableSwitchInsn(0, 2, cases, h1, h2);

		m.visitLabel(h1);
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitLdcInsn("a");
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "equals",
				"(Ljava/lang/Object;)Z", false);
		// if equal "a", then goto its case
		m.visitJumpInsn(Opcodes.IFNE, cases);

		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitLdcInsn("\0a");
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "equals",
				"(Ljava/lang/Object;)Z", false);
		// if equal "\0a", then goto its case
		m.visitJumpInsn(Opcodes.IFNE, cases);

		// goto default case
		m.visitJumpInsn(Opcodes.GOTO, cases);

		m.visitLabel(h2);
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitLdcInsn("b");
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "equals",
				"(Ljava/lang/Object;)Z", false);
		// if equal "b", then goto its case
		m.visitJumpInsn(Opcodes.IFNE, cases);

		// goto default case
		m.visitJumpInsn(Opcodes.GOTO, cases);

		m.visitLabel(cases);

		filter.filter("Foo", "java/lang/Object", m, this);

		assertNull(this.fromInclusive);
		assertNull(this.toInclusive);
	}

	public void ignore(final AbstractInsnNode fromInclusive,
			final AbstractInsnNode toInclusive) {
		assertNull(this.fromInclusive);
		this.fromInclusive = fromInclusive;
		this.toInclusive = toInclusive;
	}

	public void merge(final AbstractInsnNode i1, final AbstractInsnNode i2) {
		fail();
	}

}
