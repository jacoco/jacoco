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
import org.jacoco.core.internal.flow.LabelInfo;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;

/**
 * Unit tests for {@link MethodInstrumenter}.
 */
public class MethodInstrumenterTest {

	private MethodInstrumenter instrumenter;

	private MethodRecorder expected;

	private MethodRecorder actual;

	@Before
	public void setup() {
		actual = new MethodRecorder();
		expected = new MethodRecorder();
		final IProbeInserter probeInserter = new IProbeInserter() {

			public void insertProbe(int id) {
				actual.visitLdcInsn("Probe " + id);
			}
		};
		final IFrameInserter frameInserter = new IFrameInserter() {

			public void insertFrame() {
				actual.visitLdcInsn("Frame");
			}
		};
		instrumenter = new MethodInstrumenter(actual, probeInserter,
				frameInserter);
	}

	void sampleReturn() {
		return;
	}

	@Test
	public void testVisitProbe() {
		instrumenter.visitProbe(33);

		expected.visitLdcInsn("Probe 33");

		assertEquals(expected, actual);
	}

	@Test
	public void testVisitInsnWithProbe() {
		instrumenter.visitInsnWithProbe(Opcodes.RETURN, 3);

		expected.visitLdcInsn("Probe 3");
		expected.visitInsn(Opcodes.RETURN);

		assertEquals(expected, actual);
	}

	@Test
	public void testVisitJumpInsnWithProbe_GOTO() {
		final Label label = new Label();
		instrumenter.visitJumpInsnWithProbe(Opcodes.GOTO, label, 3);

		expected.visitLdcInsn("Probe 3");
		expected.visitJumpInsn(Opcodes.GOTO, label);

		assertEquals(expected, actual);
	}

	@Test
	public void testVisitJumpInsnWithProbe_IFEQ() {
		testVisitJumpInsnWithProbe(Opcodes.IFEQ, Opcodes.IFNE);
	}

	@Test
	public void testVisitJumpInsnWithProbe_IFGE() {
		testVisitJumpInsnWithProbe(Opcodes.IFGE, Opcodes.IFLT);
	}

	@Test
	public void testVisitJumpInsnWithProbe_IFGT() {
		testVisitJumpInsnWithProbe(Opcodes.IFGT, Opcodes.IFLE);
	}

	@Test
	public void testVisitJumpInsnWithProbe_IFLE() {
		testVisitJumpInsnWithProbe(Opcodes.IFLE, Opcodes.IFGT);
	}

	@Test
	public void testVisitJumpInsnWithProbe_IFLT() {
		testVisitJumpInsnWithProbe(Opcodes.IFLT, Opcodes.IFGE);
	}

	@Test
	public void testVisitJumpInsnWithProbe_IFNE() {
		testVisitJumpInsnWithProbe(Opcodes.IFNE, Opcodes.IFEQ);
	}

	@Test
	public void testVisitJumpInsnWithProbe_IF_ACMPEQ() {
		testVisitJumpInsnWithProbe(Opcodes.IF_ACMPEQ, Opcodes.IF_ACMPNE);
	}

	@Test
	public void testVisitJumpInsnWithProbe_IF_ACMPNE() {
		testVisitJumpInsnWithProbe(Opcodes.IF_ACMPNE, Opcodes.IF_ACMPEQ);
	}

	@Test
	public void testVisitJumpInsnWithProbe_IF_ICMPEQ() {
		testVisitJumpInsnWithProbe(Opcodes.IF_ICMPEQ, Opcodes.IF_ICMPNE);
	}

	@Test
	public void testVisitJumpInsnWithProbe_IF_ICMPGE() {
		testVisitJumpInsnWithProbe(Opcodes.IF_ICMPGE, Opcodes.IF_ICMPLT);
	}

	@Test
	public void testVisitJumpInsnWithProbe_IF_ICMPGT() {
		testVisitJumpInsnWithProbe(Opcodes.IF_ICMPGT, Opcodes.IF_ICMPLE);
	}

