/*******************************************************************************
 * Copyright (c) 2009, 2021 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Fabian Mastenbroek - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.internal.analysis.filter;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * Filters branch in bytecode that Kotlin compiler generates for reading from
 * <code>lateinit</code> properties.
 */
public class KotlinLateinitFilter implements IFilter {

	public void filter(final MethodNode methodNode,
			final IFilterContext context, final IFilterOutput output) {
		final Matcher matcher = new Matcher();
		for (final AbstractInsnNode node : methodNode.instructions) {
			final AbstractInsnNode to = matcher.match(node);
			if (to != null) {
				output.ignore(node, to);
			}
		}
	}

	private static class Matcher extends AbstractMatcher {

		public AbstractInsnNode match(final AbstractInsnNode start) {

			if (Opcodes.IFNONNULL != start.getOpcode()
					&& Opcodes.IFNULL != start.getOpcode()) {
				return null;
			}

			cursor = start;

			if (Opcodes.IFNULL == start.getOpcode()) {
				nextIs(Opcodes.ALOAD);
				nextIs(Opcodes.ARETURN);
			}

			nextIs(Opcodes.LDC);
			nextIsInvoke(Opcodes.INVOKESTATIC, "kotlin/jvm/internal/Intrinsics",
					"throwUninitializedPropertyAccessException",
					"(Ljava/lang/String;)V");

			if (cursor != null
					&& skipNonOpcodes(cursor.getNext()) != skipNonOpcodes(
							((JumpInsnNode) start).label)) {

				final AbstractInsnNode node = cursor;
				if (isKotlin1_5(node) || isKotlin1_5_30(node)) {
					return cursor;
				}
				return null;
			}
			return cursor;
		}

		private boolean isKotlin1_5(AbstractInsnNode node) {
			cursor = node;
			nextIs(Opcodes.ACONST_NULL);
			nextIs(Opcodes.ATHROW);
			return cursor != null;
		}

		private boolean isKotlin1_5_30(AbstractInsnNode node) {
			cursor = node;
			if (cursor.getNext() != null) {
				switch (cursor.getNext().getOpcode()) {
				case Opcodes.GETSTATIC:
					nextIsField(Opcodes.GETSTATIC, "kotlin/Unit", "INSTANCE",
							"Lkotlin/Unit;");
					break;
				case Opcodes.ACONST_NULL:
					nextIs(Opcodes.ACONST_NULL);
					break;
				}
			}
			if (cursor.getNext() != null) {
				switch (cursor.getNext().getOpcode()) {
				case Opcodes.GOTO:
					nextIs(Opcodes.GOTO);
					break;
				case Opcodes.ARETURN:
					nextIs(Opcodes.ARETURN);
					break;
				}
			}
			return cursor != null;
		}

	}
}
