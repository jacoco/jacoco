/*******************************************************************************
 * Copyright (c) 2009, 2023 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.internal.flow;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.objectweb.asm.Opcodes.*;

import org.junit.Before;
import org.junit.Test;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;

/**
 * Unit tests for {@link LabelFlowAnalyzer}.
 */
public class LabelFlowAnalyzerTest {

	private LabelFlowAnalyzer analyzer;

	private Label label;

	@Before
	public void setup() {
		analyzer = new LabelFlowAnalyzer();
		label = new Label();
	}

	@Test
	public void testInit() {
		assertFalse(analyzer.successor);
		assertTrue(analyzer.first);
		assertNull(analyzer.lineStart);
	}

	@Test
	public void testFlowScenario01() {
		assertFalse(LabelInfo.isMultiTarget(label));
		assertFalse(LabelInfo.isSuccessor(label));
	}

	@Test
	public void testFlowScenario02() {
		analyzer.visitJumpInsn(GOTO, label);
		assertFalse(LabelInfo.isMultiTarget(label));
		assertFalse(LabelInfo.isSuccessor(label));
	}

	@Test
	public void testFlowScenario03() {
		analyzer.visitInsn(RETURN);
		analyzer.visitLabel(label);
		assertFalse(LabelInfo.isMultiTarget(label));
		assertFalse(LabelInfo.isSuccessor(label));
	}

	@Test
	public void testFlowScenario04() {
		analyzer.visitLabel(label);
		assertFalse(LabelInfo.isMultiTarget(label));
		assertFalse(LabelInfo.isSuccessor(label));
	}

	@Test
	public void testFlowScenario05() {
		analyzer.visitLabel(label);
		analyzer.visitJumpInsn(GOTO, label);
		assertTrue(LabelInfo.isMultiTarget(label));
		assertFalse(LabelInfo.isSuccessor(label));
	}

	@Test
	public void testFlowScenario06() {
		analyzer.visitJumpInsn(IFEQ, label);
		analyzer.visitLabel(label);
		assertTrue(LabelInfo.isMultiTarget(label));
		assertTrue(LabelInfo.isSuccessor(label));
	}

	@Test
	public void testFlowScenario07() {
		analyzer.visitJumpInsn(IFEQ, label);
		analyzer.visitJumpInsn(GOTO, label);
		assertTrue(LabelInfo.isMultiTarget(label));
		assertFalse(LabelInfo.isSuccessor(label));
	}

	@Test
	public void testFlowScenario08() {
		analyzer.visitJumpInsn(IFEQ, label);
		analyzer.visitJumpInsn(IFGT, label);
		analyzer.visitLabel(label);
		assertTrue(LabelInfo.isMultiTarget(label));
		assertTrue(LabelInfo.isSuccessor(label));
	}

	@Test
	public void testFlowScenario09() {
		analyzer.visitInsn(Opcodes.NOP);
		analyzer.visitLabel(label);
		analyzer.visitLabel(label);
		assertFalse(LabelInfo.isMultiTarget(label));
		assertTrue(LabelInfo.isSuccessor(label));
	}

	@Test
	public void testFlowScenario10() {
		analyzer.visitTryCatchBlock(new Label(), new Label(), label,
				"java/lang/Exception");
		analyzer.visitJumpInsn(GOTO, label);
		assertTrue(LabelInfo.isMultiTarget(label));
		assertFalse(LabelInfo.isSuccessor(label));
	}

	@Test
	public void testFlowScenario11() {
		// Even if the same label is referenced multiple times but from the same
		// source instruction this is only counted as one target.
		analyzer.visitLookupSwitchInsn(label, new int[] { 0, 1 },
				new Label[] { label, label });
		assertFalse(LabelInfo.isMultiTarget(label));
		assertFalse(LabelInfo.isSuccessor(label));
	}

	@Test
	public void testFlowScenario12() {
		// Even if the same label is referenced multiple times but from the same
		// source instruction this is only counted as one target.
		analyzer.visitTableSwitchInsn(0, 1, label,
				new Label[] { label, label });
		assertFalse(LabelInfo.isMultiTarget(label));
		assertFalse(LabelInfo.isSuccessor(label));
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
		// ensure the flags are actually set:
		analyzer.successor = !expected;
		analyzer.first = true;
		analyzer.visitInsn(opcode);
		assertTrue(expected == analyzer.successor);
		assertFalse(analyzer.first);
	}

	@Test(expected = AssertionError.class)
	public void testVisitInsnNegative() {
		analyzer.visitInsn(RET);
	}

	@Test
	public void testIntInsn() {
		analyzer.visitIntInsn(BIPUSH, 0);
		assertTrue(analyzer.successor);
		assertFalse(analyzer.first);
	}

	@Test
	public void testVarInsn() {
		analyzer.visitVarInsn(ILOAD, 0);
		assertTrue(analyzer.successor);
		assertFalse(analyzer.first);
	}

	@Test
	public void testTypeInsn() {
		analyzer.visitTypeInsn(NEW, "java/lang/String");
		assertTrue(analyzer.successor);
		assertFalse(analyzer.first);
	}

	@Test
	public void testFieldInsn() {
		analyzer.successor = false;
		analyzer.visitFieldInsn(GETFIELD, "Foo", "name", "Ljava/lang/String;");
		assertTrue(analyzer.successor);
		assertFalse(analyzer.first);
	}

	@Test
	public void testLineNumber() {
		analyzer.visitLineNumber(42, label);
		assertSame(label, analyzer.lineStart);
	}

	@Test
	public void testMethodInsn() {
		analyzer.visitLineNumber(42, label);
		analyzer.visitMethodInsn(INVOKEVIRTUAL, "Foo", "doit", "()V", false);
		assertTrue(analyzer.successor);
		assertFalse(analyzer.first);
		assertTrue(LabelInfo.isMethodInvocationLine(label));
	}

	@Test
	public void testInvokeDynamicInsn() {
		analyzer.visitLineNumber(42, label);
		analyzer.visitInvokeDynamicInsn("foo", "()V", null);
		assertTrue(analyzer.successor);
		assertFalse(analyzer.first);
		assertTrue(LabelInfo.isMethodInvocationLine(label));
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
		// ensure the flags are actually set:
		analyzer.successor = !expected;
		analyzer.first = true;
		analyzer.visitJumpInsn(opcode, label);
		assertTrue(expected == analyzer.successor);
		assertFalse(analyzer.first);
	}

	@Test(expected = AssertionError.class)
	public void testVisitJumpInsnNegative() {
		analyzer.visitJumpInsn(JSR, label);
	}

	@Test
	public void testLdcInsn() {
		analyzer.visitLdcInsn("Foo");
		assertTrue(analyzer.successor);
		assertFalse(analyzer.first);
	}

	@Test
	public void testIincInsn() {
		analyzer.visitIincInsn(0, 1);
		assertTrue(analyzer.successor);
		assertFalse(analyzer.first);
	}

	@Test
	public void testTableSwitchInsn() {
		analyzer.visitTableSwitchInsn(0, 0, label, new Label[] { label });
		assertFalse(analyzer.successor);
		assertFalse(analyzer.first);
	}

	@Test
	public void testLookupSwitchInsn() {
		analyzer.visitLookupSwitchInsn(label, new int[] { 0 },
				new Label[] { label });
		assertFalse(analyzer.successor);
		assertFalse(analyzer.first);
	}

	@Test
	public void testMultiANewArrayInsn() {
		analyzer.visitMultiANewArrayInsn("java/lang/String", 3);
		assertTrue(analyzer.successor);
		assertFalse(analyzer.first);
	}

}
