/*******************************************************************************
 * Copyright (c) 2009, 2012 Mountainminds GmbH & Co. KG and Contributors
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

import org.jacoco.core.instr.MethodRecorder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Unit tests for {@link ProbeInserter}.
 */
public class ProbeInserterTest {

	private MethodRecorder actual, expected;

	private MethodVisitor actualVisitor, expectedVisitor;

	private IProbeArrayStrategy arrayStrategy;

	@Before
	public void setup() {
		actual = new MethodRecorder();
		actualVisitor = actual.getVisitor();
		expected = new MethodRecorder();
		expectedVisitor = expected.getVisitor();
		arrayStrategy = new IProbeArrayStrategy() {

			public int storeInstance(MethodVisitor mv, int variable) {
				mv.visitLdcInsn("init");
				return 5;
			}

			public void addMembers(ClassVisitor delegate) {
			}
		};
	}

	@After
	public void verify() {
		assertEquals(expected, actual);
	}

	@Test
	public void testVariableStatic() {
		ProbeInserter pi = new ProbeInserter(Opcodes.ACC_STATIC, "()V",
				actualVisitor, arrayStrategy);
		pi.insertProbe(0);

		expectedVisitor.visitLdcInsn("init");
		expectedVisitor.visitVarInsn(Opcodes.ALOAD, 0);
		expectedVisitor.visitInsn(Opcodes.ICONST_0);
		expectedVisitor.visitInsn(Opcodes.ICONST_1);
		expectedVisitor.visitInsn(Opcodes.BASTORE);
	}

	@Test
	public void testVariableNonStatic() {
		ProbeInserter pi = new ProbeInserter(0, "()V", actualVisitor,
				arrayStrategy);
		pi.insertProbe(0);

		expectedVisitor.visitLdcInsn("init");
		expectedVisitor.visitVarInsn(Opcodes.ALOAD, 1);
		expectedVisitor.visitInsn(Opcodes.ICONST_0);
		expectedVisitor.visitInsn(Opcodes.ICONST_1);
		expectedVisitor.visitInsn(Opcodes.BASTORE);
	}

	@Test
	public void testVariableNonStatic_IZObject() {
		ProbeInserter pi = new ProbeInserter(0, "(IZLjava/lang/Object;)V",
				actualVisitor, arrayStrategy);
		pi.insertProbe(0);

		expectedVisitor.visitLdcInsn("init");
		expectedVisitor.visitVarInsn(Opcodes.ALOAD, 4);
		expectedVisitor.visitInsn(Opcodes.ICONST_0);
		expectedVisitor.visitInsn(Opcodes.ICONST_1);
		expectedVisitor.visitInsn(Opcodes.BASTORE);
	}

	@Test
	public void testVariableNonStatic_JD() {
		ProbeInserter pi = new ProbeInserter(0, "(JD)V", actualVisitor,
				arrayStrategy);
		pi.insertProbe(0);

		expectedVisitor.visitLdcInsn("init");
		expectedVisitor.visitVarInsn(Opcodes.ALOAD, 5);
		expectedVisitor.visitInsn(Opcodes.ICONST_0);
		expectedVisitor.visitInsn(Opcodes.ICONST_1);
		expectedVisitor.visitInsn(Opcodes.BASTORE);
	}

	@Test
	public void testVisitProlog() {
		ProbeInserter pi = new ProbeInserter(0, "(I)V", actualVisitor,
				arrayStrategy);
		Label label = new Label();
		pi.visitLabel(label);
		pi.visitLineNumber(123, label);
		pi.visitFrame(Opcodes.F_NEW, 1, new Object[] { "I" }, 0, new Object[0]);
		pi.visitInsn(Opcodes.NOP);

		expectedVisitor.visitFrame(Opcodes.F_NEW, 1, new Object[] { "I" }, 0,
				new Object[0]);
		expectedVisitor.visitLdcInsn("init");
		expectedVisitor.visitLabel(label);
		expectedVisitor.visitLineNumber(123, label);
		expectedVisitor.visitInsn(Opcodes.NOP);
	}

