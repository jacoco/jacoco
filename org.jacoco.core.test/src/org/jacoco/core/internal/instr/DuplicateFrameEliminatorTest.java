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
package org.jacoco.core.internal.instr;

import static org.junit.Assert.assertEquals;

import org.jacoco.core.instr.MethodRecorder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.asm.Handle;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.IincInsnNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.LookupSwitchInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MultiANewArrayInsnNode;
import org.objectweb.asm.tree.TableSwitchInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

/**
 * Unit tests for {@link DuplicateFrameEliminator}.
 */
public class DuplicateFrameEliminatorTest {

	private MethodRecorder actual;
	private MethodRecorder expected;
	private MethodVisitor eliminator;

	@Before
	public void setup() {
		actual = new MethodRecorder();
		expected = new MethodRecorder();
		eliminator = new DuplicateFrameEliminator(actual.getVisitor());
	}

	@After
	public void verify() {
		assertEquals(expected, actual);
	}

	@Test
	public void testDuplicateFrame() {
		frame(eliminator);
		frame(eliminator);

		frame(expected.getVisitor());
	}

	@Test
	public void testInsn() {
		testInstructionBetweenFrames(new InsnNode(Opcodes.NOP));
	}

	@Test
	public void testIntInsn() {
		testInstructionBetweenFrames(new IntInsnNode(Opcodes.BIPUSH, 123));
	}

	@Test
	public void testVarInsn() {
		testInstructionBetweenFrames(new VarInsnNode(Opcodes.ILOAD, 0));
	}

	@Test
	public void testTypeInsn() {
		testInstructionBetweenFrames(
				new TypeInsnNode(Opcodes.NEW, "java/lang/Object"));
	}

	@Test
	public void testFieldInsn() {
		testInstructionBetweenFrames(
				new FieldInsnNode(Opcodes.GETFIELD, "Foo", "f", "I"));
	}

	@Test
	public void testMethodInsn() {
		testInstructionBetweenFrames(new MethodInsnNode(Opcodes.INVOKEVIRTUAL,
				"Foo", "run", "()V", false));
	}

	@Test
	public void testInvokeDynamicInsn() {
		testInstructionBetweenFrames(new InvokeDynamicInsnNode("foo", "()V",
				new Handle(Opcodes.H_INVOKEVIRTUAL, null, null, null, false)));
	}

	@Test
	public void testJumpInsn() {
		testInstructionBetweenFrames(
				new JumpInsnNode(Opcodes.GOTO, new LabelNode()));
	}

	@Test
	public void testLdcInsn() {
		testInstructionBetweenFrames(new LdcInsnNode("JaCoCo"));
	}

	@Test
	public void testIincInsn() {
		testInstructionBetweenFrames(new IincInsnNode(3, 42));
	}

	@Test
	public void testTableSwitchInsn() {
		testInstructionBetweenFrames(new TableSwitchInsnNode(0, 0,
				new LabelNode(), new LabelNode[0]));
	}

	@Test
	public void testLookupSwitchInsn() {
		testInstructionBetweenFrames(new LookupSwitchInsnNode(new LabelNode(),
				new int[0], new LabelNode[0]));
	}

	@Test
	public void testMultiANewArrayInsn() {
		testInstructionBetweenFrames(
				new MultiANewArrayInsnNode("java/lang/String", 4));
	}

	private void testInstructionBetweenFrames(AbstractInsnNode node) {
		frame(eliminator);
		node.accept(eliminator);
		frame(eliminator);

		frame(expected.getVisitor());
		node.accept(expected.getVisitor());
		frame(expected.getVisitor());
	}

	private void frame(MethodVisitor mv) {
		mv.visitFrame(Opcodes.F_NEW, 1, new Object[] { Opcodes.INTEGER }, 0,
				new Object[0]);
	}
}
