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

	final boolean nextIsAddSuppressed() {
		if (!nextIs(Opcodes.INVOKEVIRTUAL)) {
			return false;
		}
		final MethodInsnNode m = (MethodInsnNode) cursor;
		return "java/lang/Throwable".equals(m.owner)
				&& "addSuppressed".equals(m.name);
	}

	final boolean nextIsVar(final int opcode, final String name) {
		if (!nextIs(opcode)) {
			return false;
		}
		final VarInsnNode actual = (VarInsnNode) cursor;
		final VarInsnNode expected = vars.get(name);
		if (expected == null) {
			vars.put(name, actual);
			return true;
		} else {
			return expected.var == actual.var;
		}
	}

	/**
	 * Moves {@link #cursor} to next instruction and returns <code>true</code>
	 * if it has given opcode.
	 */
	final boolean nextIs(final int opcode) {
		next();
		return cursor != null && cursor.getOpcode() == opcode;
	}

	/**
	 * Moves {@link #cursor} to next instruction.
	 */
	final void next() {
		do {
			cursor = cursor.getNext();
		} while (cursor != null && (cursor.getType() == AbstractInsnNode.FRAME
				|| cursor.getType() == AbstractInsnNode.LABEL
				|| cursor.getType() == AbstractInsnNode.LINE));
	}

}
