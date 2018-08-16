/*******************************************************************************
 * Copyright (c) 2009, 2018 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.internal.analysis.filter;

import java.util.HashMap;
import java.util.Map;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

abstract class AbstractMatcher {

	final Map<String, VarInsnNode> vars = new HashMap<String, VarInsnNode>();

	AbstractInsnNode cursor;

	/**
	 * Sets {@link #cursor} to first instruction of method if it is
	 * <code>ALOAD 0</code>, otherwise sets it to <code>null</code>.
	 */
	final void firstIsALoad0(final MethodNode methodNode) {
		cursor = methodNode.instructions.getFirst();
		skipNonOpcodes();
		if (cursor.getOpcode() == Opcodes.ALOAD
				&& ((VarInsnNode) cursor).var == 0) {
			return;
		}
		cursor = null;
	}

	/**
	 * Moves {@link #cursor} to next instruction if it is <code>NEW</code> with
	 * given operand, otherwise sets it to <code>null</code>.
	 */
	final void nextIsNew(final String desc) {
		nextIs(Opcodes.NEW);
		if (cursor == null) {
			return;
		}
		final TypeInsnNode i = (TypeInsnNode) cursor;
		if (desc.equals(i.desc)) {
			return;
		}
		cursor = null;
	}

	/**
	 * Moves {@link #cursor} to next instruction if it is
	 * <code>INVOKESPECIAL &lt;init&gt;</code> with given owner and descriptor,
	 * otherwise sets it to <code>null</code>.
	 */
	final void nextIsInvokeSuper(final String owner, final String desc) {
		nextIs(Opcodes.INVOKESPECIAL);
		MethodInsnNode m = (MethodInsnNode) cursor;
		if (m != null && owner.equals(m.owner) && "<init>".equals(m.name)
				&& desc.equals(m.desc)) {
			return;
		}
		cursor = null;
	}

	final void nextIsInvokeVirtual(final String owner, final String name) {
		nextIs(Opcodes.INVOKEVIRTUAL);
		if (cursor == null) {
			return;
		}
		final MethodInsnNode m = (MethodInsnNode) cursor;
		if (owner.equals(m.owner) && name.equals(m.name)) {
			return;
		}
		cursor = null;
	}

	final void nextIsInvokeStatic(final String owner, final String name) {
		nextIs(Opcodes.INVOKESTATIC);
		if (cursor == null) {
			return;
		}
		final MethodInsnNode m = (MethodInsnNode) cursor;
		if (owner.equals(m.owner) && name.equals(m.name)) {
			return;
		}
		cursor = null;
	}

	final void nextIsVar(final int opcode, final String name) {
		nextIs(opcode);
		if (cursor == null) {
			return;
		}
		final VarInsnNode actual = (VarInsnNode) cursor;
		final VarInsnNode expected = vars.get(name);
		if (expected == null) {
			vars.put(name, actual);
		} else if (expected.var != actual.var) {
			cursor = null;
		}
	}

	/**
	 * Moves {@link #cursor} to next instruction if it is
	 * <code>TABLESWITCH</code> or <code>LOOKUPSWITCH</code>, otherwise sets it
	 * to <code>null</code>.
	 */
	final void nextIsSwitch() {
		next();
		if (cursor == null) {
			return;
		}
		switch (cursor.getOpcode()) {
		case Opcodes.TABLESWITCH:
		case Opcodes.LOOKUPSWITCH:
			return;
		default:
			cursor = null;
		}
	}

	/**
	 * Moves {@link #cursor} to next instruction if it has given opcode,
	 * otherwise sets it to <code>null</code>.
	 */
	final void nextIs(final int opcode) {
		next();
		if (cursor == null) {
			return;
		}
		if (cursor.getOpcode() != opcode) {
			cursor = null;
		}
	}

	/**
	 * Moves {@link #cursor} to next instruction.
	 */
	final void next() {
		if (cursor == null) {
			return;
		}
		cursor = cursor.getNext();
		skipNonOpcodes();
	}

	private void skipNonOpcodes() {
		while (cursor != null && (cursor.getType() == AbstractInsnNode.FRAME
				|| cursor.getType() == AbstractInsnNode.LABEL
				|| cursor.getType() == AbstractInsnNode.LINE)) {
			cursor = cursor.getNext();
		}
	}

}
