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
	public void testNullAnalyzer() {
		frame = FrameSnapshot.create(null, 0);
	}

	@Test
	public void testNoFrame() {
		analyzer.visitJumpInsn(Opcodes.GOTO, new Label());
		frame = FrameSnapshot.create(analyzer, 0);
	}

	@Test
	public void testFrame() {
		analyzer.visitInsn(Opcodes.ICONST_0);
		frame = FrameSnapshot.create(analyzer, 0);

		expectedVisitor.visitFrame(Opcodes.F_FULL, 1, arr("Foo"), 1,
				arr(Opcodes.INTEGER));
	}

	@Test
	public void testReduce() {
		analyzer.visitInsn(Opcodes.ICONST_0);
		analyzer.visitInsn(Opcodes.LCONST_0);
		analyzer.visitInsn(Opcodes.ICONST_0);
		analyzer.visitInsn(Opcodes.DCONST_0);
		frame = FrameSnapshot.create(analyzer, 0);

		final Object[] stack = arr(Opcodes.INTEGER, Opcodes.LONG,
				Opcodes.INTEGER, Opcodes.DOUBLE);
		expectedVisitor.visitFrame(Opcodes.F_FULL, 1, arr("Foo"), 4, stack);
	}

	@Test
	public void testPop() {
		analyzer.visitInsn(Opcodes.ICONST_0);
		analyzer.visitInsn(Opcodes.LCONST_0);
		analyzer.visitInsn(Opcodes.ICONST_0);
		analyzer.visitInsn(Opcodes.ICONST_0);
		frame = FrameSnapshot.create(analyzer, 2);

		final Object[] stack = arr(Opcodes.INTEGER, Opcodes.LONG);
		expectedVisitor.visitFrame(Opcodes.F_FULL, 1, arr("Foo"), 2, stack);
	}

	private Object[] arr(Object... elements) {
		return elements;
	}
}
