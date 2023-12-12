/*******************************************************************************
 * Copyright (c) 2009, 2023 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.internal.analysis.filter;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import org.jacoco.core.internal.instr.InstrSupport;
import org.junit.Test;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

/**
 * Unit tests for {@link AbstractMatcher}.
 */
public class AbstractMatcherTest {

	private final AbstractMatcher matcher = new AbstractMatcher() {
	};

	private final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
			"method_name", "()V", null, null);

	@Test
	public void skipNonOpcodes() {
		m.visitFrame(Opcodes.F_FULL, 0, null, 0, null);
		final Label label = new Label();
		m.visitLabel(label);
		m.visitLineNumber(42, label);
		m.visitInsn(Opcodes.NOP);

		// should skip all non opcodes
		matcher.cursor = m.instructions.getFirst();
		matcher.skipNonOpcodes();
		assertSame(m.instructions.getLast(), matcher.cursor);

		// should not change cursor when it points on instruction with opcode
		matcher.skipNonOpcodes();
		assertSame(m.instructions.getLast(), matcher.cursor);

		// should not do anything when cursor is null
		matcher.cursor = null;
		matcher.skipNonOpcodes();
	}

	@Test
	public void nextIs() {
		m.visitInsn(Opcodes.NOP);
		m.visitInsn(Opcodes.NOP);

		// should set cursor to null when opcode mismatch
		matcher.cursor = m.instructions.getFirst();
		matcher.nextIs(Opcodes.ATHROW);
		assertNull(matcher.cursor);

		// should set cursor to next instruction when match
		matcher.cursor = m.instructions.getFirst();
		matcher.nextIs(Opcodes.NOP);
		assertSame(m.instructions.getLast(), matcher.cursor);

		// should not do anything when cursor is null
		matcher.cursor = null;
		matcher.nextIs(Opcodes.NOP);
	}

	@Test
	public void nextIsSwitch() {
		// should set cursor to null when opcode mismatch
		m.visitInsn(Opcodes.NOP);
		m.visitInsn(Opcodes.NOP);
		matcher.cursor = m.instructions.getFirst();
		matcher.nextIsSwitch();
		assertNull(matcher.cursor);

		// should set cursor to next instruction when match
		m.instructions.clear();
		m.visitInsn(Opcodes.NOP);
		m.visitTableSwitchInsn(0, 0, new Label());
		matcher.cursor = m.instructions.getFirst();
		matcher.nextIsSwitch();
		assertSame(m.instructions.getLast(), matcher.cursor);

		// should set cursor to next instruction when match
		m.instructions.clear();
		m.visitInsn(Opcodes.NOP);
		m.visitLookupSwitchInsn(new Label(), null, new Label[0]);
		matcher.cursor = m.instructions.getFirst();
		matcher.nextIsSwitch();
		assertSame(m.instructions.getLast(), matcher.cursor);

		// should not do anything when cursor is null
		matcher.cursor = null;
		matcher.nextIsSwitch();
	}

	@Test
	public void nextIsVar() {
		m.visitInsn(Opcodes.NOP);
		m.visitVarInsn(Opcodes.ILOAD, 42);

		// should set cursor to null when opcode mismatch
		matcher.cursor = m.instructions.getFirst();
		matcher.nextIsVar(Opcodes.ALOAD, "name");
		assertNull(matcher.cursor);

		// should set cursor to next instruction when match
		matcher.cursor = m.instructions.getFirst();
		matcher.nextIsVar(Opcodes.ILOAD, "name");
		assertSame(m.instructions.getLast(), matcher.cursor);

		// should set cursor to null when var mismatch
		matcher.cursor = m.instructions.getFirst();
		matcher.vars.put("name", new VarInsnNode(Opcodes.ILOAD, 13));
		matcher.nextIsVar(Opcodes.ILOAD, "name");
		assertNull(matcher.cursor);

		// should set cursor to next instruction when match
		matcher.cursor = m.instructions.getFirst();
		matcher.vars.put("name", new VarInsnNode(Opcodes.ILOAD, 42));
		matcher.nextIsVar(Opcodes.ILOAD, "name");
		assertSame(m.instructions.getLast(), matcher.cursor);

		// should not do anything when cursor is null
		matcher.cursor = null;
		matcher.nextIsVar(Opcodes.ILOAD, "name");
	}

	@Test
	public void nextIsField() {
		m.visitInsn(Opcodes.NOP);
		m.visitFieldInsn(Opcodes.PUTSTATIC, "owner", "name", "Z");

		// should set cursor to null when opcode mismatch
		matcher.cursor = m.instructions.getFirst();
		matcher.nextIsField(Opcodes.GETSTATIC, "owner", "name", "Z");
		assertNull(matcher.cursor);

		// should set cursor to null when owner mismatch
		matcher.cursor = m.instructions.getFirst();
		matcher.nextIsField(Opcodes.PUTSTATIC, "another_owner", "name", "Z");
		assertNull(matcher.cursor);

		// should set cursor to null when name mismatch
		matcher.cursor = m.instructions.getFirst();
		matcher.nextIsField(Opcodes.PUTSTATIC, "owner", "another_name", "Z");
		assertNull(matcher.cursor);

		// should set cursor to null when descriptor mismatch
		matcher.cursor = m.instructions.getFirst();
		matcher.nextIsField(Opcodes.PUTSTATIC, "owner", "name",
				"another_descriptor");
		assertNull(matcher.cursor);

		// should set cursor to next instruction when match
		matcher.cursor = m.instructions.getFirst();
		matcher.nextIsField(Opcodes.PUTSTATIC, "owner", "name", "Z");
		assertSame(m.instructions.getLast(), matcher.cursor);

		// should not do anything when cursor is null
		matcher.cursor = null;
		matcher.nextIsField(Opcodes.PUTSTATIC, "owner", "name", "Z");
	}

	@Test
	public void nextIsInvoke() {
		m.visitInsn(Opcodes.NOP);
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "owner", "name", "()V", false);

		// should set cursor to null when opcode mismatch
		matcher.cursor = m.instructions.getFirst();
		matcher.nextIsInvoke(Opcodes.INVOKESTATIC, "owner", "name", "()V");
		assertNull(matcher.cursor);

		// should set cursor to null when owner mismatch
		matcher.cursor = m.instructions.getFirst();
		matcher.nextIsInvoke(Opcodes.INVOKEVIRTUAL, "another_owner", "name",
				"()V");
		assertNull(matcher.cursor);

		// should set cursor to null when name mismatch
		matcher.cursor = m.instructions.getFirst();
		matcher.nextIsInvoke(Opcodes.INVOKEVIRTUAL, "owner", "another_name",
				"()V");
		assertNull(matcher.cursor);

		// should set cursor to null when descriptor mismatch
		matcher.cursor = m.instructions.getFirst();
		matcher.nextIsInvoke(Opcodes.INVOKEVIRTUAL, "owner", "name",
				"(Lanother_descriptor;)V");
		assertNull(matcher.cursor);

		// should set cursor to next instruction when match
		matcher.cursor = m.instructions.getFirst();
		matcher.nextIsInvoke(Opcodes.INVOKEVIRTUAL, "owner", "name", "()V");
		assertSame(m.instructions.getLast(), matcher.cursor);

		// should not do anything when cursor is null
		matcher.cursor = null;
		matcher.nextIsInvoke(Opcodes.INVOKEVIRTUAL, "owner", "name", "()V");
	}

	@Test
	public void nextIsType() {
		m.visitInsn(Opcodes.NOP);
		m.visitTypeInsn(Opcodes.NEW, "descriptor");

		// should set cursor to null when opcode mismatch
		matcher.cursor = m.instructions.getFirst();
		matcher.nextIsType(Opcodes.CHECKCAST, "descriptor");
		assertNull(matcher.cursor);

		// should set cursor to null when descriptor mismatch
		matcher.cursor = m.instructions.getFirst();
		matcher.nextIsType(Opcodes.NEW, "another_descriptor");
		assertNull(matcher.cursor);

		// should set cursor to next instruction when match
		matcher.cursor = m.instructions.getFirst();
		matcher.nextIsType(Opcodes.NEW, "descriptor");
		assertSame(m.instructions.getLast(), matcher.cursor);

		// should not do anything when cursor is null
		matcher.cursor = null;
		matcher.nextIsType(Opcodes.NEW, "descriptor");
	}

	@Test
	public void firstIsALoad0() {
		// should set cursor to null when no opcodes are present
		matcher.firstIsALoad0(m);
		assertNull(matcher.cursor);

		// should set cursor to null when opcode mismatch
		m.visitInsn(Opcodes.NOP);
		matcher.firstIsALoad0(m);
		assertNull(matcher.cursor);

		// should set cursor to null when var mismatch
		m.instructions.clear();
		m.visitVarInsn(Opcodes.ALOAD, 1);
		matcher.firstIsALoad0(m);
		assertNull(matcher.cursor);

		// should set cursor to first instruction when match
		m.instructions.clear();
		m.visitVarInsn(Opcodes.ALOAD, 0);
		matcher.firstIsALoad0(m);
		assertSame(m.instructions.getLast(), matcher.cursor);
	}

}
