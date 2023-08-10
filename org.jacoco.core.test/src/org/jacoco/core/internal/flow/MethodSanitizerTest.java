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
import org.junit.Before;
import org.junit.Test;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodNode;

/**
 * Unit tests for {@link MethodSanitizer}.
 */
public class MethodSanitizerTest {

	private MethodNode actual;
	private MethodNode expected;

	private MethodVisitor sanitizer;

	@Before
	public void setup() {
		actual = new MethodNode(0, "test", "()V", null, null);
		expected = new MethodNode(0, "test", "()V", null, null);
		sanitizer = new MethodSanitizer(actual, 0, "test", "()V", null, null);
	}

	@Test
	public void testLocalVariablePositive() {
		Label l1 = new Label();
		Label l2 = new Label();
		sanitizer.visitCode();
		sanitizer.visitLabel(l1);
		sanitizer.visitInsn(Opcodes.RETURN);
		sanitizer.visitLabel(l2);
		sanitizer.visitLocalVariable("x", "I", null, l1, l2, 0);
		sanitizer.visitMaxs(0, 0);
		sanitizer.visitEnd();

		Label m1 = new Label();
		Label m2 = new Label();
		expected.visitLabel(m1);
		expected.visitInsn(Opcodes.RETURN);
		expected.visitLabel(m2);
		expected.visitLocalVariable("x", "I", null, m1, m2, 0);
		expected.visitMaxs(0, 0);
		expected.visitEnd();

		assertOutput();
	}

	@Test
	public void testLocalVariableNegative1() {
		Label l1 = new Label();
		Label l2 = new Label();
		sanitizer.visitCode();
		sanitizer.visitInsn(Opcodes.RETURN);
		sanitizer.visitLabel(l2);
		sanitizer.visitLocalVariable("x", "I", null, l1, l2, 0);
		sanitizer.visitMaxs(0, 0);
		sanitizer.visitEnd();

		Label m2 = new Label();
		expected.visitInsn(Opcodes.RETURN);
		expected.visitLabel(m2);
		expected.visitMaxs(0, 0);
		expected.visitEnd();

		assertOutput();
	}

	@Test
	public void testLocalVariableNegative2() {
		Label l1 = new Label();
		Label l2 = new Label();
		sanitizer.visitCode();
		sanitizer.visitLabel(l1);
		sanitizer.visitInsn(Opcodes.RETURN);
		sanitizer.visitLocalVariable("x", "I", null, l1, l2, 0);
		sanitizer.visitMaxs(0, 0);
		sanitizer.visitEnd();

		Label m1 = new Label();
		expected.visitLabel(m1);
		expected.visitInsn(Opcodes.RETURN);
		expected.visitMaxs(0, 0);
		expected.visitEnd();

		assertOutput();
	}

	@Test
	public void testLineNumberPositive() {
		Label l1 = new Label();
		sanitizer.visitCode();
		sanitizer.visitLabel(l1);
		sanitizer.visitLineNumber(15, l1);
		sanitizer.visitInsn(Opcodes.RETURN);
		sanitizer.visitMaxs(0, 0);
		sanitizer.visitEnd();

		Label m1 = new Label();
		expected.visitLabel(m1);
		expected.visitLineNumber(15, m1);
		expected.visitInsn(Opcodes.RETURN);
		expected.visitMaxs(0, 0);
		expected.visitEnd();

		assertOutput();
	}

	@Test
	public void testLineNumberNegative() {
		Label l1 = new Label();
		sanitizer.visitCode();
		sanitizer.visitLineNumber(15, l1);
		sanitizer.visitInsn(Opcodes.RETURN);
		sanitizer.visitMaxs(0, 0);
		sanitizer.visitEnd();

		expected.visitInsn(Opcodes.RETURN);
		expected.visitMaxs(0, 0);
		expected.visitEnd();

		assertOutput();
	}

	private void assertOutput() {
		assertEquals(dump(expected), dump(actual));
	}

	private MethodRecorder dump(MethodNode node) {
		MethodRecorder rec = new MethodRecorder();
		node.accept(rec.getVisitor());
		return rec;
	}
}
