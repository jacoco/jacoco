/*******************************************************************************
 * Copyright (c) Copyright (c) Copyright (c) 2009, 2012 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.core.internal.instr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Arrays;

import org.jacoco.core.internal.flow.LabelInfo;
import org.junit.Test;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.EmptyVisitor;
import org.objectweb.asm.tree.FrameNode;

/**
 * Unit tests for {@link ProbeVariableInserter}.
 */
public class ProbeVariableInserterTest {

	private final MethodVisitor delegate = new EmptyVisitor();

	private int var;

	@Test
	public void testVariableStatic() {
		ProbeVariableInserter i = new ProbeVariableInserter(Opcodes.ACC_STATIC,
				"()V", delegate);
		assertEquals(0, i.variable);
	}

	@Test
	public void testVariableNonStatic() {
		ProbeVariableInserter i = new ProbeVariableInserter(0, "()V", delegate);
		assertEquals(1, i.variable);
	}

	@Test
	public void testVariableNonStatic_IZObject() {
		ProbeVariableInserter i = new ProbeVariableInserter(0,
				"(IZLjava/lang/Object;)V", delegate);
		assertEquals(4, i.variable);
	}

	@Test
	public void testVariableNonStatic_JD() {
		ProbeVariableInserter i = new ProbeVariableInserter(0, "(JD)V",
				delegate);
		assertEquals(5, i.variable);
	}

	@Test
	public void testVisitVarIns() {
		ProbeVariableInserter i = new ProbeVariableInserter(0, "(II)V",
				new EmptyVisitor() {
					@Override
					public void visitVarInsn(final int opcode, final int var) {
						ProbeVariableInserterTest.this.var = var;
					}
				});
		// Argument variables stay at the same position:
		i.visitVarInsn(Opcodes.ALOAD, 0);
		assertEquals(0, var);
		i.visitVarInsn(Opcodes.ALOAD, 1);
		assertEquals(1, var);
		i.visitVarInsn(Opcodes.ALOAD, 2);
		assertEquals(2, var);

		assertEquals(3, i.variable);

		// Local variables are shifted by one:
		i.visitVarInsn(Opcodes.ALOAD, 3);
		assertEquals(4, var);
		i.visitVarInsn(Opcodes.ALOAD, 4);
		assertEquals(5, var);
	}

	@Test
	public void testVisitIincInsn() {
		ProbeVariableInserter i = new ProbeVariableInserter(0, "(II)V",
				new EmptyVisitor() {
					@Override
					public void visitIincInsn(final int var, final int increment) {
						ProbeVariableInserterTest.this.var = var;
					}
				});
		// Argument variables stay at the same position:
		i.visitIincInsn(0, 999);
		assertEquals(0, var);
		i.visitIincInsn(1, 999);
		assertEquals(1, var);
		i.visitIincInsn(2, 999);
		assertEquals(2, var);

		assertEquals(3, i.variable);

		// Local variables are shifted by one:
		i.visitIincInsn(3, 999);
		assertEquals(4, var);
		i.visitIincInsn(4, 999);
		assertEquals(5, var);
	}

	@Test
	public void testVisitLocalVariable() {
		ProbeVariableInserter i = new ProbeVariableInserter(0, "(II)V",
				new EmptyVisitor() {
					@Override
					public void visitLocalVariable(final String name,
							final String desc, final String signature,
							final Label start, final Label end, final int index) {
						ProbeVariableInserterTest.this.var = index;
					}
				});
		// Argument variables stay at the same position:
		i.visitLocalVariable(null, null, null, null, null, 0);
		assertEquals(0, var);
		i.visitLocalVariable(null, null, null, null, null, 1);
		assertEquals(1, var);
		i.visitLocalVariable(null, null, null, null, null, 2);
		assertEquals(2, var);

		assertEquals(3, i.variable);

		// Local variables are shifted by one:
		i.visitLocalVariable(null, null, null, null, null, 3);
		assertEquals(4, var);
		i.visitLocalVariable(null, null, null, null, null, 4);
		assertEquals(5, var);
	}

	@Test
	public void testVisitMaxs() {
		ProbeVariableInserter i = new ProbeVariableInserter(0, "(II)V",
				new EmptyVisitor() {
					@Override
					public void visitMaxs(final int maxStack,
							final int maxLocals) {
						ProbeVariableInserterTest.this.var = maxLocals;
					}
				});
		i.visitMaxs(4, 8);
		assertEquals(9, var);
	}

