/*******************************************************************************
 * Copyright (c) 2009, 2021 Mountainminds GmbH & Co. KG and Contributors
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
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * Filters branch in bytecode that Kotlin compiler generates for "unsafe" cast
 * operator.
 */
public final class KotlinUnsafeCastOperatorFilter implements IFilter {

	public void filter(final MethodNode methodNode,
			final IFilterContext context, final IFilterOutput output) {
		if (!KotlinGeneratedFilter.isKotlinClass(context)) {
			return;
		}
		final Matcher matcher = new Matcher();
		for (final AbstractInsnNode i : methodNode.instructions) {
			matcher.match("kotlin/TypeCastException", i, output);
			// Since Kotlin 1.4.0:
			matcher.match("java/lang/NullPointerException", i, output);
		}
	}

	private static class Matcher extends AbstractMatcher {
		public void match(final String exceptionType,
				final AbstractInsnNode start, final IFilterOutput output) {

			if (Opcodes.DUP != start.getOpcode()) {
				return;
			}
			cursor = start;
			nextIs(Opcodes.IFNONNULL);
			final JumpInsnNode jumpInsnNode = (JumpInsnNode) cursor;
			nextIsType(Opcodes.NEW, exceptionType);
			nextIs(Opcodes.DUP);
			nextIs(Opcodes.LDC);
			if (cursor == null) {
				return;
			}
			final LdcInsnNode ldc = (LdcInsnNode) cursor;
			if (!(ldc.cst instanceof String && ((String) ldc.cst)
					.startsWith("null cannot be cast to non-null type"))) {
				return;
			}
			nextIsInvoke(Opcodes.INVOKESPECIAL, exceptionType, "<init>",
					"(Ljava/lang/String;)V");
			nextIs(Opcodes.ATHROW);
			if (cursor == null) {
				return;
			}
			if (cursor.getNext() != jumpInsnNode.label) {
				return;
			}

			output.ignore(start, cursor);
		}
	}

}
