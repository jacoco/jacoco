/*******************************************************************************
 * Copyright (c) 2009, 2013 Mountainminds GmbH & Co. KG and Contributors
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
import static org.objectweb.asm.Opcodes.*;

import org.jacoco.core.JaCoCo;
import org.jacoco.core.instr.MethodRecorder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.MethodNode;

/**
 * Unit tests for {@link ClassInstrumenter}.
 */
public class FrameTrackerTest {

	private static class FrameBuilder {

		private Object[] stack = new Object[0];
		private Object[] locals = new Object[0];

		FrameBuilder stack(Object... stack) {
			this.stack = stack;
			return this;
		}

		FrameBuilder locals(Object... locals) {
			this.locals = locals;
			return this;
		}

		void accept(MethodVisitor mv) {
			mv.visitFrame(F_NEW, locals.length, locals, stack.length, stack);
		}

	}

	private FrameBuilder before, after;

	private MethodNode mv;

	private Label label;

	@Before
	public void setup() {
		before = new FrameBuilder();
		after = new FrameBuilder();
		mv = new MethodNode(0, "test", "()V", null, null);
		label = new Label();
	}

	@After
	public void verify() {
		MethodRecorder actual = new MethodRecorder();
		MethodVisitor noLabels = new MethodVisitor(JaCoCo.ASM_API_VERSION,
				actual.getVisitor()) {
			@Override
			public void visitLabel(Label label) {
				// Ignore labels inserted by the tracker
			}
		};
		FrameTracker tracker = new FrameTracker("Test", ACC_STATIC, "test",
				"()V", noLabels);
		before.accept(tracker);
		mv.instructions.accept(tracker);
		tracker.insertFrame();

		MethodRecorder expected = new MethodRecorder();
		before.accept(expected.getVisitor());
		mv.instructions.accept(expected.getVisitor());
		after.accept(expected.getVisitor());

		assertEquals(expected, actual);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testVisitFrameIllegalFrameType() {
		FrameTracker tracker = new FrameTracker("Test", ACC_STATIC, "test",
				"()V", null);
		tracker.visitFrame(F_APPEND, 0, null, 0, null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testVisitInsnIllegalOpcode() {
		FrameTracker tracker = new FrameTracker("Test", ACC_STATIC, "test",
				"()V", null);
		tracker.visitInsn(GOTO);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testVisitIntInsnIllegalOpcode() {
		FrameTracker tracker = new FrameTracker("Test", ACC_STATIC, "test",
				"()V", null);
		tracker.visitIntInsn(NOP, 0);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testVisitVarInsnIllegalOpcode() {
		FrameTracker tracker = new FrameTracker("Test", ACC_STATIC, "test",
				"()V", null);
		tracker.visitVarInsn(NOP, 0);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testVisitTypeInsnIllegalOpcode() {
		FrameTracker tracker = new FrameTracker("Test", ACC_STATIC, "test",
				"()V", null);
		tracker.visitTypeInsn(NOP, "A");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testVisitFieldInsnIllegalOpcode() {
		FrameTracker tracker = new FrameTracker("Test", ACC_STATIC, "test",
				"()V", null);
		tracker.visitFieldInsn(NOP, "A", "x", "I");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testVisitJumpInsnIllegalOpcode() {
		FrameTracker tracker = new FrameTracker("Test", ACC_STATIC, "test",
				"()V", null);
		tracker.visitJumpInsn(NOP, new Label());
	}

	@Test(expected = IllegalStateException.class)
	public void testInvalidFrame_StackUnderflow() {
		FrameTracker tracker = new FrameTracker("Test", ACC_STATIC, "test",
				"()V", null);
		tracker.visitInsn(POP);
	}

	@Test(expected = IllegalStateException.class)
	public void testInvalidFrame_UndefinedLocal() {
		FrameTracker tracker = new FrameTracker("Test", ACC_STATIC, "test",
				"()V", null);
		tracker.visitVarInsn(ALOAD, 1);
	}

	@Test
	public void testArgumentsConstructor() {
		FrameBuilder expectedFrame = new FrameBuilder();
		expectedFrame.locals(UNINITIALIZED_THIS);
		testArguments(0, "<init>", "()V", expectedFrame);
	}

	@Test
	public void testArgumentsStatic() {
		FrameBuilder expectedFrame = new FrameBuilder();
		testArguments(Opcodes.ACC_STATIC, "test", "()V", expectedFrame);
	}

	@Test
	public void testArgumentsStaticIJZ() {
		FrameBuilder expectedFrame = new FrameBuilder();
		expectedFrame.locals(INTEGER, LONG, INTEGER);
		testArguments(Opcodes.ACC_STATIC, "test", "(IJZ)V", expectedFrame);
	}

	@Test
	public void testArgumentsStaticLArr() {
		FrameBuilder expectedFrame = new FrameBuilder();
		expectedFrame.locals("Foo", "[[S");
		testArguments(Opcodes.ACC_STATIC, "test", "(LFoo;[[S)V", expectedFrame);
	}

	@Test
	public void testArgumentsFD() {
		FrameBuilder expectedFrame = new FrameBuilder();
		expectedFrame.locals("Test", FLOAT, DOUBLE);
		testArguments(0, "test", "(FD)V", expectedFrame);
	}

	private void testArguments(int access, String name, String desc,
			FrameBuilder expectedFrame) {
		MethodRecorder actual = new MethodRecorder();
		FrameTracker tracker = new FrameTracker("Test", access, name, desc,
				actual.getVisitor());
		tracker.insertFrame();

		MethodRecorder expected = new MethodRecorder();
		expectedFrame.accept(expected.getVisitor());

		assertEquals(expected, actual);
	}

	@Test
	public void testFrameGaps() {
		before.locals().stack(INTEGER);
		mv.visitVarInsn(ISTORE, 3);
		after.locals(TOP, TOP, TOP, INTEGER).stack();
	}

	@Test
	public void testLargeFrame() {
		before.locals("A", "B", "C", "D", "E").stack("AA", "BB", "CC", "DD",
				"EE");
		mv.visitInsn(NOP);
		after.locals("A", "B", "C", "D", "E").stack("AA", "BB", "CC", "DD",
				"EE");
	}

	@Test
	public void AALOAD_multidim_obj() {
		before.locals().stack("[[Ljava/lang/String;", INTEGER);
		mv.visitInsn(AALOAD);
		after.locals().stack("[Ljava/lang/String;");
	}

	@Test
	public void AALOAD_multidim_prim() {
		before.locals().stack("[[I", INTEGER);
		mv.visitInsn(AALOAD);
		after.locals().stack("[I");
	}

	@Test
	public void AASTORE() {
		before.locals().stack("[Ljava/lang/String;", INTEGER,
				"[Ljava/lang/String;");
		mv.visitInsn(AASTORE);
		after.locals().stack();
	}

	@Test
	public void ACONST_NULL() {
		before.locals().stack();
		mv.visitInsn(ACONST_NULL);
		after.locals().stack(NULL);
	}

	@Test
	public void ALOAD() {
		before.locals(LONG, "X", INTEGER).stack();
		mv.visitVarInsn(ALOAD, 2);
		after.locals(LONG, "X", INTEGER).stack("X");
	}

	@Test
	public void ANEWARRAY() {
		before.locals().stack(INTEGER);
		mv.visitTypeInsn(ANEWARRAY, "java/lang/String");
		after.locals().stack("[Ljava/lang/String;");
	}

	@Test
	public void ANEWARRAY_multidim_obj() {
		before.locals().stack(INTEGER);
		mv.visitTypeInsn(ANEWARRAY, "[Ljava/lang/String;");
		after.locals().stack("[[Ljava/lang/String;");
	}

	@Test
	public void ANEWARRAY_multidim_prim() {
		before.locals().stack(INTEGER);
		mv.visitTypeInsn(ANEWARRAY, "[I");
		after.locals().stack("[[I");
	}

	@Test
	public void ARETURN() {
		before.locals().stack("java/lang/Object");
		mv.visitInsn(ARETURN);
		after.locals().stack();
	}

	@Test
	public void ARRAYLENGTH() {
		before.locals().stack("[Z");
		mv.visitInsn(ARRAYLENGTH);
		after.locals().stack(INTEGER);
	}

	@Test
	public void ASTORE() {
		before.locals(LONG, "X", INTEGER).stack("Y");
		mv.visitVarInsn(ASTORE, 3);
		after.locals(LONG, "X", "Y").stack();
	}

	@Test
	public void ATHROW() {
		before.locals().stack("java/lang/Exception");
		mv.visitInsn(ATHROW);
		after.locals().stack();
	}

	@Test
	public void BALOAD() {
		before.locals().stack("[B", INTEGER);
		mv.visitInsn(BALOAD);
		after.locals().stack(INTEGER);
	}

	@Test
	public void BASTORE() {
		before.locals().stack("[B", INTEGER, INTEGER);
		mv.visitInsn(BASTORE);
		after.locals().stack();
	}

	@Test
	public void BIPUSH() {
		before.locals().stack();
		mv.visitIntInsn(BIPUSH, 123);
		after.locals().stack(INTEGER);
	}

	@Test
	public void CALOAD() {
		before.locals().stack("[C", INTEGER);
		mv.visitInsn(CALOAD);
		after.locals().stack(INTEGER);
	}

	@Test
	public void CASTORE() {
		before.locals().stack("[C", INTEGER, INTEGER);
		mv.visitInsn(CASTORE);
		after.locals().stack();
	}

	@Test
	public void CHECKCAST() {
		before.locals().stack("java/lang/Object");
		mv.visitTypeInsn(CHECKCAST, "java/lang/String");
		after.locals().stack("java/lang/String");
	}

	@Test
	public void D2F() {
		before.locals().stack(DOUBLE);
		mv.visitInsn(D2F);
		after.locals().stack(FLOAT);
	}

	@Test
	public void D2I() {
		before.locals().stack(DOUBLE);
		mv.visitInsn(D2I);
		after.locals().stack(INTEGER);
	}

	@Test
	public void D2L() {
		before.locals().stack(DOUBLE);
		mv.visitInsn(D2L);
		after.locals().stack(LONG);
	}

	@Test
	public void DADD() {
		before.locals().stack(DOUBLE, DOUBLE);
		mv.visitInsn(DADD);
		after.locals().stack(DOUBLE);
	}

	@Test
	public void DALOAD() {
		before.locals().stack("[D", INTEGER);
		mv.visitInsn(DALOAD);
		after.locals().stack(DOUBLE);
	}

	@Test
	public void DASTORE() {
		before.locals().stack("[D", INTEGER, DOUBLE);
		mv.visitInsn(DASTORE);
		after.locals().stack();
	}

	@Test
	public void DCMPG() {
		before.locals().stack(DOUBLE, DOUBLE);
		mv.visitInsn(DCMPG);
		after.locals().stack(INTEGER);
	}

	@Test
	public void DCMPL() {
		before.locals().stack(DOUBLE, DOUBLE);
		mv.visitInsn(DCMPL);
		after.locals().stack(INTEGER);
	}

	@Test
	public void DCONST_0() {
		before.locals().stack();
		mv.visitInsn(DCONST_0);
		after.locals().stack(DOUBLE);
	}

	@Test
	public void DCONST_1() {
		before.locals().stack();
		mv.visitInsn(DCONST_1);
		after.locals().stack(DOUBLE);
	}

	@Test
	public void DDIV() {
		before.locals().stack(DOUBLE, DOUBLE);
		mv.visitInsn(DDIV);
		after.locals().stack(DOUBLE);
	}

	@Test
	public void DLOAD() {
		before.locals(DOUBLE).stack();
		mv.visitVarInsn(DLOAD, 0);
		after.locals(DOUBLE).stack(DOUBLE);
	}

	@Test
	public void DMUL() {
		before.locals().stack(DOUBLE, DOUBLE);
		mv.visitInsn(DMUL);
		after.locals().stack(DOUBLE);
	}

	@Test
	public void DNEG() {
		before.locals().stack(DOUBLE);
		mv.visitInsn(DNEG);
		after.locals().stack(DOUBLE);
	}

	@Test
	public void DREM() {
		before.locals().stack(DOUBLE, DOUBLE);
		mv.visitInsn(DREM);
		after.locals().stack(DOUBLE);
	}

	@Test
	public void DRETURN() {
		before.locals().stack(DOUBLE);
		mv.visitInsn(DRETURN);
		after.locals().stack();
	}

	@Test
	public void DSTORE() {
		before.locals().stack(DOUBLE);
		mv.visitVarInsn(DSTORE, 0);
		after.locals(DOUBLE).stack();
	}

	@Test
	public void DSUB() {
		before.locals().stack(DOUBLE, DOUBLE);
		mv.visitInsn(DSUB);
		after.locals().stack(DOUBLE);
	}

	@Test
	public void DUP() {
		before.locals().stack("A");
		mv.visitInsn(DUP);
		after.locals().stack("A", "A");
	}

	@Test
	public void DUP2_one_two_word_item() {
		before.locals().stack(LONG);
		mv.visitInsn(DUP2);
		after.locals().stack(LONG, LONG);
	}

	@Test
	public void DUP2_two_one_word_items() {
		before.locals().stack("A", "B");
		mv.visitInsn(DUP2);
		after.locals().stack("A", "B", "A", "B");
	}

	@Test
	public void DUP_X1() {
		before.locals().stack("A", "B");
		mv.visitInsn(DUP_X1);
		after.locals().stack("B", "A", "B");
	}

	@Test
	public void DUP2_X1_one_two_word_item() {
		before.locals().stack("A", LONG);
		mv.visitInsn(DUP2_X1);
		after.locals().stack(LONG, "A", LONG);
	}

	@Test
	public void DUP2_X1_two_one_word_items() {
		before.locals().stack("A", "B", "C");
		mv.visitInsn(DUP2_X1);
		after.locals().stack("B", "C", "A", "B", "C");
	}

	@Test
	public void DUP_X2() {
		before.locals().stack("A", "B", "C");
		mv.visitInsn(DUP_X2);
		after.locals().stack("C", "A", "B", "C");
	}

	@Test
	public void DUP2_X2_one_two_word_item() {
		before.locals().stack("A", "B", LONG);
		mv.visitInsn(DUP2_X2);
		after.locals().stack(LONG, "A", "B", LONG);
	}

	@Test
	public void DUP2_X2_two_one_word_items() {
		before.locals().stack("A", "B", "C", "D");
		mv.visitInsn(DUP2_X2);
		after.locals().stack("C", "D", "A", "B", "C", "D");
	}

	@Test
	public void F2D() {
		before.locals().stack(FLOAT);
		mv.visitInsn(F2D);
		after.locals().stack(DOUBLE);
	}

	@Test
	public void F2I() {
		before.locals().stack(FLOAT);
		mv.visitInsn(F2I);
		after.locals().stack(INTEGER);
	}

	@Test
	public void F2L() {
		before.locals().stack(FLOAT);
		mv.visitInsn(F2L);
		after.locals().stack(LONG);
	}

	@Test
	public void FADD() {
		before.locals().stack(FLOAT, FLOAT);
		mv.visitInsn(FADD);
		after.locals().stack(FLOAT);
	}

	@Test
	public void FALOAD() {
		before.locals().stack("[F", INTEGER);
		mv.visitInsn(FALOAD);
		after.locals().stack(FLOAT);
	}

	@Test
	public void FASTORE() {
		before.locals().stack("[F", INTEGER, FLOAT);
		mv.visitInsn(FASTORE);
		after.locals().stack();
	}

	@Test
	public void FCMPG() {
		before.locals().stack(FLOAT, FLOAT);
		mv.visitInsn(FCMPG);
		after.locals().stack(INTEGER);
	}

	@Test
	public void FCMPL() {
		before.locals().stack(FLOAT, FLOAT);
		mv.visitInsn(FCMPL);
		after.locals().stack(INTEGER);
	}

	@Test
	public void FCONST_0() {
		before.locals().stack();
		mv.visitInsn(FCONST_0);
		after.locals().stack(FLOAT);
	}

	@Test
	public void FCONST_1() {
		before.locals().stack();
		mv.visitInsn(FCONST_1);
		after.locals().stack(FLOAT);
	}

	@Test
	public void FCONST_2() {
		before.locals().stack();
		mv.visitInsn(FCONST_2);
		after.locals().stack(FLOAT);
	}

	@Test
	public void FDIV() {
		before.locals().stack(FLOAT, FLOAT);
		mv.visitInsn(FDIV);
		after.locals().stack(FLOAT);
	}

	@Test
	public void FLOAD() {
		before.locals(FLOAT).stack();
		mv.visitVarInsn(FLOAD, 0);
		after.locals(FLOAT).stack(FLOAT);
	}

	@Test
	public void FMUL() {
		before.locals().stack(FLOAT, FLOAT);
		mv.visitInsn(FMUL);
		after.locals().stack(FLOAT);
	}

	@Test
	public void FNEG() {
		before.locals().stack(FLOAT);
		mv.visitInsn(FNEG);
		after.locals().stack(FLOAT);
	}

	@Test
	public void FREM() {
		before.locals().stack(FLOAT, FLOAT);
		mv.visitInsn(FREM);
		after.locals().stack(FLOAT);
	}

	@Test
	public void FRETURN() {
		before.locals().stack(FLOAT);
		mv.visitInsn(FRETURN);
		after.locals().stack();
	}

	@Test
	public void FSTORE() {
		before.locals().stack(FLOAT);
		mv.visitVarInsn(FSTORE, 0);
		after.locals(FLOAT).stack();
	}

	@Test
	public void FSUB() {
		before.locals().stack(FLOAT, FLOAT);
		mv.visitInsn(FSUB);
		after.locals().stack(FLOAT);
	}

	@Test
	public void GETFIELD() {
		before.locals().stack("Test");
		mv.visitFieldInsn(GETFIELD, "Test", "f", "I");
		after.locals().stack(INTEGER);
	}

	@Test
	public void GETSTATIC() {
		before.locals().stack();
		mv.visitFieldInsn(GETSTATIC, "Test", "f", "Z");
		after.locals().stack(INTEGER);
	}

	@Test
	public void GETSTATIC_float() {
		before.locals().stack();
		mv.visitFieldInsn(GETSTATIC, "Test", "f", "F");
		after.locals().stack(FLOAT);
	}

	@Test
	public void GETSTATIC_double() {
		before.locals().stack();
		mv.visitFieldInsn(GETSTATIC, "Test", "f", "D");
		after.locals().stack(DOUBLE);
	}

	@Test
	public void GOTO() {
		before.locals().stack();
		mv.visitJumpInsn(GOTO, label);
		after.locals().stack();
	}

	@Test
	public void I2B() {
		before.locals().stack(INTEGER);
		mv.visitInsn(I2B);
		after.locals().stack(INTEGER);
	}

	@Test
	public void I2C() {
		before.locals().stack(INTEGER);
		mv.visitInsn(I2C);
		after.locals().stack(INTEGER);
	}

	@Test
	public void I2D() {
		before.locals().stack(INTEGER);
		mv.visitInsn(I2D);
		after.locals().stack(DOUBLE);
	}

	@Test
	public void I2F() {
		before.locals().stack(INTEGER);
		mv.visitInsn(I2F);
		after.locals().stack(FLOAT);
	}

	@Test
	public void I2L() {
		before.locals().stack(INTEGER);
		mv.visitInsn(I2L);
		after.locals().stack(LONG);
	}

	@Test
	public void I2S() {
		before.locals().stack(INTEGER);
		mv.visitInsn(I2S);
		after.locals().stack(INTEGER);
	}

	@Test
	public void IADD() {
		before.locals().stack(INTEGER, INTEGER);
		mv.visitInsn(IADD);
		after.locals().stack(INTEGER);
	}

	@Test
	public void IALOAD() {
		before.locals().stack("[I", INTEGER);
		mv.visitInsn(IALOAD);
		after.locals().stack(INTEGER);
	}

	@Test
	public void IAND() {
		before.locals().stack(INTEGER, INTEGER);
		mv.visitInsn(IAND);
		after.locals().stack(INTEGER);
	}

	@Test
	public void IASTORE() {
		before.locals().stack("[I", INTEGER, INTEGER);
		mv.visitInsn(IASTORE);
		after.locals().stack();
	}

	@Test
	public void ICONST_M1() {
		before.locals().stack();
		mv.visitInsn(ICONST_M1);
		after.locals().stack(INTEGER);
	}

	@Test
	public void ICONST_0() {
		before.locals().stack();
		mv.visitInsn(ICONST_0);
		after.locals().stack(INTEGER);
	}

	@Test
	public void ICONST_1() {
		before.locals().stack();
		mv.visitInsn(ICONST_1);
		after.locals().stack(INTEGER);
	}

	@Test
	public void ICONST_2() {
		before.locals().stack();
		mv.visitInsn(ICONST_2);
		after.locals().stack(INTEGER);
	}

	@Test
	public void ICONST_3() {
		before.locals().stack();
		mv.visitInsn(ICONST_3);
		after.locals().stack(INTEGER);
	}

	@Test
	public void ICONST_4() {
		before.locals().stack();
		mv.visitInsn(ICONST_4);
		after.locals().stack(INTEGER);
	}

	@Test
	public void ICONST_5() {
		before.locals().stack();
		mv.visitInsn(ICONST_5);
		after.locals().stack(INTEGER);
	}

	@Test
	public void IDIV() {
		before.locals().stack(INTEGER, INTEGER);
		mv.visitInsn(IDIV);
		after.locals().stack(INTEGER);
	}

	@Test
	public void IF_ACMPEQ() {
		before.locals().stack("A", "A");
		mv.visitJumpInsn(IF_ACMPEQ, label);
		after.locals().stack();
	}

	@Test
	public void IF_ACMPNE() {
		before.locals().stack("A", "A");
		mv.visitJumpInsn(IF_ACMPNE, label);
		after.locals().stack();
	}

	@Test
	public void IF_ICMPEQ() {
		before.locals().stack(INTEGER, INTEGER);
		mv.visitJumpInsn(IF_ICMPEQ, label);
		after.locals().stack();
	}

	@Test
	public void IF_ICMPGE() {
		before.locals().stack(INTEGER, INTEGER);
		mv.visitJumpInsn(IF_ICMPGE, label);
		after.locals().stack();
	}

	@Test
	public void IF_ICMPGT() {
		before.locals().stack(INTEGER, INTEGER);
		mv.visitJumpInsn(IF_ICMPGT, label);
		after.locals().stack();
	}

	@Test
	public void IF_ICMPLE() {
		before.locals().stack(INTEGER, INTEGER);
		mv.visitJumpInsn(IF_ICMPLE, label);
		after.locals().stack();
	}

	@Test
	public void IF_ICMPLT() {
		before.locals().stack(INTEGER, INTEGER);
		mv.visitJumpInsn(IF_ICMPLT, label);
		after.locals().stack();
	}

	@Test
	public void IF_ICMPNE() {
		before.locals().stack(INTEGER, INTEGER);
		mv.visitJumpInsn(IF_ICMPNE, label);
		after.locals().stack();
	}

	@Test
	public void IFEQ() {
		before.locals().stack(INTEGER);
		mv.visitJumpInsn(IFEQ, label);
		after.locals().stack();
	}

	@Test
	public void IFGE() {
		before.locals().stack(INTEGER);
		mv.visitJumpInsn(IFGE, label);
		after.locals().stack();
	}

	@Test
	public void IFGT() {
		before.locals().stack(INTEGER);
		mv.visitJumpInsn(IFGT, label);
		after.locals().stack();
	}

	@Test
	public void IFLE() {
		before.locals().stack(INTEGER);
		mv.visitJumpInsn(IFLE, label);
		after.locals().stack();
	}

	@Test
	public void IFLT() {
		before.locals().stack(INTEGER);
		mv.visitJumpInsn(IFLT, label);
		after.locals().stack();
	}

	@Test
	public void IFNE() {
		before.locals().stack(INTEGER);
		mv.visitJumpInsn(IFNE, label);
		after.locals().stack();
	}

	@Test
	public void IFNONNULL() {
		before.locals().stack("A");
		mv.visitJumpInsn(IFNONNULL, label);
		after.locals().stack();
	}

	@Test
	public void IFNULL() {
		before.locals().stack("A");
		mv.visitJumpInsn(IFNULL, label);
		after.locals().stack();
	}

	@Test
	public void IINC() {
		before.locals(INTEGER).stack();
		mv.visitIincInsn(0, 1);
		after.locals(INTEGER).stack();
	}

	@Test
	public void ILOAD() {
		before.locals(INTEGER).stack();
		mv.visitVarInsn(ILOAD, 0);
		after.locals(INTEGER).stack(INTEGER);
	}

	@Test
	public void IMUL() {
		before.locals().stack(INTEGER, INTEGER);
		mv.visitInsn(IMUL);
		after.locals().stack(INTEGER);
	}

	@Test
	public void INEG() {
		before.locals().stack(INTEGER);
		mv.visitInsn(INEG);
		after.locals().stack(INTEGER);
	}

	@Test
	public void INSTANCEOF() {
		before.locals().stack("java/lang/String");
		mv.visitTypeInsn(INSTANCEOF, "java/lang/String");
		after.locals().stack(INTEGER);
	}

	@Test
	public void INVOKEDYNAMIC() {
		before.locals().stack("java/lang/String");
		mv.visitInvokeDynamicInsn("foo", "(Ljava/lang/String;)I", new Handle(0,
				null, null, null));
		after.locals().stack(INTEGER);
	}

	@Test
	public void INVOKEINTERFACE() {
		before.locals().stack("Test");
		mv.visitMethodInsn(INVOKEVIRTUAL, "Test", "getSize", "()I");
		after.locals().stack(INTEGER);
	}

	@Test
	public void INVOKESPECIAL() {
		before.locals().stack("Test", LONG, LONG);
		mv.visitMethodInsn(INVOKEVIRTUAL, "Test", "add", "(JJ)J");
		after.locals().stack(LONG);
	}

	@Test
	public void INVOKESPECIAL_initsuper() {
		before.locals(UNINITIALIZED_THIS).stack(UNINITIALIZED_THIS);
		mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "<init>", "()V");
		after.locals("Test").stack();
	}

	@Test
	public void INVOKESTATIC() {
		before.locals().stack(LONG, LONG);
		mv.visitMethodInsn(INVOKESTATIC, "Test", "add", "(JJ)J");
		after.locals().stack(LONG);
	}

	@Test
	public void INVOKEVIRTUAL() {
		before.locals().stack("Test", INTEGER, DOUBLE);
		mv.visitMethodInsn(INVOKEVIRTUAL, "Test", "run", "(ID)V");
		after.locals().stack();
	}

	@Test
	public void IOR() {
		before.locals().stack(INTEGER, INTEGER);
		mv.visitInsn(IOR);
		after.locals().stack(INTEGER);
	}

	@Test
	public void IREM() {
		before.locals().stack(INTEGER, INTEGER);
		mv.visitInsn(IREM);
		after.locals().stack(INTEGER);
	}

	@Test
	public void IRETURN() {
		before.locals().stack(INTEGER);
		mv.visitInsn(IRETURN);
		after.locals().stack();
	}

	@Test
	public void ISHL() {
		before.locals().stack(INTEGER, INTEGER);
		mv.visitInsn(ISHL);
		after.locals().stack(INTEGER);
	}

	@Test
	public void ISHR() {
		before.locals().stack(INTEGER, INTEGER);
		mv.visitInsn(ISHR);
		after.locals().stack(INTEGER);
	}

	@Test
	public void ISSTORE() {
		before.locals().stack(INTEGER);
		mv.visitVarInsn(ISTORE, 0);
		after.locals(INTEGER).stack();
	}

	@Test
	public void ISUB() {
		before.locals().stack(INTEGER, INTEGER);
		mv.visitInsn(ISUB);
		after.locals().stack(INTEGER);
	}

	@Test
	public void IUSHR() {
		before.locals().stack(INTEGER, INTEGER);
		mv.visitInsn(IUSHR);
		after.locals().stack(INTEGER);
	}

	@Test
	public void IXOR() {
		before.locals().stack(INTEGER, INTEGER);
		mv.visitInsn(IXOR);
		after.locals().stack(INTEGER);
	}

	@Test
	public void L2D() {
		before.locals().stack(LONG);
		mv.visitInsn(L2D);
		after.locals().stack(DOUBLE);
	}

	@Test
	public void L2F() {
		before.locals().stack(LONG);
		mv.visitInsn(L2F);
		after.locals().stack(FLOAT);
	}

	@Test
	public void L2I() {
		before.locals().stack(LONG);
		mv.visitInsn(L2I);
		after.locals().stack(INTEGER);
	}

	@Test
	public void LADD() {
		before.locals().stack(LONG, LONG);
		mv.visitInsn(LADD);
		after.locals().stack(LONG);
	}

	@Test
	public void LALOAD() {
		before.locals().stack("L[", INTEGER);
		mv.visitInsn(LALOAD);
		after.locals().stack(LONG);
	}

	@Test
	public void LAND() {
		before.locals().stack(LONG, LONG);
		mv.visitInsn(LAND);
		after.locals().stack(LONG);
	}

	@Test
	public void LASTORE() {
		before.locals().stack("L[", INTEGER, LONG);
		mv.visitInsn(LASTORE);
		after.locals().stack();
	}

	@Test
	public void LCMP() {
		before.locals().stack(LONG, LONG);
		mv.visitInsn(LCMP);
		after.locals().stack(INTEGER);
	}

	@Test
	public void LCONST_0() {
		before.locals().stack();
		mv.visitInsn(LCONST_0);
		after.locals().stack(LONG);
	}

	@Test
	public void LCONST_1() {
		before.locals().stack();
		mv.visitInsn(LCONST_1);
		after.locals().stack(LONG);
	}

	@Test
	public void LDC_int() {
		before.locals().stack();
		mv.visitLdcInsn(Integer.valueOf(123));
		after.locals().stack(INTEGER);
	}

	@Test
	public void LDC_float() {
		before.locals().stack();
		mv.visitLdcInsn(Float.valueOf(123));
		after.locals().stack(FLOAT);
	}

	@Test
	public void LDC_long() {
		before.locals().stack();
		mv.visitLdcInsn(Long.valueOf(123));
		after.locals().stack(LONG);
	}

	@Test
	public void LDC_double() {
		before.locals().stack();
		mv.visitLdcInsn(Double.valueOf(123));
		after.locals().stack(DOUBLE);
	}

	@Test
	public void LDC_String() {
		before.locals().stack();
		mv.visitLdcInsn("Hello VM!");
		after.locals().stack("java/lang/String");
	}

	@Test
	public void LDC_Class() {
		before.locals().stack();
		mv.visitLdcInsn(Type.getType("[java/lang/Runnable;"));
		after.locals().stack("java/lang/Class");
	}

	@Test(expected = IllegalArgumentException.class)
	public void LDC_invalidType() {
		FrameTracker tracker = new FrameTracker("Test", ACC_STATIC, "test",
				"()V", null);
		tracker.visitLdcInsn(Byte.valueOf((byte) 123));
	}

	@Test
	public void LDIV() {
		before.locals().stack(LONG, LONG);
		mv.visitInsn(LDIV);
		after.locals().stack(LONG);
	}

	@Test
	public void LLOAD() {
		before.locals(LONG).stack();
		mv.visitVarInsn(LLOAD, 0);
		after.locals(LONG).stack(LONG);
	}

	@Test
	public void LMUL() {
		before.locals().stack(LONG, LONG);
		mv.visitInsn(LMUL);
		after.locals().stack(LONG);
	}

	@Test
	public void LNEG() {
		before.locals().stack(LONG);
		mv.visitInsn(LNEG);
		after.locals().stack(LONG);
	}

	@Test
	public void LOOKUPSWITCH() {
		before.locals().stack(INTEGER);
		mv.visitLookupSwitchInsn(new Label(), new int[0], new Label[0]);
		after.locals().stack();
	}

	@Test
	public void LOR() {
		before.locals().stack(LONG, LONG);
		mv.visitInsn(LOR);
		after.locals().stack(LONG);
	}

	@Test
	public void LREM() {
		before.locals().stack(LONG, LONG);
		mv.visitInsn(LREM);
		after.locals().stack(LONG);
	}

	@Test
	public void LRETURN() {
		before.locals().stack(LONG);
		mv.visitInsn(LRETURN);
		after.locals().stack();
	}

	@Test
	public void LSHL() {
		before.locals().stack(LONG, INTEGER);
		mv.visitInsn(LSHL);
		after.locals().stack(LONG);
	}

	@Test
	public void LSHR() {
		before.locals().stack(LONG, INTEGER);
		mv.visitInsn(LSHR);
		after.locals().stack(LONG);
	}

	@Test
	public void LSTORE() {
		before.locals().stack(LONG);
		mv.visitVarInsn(LSTORE, 0);
		after.locals(LONG).stack();
	}

	@Test
	public void LSUB() {
		before.locals().stack(LONG, LONG);
		mv.visitInsn(LSUB);
		after.locals().stack(LONG);
	}

	@Test
	public void LUSHR() {
		before.locals().stack(LONG, INTEGER);
		mv.visitInsn(LUSHR);
		after.locals().stack(LONG);
	}

	@Test
	public void LXOR() {
		before.locals().stack(LONG, LONG);
		mv.visitInsn(LXOR);
		after.locals().stack(LONG);
	}

	@Test
	public void MONITORENTER() {
		before.locals().stack("java/lang/Object");
		mv.visitInsn(MONITORENTER);
		after.locals().stack();
	}

	@Test
	public void MONITOREXIT() {
		before.locals().stack("java/lang/Object");
		mv.visitInsn(MONITOREXIT);
		after.locals().stack();
	}

	@Test
	public void MULTIANEWARRAY() {
		before.locals().stack(INTEGER, INTEGER, INTEGER);
		mv.visitMultiANewArrayInsn("[[[Ljava/lang/String;", 3);
		after.locals().stack("[[[Ljava/lang/String;");
	}

	@Test
	public void NEW() {
		before.locals(LONG).stack(LONG);
		mv.visitTypeInsn(NEW, "Test");
		mv.visitInsn(DUP);
		mv.visitMethodInsn(INVOKESPECIAL, "Test", "<init>", "()V");
		after.locals(LONG).stack(LONG, "Test");
	}

	@Test
	public void NEWARRAY_boolean() {
		before.locals().stack(INTEGER);
		mv.visitIntInsn(NEWARRAY, T_BOOLEAN);
		after.locals().stack("[Z");
	}

	@Test
	public void NEWARRAY_char() {
		before.locals().stack(INTEGER);
		mv.visitIntInsn(NEWARRAY, T_CHAR);
		after.locals().stack("[C");
	}

	@Test
	public void NEWARRAY_float() {
		before.locals().stack(INTEGER);
		mv.visitIntInsn(NEWARRAY, T_FLOAT);
		after.locals().stack("[F");
	}

	@Test
	public void NEWARRAY_double() {
		before.locals().stack(INTEGER);
		mv.visitIntInsn(NEWARRAY, T_DOUBLE);
		after.locals().stack("[D");
	}

	@Test
	public void NEWARRAY_byte() {
		before.locals().stack(INTEGER);
		mv.visitIntInsn(NEWARRAY, T_BYTE);
		after.locals().stack("[B");
	}

	@Test
	public void NEWARRAY_short() {
		before.locals().stack(INTEGER);
		mv.visitIntInsn(NEWARRAY, T_SHORT);
		after.locals().stack("[S");
	}

	@Test
	public void NEWARRAY_int() {
		before.locals().stack(INTEGER);
		mv.visitIntInsn(NEWARRAY, T_INT);
		after.locals().stack("[I");
	}

	@Test
	public void NEWARRAY_long() {
		before.locals().stack(INTEGER);
		mv.visitIntInsn(NEWARRAY, T_LONG);
		after.locals().stack("[J");
	}

	@Test(expected = IllegalArgumentException.class)
	public void NEWARRAY_invalidOperand() {
		FrameTracker tracker = new FrameTracker("Test", ACC_STATIC, "test",
				"()V", new MethodNode());
		tracker.visitFrame(F_NEW, 0, new Object[0], 1, new Object[] { INTEGER });
		tracker.visitIntInsn(NEWARRAY, -1);
	}

	@Test
	public void NOP() {
		before.locals().stack();
		mv.visitInsn(NOP);
		after.locals().stack();
	}

	@Test
	public void POP() {
		before.locals().stack(INTEGER);
		mv.visitInsn(POP);
		after.locals().stack();
	}

	@Test
	public void POP2_one_two_word_item() {
		before.locals().stack(DOUBLE);
		mv.visitInsn(POP2);
		after.locals().stack();
	}

	@Test
	public void POP2_two_one_word_items() {
		before.locals().stack("A", INTEGER);
		mv.visitInsn(POP2);
		after.locals().stack();
	}

	@Test
	public void PUTFIELD() {
		before.locals().stack("Test", INTEGER);
		mv.visitFieldInsn(PUTFIELD, "Test", "field", "I");
		after.locals().stack();
	}

	@Test
	public void PUTSTATIC() {
		before.locals().stack(INTEGER);
		mv.visitFieldInsn(PUTSTATIC, "Test", "field", "I");
		after.locals().stack();
	}

	@Test
	public void RETURN() {
		before.locals().stack();
		mv.visitInsn(RETURN);
		after.locals().stack();
	}

	@Test
	public void SALOAD() {
		before.locals().stack("[S", INTEGER);
		mv.visitInsn(SALOAD);
		after.locals().stack(INTEGER);
	}

	@Test
	public void SASTORE() {
		before.locals().stack("[S", INTEGER, INTEGER);
		mv.visitInsn(SASTORE);
		after.locals().stack();
	}

	@Test
	public void SIPUSH() {
		before.locals().stack();
		mv.visitIntInsn(SIPUSH, 123);
		after.locals().stack(INTEGER);
	}

	@Test
	public void SWAP() {
		before.locals().stack("A", "B");
		mv.visitInsn(SWAP);
		after.locals().stack("B", "A");
	}

	@Test
	public void TABLESWITCH() {
		before.locals().stack(INTEGER);
		mv.visitTableSwitchInsn(0, 1, new Label(), new Label[0]);
		after.locals().stack();
	}

}
