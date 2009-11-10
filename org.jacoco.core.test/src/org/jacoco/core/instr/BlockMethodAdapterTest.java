/*******************************************************************************
 * Copyright (c) 2009 Mountainminds GmbH & Co. KG and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 * $Id: $
 *******************************************************************************/
package org.jacoco.core.instr;

import static org.junit.Assert.assertEquals;

import org.jacoco.core.test.MethodRecorder;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Unit tests for {@link BufferedMethodVisitor}.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public class BlockMethodAdapterTest {

	private static class MockIds implements IProbeIdGenerator {

		private int c;

		public int nextId() {
			return c++;
		}
	}

	private MethodVisitor adapter;

	private MethodRecorder expected;

	private MethodRecorder actual;

	@Before
	public void setup() {
		actual = new MethodRecorder();
		expected = new MethodRecorder();
		adapter = new BlockMethodAdapter(actual, new MockIds(), 0, "test",
				"()V", null, null);
	}

	void sampleReturn() {
		return;
	}

	@Test
	public void testReturn() {
		adapter.visitCode();
		adapter.visitInsn(Opcodes.RETURN);
		adapter.visitMaxs(0, 1);
		adapter.visitEnd();

		expected.visitCode();
		expected.visitBlockEndBeforeJump(0);
		expected.visitInsn(Opcodes.RETURN);
		expected.visitBlockEnd(0);
		expected.visitMaxs(0, 1);
		expected.visitEnd();

		assertEquals(expected, actual);
	}

	void sampleThrow() {
		throw new RuntimeException();
	}

	@Test
	public void testThrow() {
		adapter.visitCode();
		adapter.visitTypeInsn(Opcodes.NEW, "java/lang/RuntimeException");
		adapter.visitInsn(Opcodes.DUP);
		adapter.visitMethodInsn(Opcodes.INVOKESPECIAL,
				"java/lang/RuntimeException", "<init>", "()V");
		adapter.visitInsn(Opcodes.ATHROW);
		adapter.visitMaxs(2, 1);
		adapter.visitEnd();

		expected.visitCode();
		expected.visitTypeInsn(Opcodes.NEW, "java/lang/RuntimeException");
		expected.visitInsn(Opcodes.DUP);
		expected.visitMethodInsn(Opcodes.INVOKESPECIAL,
				"java/lang/RuntimeException", "<init>", "()V");
		expected.visitBlockEndBeforeJump(0);
		expected.visitInsn(Opcodes.ATHROW);
		expected.visitBlockEnd(0);
		expected.visitMaxs(2, 1);
		expected.visitEnd();

		assertEquals(expected, actual);
	}

	int sampleIf(int x) {
		if (x > 0) {
			x++;
		}
		return x;
	}

	@Test
	public void testIf() {
		Label label0 = new Label();

		adapter.visitCode();
		adapter.visitVarInsn(Opcodes.ILOAD, 1);
		adapter.visitJumpInsn(Opcodes.IFLE, label0);
		adapter.visitIincInsn(1, 1);
		adapter.visitLabel(label0);
		adapter.visitVarInsn(Opcodes.ILOAD, 1);
		adapter.visitInsn(Opcodes.IRETURN);
		adapter.visitMaxs(1, 2);
		adapter.visitEnd();

		expected.visitCode();
		expected.visitVarInsn(Opcodes.ILOAD, 1);
		expected.visitBlockEndBeforeJump(0);
		expected.visitJumpInsn(Opcodes.IFLE, label0);
		expected.visitBlockEnd(0);
		expected.visitIincInsn(1, 1);
		expected.visitBlockEndBeforeJump(1);
		expected.visitBlockEnd(1);
		expected.visitLabel(label0);
		expected.visitVarInsn(Opcodes.ILOAD, 1);
		expected.visitBlockEndBeforeJump(2);
		expected.visitInsn(Opcodes.IRETURN);
		expected.visitBlockEnd(2);
		expected.visitMaxs(1, 2);
		expected.visitEnd();

		assertEquals(expected, actual);
	}

	int sampleTableSwitchp(int a) {
		int result = 0;
		switch (a) {
		case 1:
			result += 10;
		default:
			result += 20;
		}
		return result;
	}

	@Test
	public void testTableSwitch() {
		Label label0 = new Label();
		Label label1 = new Label();

		adapter.visitCode();
		adapter.visitInsn(Opcodes.ICONST_0);
		adapter.visitVarInsn(Opcodes.ISTORE, 2);
		adapter.visitVarInsn(Opcodes.ILOAD, 1);
		adapter.visitTableSwitchInsn(1, 1, label1, new Label[] { label0 });
		adapter.visitLabel(label0);
		adapter.visitIincInsn(2, 10);
		adapter.visitLabel(label1);
		adapter.visitIincInsn(2, 20);
		adapter.visitVarInsn(Opcodes.ILOAD, 2);
		adapter.visitInsn(Opcodes.IRETURN);
		adapter.visitMaxs(1, 3);
		adapter.visitEnd();

		expected.visitCode();
		expected.visitInsn(Opcodes.ICONST_0);
		expected.visitVarInsn(Opcodes.ISTORE, 2);
		expected.visitVarInsn(Opcodes.ILOAD, 1);
		expected.visitBlockEndBeforeJump(0);
		expected.visitTableSwitchInsn(1, 1, label1, new Label[] { label0 });
		expected.visitBlockEnd(0);
		expected.visitLabel(label0);
		expected.visitIincInsn(2, 10);
		expected.visitBlockEndBeforeJump(1);
		expected.visitBlockEnd(1);
		expected.visitLabel(label1);
		expected.visitIincInsn(2, 20);
		expected.visitVarInsn(Opcodes.ILOAD, 2);
		expected.visitBlockEndBeforeJump(2);
		expected.visitInsn(Opcodes.IRETURN);
		expected.visitBlockEnd(2);
		expected.visitMaxs(1, 3);
		expected.visitEnd();

		assertEquals(expected, actual);
	}

	int sampleLookupSwitch(int a) {
		int result = 0;
		switch (a) {
		case 1:
			result += 10;
		case 100:
			result += 20;
		default:
			result += 40;
		}
		return result;
	}

	@Test
	public void testLookupSwitch() {
		Label label0 = new Label();
		Label label1 = new Label();
		Label label2 = new Label();

		adapter.visitCode();
		adapter.visitInsn(Opcodes.ICONST_0);
		adapter.visitVarInsn(Opcodes.ISTORE, 2);
		adapter.visitVarInsn(Opcodes.ILOAD, 1);
		adapter.visitLookupSwitchInsn(label2, new int[] { 1, 100 },
				new Label[] { label0, label1 });
		adapter.visitLabel(label0);
		adapter.visitIincInsn(2, 10);
		adapter.visitLabel(label1);
		adapter.visitIincInsn(2, 20);
		adapter.visitLabel(label2);
		adapter.visitIincInsn(2, 40);
		adapter.visitVarInsn(Opcodes.ILOAD, 2);
		adapter.visitInsn(Opcodes.IRETURN);
		adapter.visitMaxs(1, 3);
		adapter.visitEnd();

		expected.visitCode();
		expected.visitInsn(Opcodes.ICONST_0);
		expected.visitVarInsn(Opcodes.ISTORE, 2);
		expected.visitVarInsn(Opcodes.ILOAD, 1);
		expected.visitBlockEndBeforeJump(0);
		expected.visitLookupSwitchInsn(label2, new int[] { 1, 100 },
				new Label[] { label0, label1 });
		expected.visitBlockEnd(0);
		expected.visitLabel(label0);
		expected.visitIincInsn(2, 10);
		expected.visitBlockEndBeforeJump(1);
		expected.visitBlockEnd(1);
		expected.visitLabel(label1);
		expected.visitIincInsn(2, 20);
		expected.visitBlockEndBeforeJump(2);
		expected.visitBlockEnd(2);
		expected.visitLabel(label2);
		expected.visitIincInsn(2, 40);
		expected.visitVarInsn(Opcodes.ILOAD, 2);
		expected.visitBlockEndBeforeJump(3);
		expected.visitInsn(Opcodes.IRETURN);
		expected.visitBlockEnd(3);
		expected.visitMaxs(1, 3);
		expected.visitEnd();

		assertEquals(expected, actual);
	}

	boolean sampleTryCatch(Runnable job) {
		try {
			job.run();
		} catch (IllegalStateException e) {
			return false;
		}
		return true;
	}

	@Test
	public void testTryCatch() {
		Label label0 = new Label();
		Label label1 = new Label();
		Label label2 = new Label();
		Label label3 = new Label();

		adapter.visitCode();
		adapter.visitTryCatchBlock(label0, label1, label2,
				"java/lang/IllegalStateException");
		adapter.visitLabel(label0);
		adapter.visitVarInsn(Opcodes.ALOAD, 1);
		adapter.visitMethodInsn(Opcodes.INVOKEINTERFACE, "java/lang/Runnable",
				"run", "()V");
		adapter.visitLabel(label1);
		adapter.visitJumpInsn(Opcodes.GOTO, label3);
		adapter.visitLabel(label2);
		adapter.visitVarInsn(Opcodes.ASTORE, 2);
		adapter.visitInsn(Opcodes.ICONST_0);
		adapter.visitInsn(Opcodes.IRETURN);
		adapter.visitLabel(label3);
		adapter.visitInsn(Opcodes.ICONST_1);
		adapter.visitInsn(Opcodes.IRETURN);
		adapter.visitMaxs(1, 3);
		adapter.visitEnd();

		expected.visitCode();
		expected.visitTryCatchBlock(label0, label1, label2,
				"java/lang/IllegalStateException");
		expected.visitLabel(label0);
		expected.visitVarInsn(Opcodes.ALOAD, 1);
		expected.visitMethodInsn(Opcodes.INVOKEINTERFACE, "java/lang/Runnable",
				"run", "()V");
		expected.visitBlockEndBeforeJump(0);
		expected.visitBlockEnd(0);
		expected.visitLabel(label1);
		expected.visitBlockEndBeforeJump(1);
		expected.visitJumpInsn(Opcodes.GOTO, label3);
		expected.visitBlockEnd(1);
		expected.visitLabel(label2);
		expected.visitVarInsn(Opcodes.ASTORE, 2);
		expected.visitInsn(Opcodes.ICONST_0);
		expected.visitBlockEndBeforeJump(2);
		expected.visitInsn(Opcodes.IRETURN);
		expected.visitBlockEnd(2);
		expected.visitLabel(label3);
		expected.visitInsn(Opcodes.ICONST_1);
		expected.visitBlockEndBeforeJump(3);
		expected.visitInsn(Opcodes.IRETURN);
		expected.visitBlockEnd(3);
		expected.visitMaxs(1, 3);
		expected.visitEnd();

		assertEquals(expected, actual);
	}

}
