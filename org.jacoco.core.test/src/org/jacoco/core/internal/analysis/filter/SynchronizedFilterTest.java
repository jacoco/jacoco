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
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodNode;

public class SynchronizedFilterTest implements IFilterOutput {

	private final SynchronizedFilter filter = new SynchronizedFilter();

	private final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
			"name", "()V", null, null);

	private AbstractInsnNode fromInclusive;
	private AbstractInsnNode toInclusive;

	@Test
	public void javac() {
		final Label start = new Label();
		final Label end = new Label();
		final Label handler = new Label();
		final Label handlerEnd = new Label();
		m.visitTryCatchBlock(start, end, handler, null);
		m.visitTryCatchBlock(handler, handlerEnd, handler, null);

		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitFieldInsn(Opcodes.GETFIELD, "Fun", "lock", "Ljava/lang/Object;");
		m.visitInsn(Opcodes.DUP);
		m.visitVarInsn(Opcodes.ASTORE, 1);
		m.visitInsn(Opcodes.MONITORENTER);
		m.visitLabel(start);
		m.visitInsn(Opcodes.NOP);
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitInsn(Opcodes.MONITOREXIT);
		m.visitLabel(end);
		final Label exit = new Label();
		m.visitJumpInsn(Opcodes.GOTO, exit);
		m.visitLabel(handler);
		m.visitVarInsn(Opcodes.ASTORE, 2);
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitInsn(Opcodes.MONITOREXIT);
		m.visitLabel(handlerEnd);
		m.visitVarInsn(Opcodes.ALOAD, 2);
		m.visitInsn(Opcodes.ATHROW);
		m.visitLabel(exit);
		m.visitInsn(Opcodes.RETURN);

		filter.filter(m, new FilterContextMock(), this);
		assertEquals(handler.info, fromInclusive);
		assertEquals(((LabelNode) exit.info).getPrevious(), toInclusive);
	}

	/**
	 * <pre>
	 *     try {
	 *         ...
	 *     } catch (Exception e) {
	 *         ...
	 *     } finally {
	 *         ...
	 *     }
	 * </pre>
	 */
	@Test
	public void javacTryCatchFinally() {
		final Label start = new Label();
		final Label end = new Label();
		final Label catchHandler = new Label();
		final Label finallyHandler = new Label();
		final Label catchHandlerEnd = new Label();
		m.visitTryCatchBlock(start, end, catchHandler, "java/lang/Exception");
		m.visitTryCatchBlock(start, end, finallyHandler, null);
		m.visitTryCatchBlock(catchHandler, catchHandlerEnd, finallyHandler,
				null);

		m.visitLabel(start);
		// body
		m.visitInsn(Opcodes.NOP);
		m.visitLabel(end);
		// finally
		m.visitInsn(Opcodes.NOP);
		final Label exit = new Label();
		m.visitJumpInsn(Opcodes.GOTO, exit);
		m.visitLabel(catchHandler);
		m.visitVarInsn(Opcodes.ASTORE, 1);
		// catch
		m.visitInsn(Opcodes.NOP);
		m.visitLabel(catchHandlerEnd);
		// finally
		m.visitInsn(Opcodes.NOP);
		m.visitJumpInsn(Opcodes.GOTO, exit);
		m.visitLabel(finallyHandler);
		m.visitVarInsn(Opcodes.ASTORE, 2);
		// finally
		m.visitInsn(Opcodes.NOP);
		m.visitVarInsn(Opcodes.ALOAD, 2);
		m.visitInsn(Opcodes.ATHROW);
		m.visitLabel(exit);
		m.visitInsn(Opcodes.RETURN);

		filter.filter(m, new FilterContextMock(), this);
		assertNull(fromInclusive);
	}

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

		filter.filter(m, new FilterContextMock(), this);
		assertEquals(handler.info, fromInclusive);
		assertEquals(((LabelNode) exit.info).getPrevious(), toInclusive);
	}

	public void ignore(AbstractInsnNode fromInclusive,
			AbstractInsnNode toInclusive) {
		assertNull(this.fromInclusive);
		this.fromInclusive = fromInclusive;
		this.toInclusive = toInclusive;
	}

	public void merge(final AbstractInsnNode i1, final AbstractInsnNode i2) {
		fail();
	}

}