	@Test
	public void testVisitLabel() {
		ProbeInserter pi = new ProbeInserter(0, "()V", actualVisitor,
				arrayStrategy);
		Label label = new Label();
		pi.visitInsn(Opcodes.NOP);
		pi.visitLabel(label);

		expectedVisitor.visitLdcInsn("init");
		expectedVisitor.visitInsn(Opcodes.NOP);
		expectedVisitor.visitLabel(label);
	}

	@Test
	public void testVisitLineNumber() {
		ProbeInserter pi = new ProbeInserter(0, "()V", actualVisitor,
				arrayStrategy);
		Label label = new Label();
		pi.visitInsn(Opcodes.NOP);
		pi.visitLineNumber(123, label);

		expectedVisitor.visitLdcInsn("init");
		expectedVisitor.visitInsn(Opcodes.NOP);
		expectedVisitor.visitLineNumber(123, label);
	}

	@Test
	public void testVisitVarIns() {
		ProbeInserter pi = new ProbeInserter(0, "(II)V", actualVisitor,
				arrayStrategy);

		pi.visitVarInsn(Opcodes.ALOAD, 0);
		pi.visitVarInsn(Opcodes.ILOAD, 1);
		pi.visitVarInsn(Opcodes.ILOAD, 2);
		pi.visitVarInsn(Opcodes.ISTORE, 3);
		pi.visitVarInsn(Opcodes.FSTORE, 4);

		expectedVisitor.visitLdcInsn("init");
		// Argument variables stay at the same position:
		expectedVisitor.visitVarInsn(Opcodes.ALOAD, 0);
		expectedVisitor.visitVarInsn(Opcodes.ILOAD, 1);
		expectedVisitor.visitVarInsn(Opcodes.ILOAD, 2);

		// Local variables are shifted by one:
		expectedVisitor.visitVarInsn(Opcodes.ISTORE, 4);
		expectedVisitor.visitVarInsn(Opcodes.FSTORE, 5);
	}

	@Test
	public void testVisitIincInsn() {
		ProbeInserter pi = new ProbeInserter(0, "(II)V", actualVisitor,
				arrayStrategy);
		pi.visitIincInsn(0, 100);
		pi.visitIincInsn(1, 101);
		pi.visitIincInsn(2, 102);
		pi.visitIincInsn(3, 103);
		pi.visitIincInsn(4, 104);

		expectedVisitor.visitLdcInsn("init");
		// Argument variables stay at the same position:
		expectedVisitor.visitIincInsn(0, 100);
		expectedVisitor.visitIincInsn(1, 101);
		expectedVisitor.visitIincInsn(2, 102);

		// Local variables are shifted by one:
		expectedVisitor.visitIincInsn(4, 103);
		expectedVisitor.visitIincInsn(5, 104);
	}

	@Test
	public void testVisitLocalVariable() {
		ProbeInserter pi = new ProbeInserter(0, "(II)V", actualVisitor,
				arrayStrategy);

		pi.visitLocalVariable(null, null, null, null, null, 0);
		pi.visitLocalVariable(null, null, null, null, null, 1);
		pi.visitLocalVariable(null, null, null, null, null, 2);
		pi.visitLocalVariable(null, null, null, null, null, 3);
		pi.visitLocalVariable(null, null, null, null, null, 4);

		expectedVisitor.visitLdcInsn("init");
		// Argument variables stay at the same position:
		expectedVisitor.visitLocalVariable(null, null, null, null, null, 0);
		expectedVisitor.visitLocalVariable(null, null, null, null, null, 1);
		expectedVisitor.visitLocalVariable(null, null, null, null, null, 2);

		// Local variables are shifted by one:
		expectedVisitor.visitLocalVariable(null, null, null, null, null, 4);
		expectedVisitor.visitLocalVariable(null, null, null, null, null, 5);
	}

