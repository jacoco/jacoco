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
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Unit tests for {@link ProbeInserter}.
 */
public class ProbeInserterTest {

	private MethodRecorder actual, expected;

	private IProbeArrayStrategy arrayStrategy;

	@Before
	public void setup() {
		actual = new MethodRecorder();
		expected = new MethodRecorder();
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
		ProbeInserter pi = new ProbeInserter(Opcodes.ACC_STATIC, "()V", actual,
				arrayStrategy);
		pi.insertProbe(0);

		expected.visitLdcInsn("init");
		expected.visitVarInsn(Opcodes.ALOAD, 0);
		expected.visitInsn(Opcodes.ICONST_0);
		expected.visitInsn(Opcodes.ICONST_1);
		expected.visitInsn(Opcodes.BASTORE);
	}

	@Test
	public void testVariableNonStatic() {
		ProbeInserter pi = new ProbeInserter(0, "()V", actual, arrayStrategy);
		pi.insertProbe(0);

		expected.visitLdcInsn("init");
		expected.visitVarInsn(Opcodes.ALOAD, 1);
		expected.visitInsn(Opcodes.ICONST_0);
		expected.visitInsn(Opcodes.ICONST_1);
		expected.visitInsn(Opcodes.BASTORE);
	}

	@Test
	public void testVariableNonStatic_IZObject() {
		ProbeInserter pi = new ProbeInserter(0, "(IZLjava/lang/Object;)V",
				actual, arrayStrategy);
		pi.insertProbe(0);

		expected.visitLdcInsn("init");
		expected.visitVarInsn(Opcodes.ALOAD, 4);
		expected.visitInsn(Opcodes.ICONST_0);
		expected.visitInsn(Opcodes.ICONST_1);
		expected.visitInsn(Opcodes.BASTORE);
	}

	@Test
	public void testVariableNonStatic_JD() {
		ProbeInserter pi = new ProbeInserter(0, "(JD)V", actual, arrayStrategy);
		pi.insertProbe(0);

		expected.visitLdcInsn("init");
		expected.visitVarInsn(Opcodes.ALOAD, 5);
		expected.visitInsn(Opcodes.ICONST_0);
		expected.visitInsn(Opcodes.ICONST_1);
		expected.visitInsn(Opcodes.BASTORE);
	}

	@Test
	public void testVisitProlog() {
		ProbeInserter pi = new ProbeInserter(0, "(I)V", actual, arrayStrategy);
		Label label = new Label();
		pi.visitLabel(label);
		pi.visitLineNumber(123, label);
		pi.visitFrame(Opcodes.F_NEW, 1, new Object[] { "I" }, 0, new Object[0]);
		pi.visitInsn(Opcodes.NOP);

		expected.visitFrame(Opcodes.F_NEW, 1, new Object[] { "I" }, 0,
				new Object[0]);
		expected.visitLdcInsn("init");
		expected.visitLabel(label);
		expected.visitLineNumber(123, label);
		expected.visitInsn(Opcodes.NOP);
	}

	@Test
	public void testVisitLabel() {
		ProbeInserter pi = new ProbeInserter(0, "()V", actual, arrayStrategy);
		Label label = new Label();
		pi.visitInsn(Opcodes.NOP);
		pi.visitLabel(label);

		expected.visitLdcInsn("init");
		expected.visitInsn(Opcodes.NOP);
		expected.visitLabel(label);
	}

	@Test
	public void testVisitLineNumber() {
		ProbeInserter pi = new ProbeInserter(0, "()V", actual, arrayStrategy);
		Label label = new Label();
		pi.visitInsn(Opcodes.NOP);
		pi.visitLineNumber(123, label);

		expected.visitLdcInsn("init");
		expected.visitInsn(Opcodes.NOP);
		expected.visitLineNumber(123, label);
	}

