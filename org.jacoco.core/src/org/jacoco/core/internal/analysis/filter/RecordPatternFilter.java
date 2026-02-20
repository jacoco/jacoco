/*******************************************************************************
 * Copyright (c) 2009, 2026 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.internal.analysis.filter;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TryCatchBlockNode;

/**
 * Filters code that is generated for record patterns.
 */
final class RecordPatternFilter implements IFilter {

	public void filter(final MethodNode methodNode,
			final IFilterContext context, final IFilterOutput output) {
		final Matcher matcher = new Matcher();
		for (final TryCatchBlockNode t : methodNode.tryCatchBlocks) {
			if ("java/lang/Throwable".equals(t.type)) {
				matcher.match(t, output);
			}
		}
	}

	private static class Matcher extends AbstractMatcher {
		void match(final TryCatchBlockNode t, final IFilterOutput output) {
			if (t.end.getPrevious().getOpcode() != Opcodes.INVOKEVIRTUAL) {
				return;
			}
			final MethodInsnNode invokeInstruction = (MethodInsnNode) t.end
					.getPrevious();
			if (invokeInstruction.getPrevious() != t.start) {
				return;
			}

			cursor = t.handler;
			nextIsVar(Opcodes.ASTORE, "cause");
			nextIsType(Opcodes.NEW, "java/lang/MatchException");
			nextIs(Opcodes.DUP);
			nextIsVar(Opcodes.ALOAD, "cause");
			nextIsInvoke(Opcodes.INVOKEVIRTUAL, "java/lang/Throwable",
					"toString", "()Ljava/lang/String;");
			nextIsVar(Opcodes.ALOAD, "cause");
			nextIsInvoke(Opcodes.INVOKESPECIAL, "java/lang/MatchException",
					"<init>", "(Ljava/lang/String;Ljava/lang/Throwable;)V");
			nextIs(Opcodes.ATHROW);
			if (cursor == null) {
				return;
			}
			output.ignore(t.handler, cursor);

			cursor = t.end;
			final Type type = Type.getReturnType(invokeInstruction.desc);
			if (!isPrimitive(type)) {
				return;
			}
			nextIs(type.getOpcode(Opcodes.ISTORE));
			nextIs(Opcodes.ILOAD);
			nextIs(Opcodes.ISTORE);
			if (cursor == null) {
				cursor = t.end;
				nextIs(Opcodes.ISTORE);
			}
			nextIs(Opcodes.ICONST_1);
			nextIs(Opcodes.IFEQ);
			final JumpInsnNode jumpInstruction = (JumpInsnNode) cursor;
			if (jumpInstruction == null) {
				return;
			}
			output.ignore(jumpInstruction, jumpInstruction);
			cursor = jumpInstruction.label;
			next(/* ICONST_x, BIPUSH */);
			nextIs(Opcodes.ISTORE);
			nextIs(Opcodes.GOTO);
			if (cursor != null) {
				output.ignore(jumpInstruction.label, cursor);
			}
		}
	}

	private static boolean isPrimitive(final Type type) {
		switch (type.getSort()) {
		case Type.BOOLEAN:
		case Type.CHAR:
		case Type.BYTE:
		case Type.SHORT:
		case Type.INT:
		case Type.FLOAT:
		case Type.LONG:
		case Type.DOUBLE:
			return true;
		default:
			return false;
		}
	}

}
