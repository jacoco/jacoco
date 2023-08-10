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
		public void visitJumpInsnWithProbe(int opcode, Label label, int probeId,
				IFrame frame) {
			rec("visitJumpInsnWithProbe", Integer.valueOf(opcode), label,
					Integer.valueOf(probeId));
			frame.accept(this);
		}

		@Override
		public void visitTableSwitchInsnWithProbes(int min, int max, Label dflt,
				Label[] labels, IFrame frame) {
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

		expectedVisitor.visitJumpInsnWithProbe(Opcodes.GOTO, label, 1000,
				frame);
		expectedVisitor.visitFrame(Opcodes.F_FULL, 1, new Object[] { "Foo" }, 0,
				null);
	}

	@Test
	public void testVisitJumpInsn2() {
		LabelInfo.setTarget(label);
		LabelInfo.setTarget(label);

		adapter.visitInsn(Opcodes.ICONST_0);
		adapter.visitJumpInsn(Opcodes.IFLT, label);

		expectedVisitor.visitInsn(Opcodes.ICONST_0);
		expectedVisitor.visitJumpInsnWithProbe(Opcodes.IFLT, label, 1000,
				frame);
		expectedVisitor.visitFrame(Opcodes.F_FULL, 1, new Object[] { "Foo" }, 0,
				null);
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
		expectedVisitor.visitFrame(Opcodes.F_FULL, 1, new Object[] { "Foo" }, 0,
				null);
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
		expectedVisitor.visitFrame(Opcodes.F_FULL, 1, new Object[] { "Foo" }, 0,
				null);
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
		expectedVisitor.visitFrame(Opcodes.F_FULL, 1, new Object[] { "Foo" }, 0,
				null);
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
		expectedVisitor.visitFrame(Opcodes.F_FULL, 1, new Object[] { "Foo" }, 0,
				null);
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
		expectedVisitor.visitFrame(Opcodes.F_FULL, 1, new Object[] { "Foo" }, 0,
				null);
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
		adapter.visitInsn(Opcodes.NOP);
		adapter.visitLabel(end);

		expectedVisitor.visitTryCatchBlock(start, end, handler,
				"java/lang/Exception");
		expectedVisitor.visitLabel(start);
		expectedVisitor.visitInsn(Opcodes.NOP);
		expectedVisitor.visitLabel(end);
	}

	@Test
	public void testVisitTryCatchBlockWithProbeBeforeStart() {
		Label start = new Label();
		LabelInfo.setSuccessor(start);
		LabelInfo.setTarget(start);
		Label end = new Label();
		Label handler1 = new Label();
		Label handler2 = new Label();

		adapter.visitTryCatchBlock(start, end, handler1, "java/lang/Exception");
		adapter.visitTryCatchBlock(start, end, handler2, "java/lang/Throwable");
		adapter.visitLabel(start);
		adapter.visitInsn(Opcodes.NOP);
		adapter.visitLabel(end);

		Label probe = new Label();
		expectedVisitor.visitTryCatchBlock(probe, end, handler1,
				"java/lang/Exception");
		expectedVisitor.visitTryCatchBlock(probe, end, handler2,
				"java/lang/Throwable");
		expectedVisitor.visitLabel(probe);
		expectedVisitor.visitProbe(1000);
		expectedVisitor.visitLabel(start);
		expectedVisitor.visitInsn(Opcodes.NOP);
		expectedVisitor.visitLabel(end);
	}

	@Test
	public void testVisitTryCatchBlockWithProbeBeforeEnd() {
		Label start = new Label();
		Label end = new Label();
		LabelInfo.setSuccessor(end);
		LabelInfo.setTarget(end);
		Label handler1 = new Label();
		Label handler2 = new Label();

		adapter.visitTryCatchBlock(start, end, handler1, "java/lang/Exception");
		adapter.visitTryCatchBlock(start, end, handler2, "java/lang/Throwable");
		adapter.visitLabel(start);
		adapter.visitInsn(Opcodes.NOP);
		adapter.visitLabel(end);

		Label probe = new Label();
		expectedVisitor.visitTryCatchBlock(start, probe, handler1,
				"java/lang/Exception");
		expectedVisitor.visitTryCatchBlock(start, probe, handler2,
				"java/lang/Throwable");
		expectedVisitor.visitLabel(start);
		expectedVisitor.visitInsn(Opcodes.NOP);
		expectedVisitor.visitLabel(probe);
		expectedVisitor.visitProbe(1000);
		expectedVisitor.visitLabel(end);
	}

	/**
	 * https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-2.html#jvms-2.11.10
	 */
	@Test
	public void testStructuredLocking() {
		Label start = new Label();
		LabelInfo.setSuccessor(start);
		LabelInfo.setTarget(start);
		Label end = new Label();
		LabelInfo.setSuccessor(end);
		LabelInfo.setTarget(end);
		Label handlerStart = new Label();
		Label handlerEnd = new Label();
		Label after = new Label();

		adapter.visitTryCatchBlock(start, end, handlerStart, null);
		adapter.visitTryCatchBlock(handlerStart, handlerEnd, handlerStart,
				null);
		adapter.visitVarInsn(Opcodes.ALOAD, 1);
		adapter.visitInsn(Opcodes.MONITORENTER);
		adapter.visitLabel(start);
		adapter.visitInsn(Opcodes.NOP);
		adapter.visitVarInsn(Opcodes.ALOAD, 1);
		adapter.visitInsn(Opcodes.MONITOREXIT);
		adapter.visitLabel(end);
		adapter.visitJumpInsn(Opcodes.GOTO, after);
		adapter.visitLabel(handlerStart);
		adapter.visitVarInsn(Opcodes.ALOAD, 1);
		adapter.visitInsn(Opcodes.MONITOREXIT);
		adapter.visitLabel(handlerEnd);
		adapter.visitInsn(Opcodes.ATHROW);
		adapter.visitLabel(after);

		Label probe1 = new Label();
		Label probe2 = new Label();
		expectedVisitor.visitTryCatchBlock(probe1, probe2, handlerStart, null);
		expectedVisitor.visitTryCatchBlock(handlerStart, handlerEnd,
				handlerStart, null);
		expectedVisitor.visitVarInsn(Opcodes.ALOAD, 1);
		expectedVisitor.visitInsn(Opcodes.MONITORENTER);
		// next probe must be INSIDE range of instructions covered by handler,
		// otherwise monitorexit won't be executed
		// in case if probe causes exception
		expectedVisitor.visitLabel(probe1);
		expectedVisitor.visitProbe(1000);
		expectedVisitor.visitLabel(start);
		expectedVisitor.visitInsn(Opcodes.NOP);
		expectedVisitor.visitVarInsn(Opcodes.ALOAD, 1);
		expectedVisitor.visitInsn(Opcodes.MONITOREXIT);
		// next probe must be OUTSIDE range of instructions covered by handler,
		// otherwise monitorexit will be executed second time
		// in case if probe causes exception
		expectedVisitor.visitLabel(probe2);
		expectedVisitor.visitProbe(1001);
		expectedVisitor.visitLabel(end);
		expectedVisitor.visitJumpInsn(Opcodes.GOTO, after);
		expectedVisitor.visitLabel(handlerStart);
		expectedVisitor.visitVarInsn(Opcodes.ALOAD, 1);
		expectedVisitor.visitInsn(Opcodes.MONITOREXIT);
		expectedVisitor.visitLabel(handlerEnd);
		expectedVisitor.visitInsnWithProbe(Opcodes.ATHROW, 1002);
		expectedVisitor.visitLabel(after);
	}

	// === IProbeIdGenerator ===

	public int nextId() {
		return id++;
	}

}
