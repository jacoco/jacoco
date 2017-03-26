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
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import java.util.HashMap;
import java.util.Map;

/**
 * Filters code that is generated for try-with-resources statement.
 */
public final class TryWithResourcesFilter implements IFilter {

	public void filter(final MethodNode methodNode,
			final IFilterOutput output) {
		if (methodNode.tryCatchBlocks.isEmpty()
				|| methodNode.instructions.size() <= 11) {
			return;
		}
		final Matcher matcher = new Matcher(output);
		for (AbstractInsnNode i = methodNode.instructions.getFirst()
				.getNext(); i != null; i = i.getNext()) {
			if (i.getOpcode() == Opcodes.ALOAD) {
				matcher.match(i);
			}
		}
	}

	static class Matcher {
		private final IFilterOutput output;

		private final Map<String, VarInsnNode> vars = new HashMap<String, VarInsnNode>();
		private final Map<String, String> owners = new HashMap<String, String>();
		private final Map<String, LabelNode> labels = new HashMap<String, LabelNode>();

		private AbstractInsnNode start;
		private AbstractInsnNode cursor;

		Matcher(final IFilterOutput output) {
			this.output = output;
		}

		private enum Pattern {
			ECJ, JAVAC_OPTIMAL, JAVAC_FULL, JAVAC_OMITTED_NULL_CHECK, JAVAC_METHOD,
		}

		public void match(final AbstractInsnNode start) {
			this.start = start;
			for (final Pattern t : Pattern.values()) {
				cursor = start.getPrevious();
				vars.clear();
				labels.clear();
				owners.clear();
				if (matches(t)) {
					break;
				}
			}
		}

		private boolean matches(final Pattern pattern) {
			switch (pattern) {
			case ECJ:
				return matchEcj();
			default:
				return matchJavac(pattern);
			}
		}

		private boolean matchJavac(final Matcher.Pattern p) {
			if (!nextIsJavacClose(p)) {
				return false;
			}

			AbstractInsnNode c = cursor;
			if (!nextIs(Opcodes.GOTO)) {
				cursor = c;
			}

			final AbstractInsnNode bodyStart = cursor;
			while (!nextIsVar(Opcodes.ASTORE, "primaryExc")) {
				if (cursor == null) {
					return false;
				}
			}
			final AbstractInsnNode bodyEnd = cursor.getPrevious().getPrevious();
			cursor = bodyEnd.getPrevious();

			// "catch (Throwable t)"
			if (!nextIs(Opcodes.ASTORE)) {
				return false;
			}
			// "primaryExc = t"
			if (!nextIs(Opcodes.ALOAD)) {
				return false;
			}
			if (!nextIsVar(Opcodes.ASTORE, "primaryExc")) {
				return false;
			}
			// "throw t"
			if (!nextIs(Opcodes.ALOAD)) {
				return false;
			}
			if (!nextIs(Opcodes.ATHROW)) {
				return false;
			}

			// "catch (any t)"
			if (!nextIs(Opcodes.ASTORE)) {
				return false;
			}
			if (!nextIsJavacClose(p)) {
				return false;
			}
			// "throw t"
			if (!nextIs(Opcodes.ALOAD)) {
				return false;
			}
			if (!nextIs(Opcodes.ATHROW)) {
				return false;
			}

			output.ignore(start, bodyStart);
			output.ignore(bodyEnd, cursor);
			return true;
		}