	@Test
	public void testVisitVarIns() {
		ProbeInserter pi = new ProbeInserter(0, "(II)V", actual, arrayStrategy);

		pi.visitVarInsn(Opcodes.ALOAD, 0);
		pi.visitVarInsn(Opcodes.ILOAD, 1);
		pi.visitVarInsn(Opcodes.ILOAD, 2);
		pi.visitVarInsn(Opcodes.ISTORE, 3);
		pi.visitVarInsn(Opcodes.FSTORE, 4);

		expected.visitLdcInsn("init");
		// Argument variables stay at the same position:
		expected.visitVarInsn(Opcodes.ALOAD, 0);
		expected.visitVarInsn(Opcodes.ILOAD, 1);
		expected.visitVarInsn(Opcodes.ILOAD, 2);

		// Local variables are shifted by one:
		expected.visitVarInsn(Opcodes.ISTORE, 4);
		expected.visitVarInsn(Opcodes.FSTORE, 5);
	}

	@Test
	public void testVisitIincInsn() {
		ProbeInserter pi = new ProbeInserter(0, "(II)V", actual, arrayStrategy);
		pi.visitIincInsn(0, 100);
		pi.visitIincInsn(1, 101);
		pi.visitIincInsn(2, 102);
		pi.visitIincInsn(3, 103);
		pi.visitIincInsn(4, 104);

		expected.visitLdcInsn("init");
		// Argument variables stay at the same position:
		expected.visitIincInsn(0, 100);
		expected.visitIincInsn(1, 101);
		expected.visitIincInsn(2, 102);

		// Local variables are shifted by one:
		expected.visitIincInsn(4, 103);
		expected.visitIincInsn(5, 104);
	}

	@Test
	public void testVisitLocalVariable() {
		ProbeInserter pi = new ProbeInserter(0, "(II)V", actual, arrayStrategy);

		pi.visitLocalVariable(null, null, null, null, null, 0);
		pi.visitLocalVariable(null, null, null, null, null, 1);
		pi.visitLocalVariable(null, null, null, null, null, 2);
		pi.visitLocalVariable(null, null, null, null, null, 3);
		pi.visitLocalVariable(null, null, null, null, null, 4);

		expected.visitLdcInsn("init");
		// Argument variables stay at the same position:
		expected.visitLocalVariable(null, null, null, null, null, 0);
		expected.visitLocalVariable(null, null, null, null, null, 1);
		expected.visitLocalVariable(null, null, null, null, null, 2);

		// Local variables are shifted by one:
		expected.visitLocalVariable(null, null, null, null, null, 4);
		expected.visitLocalVariable(null, null, null, null, null, 5);
	}

	@Test
	public void testVisitIntInsn() {
		ProbeInserter pi = new ProbeInserter(0, "()V", actual, arrayStrategy);
		pi.visitIntInsn(Opcodes.BIPUSH, 15);

		expected.visitLdcInsn("init");
		expected.visitIntInsn(Opcodes.BIPUSH, 15);
	}

	@Test
	public void testVisitTypeInsn() {
		ProbeInserter pi = new ProbeInserter(0, "()V", actual, arrayStrategy);
		pi.visitTypeInsn(Opcodes.NEW, "Foo");

		expected.visitLdcInsn("init");
		expected.visitTypeInsn(Opcodes.NEW, "Foo");
	}

	@Test
	public void testVisitFieldInsn() {
		ProbeInserter pi = new ProbeInserter(0, "()V", actual, arrayStrategy);
		pi.visitFieldInsn(Opcodes.GETFIELD, "Foo", "i", "I");

		expected.visitLdcInsn("init");
		expected.visitFieldInsn(Opcodes.GETFIELD, "Foo", "i", "I");
	}

	@Test
	public void testVisitMethodInsn() {
		ProbeInserter pi = new ProbeInserter(0, "()V", actual, arrayStrategy);
		pi.visitMethodInsn(Opcodes.INVOKEINTERFACE, "Foo", "doit", "()V");

		expected.visitLdcInsn("init");
		expected.visitMethodInsn(Opcodes.INVOKEINTERFACE, "Foo", "doit", "()V");
	}

