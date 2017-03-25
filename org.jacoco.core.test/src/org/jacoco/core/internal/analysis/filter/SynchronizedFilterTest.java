/*******************************************************************************
 * Copyright (c) 2009, 2017 Mountainminds GmbH & Co. KG and Contributors
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

import org.jacoco.core.internal.instr.InstrSupport;
import org.junit.Test;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodNode;

public class SynchronizedFilterTest implements IFilterOutput {

	private final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
			"name", "()V", null, null);

	private AbstractInsnNode from;
	private AbstractInsnNode to;

	@Test
	public void ecj() {
		final Label start = new Label();
		final Label end = new Label();
		final Label handler = new Label();
		final Label handlerEnd = new Label();
		m.visitTryCatchBlock(start, end, handler, null);
		m.visitTryCatchBlock(handler, handlerEnd, handler, null);

		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitFieldInsn(Opcodes.GETFIELD, "Target", "lock",
				"Ljava/lang/Object;");
		m.visitInsn(Opcodes.DUP);
		m.visitVarInsn(Opcodes.ASTORE, 1);
		m.visitInsn(Opcodes.MONITORENTER);
		m.visitLabel(start);
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitInsn(Opcodes.NOP);
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitInsn(Opcodes.MONITOREXIT);
		m.visitLabel(end);
		final Label exit = new Label();
		m.visitJumpInsn(Opcodes.GOTO, exit);
		m.visitLabel(handler);
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitInsn(Opcodes.MONITOREXIT);
		m.visitLabel(handlerEnd);
		m.visitInsn(Opcodes.ATHROW);
		m.visitLabel(exit);
		m.visitInsn(Opcodes.RETURN);

		new SynchronizedFilter().filter(m, this);
		assertEquals(m.instructions.get(12), from);
		assertEquals(m.instructions.get(16), to);
	}

	public void ignore(AbstractInsnNode from, AbstractInsnNode to) {
		assertNull(this.from);
		this.from = from;
		this.to = to;
	}

}