	@Test
	public void testVisitIntInsn() {
		ProbeInserter pi = new ProbeInserter(0, "()V", actualVisitor,
				arrayStrategy);
		pi.visitIntInsn(Opcodes.BIPUSH, 15);

		expectedVisitor.visitLdcInsn("init");
		expectedVisitor.visitIntInsn(Opcodes.BIPUSH, 15);
	}

	@Test
	public void testVisitTypeInsn() {
		ProbeInserter pi = new ProbeInserter(0, "()V", actualVisitor,
				arrayStrategy);
		pi.visitTypeInsn(Opcodes.NEW, "Foo");

		expectedVisitor.visitLdcInsn("init");
		expectedVisitor.visitTypeInsn(Opcodes.NEW, "Foo");
	}

	@Test
	public void testVisitFieldInsn() {
		ProbeInserter pi = new ProbeInserter(0, "()V", actualVisitor,
				arrayStrategy);
		pi.visitFieldInsn(Opcodes.GETFIELD, "Foo", "i", "I");

		expectedVisitor.visitLdcInsn("init");
		expectedVisitor.visitFieldInsn(Opcodes.GETFIELD, "Foo", "i", "I");
	}

	@Test
	public void testVisitMethodInsn() {
		ProbeInserter pi = new ProbeInserter(0, "()V", actualVisitor,
				arrayStrategy);
		pi.visitMethodInsn(Opcodes.INVOKEINTERFACE, "Foo", "doit", "()V");

		expectedVisitor.visitLdcInsn("init");
		expectedVisitor.visitMethodInsn(Opcodes.INVOKEINTERFACE, "Foo", "doit",
				"()V");
	}

	@Test
	public void testInvokeDynamicInsn() {
		ProbeInserter pi = new ProbeInserter(0, "()V", actualVisitor,
				arrayStrategy);
		Handle handle = new Handle(0, null, null, null);
		pi.visitInvokeDynamicInsn("foo", "()V", handle);

		expectedVisitor.visitLdcInsn("init");
		expectedVisitor.visitInvokeDynamicInsn("foo", "()V", handle);
	}

	@Test
	public void testVisitJumpInsn() {
		ProbeInserter pi = new ProbeInserter(0, "()V", actualVisitor,
				arrayStrategy);
		Label label = new Label();
		pi.visitJumpInsn(Opcodes.GOTO, label);

		expectedVisitor.visitLdcInsn("init");
		expectedVisitor.visitJumpInsn(Opcodes.GOTO, label);
	}

	@Test
	public void testVisitLdcInsn() {
		ProbeInserter pi = new ProbeInserter(0, "()V", actualVisitor,
				arrayStrategy);
		pi.visitLdcInsn("JaCoCo");

		expectedVisitor.visitLdcInsn("init");
		expectedVisitor.visitLdcInsn("JaCoCo");
	}

	@Test
	public void testVisitTableSwitchInsn() {
		ProbeInserter pi = new ProbeInserter(0, "()V", actualVisitor,
				arrayStrategy);
		Label dflt = new Label();
		pi.visitTableSwitchInsn(0, 1, dflt, new Label[0]);

		expectedVisitor.visitLdcInsn("init");
		expectedVisitor.visitTableSwitchInsn(0, 1, dflt, new Label[0]);
	}

	@Test
	public void testVisitLookupSwitchInsn() {
		ProbeInserter pi = new ProbeInserter(0, "()V", actualVisitor,
				arrayStrategy);
		Label dflt = new Label();
		pi.visitLookupSwitchInsn(dflt, new int[0], new Label[0]);

		expectedVisitor.visitLdcInsn("init");
		expectedVisitor.visitLookupSwitchInsn(dflt, new int[0], new Label[0]);
	}

	@Test
	public void testVisitMultiANewArrayInsn() {
		ProbeInserter pi = new ProbeInserter(0, "()V", actualVisitor,
				arrayStrategy);
		pi.visitMultiANewArrayInsn("[[[I", 3);

		expectedVisitor.visitLdcInsn("init");
		expectedVisitor.visitMultiANewArrayInsn("[[[I", 3);
	}

