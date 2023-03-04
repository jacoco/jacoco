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

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TryCatchBlockNode;

/**
 * Filters code which is generated for try-with-resources statement by javac
 * starting from version 11.
 */
public final class TryWithResourcesJavac11Filter implements IFilter {

	public void filter(final MethodNode methodNode,
			final IFilterContext context, final IFilterOutput output) {
		if (methodNode.tryCatchBlocks.isEmpty()) {
			return;
		}
		final Matcher matcher = new Matcher();
		for (TryCatchBlockNode t : methodNode.tryCatchBlocks) {
			if ("java/lang/Throwable".equals(t.type)) {
				matcher.match(t.handler, output, true);
				matcher.match(t.handler, output, false);
			}
		}
	}

	/**
	 * <pre>
	 *     r = ...;
	 *     try {
	 *         ...
	 *     } body-only-finally {
	 *         if (r != null)
	 *             r.close();
	 *     } catch (Throwable primaryExc) {
	 *         if (r != null)
	 *             try {
	 *                 r.close();
	 *             } catch (Throwable t) {
	 *                 primaryExc.addSuppressed(t);
	 *             }
	 *         throw primaryExc;
	 *     }
	 * </pre>
	 *
	 * <code>null</code> check for resource is omitted when it is initialized
	 * using <code>new</code>
	 */
	private class Matcher extends AbstractMatcher {
		private boolean withNullCheck;

		private String expectedOwner;

		void match(final AbstractInsnNode start, final IFilterOutput output,
				final boolean withNullCheck) {
			this.withNullCheck = withNullCheck;
			vars.clear();
			expectedOwner = null;

			cursor = start.getPrevious();
			nextIsVar(Opcodes.ASTORE, "primaryExc");
			nextIsJavacClose();
			nextIs(Opcodes.GOTO);
			nextIsVar(Opcodes.ASTORE, "t");
			nextIsVar(Opcodes.ALOAD, "primaryExc");
			nextIsVar(Opcodes.ALOAD, "t");
			nextIsInvoke(Opcodes.INVOKEVIRTUAL, "java/lang/Throwable",
					"addSuppressed", "(Ljava/lang/Throwable;)V"); // primaryExc.addSuppressed(t)
			nextIsVar(Opcodes.ALOAD, "primaryExc");
			nextIs(Opcodes.ATHROW);
			if (cursor == null) {
				return;
			}
			final AbstractInsnNode end = cursor;

			AbstractInsnNode s = start.getPrevious();
			cursor = start.getPrevious();
			while (!nextIsJavacClose()) {
				s = s.getPrevious();
				cursor = s;
				if (cursor == null) {
					return;
				}
			}
			s = s.getNext();

			final AbstractInsnNode m = cursor;
			next();
			if (cursor.getOpcode() != Opcodes.GOTO) {
				cursor = m;
			}

			output.ignore(s, cursor);
			output.ignore(start, end);
		}

		private boolean nextIsJavacClose() {
			if (withNullCheck) {
				nextIsVar(Opcodes.ALOAD, "r");
				nextIs(Opcodes.IFNULL);
			}
			nextIsClose();
			return cursor != null;
		}

		private void nextIsClose() {
			nextIsVar(Opcodes.ALOAD, "r");
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
			if (expectedOwner == null) {
				expectedOwner = actual;
			} else if (!expectedOwner.equals(actual)) {
				cursor = null;
			}
		}

	}

}
