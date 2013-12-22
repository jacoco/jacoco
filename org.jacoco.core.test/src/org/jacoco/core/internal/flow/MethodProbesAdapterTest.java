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
package org.jacoco.core.internal.flow;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.jacoco.core.instr.MethodRecorder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.util.Printer;

/**
 * Unit tests for {@link MethodProbesAdapter}.
 */
public class MethodProbesAdapterTest implements IProbeIdGenerator {

	private Label label;

	private int id;

	private MethodRecorder expected, actual;

	private MethodProbesVisitor expectedVisitor;

	private MethodVisitor adapter;

	private static class TraceAdapter extends MethodProbesVisitor {

		private final Printer printer;

		TraceAdapter(MethodRecorder recorder) {
			super(recorder.getVisitor());
			printer = recorder.getPrinter();
		}

		@Override
		public void visitProbe(int probeId) {
			rec("visitProbe", Integer.valueOf(probeId));
		}

		@Override
		public void visitInsnWithProbe(int opcode, int probeId) {
			rec("visitInsnWithProbe", Integer.valueOf(opcode),
					Integer.valueOf(probeId));
		}

		@Override
		public void visitJumpInsnWithProbe(int opcode, Label label, int probeId) {
			rec("visitJumpInsnWithProbe", Integer.valueOf(opcode), label,
					Integer.valueOf(probeId));
		}

		@Override
		public void visitTableSwitchInsnWithProbes(int min, int max,
				Label dflt, Label[] labels) {
			rec("visitTableSwitchInsnWithProbes", Integer.valueOf(min),
					Integer.valueOf(max), dflt, labels);
		}

		@Override
		public void visitLookupSwitchInsnWithProbes(Label dflt, int[] keys,
				Label[] labels) {
			rec("visitLookupSwitchInsnWithProbes", dflt, keys, labels);
		}

		private void rec(String name, Object... args) {
			@SuppressWarnings("unchecked")
			final List<Object> text = printer.text;
			text.add(name + Arrays.asList(args));
		}

	}

	@Before
	public void setup() {
		label = new Label();
		id = 1000;
		expected = new MethodRecorder();
		expectedVisitor = new TraceAdapter(expected);
		actual = new MethodRecorder();
		MethodProbesVisitor actualVisitor = new TraceAdapter(actual);
		adapter = new MethodProbesAdapter(actualVisitor, this);
	}

	@After
	public void verify() {
		assertEquals(expected, actual);
	}

	@Test
	public void testVisitProbe1() {
		LabelInfo.setTarget(label);
		LabelInfo.setSuccessor(label);

		adapter.visitLabel(label);

		expectedVisitor.visitProbe(1000);
		expectedVisitor.visitLabel(label);
	}

	@Test
	public void testVisitProbe2() {
		adapter.visitLabel(label);

		expectedVisitor.visitLabel(label);
	}

	@Test
	public void testVisitInsn1() {
		adapter.visitInsn(Opcodes.RETURN);

		expectedVisitor.visitInsnWithProbe(Opcodes.RETURN, 1000);
	}

	@Test
	public void testVisitInsn2() {
		adapter.visitInsn(Opcodes.IADD);

		expectedVisitor.visitInsn(Opcodes.IADD);
	}

	@Test
	public void testVisitJumpInsn1() {
		LabelInfo.setTarget(label);
		LabelInfo.setTarget(label);

		adapter.visitJumpInsn(Opcodes.IFLT, label);

		expectedVisitor.visitJumpInsnWithProbe(Opcodes.IFLT, label, 1000);
	}

	@Test
	public void testVisitJumpInsn2() {
		adapter.visitJumpInsn(Opcodes.IFLT, label);

		expectedVisitor.visitJumpInsn(Opcodes.IFLT, label);
	}

	@Test
	public void testVisitLookupSwitchInsn1() {
		LabelInfo.setTarget(label);
		LabelInfo.setTarget(label);

		final int[] keys = new int[] { 0, 1 };
		final Label[] labels = new Label[] { label, label };
		adapter.visitLookupSwitchInsn(label, keys, labels);

		expectedVisitor.visitLookupSwitchInsnWithProbes(label, keys, labels);
		assertEquals(1000, LabelInfo.getProbeId(label));
	}

	@Test
	public void testVisitLookupSwitchInsn2() {
		Label label2 = new Label();
		LabelInfo.setTarget(label2);
		LabelInfo.setTarget(label2);

		final int[] keys = new int[] { 0, 1 };
		final Label[] labels = new Label[] { label2, label };
		adapter.visitLookupSwitchInsn(label, keys, labels);

		expectedVisitor.visitLookupSwitchInsnWithProbes(label, keys, labels);
		assertEquals(LabelInfo.NO_PROBE, LabelInfo.getProbeId(label));
		assertEquals(1000, LabelInfo.getProbeId(label2));
	}

	@Test
	public void testVisitLookupSwitchInsn3() {
		final int[] keys = new int[] { 0, 1 };
		final Label[] labels = new Label[] { label, label };
		adapter.visitLookupSwitchInsn(label, keys, labels);

		expectedVisitor.visitLookupSwitchInsn(label, keys, labels);
	}

	@Test
	public void testVisitTableSwitchInsn1() {
		LabelInfo.setTarget(label);
		LabelInfo.setTarget(label);

		final Label[] labels = new Label[] { label, label };
		adapter.visitTableSwitchInsn(0, 1, label, labels);

		expectedVisitor.visitTableSwitchInsnWithProbes(0, 1, label, labels);
		assertEquals(1000, LabelInfo.getProbeId(label));
	}

	@Test
	public void testVisitTableSwitchInsn2() {
		Label label2 = new Label();
		LabelInfo.setTarget(label2);
		LabelInfo.setTarget(label2);

		final Label[] labels = new Label[] { label2, label };
		adapter.visitTableSwitchInsn(0, 1, label, labels);

		expectedVisitor.visitTableSwitchInsnWithProbes(0, 1, label, labels);
		assertEquals(LabelInfo.NO_PROBE, LabelInfo.getProbeId(label));
		assertEquals(1000, LabelInfo.getProbeId(label2));
	}

	@Test
	public void testVisitTableSwitchInsn3() {
		final Label[] labels = new Label[] { label, label };
		adapter.visitTableSwitchInsn(0, 1, label, labels);

		expectedVisitor.visitTableSwitchInsn(0, 1, label, labels);
	}

	// === IProbeIdGenerator ===

	public int nextId() {
		return id++;
	}

}
