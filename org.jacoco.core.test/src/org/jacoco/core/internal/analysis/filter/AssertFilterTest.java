/*******************************************************************************
 * Copyright (c) 2009, 2018 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *
 *******************************************************************************/
package org.jacoco.core.internal.analysis.filter;

import org.jacoco.core.internal.instr.InstrSupport;
import org.junit.Test;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodNode;

public class AssertFilterTest extends FilterTestBase {

	private final IFilter filter = new AssertFilter();

	@Test
	public void should_filter_initialize() {
		final Label disable = new Label();
		final Label init = new Label();

		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"<clinit>", "()V", null, null);
		m.visitLdcInsn(Type.getObjectType("Foo"));
		final AbstractInsnNode fromInclusive = m.instructions.getLast();

		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Class",
				"desiredAssertionStatus", "()Z", false);
		m.visitJumpInsn(Opcodes.IFNE, disable);
		m.visitInsn(Opcodes.ICONST_1);
		m.visitJumpInsn(Opcodes.GOTO, init);

		m.visitLabel(disable);
		m.visitInsn(Opcodes.ICONST_0);

		m.visitLabel(init);
		m.visitFieldInsn(Opcodes.PUTSTATIC, "Foo", "$assertionsDisabled", "Z");
		final AbstractInsnNode toInclusive = m.instructions.getLast();

		filter.filter(m, context, output);

		assertIgnored(new Range(fromInclusive, toInclusive));
	}

	@Test
	public void should_filter_assert() {
		final Label disabled = new Label();

		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"m", "(Z)V", null, null);
		m.visitFieldInsn(Opcodes.GETSTATIC, "Foo", "$assertionsDisabled", "Z");
		final AbstractInsnNode fromInclusive = m.instructions.getLast();

		m.visitJumpInsn(Opcodes.IFNE, disabled);
		final AbstractInsnNode toInclusive = m.instructions.getLast();

		m.visitVarInsn(Opcodes.ILOAD, 1);
		m.visitJumpInsn(Opcodes.IFNE, disabled);
		m.visitTypeInsn(Opcodes.NEW, "java/lang/AssertionError");
		m.visitInsn(Opcodes.DUP);
		m.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/AssertionError",
				"<init>", "()V", false);
		m.visitInsn(Opcodes.ATHROW);
		m.visitLabel(disabled);

		filter.filter(m, context, output);

		assertIgnored(new Range(fromInclusive, toInclusive));
	}

	@Test
	public void should_filter_assert_message() {
		final Label disabled = new Label();

		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"m", "(Z)V", null, null);
		m.visitFieldInsn(Opcodes.GETSTATIC, "Foo", "$assertionsDisabled", "Z");
		final AbstractInsnNode fromInclusive = m.instructions.getLast();

		m.visitJumpInsn(Opcodes.IFNE, disabled);
		final AbstractInsnNode toInclusive = m.instructions.getLast();

		m.visitVarInsn(Opcodes.ILOAD, 1);
		m.visitJumpInsn(Opcodes.IFNE, disabled);
		m.visitTypeInsn(Opcodes.NEW, "java/lang/AssertionError");
		m.visitInsn(Opcodes.DUP);
		m.visitLdcInsn("m");
		m.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/AssertionError",
				"<init>", "(Ljava/lang/Object;)V", false);
		m.visitInsn(Opcodes.ATHROW);
		m.visitLabel(disabled);

		filter.filter(m, context, output);

		assertIgnored(new Range(fromInclusive, toInclusive));
	}

}
