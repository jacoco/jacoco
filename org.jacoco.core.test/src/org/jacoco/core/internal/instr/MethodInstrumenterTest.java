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

import org.jacoco.core.instr.MethodRecorder;
import org.jacoco.core.internal.flow.LabelInfo;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Unit tests for {@link MethodInstrumenter}.
 */
public class MethodInstrumenterTest {

	private IProbeArrayStrategy probeArrayStrategy;

	private MethodInstrumenter instrumenter;

	private MethodRecorder expected;

	private MethodRecorder actual;

	@Before
	public void setup() {
		actual = new MethodRecorder();
		expected = new MethodRecorder();
		probeArrayStrategy = new IProbeArrayStrategy() {

			public int pushInstance(MethodVisitor mv) {
				mv.visitMethodInsn(Opcodes.INVOKESTATIC, "Target",
						"$jacocoInit", "()[Z");
				return 1;
			}

			public void addMembers(ClassVisitor delegate) {
			}
		};
		instrumenter = new MethodInstrumenter(actual, 0, "()V",
				probeArrayStrategy);
	}

	void sampleReturn() {
		return;
	}

	@Test
	public void testVisitCode() {
		instrumenter.visitCode();

		expected.visitMethodInsn(Opcodes.INVOKESTATIC, "Target", "$jacocoInit",
				"()[Z");
		expected.visitVarInsn(Opcodes.ASTORE, 1);

		assertEquals(expected, actual);
	}

	@Test
	public void testVisitMaxs() {
		instrumenter.visitMaxs(2, 7);

		expected.visitMaxs(5, 8);

		assertEquals(expected, actual);
	}

	@Test
	public void testVisitProbe() {
		instrumenter.visitProbe(33);

		expected.visitVarInsn(Opcodes.ALOAD, 1);
		expected.visitIntInsn(Opcodes.BIPUSH, 33);
		expected.visitInsn(Opcodes.ICONST_1);
		expected.visitInsn(Opcodes.BASTORE);

		assertEquals(expected, actual);
	}

	@Test
	public void testVisitInsnWithProbe() {
		instrumenter.visitInsnWithProbe(Opcodes.RETURN, 3);

		expected.visitVarInsn(Opcodes.ALOAD, 1);
		expected.visitInsn(Opcodes.ICONST_3);
		expected.visitInsn(Opcodes.ICONST_1);
		expected.visitInsn(Opcodes.BASTORE);
		expected.visitInsn(Opcodes.RETURN);

		assertEquals(expected, actual);
	}

	@Test
	public void testVisitJumpInsnWithProbe_GOTO() {
		final Label label = new Label();
		instrumenter.visitJumpInsnWithProbe(Opcodes.GOTO, label, 3);

		expected.visitVarInsn(Opcodes.ALOAD, 1);
		expected.visitInsn(Opcodes.ICONST_3);
		expected.visitInsn(Opcodes.ICONST_1);
		expected.visitInsn(Opcodes.BASTORE);
		expected.visitJumpInsn(Opcodes.GOTO, label);

		assertEquals(expected, actual);
	}

	@Test
	public void testVisitJumpInsnWithProbe() {
		final Label label = new Label();
		instrumenter.visitJumpInsnWithProbe(Opcodes.IFEQ, label, 3);
		instrumenter.visitMaxs(0, 0);

		final Label l2 = new Label();
		expected.visitJumpInsn(Opcodes.IFEQ, l2);
		expected.visitLabel(l2);
		expected.visitVarInsn(Opcodes.ALOAD, 1);
		expected.visitInsn(Opcodes.ICONST_3);
		expected.visitInsn(Opcodes.ICONST_1);
		expected.visitInsn(Opcodes.BASTORE);
		expected.visitJumpInsn(Opcodes.GOTO, label);
		expected.visitMaxs(3, 1);

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
		instrumenter.visitMaxs(0, 0);

		expected.visitTableSwitchInsn(3, 4, L0, new Label[] { L1, L1, L2 });
		expected.visitLabel(L0);
		expected.visitVarInsn(Opcodes.ALOAD, 1);
		expected.visitInsn(Opcodes.ICONST_0);
		expected.visitInsn(Opcodes.ICONST_1);
		expected.visitInsn(Opcodes.BASTORE);
		expected.visitJumpInsn(Opcodes.GOTO, new Label());
		expected.visitLabel(L1);
		expected.visitVarInsn(Opcodes.ALOAD, 1);
		expected.visitInsn(Opcodes.ICONST_1);
		expected.visitInsn(Opcodes.ICONST_1);
		expected.visitInsn(Opcodes.BASTORE);
		expected.visitJumpInsn(Opcodes.GOTO, new Label());
		expected.visitMaxs(3, 1);

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
		instrumenter.visitMaxs(0, 0);

		expected.visitLookupSwitchInsn(L0, new int[] { 10, 20, 30 },
				new Label[] { L1, L1, L2 });
		expected.visitLabel(L0);
		expected.visitVarInsn(Opcodes.ALOAD, 1);
		expected.visitInsn(Opcodes.ICONST_0);
		expected.visitInsn(Opcodes.ICONST_1);
		expected.visitInsn(Opcodes.BASTORE);
		expected.visitJumpInsn(Opcodes.GOTO, new Label());
		expected.visitLabel(L1);
		expected.visitVarInsn(Opcodes.ALOAD, 1);
		expected.visitInsn(Opcodes.ICONST_1);
		expected.visitInsn(Opcodes.ICONST_1);
		expected.visitInsn(Opcodes.BASTORE);
		expected.visitJumpInsn(Opcodes.GOTO, new Label());
		expected.visitMaxs(3, 1);

		assertEquals(expected, actual);
	}

}
