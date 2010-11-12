/*******************************************************************************
 * Copyright (c) 2009, 2010 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.core.internal.flow;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.objectweb.asm.Opcodes.*;

import org.junit.Before;
import org.junit.Test;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.Label;

/**
 * Unit tests for {@link SuccessorAnalyzer}.
 * 
 * @author Marc R. Hoffmann
 * @version $qualified.bundle.version$
 */
public class SuccessorAnalyzerTestBase {

	private SuccessorAnalyzer analyzer;

	protected Label label;

	@Before
	public void setup() {
		analyzer = createAnalyzer();
		label = new Label();
	}

	protected SuccessorAnalyzer createAnalyzer() {
		return new SuccessorAnalyzer() {
			public AnnotationVisitor visitAnnotationDefault() {
				return null;
			}

			public AnnotationVisitor visitAnnotation(String desc,
					boolean visible) {
				return null;
			}

			public AnnotationVisitor visitParameterAnnotation(int parameter,
					String desc, boolean visible) {
				return null;
			}

			public void visitAttribute(Attribute attr) {
			}

			public void visitCode() {
			}

			public void visitFrame(int type, int nLocal, Object[] local,
					int nStack, Object[] stack) {
			}

			public void visitLabel(Label label) {
			}

			public void visitTryCatchBlock(Label start, Label end,
					Label handler, String type) {
			}

			public void visitLocalVariable(String name, String desc,
					String signature, Label start, Label end, int index) {
			}

			public void visitLineNumber(int line, Label start) {
			}

			public void visitMaxs(int maxStack, int maxLocals) {
			}

			public void visitEnd() {
			}
		};
	}

	@Test
	public void testInit() {
		assertTrue(analyzer.successor);
	}

	@Test
	public void testInsn() {
		testInsn(NOP, true);
		testInsn(ACONST_NULL, true);
		testInsn(ICONST_M1, true);
		testInsn(ICONST_0, true);
		testInsn(ICONST_1, true);
		testInsn(ICONST_2, true);
		testInsn(ICONST_3, true);
		testInsn(ICONST_4, true);
		testInsn(ICONST_5, true);
		testInsn(LCONST_0, true);
		testInsn(LCONST_1, true);
		testInsn(FCONST_0, true);
		testInsn(FCONST_1, true);
		testInsn(FCONST_2, true);
		testInsn(DCONST_0, true);
		testInsn(DCONST_1, true);
		testInsn(IALOAD, true);
		testInsn(LALOAD, true);
		testInsn(FALOAD, true);
		testInsn(DALOAD, true);
		testInsn(AALOAD, true);
		testInsn(BALOAD, true);
		testInsn(CALOAD, true);
		testInsn(SALOAD, true);
		testInsn(IASTORE, true);
		testInsn(LASTORE, true);
		testInsn(FASTORE, true);
		testInsn(DASTORE, true);
		testInsn(AASTORE, true);
		testInsn(BASTORE, true);
		testInsn(CASTORE, true);
		testInsn(SASTORE, true);
		testInsn(POP, true);
		testInsn(POP2, true);
		testInsn(DUP, true);
		testInsn(DUP_X1, true);
		testInsn(DUP_X2, true);
		testInsn(DUP2, true);
		testInsn(DUP2_X1, true);
		testInsn(DUP2_X2, true);
		testInsn(SWAP, true);
		testInsn(IADD, true);
		testInsn(LADD, true);
		testInsn(FADD, true);
		testInsn(DADD, true);
		testInsn(ISUB, true);
		testInsn(LSUB, true);
		testInsn(FSUB, true);
		testInsn(DSUB, true);
		testInsn(IMUL, true);
		testInsn(LMUL, true);
		testInsn(FMUL, true);
		testInsn(DMUL, true);
		testInsn(IDIV, true);
		testInsn(LDIV, true);
		testInsn(FDIV, true);
		testInsn(DDIV, true);
		testInsn(IREM, true);
		testInsn(LREM, true);
		testInsn(FREM, true);
		testInsn(DREM, true);
		testInsn(INEG, true);
		testInsn(LNEG, true);
		testInsn(FNEG, true);
		testInsn(DNEG, true);
		testInsn(ISHL, true);
		testInsn(LSHL, true);
		testInsn(ISHR, true);
		testInsn(LSHR, true);
		testInsn(IUSHR, true);
		testInsn(LUSHR, true);
		testInsn(IAND, true);
		testInsn(LAND, true);
		testInsn(IOR, true);
		testInsn(LOR, true);
		testInsn(IXOR, true);
		testInsn(LXOR, true);
		testInsn(I2L, true);
		testInsn(I2F, true);
		testInsn(I2D, true);
		testInsn(L2I, true);
		testInsn(L2F, true);
		testInsn(L2D, true);
		testInsn(F2I, true);
		testInsn(F2L, true);
		testInsn(F2D, true);
		testInsn(D2I, true);
		testInsn(D2L, true);
		testInsn(D2F, true);
		testInsn(I2B, true);
		testInsn(I2C, true);
		testInsn(I2S, true);
		testInsn(LCMP, true);
		testInsn(FCMPL, true);
		testInsn(FCMPG, true);
		testInsn(DCMPL, true);
		testInsn(DCMPG, true);
		testInsn(IRETURN, false);
		testInsn(LRETURN, false);
		testInsn(FRETURN, false);
		testInsn(DRETURN, false);
		testInsn(ARETURN, false);
		testInsn(RETURN, false);
		testInsn(ARRAYLENGTH, true);
		testInsn(ATHROW, false);
		testInsn(MONITORENTER, true);
		testInsn(MONITOREXIT, true);
	}

