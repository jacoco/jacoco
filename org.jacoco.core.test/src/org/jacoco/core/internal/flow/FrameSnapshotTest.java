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

import org.jacoco.core.instr.MethodRecorder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.AnalyzerAdapter;

/**
 * Unit tests for {@link FrameSnapshot}.
 */
public class FrameSnapshotTest {

	private AnalyzerAdapter analyzer;
	private IFrame frame;

	private MethodRecorder expected;

	private MethodVisitor expectedVisitor;

	@Before
	public void setup() {
		analyzer = new AnalyzerAdapter("Foo", 0, "doit", "()V", null);
		expected = new MethodRecorder();
		expectedVisitor = expected.getVisitor();
	}

	@After
	public void teardown() {
		MethodRecorder actual = new MethodRecorder();
		frame.accept(actual.getVisitor());
		assertEquals(expected, actual);
	}

	@Test
	public void should_not_capture_frame_when_no_analyzer_is_given() {
		frame = FrameSnapshot.create(null, 0);
	}

	@Test
	public void should_not_capture_frame_when_no_frame_is_defined() {
		analyzer.visitJumpInsn(Opcodes.GOTO, new Label());
		frame = FrameSnapshot.create(analyzer, 0);
	}

	@Test
	public void should_capture_frame_when_frame_is_defined() {
		analyzer.visitInsn(Opcodes.FCONST_0);
		analyzer.visitVarInsn(Opcodes.FSTORE, 1);
		analyzer.visitInsn(Opcodes.ICONST_0);
		frame = FrameSnapshot.create(analyzer, 0);

		expectedVisitor.visitFrame(Opcodes.F_FULL, 2, arr("Foo", Opcodes.FLOAT),
				1, arr(Opcodes.INTEGER));
	}

	@Test
	public void should_combine_slots_when_doube_or_long_types_are_given() {
		analyzer.visitInsn(Opcodes.DCONST_0);
		analyzer.visitVarInsn(Opcodes.DSTORE, 1);
		analyzer.visitInsn(Opcodes.FCONST_0);
		analyzer.visitVarInsn(Opcodes.FSTORE, 3);

		analyzer.visitInsn(Opcodes.ICONST_0);
		analyzer.visitInsn(Opcodes.LCONST_0);
		analyzer.visitInsn(Opcodes.ICONST_0);
		analyzer.visitInsn(Opcodes.DCONST_0);
		frame = FrameSnapshot.create(analyzer, 0);

		final Object[] vars = arr("Foo", Opcodes.DOUBLE, Opcodes.FLOAT);
		final Object[] stack = arr(Opcodes.INTEGER, Opcodes.LONG,
				Opcodes.INTEGER, Opcodes.DOUBLE);
		expectedVisitor.visitFrame(Opcodes.F_FULL, 3, vars, 4, stack);
	}

	@Test
	public void should_decrease_stack_when_popCount_is_given() {
		analyzer.visitInsn(Opcodes.ICONST_0);
		analyzer.visitInsn(Opcodes.LCONST_0);
		analyzer.visitInsn(Opcodes.ICONST_0);
		analyzer.visitInsn(Opcodes.ICONST_0);
		frame = FrameSnapshot.create(analyzer, 2);

		final Object[] stack = arr(Opcodes.INTEGER, Opcodes.LONG);
		expectedVisitor.visitFrame(Opcodes.F_FULL, 1, arr("Foo"), 2, stack);
	}

	/**
	 * Test of <a href="https://gitlab.ow2.org/asm/asm/issues/317793">ASM
	 * bug</a>: according to <a href=
	 * "https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-4.html#jvms-4.10.1.9.aaload">JVMS
	 * "4.10.1.9 Type Checking Instructions, AALOAD"</a> resulting type on the
	 * operand stack should be null if the input array is null.
	 */
	@Test
	public void after_aaload_stack_should_contain_null_when_input_array_is_null() {
		analyzer.visitInsn(Opcodes.ACONST_NULL);
		analyzer.visitInsn(Opcodes.ICONST_0);
		analyzer.visitInsn(Opcodes.AALOAD);
		frame = FrameSnapshot.create(analyzer, 0);

		final Object[] stack = arr(Opcodes.NULL);
		expectedVisitor.visitFrame(Opcodes.F_FULL, 1, arr("Foo"), 1, stack);
	}

	private Object[] arr(Object... elements) {
		return elements;
	}
}