		/**
		 * On a first invocation will associate variables with names "r" and
		 * "primaryExc", on subsequent invocations will use those associations
		 * for checks.
		 */
		private boolean nextIsJavacClose(final Pattern p) {
			switch (p) {
			case JAVAC_METHOD:
			case JAVAC_FULL:
				// "if (r != null)"
				if (!(nextIsVar(Opcodes.ALOAD, "r")
						&& nextIs(Opcodes.IFNULL))) {
					return false;
				}
			}
			switch (p) {
			case JAVAC_METHOD:
			case JAVAC_OPTIMAL:
				if (nextIsVar(Opcodes.ALOAD, "primaryExc")
						&& nextIsVar(Opcodes.ALOAD, "r")
						&& nextIs(Opcodes.INVOKESTATIC)) {
					final MethodInsnNode m = (MethodInsnNode) cursor;
					return "$closeResource".equals(m.name)
							&& "(Ljava/lang/Throwable;Ljava/lang/AutoCloseable;)V"
									.equals(m.desc);
				}
				return false;
			case JAVAC_FULL:
			case JAVAC_OMITTED_NULL_CHECK:
				return nextIsVar(Opcodes.ALOAD, "primaryExc")
						// "if (primaryExc != null)"
						&& nextIs(Opcodes.IFNULL)
						// "r.close()"
						&& nextIsClose("r") && nextIs(Opcodes.GOTO)
						// "catch (Throwable t)"
						&& nextIs(Opcodes.ASTORE)
						// "primaryExc.addSuppressed(t)"
						&& nextIsVar(Opcodes.ALOAD, "primaryExc")
						&& nextIs(Opcodes.ALOAD) && nextIsAddSuppressed()
						&& nextIs(Opcodes.GOTO)
						// "r.close()"
						&& nextIsClose("r");
			default:
				return false;
			}
		}

		private boolean matchEcj() {
			if (!nextIsEcjClose("r0")) {
				return false;
			}

			AbstractInsnNode c = cursor;
			next();
			if (cursor.getOpcode() != Opcodes.GOTO) {
				cursor = c;
				return nextIsEcjNoFlowOut(output);
			}
			cursor = c;

			if (!nextIsJump(Opcodes.GOTO, "r0.end")) {
				return false;
			}
			// "catch (any primaryExc)"
			if (!nextIsVar(Opcodes.ASTORE, "primaryExc")) {
				return false;
			}
			if (!nextIsEcjCloseAndThrow("r0")) {
				return false;
			}
			int resources = 1;
			String r = "r" + resources;
			c = cursor;
			while (nextIsLabel("r" + (resources - 1) + ".end")
					&& nextIsEcjClose(r)) {
				if (!nextIsJump(Opcodes.GOTO, r + ".end")) {
					return false;
				}
				if (!nextIsEcjSuppress(r)) {
					return false;
				}
				if (!nextIsEcjCloseAndThrow(r)) {
					return false;
				}
				resources++;
				r = "r" + resources;
				c = cursor;
			}
			cursor = c;
			if (!nextIsEcjSuppress("last")) {
				return false;
			}
			// "throw primaryExc"
			if (!nextIsVar(Opcodes.ALOAD, "primaryExc")) {
				return false;
			}
			if (!nextIs(Opcodes.ATHROW)) {
				return false;
			}

			output.ignore(start, cursor);
			return true;
		}

		private boolean nextIsEcjNoFlowOut(final IFilterOutput output) {
			int resources = 1;

			AbstractInsnNode c = cursor;
			while (nextIsEcjClose("r" + resources)) {
				c = cursor;
				resources++;
			}
			cursor = c;

			final AbstractInsnNode bodyStart = cursor;
			while (!(Opcodes.IRETURN <= cursor.getOpcode()
					&& cursor.getOpcode() <= Opcodes.ARETURN)) {
				next();
				if (cursor == null) {
					return false;
				}
			}
			final AbstractInsnNode bodyEnd = cursor.getNext();

			// "catch (any primaryExc)"
			if (!nextIsVar(Opcodes.ASTORE, "primaryExc")) {
				return false;
			}
			for (int r = 0; r < resources; r++) {
				if (!nextIsEcjCloseAndThrow("r" + r)) {
					return false;
				}
				if (!nextIsEcjSuppress("r" + r)) {
					return false;
				}
			}
			// "throw primaryExc"
			if (!nextIsVar(Opcodes.ALOAD, "primaryExc")) {
				return false;
			}
			if (!nextIs(Opcodes.ATHROW)) {
				return false;
			}

			output.ignore(start, bodyStart);
			output.ignore(bodyEnd, cursor);
			return true;
		}