	@Test
	public void testVisitFrame() {
		final FrameRecorder rec = new FrameRecorder();
		ProbeVariableInserter i = new ProbeVariableInserter(0, "(J)V", rec);

		// The first (implicit) frame must not be modified:
		i.visitFrame(Opcodes.F_NEW, 2, new Object[] { "LFoo;", Opcodes.LONG },
				0, new Object[0]);
		assertEquals(Arrays.asList((Object) "LFoo;", Opcodes.LONG),
				rec.frame.local);

		// Starting from the second frame on the probe variable is inserted:
		i.visitFrame(Opcodes.F_NEW, 3, new Object[] { "LFoo;", Opcodes.LONG,
				"Ljava/lang/String;" }, 0, new Object[0]);
		assertEquals(Arrays.asList((Object) "LFoo;", Opcodes.LONG, "[Z",
				"Ljava/lang/String;"), rec.frame.local);
	}

	@Test(expected = IllegalStateException.class)
	public void testVisitFrameNegative() {
		ProbeVariableInserter i = new ProbeVariableInserter(0, "(J)V", delegate);
		i.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
	}

	@Test
	public void testInsertProbeFrame() {
		final FrameRecorder rec = new FrameRecorder();
		ProbeVariableInserter i = new ProbeVariableInserter(0, "(J)V", rec);

		// The first (implicit) frame:
		i.visitFrame(Opcodes.F_NEW, 2, new Object[] { "LFoo;", Opcodes.LONG },
				0, new Object[0]);

		// There must be a label which is a multi-jump target:
		Label label = new Label();
		LabelInfo.setTarget(label);
		LabelInfo.setTarget(label);
		i.visitLabel(label);

		// Insert a frame for this label:
		i.visitFrame(Opcodes.F_NEW, 3, new Object[] { "LFoo;", Opcodes.LONG,
				"Ljava/lang/String;" }, 0, new Object[0]);

		// Insert this frame again:
		rec.frame = null;
		i.insertProbeFrame(label);

		assertEquals(Arrays.asList((Object) "LFoo;", Opcodes.LONG, "[Z",
				"Ljava/lang/String;"), rec.frame.local);
	}

	@Test
	public void testInsertProbeFrameNoFrameForLabel() {
		final FrameRecorder rec = new FrameRecorder();
		ProbeVariableInserter i = new ProbeVariableInserter(0, "(J)V", rec);

		// The first (implicit) frame:
		i.visitFrame(Opcodes.F_NEW, 2, new Object[] { "LFoo;", Opcodes.LONG },
				0, new Object[0]);

		// There must be a label which is a multi-jump target:
		Label label = new Label();
		LabelInfo.setTarget(label);
		LabelInfo.setTarget(label);
		i.visitLabel(label);

		// Insert a frame for this label:
		i.visitFrame(Opcodes.F_NEW, 3, new Object[] { "LFoo;", Opcodes.LONG,
				"Ljava/lang/String;" }, 0, new Object[0]);

		// Try to insert a frame for a different label:
		rec.frame = null;
		i.insertProbeFrame(new Label());
		assertNull(rec.frame);
	}

	@Test
	public void testInsertProbeFrameNoMultiTarget() {
		final FrameRecorder rec = new FrameRecorder();
		ProbeVariableInserter i = new ProbeVariableInserter(0, "(J)V", rec);

		// The first (implicit) frame:
		i.visitFrame(Opcodes.F_NEW, 2, new Object[] { "LFoo;", Opcodes.LONG },
				0, new Object[0]);

		// The frame for this label isn't required:
		Label label = new Label();
		i.visitLabel(label);

		// Insert a frame for this label:
		i.visitFrame(Opcodes.F_NEW, 3, new Object[] { "LFoo;", Opcodes.LONG,
				"Ljava/lang/String;" }, 0, new Object[0]);

		// Inserting a frame for the label shouldn't work:
		rec.frame = null;
		i.insertProbeFrame(label);
		assertNull(rec.frame);
	}

	private static class FrameRecorder extends EmptyVisitor {

		FrameNode frame;

		@Override
		public void visitFrame(final int type, final int nLocal,
				final Object[] local, final int nStack, final Object[] stack) {
			assertEquals(Opcodes.F_NEW, type);
			this.frame = new FrameNode(type, nLocal, local, nStack, stack);
		}
	}

}
