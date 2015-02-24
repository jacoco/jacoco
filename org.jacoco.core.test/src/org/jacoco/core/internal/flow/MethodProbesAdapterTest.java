/*******************************************************************************
 * Copyright (c) 2009, 2015 Mountainminds GmbH & Co. KG and Contributors
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

import org.jacoco.core.instr.MethodRecorder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.AnalyzerAdapter;
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

	private IFrame frame;

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
		public void visitJumpInsnWithProbe(int opcode, Label label,
				int probeId, IFrame frame) {
			rec("visitJumpInsnWithProbe", Integer.valueOf(opcode), label,
					Integer.valueOf(probeId));
			frame.accept(this);
		}

		@Override
		public void visitTableSwitchInsnWithProbes(int min, int max,
				Label dflt, Label[] labels, IFrame frame) {
			rec("visitTableSwitchInsnWithProbes", Integer.valueOf(min),
					Integer.valueOf(max), dflt, labels);
			frame.accept(this);
		}

		@Override
		public void visitLookupSwitchInsnWithProbes(Label dflt, int[] keys,
				Label[] labels, IFrame frame) {
			rec("visitLookupSwitchInsnWithProbes", dflt, keys, labels);
			frame.accept(this);
		}

		private void rec(String name, Object... args) {
			printer.text.add(name + Arrays.asList(args));
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
		MethodProbesAdapter probesAdapter = new MethodProbesAdapter(
				actualVisitor, this);
		final AnalyzerAdapter analyzer = new AnalyzerAdapter("Foo", 0, "doit",
				"()V", probesAdapter);
		probesAdapter.setAnalyzer(analyzer);
		adapter = analyzer;
		frame = new IFrame() {

			public void accept(MethodVisitor mv) {
			}
		};
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
		LabelInfo.setTarget(label);
		LabelInfo.setTarget(label);

		adapter.visitLabel(label);

		expectedVisitor.visitLabel(label);
	}

	@Test
	public void testVisitProbe3() {
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
		adapter.visitInsn(Opcodes.ICONST_0);
		adapter.visitInsn(Opcodes.ICONST_0);
		adapter.visitInsn(Opcodes.IADD);

		expectedVisitor.visitInsn(Opcodes.ICONST_0);
		expectedVisitor.visitInsn(Opcodes.ICONST_0);
		expectedVisitor.visitInsn(Opcodes.IADD);
	}

	@Test
	public void testVisitJumpInsn1() {
		LabelInfo.setTarget(label);
		LabelInfo.setTarget(label);

		adapter.visitJumpInsn(Opcodes.GOTO, label);

		expectedVisitor
				.visitJumpInsnWithProbe(Opcodes.GOTO, label, 1000, frame);
		expectedVisitor.visitFrame(Opcodes.F_FULL, 1, new Object[] { "Foo" },
				0, null);
	}

	@Test
	public void testVisitJumpInsn2() {
		LabelInfo.setTarget(label);
		LabelInfo.setTarget(label);

		adapter.visitInsn(Opcodes.ICONST_0);
		adapter.visitJumpInsn(Opcodes.IFLT, label);

		expectedVisitor.visitInsn(Opcodes.ICONST_0);
		expectedVisitor
				.visitJumpInsnWithProbe(Opcodes.IFLT, label, 1000, frame);
		expectedVisitor.visitFrame(Opcodes.F_FULL, 1, new Object[] { "Foo" },
				0, null);
	}

	@Test
	public void testVisitJumpInsn3() {
		adapter.visitInsn(Opcodes.ICONST_0);
		adapter.visitJumpInsn(Opcodes.IFLT, label);

		expectedVisitor.visitInsn(Opcodes.ICONST_0);
		expectedVisitor.visitJumpInsn(Opcodes.IFLT, label);
	}

	@Test
	public void testVisitJumpInsn4() {
		LabelInfo.setTarget(label);
		LabelInfo.setTarget(label);

		adapter.visitInsn(Opcodes.ICONST_0);
		adapter.visitInsn(Opcodes.ICONST_0);
		adapter.visitJumpInsn(Opcodes.IF_ICMPEQ, label);

		expectedVisitor.visitInsn(Opcodes.ICONST_0);
		expectedVisitor.visitInsn(Opcodes.ICONST_0);
		expectedVisitor.visitJumpInsnWithProbe(Opcodes.IF_ICMPEQ, label, 1000,
				frame);
		expectedVisitor.visitFrame(Opcodes.F_FULL, 1, new Object[] { "Foo" },
				0, null);
	}

	@Test
	public void testVisitLookupSwitchInsn1() {
		LabelInfo.setTarget(label);
		LabelInfo.setTarget(label);

		final int[] keys = new int[] { 0, 1 };
		final Label[] labels = new Label[] { label, label };
		adapter.visitInsn(Opcodes.ICONST_0);
		adapter.visitLookupSwitchInsn(label, keys, labels);

		expectedVisitor.visitInsn(Opcodes.ICONST_0);
		expectedVisitor.visitLookupSwitchInsnWithProbes(label, keys, labels,
				frame);
		expectedVisitor.visitFrame(Opcodes.F_FULL, 1, new Object[] { "Foo" },
				0, null);
		assertEquals(1000, LabelInfo.getProbeId(label));
	}

	@Test
	public void testVisitLookupSwitchInsn2() {
		Label label2 = new Label();
		LabelInfo.setTarget(label2);
		LabelInfo.setTarget(label2);

		final int[] keys = new int[] { 0, 1 };
		final Label[] labels = new Label[] { label2, label };
		adapter.visitInsn(Opcodes.ICONST_0);
		adapter.visitLookupSwitchInsn(label, keys, labels);

		expectedVisitor.visitInsn(Opcodes.ICONST_0);
		expectedVisitor.visitLookupSwitchInsnWithProbes(label, keys, labels,
				frame);
		expectedVisitor.visitFrame(Opcodes.F_FULL, 1, new Object[] { "Foo" },
				0, null);
		assertEquals(LabelInfo.NO_PROBE, LabelInfo.getProbeId(label));
		assertEquals(1000, LabelInfo.getProbeId(label2));
	}

	@Test
	public void testVisitLookupSwitchInsn3() {
		final int[] keys = new int[] { 0, 1 };
		final Label[] labels = new Label[] { label, label };
		adapter.visitInsn(Opcodes.ICONST_0);
		adapter.visitLookupSwitchInsn(label, keys, labels);

		expectedVisitor.visitInsn(Opcodes.ICONST_0);
		expectedVisitor.visitLookupSwitchInsn(label, keys, labels);
	}

	@Test
	public void testVisitTableSwitchInsn1() {
		LabelInfo.setTarget(label);
		LabelInfo.setTarget(label);

		final Label[] labels = new Label[] { label, label };
		adapter.visitInsn(Opcodes.ICONST_0);
		adapter.visitTableSwitchInsn(0, 1, label, labels);

		expectedVisitor.visitInsn(Opcodes.ICONST_0);
		expectedVisitor.visitTableSwitchInsnWithProbes(0, 1, label, labels,
				frame);
		expectedVisitor.visitFrame(Opcodes.F_FULL, 1, new Object[] { "Foo" },
				0, null);
		assertEquals(1000, LabelInfo.getProbeId(label));
	}

	@Test
	public void testVisitTableSwitchInsn2() {
		Label label2 = new Label();
		LabelInfo.setTarget(label2);
		LabelInfo.setTarget(label2);

		final Label[] labels = new Label[] { label2, label };
		adapter.visitInsn(Opcodes.ICONST_0);
		adapter.visitTableSwitchInsn(0, 1, label, labels);

		expectedVisitor.visitInsn(Opcodes.ICONST_0);
		expectedVisitor.visitTableSwitchInsnWithProbes(0, 1, label, labels,
				frame);
		expectedVisitor.visitFrame(Opcodes.F_FULL, 1, new Object[] { "Foo" },
				0, null);
		assertEquals(LabelInfo.NO_PROBE, LabelInfo.getProbeId(label));
		assertEquals(1000, LabelInfo.getProbeId(label2));
	}

	@Test
	public void testVisitTableSwitchInsn3() {
		final Label[] labels = new Label[] { label, label };
		adapter.visitInsn(Opcodes.ICONST_0);
		adapter.visitTableSwitchInsn(0, 1, label, labels);

		expectedVisitor.visitInsn(Opcodes.ICONST_0);
		expectedVisitor.visitTableSwitchInsn(0, 1, label, labels);
	}

	@Test
	public void testVisitTryCatchBlockNoProbe() {
		Label start = new Label();
		Label end = new Label();
		Label handler = new Label();

		adapter.visitTryCatchBlock(start, end, handler, "java/lang/Exception");
		adapter.visitLabel(start);

		expectedVisitor.visitTryCatchBlock(start, end, handler,
				"java/lang/Exception");
		expectedVisitor.visitLabel(start);
	}

	@Test
	public void testVisitTryCatchBlockWithProbe() {
		Label target = new Label();
		LabelInfo.setSuccessor(target);
		LabelInfo.setTarget(target);
		Label end = new Label();
		Label handler = new Label();
		Label start = new Label();

		adapter.visitTryCatchBlock(target, end, handler, "java/lang/Exception");
		adapter.visitLabel(target);

		expectedVisitor.visitTryCatchBlock(start, end, handler,
				"java/lang/Exception");
		expectedVisitor.visitLabel(start);
		expectedVisitor.visitProbe(1000);
		expectedVisitor.visitLabel(target);
	}

	@Test
	public void testVisitMultipleTryCatchBlocksWithProbe() {
		Label target = new Label();
		LabelInfo.setSuccessor(target);
		LabelInfo.setTarget(target);
		Label end = new Label();
		Label handler1 = new Label();
		Label handler2 = new Label();
		Label start = new Label();

		adapter.visitTryCatchBlock(target, end, handler1, "java/lang/Exception");
		adapter.visitTryCatchBlock(target, end, handler2, "java/io/IOException");
		adapter.visitLabel(target);

		expectedVisitor.visitTryCatchBlock(start, end, handler1,
				"java/lang/Exception");
		expectedVisitor.visitTryCatchBlock(start, end, handler2,
				"java/io/IOException");
		expectedVisitor.visitLabel(start);
		expectedVisitor.visitProbe(1000);
		expectedVisitor.visitLabel(target);
	}

	// === IProbeIdGenerator ===

	public int nextId() {
		return id++;
	}

}