	@Test
	public void testVisitJumpInsnWithProbe_IF_ICMPLE() {
		testVisitJumpInsnWithProbe(Opcodes.IF_ICMPLE, Opcodes.IF_ICMPGT);
	}

	@Test
	public void testVisitJumpInsnWithProbe_IF_ICMPLT() {
		testVisitJumpInsnWithProbe(Opcodes.IF_ICMPLT, Opcodes.IF_ICMPGE);
	}

	@Test
	public void testVisitJumpInsnWithProbe_IF_ICMPNE() {
		testVisitJumpInsnWithProbe(Opcodes.IF_ICMPNE, Opcodes.IF_ICMPEQ);
	}

	@Test
	public void testVisitJumpInsnWithProbe_IFNULL() {
		testVisitJumpInsnWithProbe(Opcodes.IFNULL, Opcodes.IFNONNULL);
	}

	@Test
	public void testVisitJumpInsnWithProbe_IFNONNULL() {
		testVisitJumpInsnWithProbe(Opcodes.IFNONNULL, Opcodes.IFNULL);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testVisitJumpInsnWithProbe_InvalidOpcode() {
		testVisitJumpInsnWithProbe(Opcodes.NOP, Opcodes.NOP);
	}

	private void testVisitJumpInsnWithProbe(int opcodeOrig, int opcodeInstr) {
		final Label label = new Label();
		instrumenter.visitJumpInsnWithProbe(opcodeOrig, label, 3);

		final Label l2 = new Label();
		expected.visitJumpInsn(opcodeInstr, l2);
		expected.visitLdcInsn("Probe 3");
		expected.visitJumpInsn(Opcodes.GOTO, label);
		expected.visitLabel(l2);
		expected.visitLdcInsn("Frame");

		assertEquals(expected, actual);
	}

	@Test
	public void testVisitTableSwitchInsnWithProbes() {
		final Label L0 = new Label();
		final Label L1 = new Label();
		final Label L2 = new Label();
		LabelInfo.setProbeId(L0, 0);
		LabelInfo.setProbeId(L1, 1);
		instrumenter.visitTableSwitchInsnWithProbes(3, 5, L0, new Label[] { L1,
				L1, L2 });

		expected.visitTableSwitchInsn(3, 4, L0, new Label[] { L1, L1, L2 });
		expected.visitLabel(L0);
		expected.visitLdcInsn("Frame");
		expected.visitLdcInsn("Probe 0");
		expected.visitJumpInsn(Opcodes.GOTO, new Label());
		expected.visitLabel(L1);
		expected.visitLdcInsn("Frame");
		expected.visitLdcInsn("Probe 1");
		expected.visitJumpInsn(Opcodes.GOTO, new Label());

		assertEquals(expected, actual);
	}

	@Test
	public void testVisitLookupSwitchInsnWithProbes() {
		final Label L0 = new Label();
		final Label L1 = new Label();
		final Label L2 = new Label();
		LabelInfo.setProbeId(L0, 0);
		LabelInfo.setProbeId(L1, 1);
		instrumenter.visitLookupSwitchInsnWithProbes(L0,
				new int[] { 10, 20, 30 }, new Label[] { L1, L1, L2 });

		expected.visitLookupSwitchInsn(L0, new int[] { 10, 20, 30 },
				new Label[] { L1, L1, L2 });
		expected.visitLabel(L0);
		expected.visitLdcInsn("Frame");
		expected.visitLdcInsn("Probe 0");
		expected.visitJumpInsn(Opcodes.GOTO, new Label());
		expected.visitLabel(L1);
		expected.visitLdcInsn("Frame");
		expected.visitLdcInsn("Probe 1");
		expected.visitJumpInsn(Opcodes.GOTO, new Label());

		assertEquals(expected, actual);
	}

}
