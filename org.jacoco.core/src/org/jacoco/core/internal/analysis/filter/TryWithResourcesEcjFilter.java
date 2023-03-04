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

import java.util.HashMap;
import java.util.Map;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TryCatchBlockNode;

/**
 * Filters code that ECJ generates for try-with-resources statement.
 */
public final class TryWithResourcesEcjFilter implements IFilter {

	public void filter(final MethodNode methodNode,
			final IFilterContext context, final IFilterOutput output) {
		if (methodNode.tryCatchBlocks.isEmpty()) {
			return;
		}
		final Matcher matcher = new Matcher(output);
		for (final TryCatchBlockNode t : methodNode.tryCatchBlocks) {
			if (t.type == null) {
				matcher.start(t.handler);
				if (!matcher.matchEcj()) {
					matcher.start(t.handler);
					matcher.matchEcjNoFlowOut();
				}
			}
		}
	}

	static class Matcher extends AbstractMatcher {

		private final IFilterOutput output;

		private final Map<String, String> owners = new HashMap<String, String>();
		private final Map<String, LabelNode> labels = new HashMap<String, LabelNode>();

		private AbstractInsnNode start;

		Matcher(final IFilterOutput output) {
			this.output = output;
		}

		private void start(final AbstractInsnNode start) {
			this.start = start;
			cursor = start.getPrevious();
			vars.clear();
			labels.clear();
			owners.clear();
		}

		private boolean matchEcj() {
			// "catch (any primaryExc)"
			nextIsVar(Opcodes.ASTORE, "primaryExc");
			nextIsEcjCloseAndThrow("r0");

			AbstractInsnNode c;
			int resources = 1;
			String r = "r" + resources;
			c = cursor;
			while (nextIsEcjClose(r)) {
				nextIsJump(Opcodes.GOTO, r + ".end");
				nextIsEcjSuppress(r);
				nextIsEcjCloseAndThrow(r);
				resources++;
				r = "r" + resources;
				c = cursor;
			}
			cursor = c;
			nextIsEcjSuppress("last");
			// "throw primaryExc"
			nextIsVar(Opcodes.ALOAD, "primaryExc");
			nextIs(Opcodes.ATHROW);
			if (cursor == null) {
				return false;
			}
			final AbstractInsnNode end = cursor;

			AbstractInsnNode startOnNonExceptionalPath = start.getPrevious();
			cursor = startOnNonExceptionalPath;
			while (!nextIsEcjClose("r0")) {
				startOnNonExceptionalPath = startOnNonExceptionalPath
						.getPrevious();
				cursor = startOnNonExceptionalPath;
				if (cursor == null) {
					return false;
				}
			}
			startOnNonExceptionalPath = startOnNonExceptionalPath.getNext();

			next();
			if (cursor == null || cursor.getOpcode() != Opcodes.GOTO) {
				return false;
			}

			output.ignore(startOnNonExceptionalPath, cursor);
			output.ignore(start, end);
			return true;
		}

		private boolean matchEcjNoFlowOut() {
			// "catch (any primaryExc)"
			nextIsVar(Opcodes.ASTORE, "primaryExc");

			AbstractInsnNode c;
			int resources = 0;
			String r = "r" + resources;
			c = cursor;
			while (nextIsEcjCloseAndThrow(r) && nextIsEcjSuppress(r)) {
				resources++;
				r = "r" + resources;
				c = cursor;
			}
			cursor = c;
			// "throw primaryExc"
			nextIsVar(Opcodes.ALOAD, "primaryExc");
			nextIs(Opcodes.ATHROW);
			if (cursor == null) {
				return false;
			}
			final AbstractInsnNode end = cursor;

			AbstractInsnNode startOnNonExceptionalPath = start.getPrevious();
			cursor = startOnNonExceptionalPath;
			while (!nextIsEcjClose("r0")) {
				startOnNonExceptionalPath = startOnNonExceptionalPath
						.getPrevious();
				cursor = startOnNonExceptionalPath;
				if (cursor == null) {
					return false;
				}
			}
			startOnNonExceptionalPath = startOnNonExceptionalPath.getNext();
			for (int i = 1; i < resources; i++) {
				if (!nextIsEcjClose("r" + i)) {
					return false;
				}
			}

			output.ignore(startOnNonExceptionalPath, cursor);
			output.ignore(start, end);
			return true;
		}

