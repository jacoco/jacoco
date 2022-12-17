/*******************************************************************************
 * Copyright (c) 2009, 2022 Mountainminds GmbH & Co. KG and Contributors
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
import org.objectweb.asm.tree.FrameNode;
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
				// we're looking for the
				// throwUninitializedPropertyAccessException instruction, which
				// is in the "null" branch, so we have to follow this jump. If
				// we have an IFNONNULL instruction, we are already in die
				// "null" branch and don't have to jump.
				cursor = ((JumpInsnNode) start).label;
			}

			AbstractInsnNode optionalFrame = cursor.getNext();
			if (optionalFrame != null && optionalFrame instanceof FrameNode
					&& ((FrameNode) optionalFrame).type == Opcodes.F_SAME1) {
				next();
			}

			AbstractInsnNode optionalPop = cursor.getNext();
			if (optionalPop != null && optionalPop.getOpcode() == Opcodes.POP) {
				// Kotlin 1.6.0 DUPs the lateinit variable and POPs it here,
				// previous versions instead load the variable twice. To be
				// compatible with both, we can just skip the POP.
				next();
			}

			nextIs(Opcodes.LDC);
			nextIsInvoke(Opcodes.INVOKESTATIC, "kotlin/jvm/internal/Intrinsics",
					"throwUninitializedPropertyAccessException",
					"(Ljava/lang/String;)V");

			if (cursor == null) {
				return null;
			}

			if (Opcodes.IFNONNULL == start.getOpcode()) {
				// ignore everything until the jump target of our IFNONNULL.
				return ((JumpInsnNode) start).label;
			} else {
				// ignore everything until the next ARETURN or ATHROW
				// instruction; or until the end of the function.
				while (cursor.getOpcode() != Opcodes.ATHROW
						&& cursor.getOpcode() != Opcodes.ARETURN) {
					AbstractInsnNode next = cursor.getNext();
					if (next == null) {
						break;
					}
					cursor = next;
				}
				return cursor;
			}
		}
	}
}
