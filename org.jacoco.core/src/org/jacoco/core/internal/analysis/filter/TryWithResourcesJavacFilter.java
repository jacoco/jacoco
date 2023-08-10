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
 * versions from 7 to 10.
 */
public final class TryWithResourcesJavacFilter implements IFilter {

	public void filter(final MethodNode methodNode,
			final IFilterContext context, final IFilterOutput output) {
		if (methodNode.tryCatchBlocks.isEmpty()) {
			return;
		}
		final Matcher matcher = new Matcher(output);
		for (final TryCatchBlockNode t : methodNode.tryCatchBlocks) {
			if ("java/lang/Throwable".equals(t.type)) {
				for (final Matcher.JavacPattern p : Matcher.JavacPattern
						.values()) {
					matcher.start(t.handler);
					if (matcher.matchJavac(p)) {
						break;
					}
				}
			}
		}
	}

	/**
	 * javac from JDK 7 and 8 generates bytecode that is equivalent to the
	 * compilation of source code that is described in <a href=
	 * "http://docs.oracle.com/javase/specs/jls/se8/html/jls-14.html#jls-14.20.3.1">JLS
	 * 14.20.3. try-with-resources</a>:
	 *
	 * <pre>
	 *     Resource r = ...;
	 *     Throwable primaryExc = null;
	 *     try {
	 *         ...
	 *     } finally {
	 *         if (r != null) {
	 *             if (primaryExc != null) {
	 *                 try {
	 *                     r.close();
	 *                 } catch (Throwable suppressedExc) {
	 *                     primaryExc.addSuppressed(suppressedExc);
	 *                 }
	 *             } else {
	 *                 r.close();
	 *             }
	 *         }
	 *     }
	 * </pre>
	 *
	 * Case of multiple resources looks like multiple nested try-with-resources
	 * statements. javac from JDK 9 EA b160 does the same, but with some
	 * optimizations (see <a href=
	 * "https://bugs.openjdk.java.net/browse/JDK-7020499">JDK-7020499</a>):
	 * <ul>
	 * <li><code>null</code> check for resource is omitted when it is
	 * initialized using <code>new</code></li>
	 * <li>synthetic method <code>$closeResource</code> containing
	 * <code>null</code> check of primaryExc and calls to methods
	 * <code>addSuppressed</code> and <code>close</code> is used when number of
	 * copies of closing logic reaches threshold, <code>null</code> check of
	 * resource (if present) is done before call of this method</li>
	 * </ul>
	 * During matching association between resource and slot of variable is done
	 * on exceptional path and is used to find close of resource on normal path.
	 * Order of loading variables primaryExc and r is different in different
	 * cases, which implies that this order should be determined before
	 * association. So {@link JavacPattern} defines all possible variants that
	 * will be tried sequentially.
	 */
	static class Matcher extends AbstractMatcher {
		private final IFilterOutput output;

		private String expectedOwner;

		private AbstractInsnNode start;

		Matcher(final IFilterOutput output) {
			this.output = output;
		}

		private enum JavacPattern {
			/**
			 * resource is loaded after primaryExc, <code>null</code> check of
			 * resource is omitted, method <code>$closeResource</code> is used
			 */
			OPTIMAL,
			/**
			 * resource is loaded before primaryExc and both are checked on
			 * <code>null</code>
			 */
			FULL,
			/**
			 * resource is loaded after primaryExc, <code>null</code> check of
			 * resource is omitted
			 */
			OMITTED_NULL_CHECK,
			/**
			 * resource is loaded before primaryExc and checked on
			 * <code>null</code>, method <code>$closeResource</code> is used
			 */
			METHOD,
		}

		private void start(final AbstractInsnNode start) {
			this.start = start;
			cursor = start.getPrevious();
			vars.clear();
			expectedOwner = null;
		}

		private boolean matchJavac(final JavacPattern p) {
			// "catch (Throwable t)"
			nextIsVar(Opcodes.ASTORE, "t1");
			// "primaryExc = t"
			nextIsVar(Opcodes.ALOAD, "t1");
			nextIsVar(Opcodes.ASTORE, "primaryExc");
			// "throw t"
			nextIsVar(Opcodes.ALOAD, "t1");
			nextIs(Opcodes.ATHROW);

			// "catch (any t)"
			nextIsVar(Opcodes.ASTORE, "t2");
			nextIsJavacClose(p, "e");
			// "throw t"
			nextIsVar(Opcodes.ALOAD, "t2");
			nextIs(Opcodes.ATHROW);
			if (cursor == null) {
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
			output.ignore(start, end);
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
				nextIsVar(Opcodes.ALOAD, "r");
				nextIs(Opcodes.IFNULL);
			}
			switch (p) {
			case METHOD:
			case OPTIMAL:
				nextIsVar(Opcodes.ALOAD, "primaryExc");
				nextIsVar(Opcodes.ALOAD, "r");
				nextIs(Opcodes.INVOKESTATIC);
				if (cursor != null) {
					final MethodInsnNode m = (MethodInsnNode) cursor;
					if ("$closeResource".equals(m.name)
							&& "(Ljava/lang/Throwable;Ljava/lang/AutoCloseable;)V"
									.equals(m.desc)) {
						return true;
					}
					cursor = null;
				}
				return false;
			case FULL:
			case OMITTED_NULL_CHECK:
				nextIsVar(Opcodes.ALOAD, "primaryExc");
				// "if (primaryExc != null)"
				nextIs(Opcodes.IFNULL);
				// "r.close()"
				nextIsClose();
				nextIs(Opcodes.GOTO);
				// "catch (Throwable t)"
				nextIsVar(Opcodes.ASTORE, ctx + "t");
				// "primaryExc.addSuppressed(t)"
				nextIsVar(Opcodes.ALOAD, "primaryExc");
				nextIsVar(Opcodes.ALOAD, ctx + "t");
				nextIsInvoke(Opcodes.INVOKEVIRTUAL, "java/lang/Throwable",
						"addSuppressed", "(Ljava/lang/Throwable;)V");
				nextIs(Opcodes.GOTO);
				// "r.close()"
				nextIsClose();
				return cursor != null;
			default:
				throw new AssertionError();
			}
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