		private boolean nextIsEcjClose(final String name) {
			nextIsVar(Opcodes.ALOAD, name);
			// "if (r != null)"
			nextIsJump(Opcodes.IFNULL, name + ".end");
			// "r.close()"
			nextIsClose(name);
			return cursor != null;
		}

		private boolean nextIsEcjCloseAndThrow(final String name) {
			nextIsVar(Opcodes.ALOAD, name);
			// "if (r != null)"
			nextIsJump(Opcodes.IFNULL, name);
			// "r.close()"
			nextIsClose(name);
			nextIsLabel(name);
			nextIsVar(Opcodes.ALOAD, "primaryExc");
			nextIs(Opcodes.ATHROW);
			return cursor != null;
		}

		private boolean nextIsEcjSuppress(final String name) {
			final String suppressedExc = name + ".t";
			final String startLabel = name + ".suppressStart";
			final String endLabel = name + ".suppressEnd";
			nextIsVar(Opcodes.ASTORE, suppressedExc);
			// "suppressedExc = t"
			// "if (primaryExc != null)"
			nextIsVar(Opcodes.ALOAD, "primaryExc");
			nextIsJump(Opcodes.IFNONNULL, startLabel);
			// "primaryExc = suppressedExc"
			nextIsVar(Opcodes.ALOAD, suppressedExc);
			nextIsVar(Opcodes.ASTORE, "primaryExc");
			nextIsJump(Opcodes.GOTO, endLabel);
			// "if (primaryExc == suppressedExc)"
			nextIsLabel(startLabel);
			nextIsVar(Opcodes.ALOAD, "primaryExc");
			nextIsVar(Opcodes.ALOAD, suppressedExc);
			nextIsJump(Opcodes.IF_ACMPEQ, endLabel);
			// "primaryExc.addSuppressed(suppressedExc)"
			nextIsVar(Opcodes.ALOAD, "primaryExc");
			nextIsVar(Opcodes.ALOAD, suppressedExc);
			nextIsInvoke(Opcodes.INVOKEVIRTUAL, "java/lang/Throwable",
					"addSuppressed", "(Ljava/lang/Throwable;)V");
			nextIsLabel(endLabel);
			return cursor != null;
		}

		private void nextIsClose(final String name) {
			nextIsVar(Opcodes.ALOAD, name);
			next();
			if (cursor == null) {
				return;
			}
			if (cursor.getOpcode() != Opcodes.INVOKEINTERFACE
					&& cursor.getOpcode() != Opcodes.INVOKEVIRTUAL) {
				cursor = null;
				return;
			}
			final MethodInsnNode m = (MethodInsnNode) cursor;
			if (!"close".equals(m.name) || !"()V".equals(m.desc)) {
				cursor = null;
				return;
			}
			final String actual = m.owner;
			final String expected = owners.get(name);
			if (expected == null) {
				owners.put(name, actual);
			} else if (!expected.equals(actual)) {
				cursor = null;
			}
		}

		private void nextIsJump(final int opcode, final String name) {
			nextIs(opcode);
			if (cursor == null) {
				return;
			}
			final LabelNode actual = ((JumpInsnNode) cursor).label;
			final LabelNode expected = labels.get(name);
			if (expected == null) {
				labels.put(name, actual);
			} else if (expected != actual) {
				cursor = null;
			}
		}

		private void nextIsLabel(final String name) {
			if (cursor == null) {
				return;
			}
			cursor = cursor.getNext();
			if (cursor.getType() != AbstractInsnNode.LABEL) {
				cursor = null;
				return;
			}
			final LabelNode actual = (LabelNode) cursor;
			final LabelNode expected = labels.get(name);
			if (expected != actual) {
				cursor = null;
			}
		}

	}

}
