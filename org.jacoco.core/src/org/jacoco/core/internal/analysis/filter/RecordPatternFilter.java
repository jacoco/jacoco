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
				matcher.match(t.handler, output);
			}
		}
	}

	private static class Matcher extends AbstractMatcher {
		void match(final AbstractInsnNode start, final IFilterOutput output) {
			cursor = start;
			nextIsVar(Opcodes.ASTORE, "cause");
			nextIsType(org.objectweb.asm.Opcodes.NEW,
					"java/lang/MatchException");
			nextIs(Opcodes.DUP);
			nextIsVar(Opcodes.ALOAD, "cause");
			nextIsInvoke(Opcodes.INVOKEVIRTUAL, "java/lang/Throwable",
					"toString", "()Ljava/lang/String;");
			nextIsVar(Opcodes.ALOAD, "cause");
			nextIsInvoke(Opcodes.INVOKESPECIAL, "java/lang/MatchException",
					"<init>", "(Ljava/lang/String;Ljava/lang/Throwable;)V");
			nextIs(Opcodes.ATHROW);
			if (cursor != null) {
				output.ignore(start, cursor);
			}
		}
	}

}
