/*******************************************************************************
 * Copyright (c) 2009, 2025 Mountainminds GmbH & Co. KG and Contributors
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

import java.util.List;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.LookupSwitchInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TableSwitchInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;

/**
 * Filters default branch generated by compilers for exhaustive switch
 * expressions.
 */
final class ExhaustiveSwitchFilter implements IFilter {

	public void filter(final MethodNode methodNode,
			final IFilterContext context, final IFilterOutput output) {
		final Matcher matcher = new Matcher();
		int line = -1;
		for (final AbstractInsnNode i : methodNode.instructions) {
			if (i.getType() == AbstractInsnNode.LINE) {
				line = ((LineNumberNode) i).line;
			}
			matcher.match(i, line, output);
		}
	}

	private static class Matcher extends AbstractMatcher {
		public void match(final AbstractInsnNode start, final int line,
				final IFilterOutput output) {
			final LabelNode dflt;
			if (start.getOpcode() == Opcodes.LOOKUPSWITCH) {
				dflt = ((LookupSwitchInsnNode) start).dflt;
			} else if (start.getOpcode() == Opcodes.TABLESWITCH) {
				dflt = ((TableSwitchInsnNode) start).dflt;
			} else {
				return;
			}

			cursor = skipToLineNumberOrInstruction(dflt);
			if (cursor == null) {
				return;
			}
			if (cursor.getType() == AbstractInsnNode.LINE) {
				if (line != ((LineNumberNode) cursor).line) {
					return;
				}
				cursor = skipNonOpcodes(cursor);
			}
			if (cursor == null || cursor.getOpcode() != Opcodes.NEW) {
				return;
			}
			if ("java/lang/MatchException"
					.equals(((TypeInsnNode) cursor).desc)) {
				// since Java 21
				nextIs(Opcodes.DUP);
				nextIs(Opcodes.ACONST_NULL);
				nextIs(Opcodes.ACONST_NULL);
				nextIsInvoke(Opcodes.INVOKESPECIAL, "java/lang/MatchException",
						"<init>", "(Ljava/lang/String;Ljava/lang/Throwable;)V");
			} else if ("java/lang/IncompatibleClassChangeError"
					.equals(((TypeInsnNode) cursor).desc)) {
				// prior to Java 21
				nextIs(Opcodes.DUP);
				nextIsInvoke(Opcodes.INVOKESPECIAL,
						"java/lang/IncompatibleClassChangeError", "<init>",
						"()V");
			} else {
				return;
			}
			nextIs(Opcodes.ATHROW);
			if (cursor == null) {
				return;
			}
			output.ignore(dflt, cursor);
			KotlinWhenFilter.ignoreDefaultBranch(start, output);
		}

		private static AbstractInsnNode skipToLineNumberOrInstruction(
				AbstractInsnNode cursor) {
			while (cursor != null && (cursor.getType() == AbstractInsnNode.FRAME
					|| cursor.getType() == AbstractInsnNode.LABEL)) {
				cursor = cursor.getNext();
			}
			return cursor;
		}
	}

}
