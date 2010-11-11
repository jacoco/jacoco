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
import org.objectweb.asm.Label;

/**
 * Unit tests for {@link LabelFlowAnayzer}.
 * 
 * @author Marc R. Hoffmann
 * @version $qualified.bundle.version$
 */
public class LabelFlowAnalyzerTest {

	private LabelFlowAnayzer info;
	private Label label;

	@Before
	public void setup() {
		info = new LabelFlowAnayzer();
		label = new Label();
	}

	@Test
	public void testSuccessorInit() {
		assertTrue(info.successor);
	}

	@Test
	public void testSuccessorInsn() {
		testSuccessorInsn(NOP, true);
		testSuccessorInsn(ACONST_NULL, true);
		testSuccessorInsn(ICONST_M1, true);
		testSuccessorInsn(ICONST_0, true);
		testSuccessorInsn(ICONST_1, true);
		testSuccessorInsn(ICONST_2, true);
		testSuccessorInsn(ICONST_3, true);
		testSuccessorInsn(ICONST_4, true);
		testSuccessorInsn(ICONST_5, true);
		testSuccessorInsn(LCONST_0, true);
		testSuccessorInsn(LCONST_1, true);
		testSuccessorInsn(FCONST_0, true);
		testSuccessorInsn(FCONST_1, true);
		testSuccessorInsn(FCONST_2, true);
		testSuccessorInsn(DCONST_0, true);
		testSuccessorInsn(DCONST_1, true);
		testSuccessorInsn(IALOAD, true);
		testSuccessorInsn(LALOAD, true);
		testSuccessorInsn(FALOAD, true);
		testSuccessorInsn(DALOAD, true);
		testSuccessorInsn(AALOAD, true);
		testSuccessorInsn(BALOAD, true);
		testSuccessorInsn(CALOAD, true);
		testSuccessorInsn(SALOAD, true);
		testSuccessorInsn(IASTORE, true);
		testSuccessorInsn(LASTORE, true);
		testSuccessorInsn(FASTORE, true);
		testSuccessorInsn(DASTORE, true);
		testSuccessorInsn(AASTORE, true);
		testSuccessorInsn(BASTORE, true);
		testSuccessorInsn(CASTORE, true);
		testSuccessorInsn(SASTORE, true);
		testSuccessorInsn(POP, true);
		testSuccessorInsn(POP2, true);
		testSuccessorInsn(DUP, true);
		testSuccessorInsn(DUP_X1, true);
		testSuccessorInsn(DUP_X2, true);
		testSuccessorInsn(DUP2, true);
		testSuccessorInsn(DUP2_X1, true);
		testSuccessorInsn(DUP2_X2, true);
		testSuccessorInsn(SWAP, true);
		testSuccessorInsn(IADD, true);
		testSuccessorInsn(LADD, true);
		testSuccessorInsn(FADD, true);
		testSuccessorInsn(DADD, true);
		testSuccessorInsn(ISUB, true);
		testSuccessorInsn(LSUB, true);
		testSuccessorInsn(FSUB, true);
		testSuccessorInsn(DSUB, true);
		testSuccessorInsn(IMUL, true);
		testSuccessorInsn(LMUL, true);
		testSuccessorInsn(FMUL, true);
		testSuccessorInsn(DMUL, true);
		testSuccessorInsn(IDIV, true);
		testSuccessorInsn(LDIV, true);
		testSuccessorInsn(FDIV, true);
		testSuccessorInsn(DDIV, true);
		testSuccessorInsn(IREM, true);
		testSuccessorInsn(LREM, true);
		testSuccessorInsn(FREM, true);
		testSuccessorInsn(DREM, true);
		testSuccessorInsn(INEG, true);
		testSuccessorInsn(LNEG, true);
		testSuccessorInsn(FNEG, true);
		testSuccessorInsn(DNEG, true);
		testSuccessorInsn(ISHL, true);
		testSuccessorInsn(LSHL, true);
		testSuccessorInsn(ISHR, true);
		testSuccessorInsn(LSHR, true);
		testSuccessorInsn(IUSHR, true);
		testSuccessorInsn(LUSHR, true);
		testSuccessorInsn(IAND, true);
		testSuccessorInsn(LAND, true);
		testSuccessorInsn(IOR, true);
		testSuccessorInsn(LOR, true);
		testSuccessorInsn(IXOR, true);
		testSuccessorInsn(LXOR, true);
		testSuccessorInsn(I2L, true);
		testSuccessorInsn(I2F, true);
		testSuccessorInsn(I2D, true);
		testSuccessorInsn(L2I, true);
		testSuccessorInsn(L2F, true);
		testSuccessorInsn(L2D, true);
		testSuccessorInsn(F2I, true);
		testSuccessorInsn(F2L, true);
		testSuccessorInsn(F2D, true);
		testSuccessorInsn(D2I, true);
		testSuccessorInsn(D2L, true);
		testSuccessorInsn(D2F, true);
		testSuccessorInsn(I2B, true);
		testSuccessorInsn(I2C, true);
		testSuccessorInsn(I2S, true);
		testSuccessorInsn(LCMP, true);
		testSuccessorInsn(FCMPL, true);
		testSuccessorInsn(FCMPG, true);
		testSuccessorInsn(DCMPL, true);
		testSuccessorInsn(DCMPG, true);
		testSuccessorInsn(IRETURN, false);
		testSuccessorInsn(LRETURN, false);
		testSuccessorInsn(FRETURN, false);
		testSuccessorInsn(DRETURN, false);
		testSuccessorInsn(ARETURN, false);
		testSuccessorInsn(RETURN, false);
		testSuccessorInsn(ARRAYLENGTH, true);
		testSuccessorInsn(ATHROW, false);
		testSuccessorInsn(MONITORENTER, true);
		testSuccessorInsn(MONITOREXIT, true);
	}