	@Test
	public void testVisitMaxs1() {
		ProbeInserter pi = new ProbeInserter(0, "(II)V", actualVisitor,
				arrayStrategy);
		pi.visitInsn(Opcodes.NOP);
		pi.visitMaxs(0, 8);

		expectedVisitor.visitLdcInsn("init");
		expectedVisitor.visitInsn(Opcodes.NOP);
		expectedVisitor.visitMaxs(5, 9);
	}

	@Test
	public void testVisitMaxs2() {
		ProbeInserter pi = new ProbeInserter(0, "(II)V", actualVisitor,
				arrayStrategy);
		pi.visitInsn(Opcodes.NOP);
		pi.visitMaxs(10, 8);

		expectedVisitor.visitLdcInsn("init");
		expectedVisitor.visitInsn(Opcodes.NOP);
		expectedVisitor.visitMaxs(13, 9);
	}

	@Test
	public void testVisitFrame() {
		ProbeInserter pi = new ProbeInserter(0, "(J)V", actualVisitor,
				arrayStrategy);

		pi.visitFrame(Opcodes.F_NEW, 2, new Object[] { "Foo", Opcodes.LONG },
				0, new Object[0]);
		pi.visitInsn(Opcodes.NOP);
		pi.visitFrame(Opcodes.F_NEW, 3, new Object[] { "Foo", Opcodes.LONG,
				"java/lang/String" }, 0, new Object[0]);

		// The first (implicit) frame must not be modified:
		expectedVisitor.visitFrame(Opcodes.F_NEW, 2, new Object[] { "Foo",
				Opcodes.LONG }, 0, new Object[0]);

		expectedVisitor.visitLdcInsn("init");
		expectedVisitor.visitInsn(Opcodes.NOP);

		// Starting from the second frame on the probe variable is inserted:
		expectedVisitor.visitFrame(Opcodes.F_NEW, 4, new Object[] { "Foo",
				Opcodes.LONG, "[Z", "java/lang/String" }, 0, new Object[0]);
	}

	@Test
	public void testVisitFrameDeadCode() {
		ProbeInserter pi = new ProbeInserter(0, "(J)V", actualVisitor,
				arrayStrategy);

		pi.visitFrame(Opcodes.F_NEW, 2, new Object[] { "Foo", Opcodes.LONG },
				0, new Object[0]);
		pi.visitInsn(Opcodes.RETURN);

		// Such sequences are generated by ASM to replace dead code, see
		// http://asm.ow2.org/doc/developer-guide.html#deadcode
		pi.visitFrame(Opcodes.F_NEW, 0, new Object[] {}, 1,
				new Object[] { "java/lang/Throwable" });
		pi.visitInsn(Opcodes.NOP);
		pi.visitInsn(Opcodes.NOP);
		pi.visitInsn(Opcodes.ATHROW);

		// The first (implicit) frame must not be modified:
		expectedVisitor.visitFrame(Opcodes.F_NEW, 2, new Object[] { "Foo",
				Opcodes.LONG }, 0, new Object[0]);
		expectedVisitor.visitLdcInsn("init");
		expectedVisitor.visitInsn(Opcodes.RETURN);

		// The locals in this frame are filled with TOP up to the probe variable
		expectedVisitor.visitFrame(Opcodes.F_NEW, 3, new Object[] {
				Opcodes.TOP, Opcodes.TOP, "[Z", }, 1,
				new Object[] { "java/lang/Throwable" });
		expectedVisitor.visitInsn(Opcodes.NOP);
		expectedVisitor.visitInsn(Opcodes.NOP);
		expectedVisitor.visitInsn(Opcodes.ATHROW);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testVisitFrame_invalidType() {
		ProbeInserter pi = new ProbeInserter(0, "()V", actualVisitor,
				arrayStrategy);
		pi.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
	}

}
