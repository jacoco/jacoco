/*******************************************************************************
 * Copyright (c) 2009, 2017 Mountainminds GmbH & Co. KG and Contributors
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
import org.objectweb.asm.tree.VarInsnNode;

abstract class AbstractMatcher {

	final Map<String, VarInsnNode> vars = new HashMap<String, VarInsnNode>();

	AbstractInsnNode cursor;

	final void nextIsAddSuppressed() {
		nextIs(Opcodes.INVOKEVIRTUAL);
		if (cursor == null) {
			return;
		}
		final MethodInsnNode m = (MethodInsnNode) cursor;
		if ("java/lang/Throwable".equals(m.owner)
				&& "addSuppressed".equals(m.name)) {
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

	final void skipNonOpcodes() {
		while (cursor != null && (cursor.getType() == AbstractInsnNode.FRAME
				|| cursor.getType() == AbstractInsnNode.LABEL
				|| cursor.getType() == AbstractInsnNode.LINE)) {
			cursor = cursor.getNext();
		}
	}

}