		private boolean nextIsEcjClose(final String name) {
			return nextIsVar(Opcodes.ALOAD, name)
					// "if (r != null)"
					&& nextIsJump(Opcodes.IFNULL, name + ".end")
					// "r.close()"
					&& nextIsClose(name);
		}

		private boolean nextIsEcjCloseAndThrow(final String name) {
			return nextIsVar(Opcodes.ALOAD, name)
					// "if (r != null)"
					&& nextIsJump(Opcodes.IFNULL, name)
					// "r.close()"
					&& nextIsClose(name) && nextIsLabel(name)
					&& nextIs(Opcodes.ALOAD) && nextIs(Opcodes.ATHROW);
		}

		private boolean nextIsEcjSuppress(final String name) {
			final String suppressedExc = name + ".t";
			final String startLabel = name + ".suppressStart";
			final String endLabel = name + ".suppressEnd";
			return nextIsVar(Opcodes.ASTORE, suppressedExc)
					// "suppressedExc = t"
					// "if (primaryExc != null)"
					&& nextIsVar(Opcodes.ALOAD, "primaryExc")
					&& nextIsJump(Opcodes.IFNONNULL, startLabel)
					// "primaryExc = suppressedExc"
					&& nextIsVar(Opcodes.ALOAD, suppressedExc)
					&& nextIsVar(Opcodes.ASTORE, "primaryExc")
					&& nextIsJump(Opcodes.GOTO, endLabel)
					// "if (primaryExc == suppressedExc)"
					&& nextIsLabel(startLabel)
					&& nextIsVar(Opcodes.ALOAD, "primaryExc")
					&& nextIsVar(Opcodes.ALOAD, suppressedExc)
					&& nextIsJump(Opcodes.IF_ACMPEQ, endLabel)
					// "primaryExc.addSuppressed(suppressedExc)"
					&& nextIsAddSuppressed(suppressedExc)
					&& nextIsLabel(endLabel);
		}

		private boolean nextIsClose(final String name) {
			if (!nextIsVar(Opcodes.ALOAD, name)) {
				return false;
			}
			next();
			if (cursor.getOpcode() != Opcodes.INVOKEINTERFACE
					&& cursor.getOpcode() != Opcodes.INVOKEVIRTUAL) {
				return false;
			}
			final MethodInsnNode m = (MethodInsnNode) cursor;
			if (!"close".equals(m.name) || !"()V".equals(m.desc)) {
				return false;
			}
			final String actual = m.owner;
			final String expected = owners.get(name);
			if (expected == null) {
				owners.put(name, actual);
				return true;
			} else {
				return expected.equals(actual);
			}
		}

		private boolean nextIsAddSuppressed() {
			if (!nextIs(Opcodes.INVOKEVIRTUAL)) {
				return false;
			}
			final MethodInsnNode m = (MethodInsnNode) cursor;
			return "java/lang/Throwable".equals(m.owner)
					&& "addSuppressed".equals(m.name);
		}

		private boolean nextIsAddSuppressed(final String name) {
			return nextIsVar(Opcodes.ALOAD, "primaryExc")
					&& nextIsVar(Opcodes.ALOAD, name) && nextIsAddSuppressed();
		}

		private boolean nextIsVar(final int opcode, final String name) {
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

		private boolean nextIsJump(final int opcode, final String name) {
			if (!nextIs(opcode)) {
				return false;
			}
			final LabelNode actual = ((JumpInsnNode) cursor).label;
			final LabelNode expected = labels.get(name);
			if (expected == null) {
				labels.put(name, actual);
				return true;
			} else {
				return expected == actual;
			}
		}

		private boolean nextIsLabel(final String name) {
			cursor = cursor.getNext();
			if (cursor.getType() != AbstractInsnNode.LABEL) {
				return false;
			}
			final LabelNode actual = (LabelNode) cursor;
			final LabelNode expected = labels.get(name);
			return expected == actual;
		}

		/**
		 * Moves {@link #cursor} to next instruction and returns
		 * <code>true</code> if it has given opcode.
		 */
		private boolean nextIs(final int opcode) {
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