	private void testSuccessorInsn(int opcode, boolean expected) {
		// ensure the flag is actually set:
		info.successor = !expected;
		info.visitInsn(opcode);
		assertTrue(expected == info.successor);
	}

	@Test
	public void testSuccessorIntInsn() {
		testSuccessorIntInsn(BIPUSH);
		testSuccessorIntInsn(SIPUSH);
		testSuccessorIntInsn(NEWARRAY);
	}

	private void testSuccessorIntInsn(int opcode) {
		// ensure the flag is actually set:
		info.successor = false;
		info.visitIntInsn(opcode, 0);
		assertTrue(info.successor);
	}

	@Test
	public void testSuccessorVarInsn() {
		testSuccessorVarInsn(ILOAD);
		testSuccessorVarInsn(LLOAD);
		testSuccessorVarInsn(FLOAD);
		testSuccessorVarInsn(DLOAD);
		testSuccessorVarInsn(ALOAD);
		testSuccessorVarInsn(ISTORE);
		testSuccessorVarInsn(LSTORE);
		testSuccessorVarInsn(FSTORE);
		testSuccessorVarInsn(DSTORE);
		testSuccessorVarInsn(ASTORE);
	}

	private void testSuccessorVarInsn(int opcode) {
		// ensure the flag is actually set:
		info.successor = false;
		info.visitVarInsn(opcode, 0);
		assertTrue(info.successor);
	}

	@Test
	public void testSuccessorTypeInsn() {
		testSuccessorTypeInsn(NEW);
		testSuccessorTypeInsn(ANEWARRAY);
		testSuccessorTypeInsn(CHECKCAST);
		testSuccessorTypeInsn(INSTANCEOF);
	}

	private void testSuccessorTypeInsn(int opcode) {
		// ensure the flag is actually set:
		info.successor = false;
		info.visitTypeInsn(opcode, "java/lang/String");
		assertTrue(info.successor);
	}

	@Test
	public void testSuccessorFieldInsn() {
		testSuccessorFieldInsn(GETSTATIC);
		testSuccessorFieldInsn(PUTSTATIC);
		testSuccessorFieldInsn(GETFIELD);
		testSuccessorFieldInsn(PUTFIELD);
	}

	private void testSuccessorFieldInsn(int opcode) {
		// ensure the flag is actually set:
		info.successor = false;
		info.visitFieldInsn(opcode, "Foo", "name", "Ljava/lang/String;");
		assertTrue(info.successor);
	}

	@Test
	public void testSuccessorMethodInsn() {
		testSuccessorMethodInsn(INVOKEVIRTUAL);
		testSuccessorMethodInsn(INVOKESPECIAL);
		testSuccessorMethodInsn(INVOKESTATIC);
		testSuccessorMethodInsn(INVOKEINTERFACE);
	}

	private void testSuccessorMethodInsn(int opcode) {
		// ensure the flag is actually set:
		info.successor = false;
		info.visitMethodInsn(opcode, "Foo", "doit", "()V");
		assertTrue(info.successor);
	}

