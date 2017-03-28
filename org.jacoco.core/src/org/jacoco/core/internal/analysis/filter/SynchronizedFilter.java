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

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TryCatchBlockNode;

/**
 * Filters code that is generated for synchronized statement.
 */
public final class SynchronizedFilter implements IFilter {

	public void filter(MethodNode methodNode, IFilterOutput output) {
		for (TryCatchBlockNode tryCatch : methodNode.tryCatchBlocks) {
			if (tryCatch.type != null) {
				continue;
			}
			if (tryCatch.start == tryCatch.handler) {
				continue;
			}
			final AbstractInsnNode to = new Matcher(tryCatch.handler).match();
			if (to == null) {
				continue;
			}
			output.ignore(tryCatch.handler, to);
		}
	}

	private static class Matcher {
		private final AbstractInsnNode start;
		private AbstractInsnNode cursor;

		private Matcher(final AbstractInsnNode start) {
			this.start = start;
		}

		private AbstractInsnNode match() {
			if (nextIsEcj() || nextIsJavac()) {
				return cursor;
			}
			return null;
		}

		private boolean nextIsJavac() {
			cursor = start;
			return nextIs(Opcodes.ASTORE) && nextIs(Opcodes.ALOAD)
					&& nextIs(Opcodes.MONITOREXIT) && nextIs(Opcodes.ALOAD)
					&& nextIs(Opcodes.ATHROW);
		}

		private boolean nextIsEcj() {
			cursor = start;
			return nextIs(Opcodes.ALOAD) && nextIs(Opcodes.MONITOREXIT)
					&& nextIs(Opcodes.ATHROW);
		}

		/**
		 * Moves {@link #cursor} to next instruction and returns
		 * <code>true</code> if it has given opcode.
		 */
		private boolean nextIs(int opcode) {
			next();
			return cursor != null && cursor.getOpcode() == opcode;
		}

		/**
		 * Moves {@link #cursor} to next instruction.
		 */
		private void next() {
			do {
				cursor = cursor.getNext();
			} while (cursor != null
					&& (cursor.getType() == AbstractInsnNode.FRAME
							|| cursor.getType() == AbstractInsnNode.LABEL
							|| cursor.getType() == AbstractInsnNode.LINE));
		}
	}

}
