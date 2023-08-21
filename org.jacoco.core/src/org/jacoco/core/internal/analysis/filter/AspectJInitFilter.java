/*******************************************************************************
 * Copyright (c) 2009, 2023 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Lars Grefer - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.internal.analysis.filter;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

public class AspectJInitFilter implements IFilter {
	@Override
	public void filter(MethodNode methodNode, IFilterContext context,
			IFilterOutput output) {
		if (!methodNode.name.equals("<clinit>")) {
			return;
		}

		PreClinitMatcher preClinitMatcher = new PreClinitMatcher(methodNode);

		AbstractInsnNode match = preClinitMatcher.match();

		if (match != null) {
			output.ignore(methodNode.instructions.getFirst(), match);
		}

		for (TryCatchBlockNode tryCatchBlock : methodNode.tryCatchBlocks) {
			PostClinitMatcher postClinitMatcher = new PostClinitMatcher(
					tryCatchBlock.start);

			AbstractInsnNode end = postClinitMatcher.match();

			if (end != null) {
				AbstractInsnNode start = tryCatchBlock.start;

				if (start.getPrevious().getOpcode() == Opcodes.NOP) {
					start = start.getPrevious();
				}

				output.ignore(start, end);
			}
		}
	}

	static class PreClinitMatcher extends AbstractMatcher {
		private MethodNode methodNode;

		PreClinitMatcher(MethodNode methodNode) {
			this.methodNode = methodNode;
		}

		public AbstractInsnNode match() {
			cursor = methodNode.instructions.getFirst();
			if (cursor.getOpcode() != Opcodes.INVOKESTATIC) {
				nextIs(Opcodes.INVOKESTATIC);
			}
			if (cursor == null || !((MethodInsnNode) cursor).name
					.equals("ajc$preClinit")) {
				cursor = null;
				return null;
			}
			skipNonOpcodes();
			AbstractInsnNode end = cursor;

			nextIs(Opcodes.NOP);
			if (cursor != null) {
				return cursor;
			} else {
				cursor = end;
			}

			nextIs(Opcodes.RETURN);
			if (cursor != null) {
				return cursor;
			} else {
				return end;
			}
		}
	}

	static class PostClinitMatcher extends AbstractMatcher {

		private AbstractInsnNode start;

		PostClinitMatcher(AbstractInsnNode start) {
			this.start = start;
		}

		public AbstractInsnNode match() {
			cursor = start;
			if (cursor.getOpcode() != Opcodes.INVOKESTATIC) {
				nextIs(Opcodes.INVOKESTATIC);
			}
			if (cursor == null || !((MethodInsnNode) cursor).name
					.equals("ajc$postClinit")) {
				cursor = null;
				return null;
			}
			nextIs(Opcodes.GOTO);
			LabelNode jumpTarget = ((JumpInsnNode) cursor).label;
			nextIs(Opcodes.ASTORE);
			nextIs(Opcodes.ALOAD);
			nextIs(Opcodes.PUTSTATIC);
			if (!((FieldInsnNode) cursor).name.startsWith("ajc$")) {
				cursor = null;
				return null;
			}
			if (cursor.getNext() != jumpTarget) {
				return cursor;
			} else {
				cursor = jumpTarget;
			}

			nextIs(Opcodes.RETURN);
			if (cursor == null) {
				return jumpTarget;
			} else {
				return cursor;
			}
		}
	}
}
