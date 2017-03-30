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
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TryCatchBlockNode;
import org.objectweb.asm.tree.VarInsnNode;

import java.util.HashMap;
import java.util.Map;

/**
 * Filters code that javac generates for try-with-resources statement.
 */
public final class TryWithResourcesFilter implements IFilter {

	public void filter(final MethodNode methodNode,
			final IFilterOutput output) {
		if (methodNode.tryCatchBlocks.isEmpty()) {
			return;
		}
		final Matcher matcher = new Matcher(output);
		for (TryCatchBlockNode t : methodNode.tryCatchBlocks) {
			if ("java/lang/Throwable".equals(t.type)) {
				for (Matcher.JavacPattern p : Matcher.JavacPattern.values()) {
					matcher.start(t.handler);
					if (matcher.matchJavac(p)) {
						break;
					}
				}
			}
		}
	}

	static class Matcher {
		private final IFilterOutput output;

		private final Map<String, VarInsnNode> vars = new HashMap<String, VarInsnNode>();
		private String expectedOwner;

		private AbstractInsnNode start;
		private AbstractInsnNode cursor;

		Matcher(final IFilterOutput output) {
			this.output = output;
		}

		private enum JavacPattern {
			OPTIMAL, FULL, OMITTED_NULL_CHECK, METHOD,
		}

		private void start(final AbstractInsnNode start) {
			this.start = start;
			cursor = start.getPrevious();
			vars.clear();
			expectedOwner = null;
		}

		private boolean matchJavac(final JavacPattern p) {
			// "catch (Throwable t)"
			if (!nextIsVar(Opcodes.ASTORE, "t1")) {
				return false;
			}
			// "primaryExc = t"
			if (!nextIsVar(Opcodes.ALOAD, "t1")) {
				return false;
			}
			if (!nextIsVar(Opcodes.ASTORE, "primaryExc")) {
				return false;
			}
			// "throw t"
			if (!nextIsVar(Opcodes.ALOAD, "t1")) {
				return false;
			}
			if (!nextIs(Opcodes.ATHROW)) {
				return false;
			}

			// "catch (any t)"
			if (!nextIsVar(Opcodes.ASTORE, "t2")) {
				return false;
			}
			if (!nextIsJavacClose(p, "e")) {
				return false;
			}
			// "throw t"
			if (!nextIsVar(Opcodes.ALOAD, "t2")) {
				return false;
			}
			if (!nextIs(Opcodes.ATHROW)) {
				return false;
			}
			final AbstractInsnNode end = cursor;

			AbstractInsnNode startOnNonExceptionalPath = start.getPrevious();
			cursor = startOnNonExceptionalPath;
			while (!nextIsJavacClose(p, "n")) {
				startOnNonExceptionalPath = startOnNonExceptionalPath
						.getPrevious();
				cursor = startOnNonExceptionalPath;
				if (cursor == null) {
					return false;
				}
			}
			startOnNonExceptionalPath = startOnNonExceptionalPath.getNext();

			final AbstractInsnNode m = cursor;
			next();
			if (cursor.getOpcode() != Opcodes.GOTO) {
				cursor = m;
			}

			output.ignore(startOnNonExceptionalPath, cursor);
			output.ignore(start.getNext(), end);
			return true;
		}

		/**
		 * On a first invocation will associate variables with names "r" and
		 * "primaryExc", on subsequent invocations will use those associations
		 * for checks.
		 */
		private boolean nextIsJavacClose(final JavacPattern p,
				final String ctx) {
			switch (p) {
			case METHOD:
			case FULL:
				// "if (r != null)"
				if (!(nextIsVar(Opcodes.ALOAD, "r")
						&& nextIs(Opcodes.IFNULL))) {
					return false;
				}
			}
			switch (p) {
			case METHOD:
			case OPTIMAL:
				if (nextIsVar(Opcodes.ALOAD, "primaryExc")
						&& nextIsVar(Opcodes.ALOAD, "r")
						&& nextIs(Opcodes.INVOKESTATIC)) {
					final MethodInsnNode m = (MethodInsnNode) cursor;
					return "$closeResource".equals(m.name)
							&& "(Ljava/lang/Throwable;Ljava/lang/AutoCloseable;)V"
									.equals(m.desc);
				}
				return false;
			case FULL:
			case OMITTED_NULL_CHECK:
				return nextIsVar(Opcodes.ALOAD, "primaryExc")
						// "if (primaryExc != null)"
						&& nextIs(Opcodes.IFNULL)
						// "r.close()"
						&& nextIsClose() && nextIs(Opcodes.GOTO)
						// "catch (Throwable t)"
						&& nextIsVar(Opcodes.ASTORE, ctx + "t")
						// "primaryExc.addSuppressed(t)"
						&& nextIsVar(Opcodes.ALOAD, "primaryExc")
						&& nextIsVar(Opcodes.ALOAD, ctx + "t")
						&& nextIsAddSuppressed() && nextIs(Opcodes.GOTO)
						// "r.close()"
						&& nextIsClose();
			default:
				return false;
			}
		}

		private boolean nextIsClose() {
			if (!nextIsVar(Opcodes.ALOAD, "r")) {
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
			if (expectedOwner == null) {
				expectedOwner = actual;
				return true;
			} else {
				return expectedOwner.equals(actual);
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