	@Test
	public void testSuccessorJumpInsn() {
		testSuccessorJumpInsn(IFEQ, true);
		testSuccessorJumpInsn(IFNE, true);
		testSuccessorJumpInsn(IFLT, true);
		testSuccessorJumpInsn(IFGE, true);
		testSuccessorJumpInsn(IFGT, true);
		testSuccessorJumpInsn(IFLE, true);
		testSuccessorJumpInsn(IF_ICMPEQ, true);
		testSuccessorJumpInsn(IF_ICMPNE, true);
		testSuccessorJumpInsn(IF_ICMPLT, true);
		testSuccessorJumpInsn(IF_ICMPGE, true);
		testSuccessorJumpInsn(IF_ICMPGT, true);
		testSuccessorJumpInsn(IF_ICMPLE, true);
		testSuccessorJumpInsn(IF_ACMPEQ, true);
		testSuccessorJumpInsn(IF_ACMPNE, true);
		testSuccessorJumpInsn(GOTO, false);
		testSuccessorJumpInsn(IFNULL, true);
		testSuccessorJumpInsn(IFNONNULL, true);
	}

	private void testSuccessorJumpInsn(int opcode, boolean expected) {
		// ensure the flag is actually set:
		info.successor = !expected;
		info.visitJumpInsn(opcode, label);
		assertTrue(expected == info.successor);
	}

	@Test
	public void testSuccessorLdcInsn() {
		// ensure the flag is actually set:
		info.successor = false;
		info.visitLdcInsn("Foo");
		assertTrue(info.successor);
	}

	@Test
	public void testSuccessorIincInsn() {
		// ensure the flag is actually set:
		info.successor = false;
		info.visitIincInsn(0, 1);
		assertTrue(info.successor);
	}

	@Test
	public void testSuccessorTableSwitchInsn() {
		// ensure the flag is actually set:
		info.successor = true;
		info.visitTableSwitchInsn(0, 0, label, new Label[] { label });
		assertFalse(info.successor);
	}

	@Test
	public void testSuccessorLookupSwitchInsn() {
		// ensure the flag is actually set:
		info.successor = true;
		info.visitLookupSwitchInsn(label, new int[] { 0 },
				new Label[] { label });
		assertFalse(info.successor);
	}

	@Test
	public void testSuccessorMultiANewArrayInsn() {
		// ensure the flag is actually set:
		info.successor = false;
		info.visitMultiANewArrayInsn("java/lang/String", 3);
		assertTrue(info.successor);
	}

	@Test
	public void testFlowScenario01() {
		assertFalse(LabelInfo.isMultiTarget(label));
		assertFalse(LabelInfo.isSuccessor(label));
	}

	@Test
	public void testFlowScenario02() {
		info.visitJumpInsn(GOTO, label);
		assertFalse(LabelInfo.isMultiTarget(label));
		assertFalse(LabelInfo.isSuccessor(label));
	}

	@Test
	public void testFlowScenario03() {
		info.visitInsn(RETURN);
		info.visitLabel(label);
		assertFalse(LabelInfo.isMultiTarget(label));
		assertFalse(LabelInfo.isSuccessor(label));
	}

	@Test
	public void testFlowScenario04() {
		info.visitLabel(label);
		assertFalse(LabelInfo.isMultiTarget(label));
		assertTrue(LabelInfo.isSuccessor(label));
	}

	@Test
	public void testFlowScenario05() {
		info.visitLabel(label);
		info.visitJumpInsn(GOTO, label);
		assertTrue(LabelInfo.isMultiTarget(label));
		assertTrue(LabelInfo.isSuccessor(label));
	}

	@Test
	public void testFlowScenario06() {
		info.visitJumpInsn(IFEQ, label);
		info.visitLabel(label);
		assertTrue(LabelInfo.isMultiTarget(label));
		assertTrue(LabelInfo.isSuccessor(label));
	}

	@Test
	public void testFlowScenario07() {
		info.visitJumpInsn(IFEQ, label);
		info.visitJumpInsn(GOTO, label);
		assertTrue(LabelInfo.isMultiTarget(label));
		assertFalse(LabelInfo.isSuccessor(label));
	}

	@Test
	public void testFlowScenario08() {
		info.visitJumpInsn(IFEQ, label);
		info.visitJumpInsn(IFGT, label);
		info.visitLabel(label);
		assertTrue(LabelInfo.isMultiTarget(label));
		assertTrue(LabelInfo.isSuccessor(label));
	}

	@Test
	public void testFlowScenario09() {
		info.visitLabel(label);
		info.visitLabel(label);
		assertFalse(LabelInfo.isMultiTarget(label));
		assertTrue(LabelInfo.isSuccessor(label));
	}

	@Test
	public void testFlowScenario10() {
		info.visitTryCatchBlock(new Label(), new Label(), label,
				"java/lang/Exception");
		info.visitJumpInsn(GOTO, label);
		assertTrue(LabelInfo.isMultiTarget(label));
		assertFalse(LabelInfo.isSuccessor(label));
	}

}
