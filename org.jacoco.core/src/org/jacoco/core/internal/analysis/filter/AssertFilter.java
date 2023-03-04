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

/**
 * Filters code that is generated for an <code>assert</code> statement.
 */
final class AssertFilter implements IFilter {

	public void filter(final MethodNode methodNode,
			final IFilterContext context, final IFilterOutput output) {
		final Matcher matcher = new Matcher();
		if ("<clinit>".equals(methodNode.name)) {
			for (final AbstractInsnNode i : methodNode.instructions) {
				matcher.matchSet(context.getClassName(), i, output);
			}
		}
		for (final AbstractInsnNode i : methodNode.instructions) {
			matcher.matchGet(context.getClassName(), i, output);
		}
	}

	private static class Matcher extends AbstractMatcher {
		public void matchSet(final String className,
				final AbstractInsnNode start, final IFilterOutput output) {
			cursor = start;
			nextIsInvoke(Opcodes.INVOKEVIRTUAL, "java/lang/Class",
					"desiredAssertionStatus", "()Z");
			nextIs(Opcodes.IFNE);
			nextIs(Opcodes.ICONST_1);
			nextIs(Opcodes.GOTO);
			nextIs(Opcodes.ICONST_0);
			nextIsField(Opcodes.PUTSTATIC, className, "$assertionsDisabled",
					"Z");
			if (cursor != null) {
				output.ignore(start, cursor);
			}
		}

		public void matchGet(final String className,
				final AbstractInsnNode start, final IFilterOutput output) {
			cursor = start;
			nextIsField(Opcodes.GETSTATIC, className, "$assertionsDisabled",
					"Z");
			nextIs(Opcodes.IFNE);
			if (cursor != null) {
				output.ignore(cursor, cursor);
			}
		}
	}

}