	@Test
	public void testVisitJumpInsn() {
		ProbeInserter pi = new ProbeInserter(0, "()V", actual, arrayStrategy);
		Label label = new Label();
		pi.visitJumpInsn(Opcodes.GOTO, label);

		expected.visitLdcInsn("init");
		expected.visitJumpInsn(Opcodes.GOTO, label);
	}

	@Test
	public void testVisitLdcInsn() {
		ProbeInserter pi = new ProbeInserter(0, "()V", actual, arrayStrategy);
		pi.visitLdcInsn("JaCoCo");

		expected.visitLdcInsn("init");
		expected.visitLdcInsn("JaCoCo");
	}

	@Test
	public void testVisitTableSwitchInsn() {
		ProbeInserter pi = new ProbeInserter(0, "()V", actual, arrayStrategy);
		Label dflt = new Label();
		pi.visitTableSwitchInsn(0, 1, dflt, new Label[0]);

		expected.visitLdcInsn("init");
		expected.visitTableSwitchInsn(0, 1, dflt, new Label[0]);
	}

	@Test
	public void testVisitLookupSwitchInsn() {
		ProbeInserter pi = new ProbeInserter(0, "()V", actual, arrayStrategy);
		Label dflt = new Label();
		pi.visitLookupSwitchInsn(dflt, new int[0], new Label[0]);

		expected.visitLdcInsn("init");
		expected.visitLookupSwitchInsn(dflt, new int[0], new Label[0]);
	}

	@Test
	public void testVisitMultiANewArrayInsn() {
		ProbeInserter pi = new ProbeInserter(0, "()V", actual, arrayStrategy);
		pi.visitMultiANewArrayInsn("[[[I", 3);

		expected.visitLdcInsn("init");
		expected.visitMultiANewArrayInsn("[[[I", 3);
	}

	@Test
	public void testVisitMaxs1() {
		ProbeInserter pi = new ProbeInserter(0, "(II)V", actual, arrayStrategy);
		pi.visitInsn(Opcodes.NOP);
		pi.visitMaxs(0, 8);

		expected.visitLdcInsn("init");
		expected.visitInsn(Opcodes.NOP);
		expected.visitMaxs(5, 9);
	}

	@Test
	public void testVisitMaxs2() {
		ProbeInserter pi = new ProbeInserter(0, "(II)V", actual, arrayStrategy);
		pi.visitInsn(Opcodes.NOP);
		pi.visitMaxs(10, 8);

		expected.visitLdcInsn("init");
		expected.visitInsn(Opcodes.NOP);
		expected.visitMaxs(13, 9);
	}

	@Test
	public void testVisitFrame() {
		ProbeInserter pi = new ProbeInserter(0, "(J)V", actual, arrayStrategy);

		pi.visitFrame(Opcodes.F_NEW, 2, new Object[] { "LFoo;", Opcodes.LONG },
				0, new Object[0]);
		pi.visitInsn(Opcodes.NOP);
		pi.visitFrame(Opcodes.F_NEW, 3, new Object[] { "LFoo;", Opcodes.LONG,
				"Ljava/lang/String;" }, 0, new Object[0]);

		// The first (implicit) frame must not be modified:
		expected.visitFrame(Opcodes.F_NEW, 2, new Object[] { "LFoo;",
				Opcodes.LONG }, 0, new Object[0]);

		expected.visitLdcInsn("init");
		expected.visitInsn(Opcodes.NOP);

		// Starting from the second frame on the probe variable is inserted:
		expected.visitFrame(Opcodes.F_NEW, 4, new Object[] { "LFoo;",
				Opcodes.LONG, "[Z", "Ljava/lang/String;" }, 0, new Object[0]);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testVisitFrame_invalidType() {
		ProbeInserter pi = new ProbeInserter(0, "()V", actual, arrayStrategy);
		pi.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
	}

}