	private void testInsn(int opcode, boolean expected) {
		// ensure the flag is actually set:
		analyzer.successor = !expected;
		analyzer.visitInsn(opcode);
		assertTrue(expected == analyzer.successor);
	}

	@Test
	public void testIntInsn() {
		testIntInsn(BIPUSH);
		testIntInsn(SIPUSH);
		testIntInsn(NEWARRAY);
	}

	private void testIntInsn(int opcode) {
		// ensure the flag is actually set:
		analyzer.successor = false;
		analyzer.visitIntInsn(opcode, 0);
		assertTrue(analyzer.successor);
	}

	@Test
	public void testVarInsn() {
		testVarInsn(ILOAD);
		testVarInsn(LLOAD);
		testVarInsn(FLOAD);
		testVarInsn(DLOAD);
		testVarInsn(ALOAD);
		testVarInsn(ISTORE);
		testVarInsn(LSTORE);
		testVarInsn(FSTORE);
		testVarInsn(DSTORE);
		testVarInsn(ASTORE);
	}

	private void testVarInsn(int opcode) {
		// ensure the flag is actually set:
		analyzer.successor = false;
		analyzer.visitVarInsn(opcode, 0);
		assertTrue(analyzer.successor);
	}

	@Test
	public void testTypeInsn() {
		testTypeInsn(NEW);
		testTypeInsn(ANEWARRAY);
		testTypeInsn(CHECKCAST);
		testTypeInsn(INSTANCEOF);
	}

	private void testTypeInsn(int opcode) {
		// ensure the flag is actually set:
		analyzer.successor = false;
		analyzer.visitTypeInsn(opcode, "java/lang/String");
		assertTrue(analyzer.successor);
	}

	@Test
	public void testFieldInsn() {
		testFieldInsn(GETSTATIC);
		testFieldInsn(PUTSTATIC);
		testFieldInsn(GETFIELD);
		testFieldInsn(PUTFIELD);
	}

	private void testFieldInsn(int opcode) {
		// ensure the flag is actually set:
		analyzer.successor = false;
		analyzer.visitFieldInsn(opcode, "Foo", "name", "Ljava/lang/String;");
		assertTrue(analyzer.successor);
	}

	@Test
	public void testMethodInsn() {
		testMethodInsn(INVOKEVIRTUAL);
		testMethodInsn(INVOKESPECIAL);
		testMethodInsn(INVOKESTATIC);
		testMethodInsn(INVOKEINTERFACE);
	}

	private void testMethodInsn(int opcode) {
		// ensure the flag is actually set:
		analyzer.successor = false;
		analyzer.visitMethodInsn(opcode, "Foo", "doit", "()V");
		assertTrue(analyzer.successor);
	}

	@Test
	public void testJumpInsn() {
		testJumpInsn(IFEQ, true);
		testJumpInsn(IFNE, true);
		testJumpInsn(IFLT, true);
		testJumpInsn(IFGE, true);
		testJumpInsn(IFGT, true);
		testJumpInsn(IFLE, true);
		testJumpInsn(IF_ICMPEQ, true);
		testJumpInsn(IF_ICMPNE, true);
		testJumpInsn(IF_ICMPLT, true);
		testJumpInsn(IF_ICMPGE, true);
		testJumpInsn(IF_ICMPGT, true);
		testJumpInsn(IF_ICMPLE, true);
		testJumpInsn(IF_ACMPEQ, true);
		testJumpInsn(IF_ACMPNE, true);
		testJumpInsn(GOTO, false);
		testJumpInsn(IFNULL, true);
		testJumpInsn(IFNONNULL, true);
	}

	private void testJumpInsn(int opcode, boolean expected) {
		// ensure the flag is actually set:
		analyzer.successor = !expected;
		analyzer.visitJumpInsn(opcode, label);
		assertTrue(expected == analyzer.successor);
	}

	@Test
	public void testLdcInsn() {
		// ensure the flag is actually set:
		analyzer.successor = false;
		analyzer.visitLdcInsn("Foo");
		assertTrue(analyzer.successor);
	}

	@Test
	public void testIincInsn() {
		// ensure the flag is actually set:
		analyzer.successor = false;
		analyzer.visitIincInsn(0, 1);
		assertTrue(analyzer.successor);
	}

	@Test
	public void testTableSwitchInsn() {
		// ensure the flag is actually set:
		analyzer.successor = true;
		analyzer.visitTableSwitchInsn(0, 0, label, new Label[] { label });
		assertFalse(analyzer.successor);
	}

	@Test
	public void testLookupSwitchInsn() {
		// ensure the flag is actually set:
		analyzer.successor = true;
		analyzer.visitLookupSwitchInsn(label, new int[] { 0 },
				new Label[] { label });
		assertFalse(analyzer.successor);
	}

	@Test
	public void testMultiANewArrayInsn() {
		// ensure the flag is actually set:
		analyzer.successor = false;
		analyzer.visitMultiANewArrayInsn("java/lang/String", 3);
		assertTrue(analyzer.successor);
	}

}
