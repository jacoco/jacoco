/*******************************************************************************
 * Copyright (c) 2009, 2025 Mountainminds GmbH & Co. KG and Contributors
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
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * Filters branch in bytecode that Kotlin compiler generates for reading from
 * <code>lateinit</code> properties.
 */
final class KotlinLateinitFilter implements IFilter {

	public void filter(final MethodNode methodNode,
			final IFilterContext context, final IFilterOutput output) {
		final Matcher matcher = new Matcher();
		for (final AbstractInsnNode node : methodNode.instructions) {
			matcher.match(node, output);
		}
	}

	private static class Matcher extends AbstractMatcher {
		public void match(final AbstractInsnNode start,
				final IFilterOutput output) {
			if (Opcodes.IFNONNULL == start.getOpcode()) {
				cursor = start;
				nextIsIntrinsic();
				final LabelNode label = ((JumpInsnNode) start).label;
				if (cursor != null && cursor.getNext() == label) {
					// Kotlin 1.2
					output.ignore(start, cursor);
					return;
				}
				next();
				if (cursor != null && cursor.getNext() == label) {
					// Kotlin 1.6.20
					output.ignore(start, cursor);
					return;
				}
				next();
				if (cursor != null && cursor.getNext() == label) {
					// Kotlin 1.5.0 or 1.5.30
					output.ignore(start, cursor);
				}
			} else if (Opcodes.IFNULL == start.getOpcode()) {
				cursor = ((JumpInsnNode) start).label;
				nextIsIntrinsic();
				next();
				next();
				if (cursor != null && (Opcodes.ARETURN == cursor.getOpcode()
						|| Opcodes.ATHROW == cursor.getOpcode())) {
					output.ignore(start, start);
					output.ignore(((JumpInsnNode) start).label, cursor);
				}
			}
		}

		private void nextIsIntrinsic() {
			next();
			if (cursor == null) {
				return;
			} else if (Opcodes.POP == cursor.getOpcode()) {
				nextIs(Opcodes.LDC);
			} else if (Opcodes.LDC != cursor.getOpcode()) {
				cursor = null;
				return;
			}
			nextIsInvoke(Opcodes.INVOKESTATIC, "kotlin/jvm/internal/Intrinsics",
					"throwUninitializedPropertyAccessException",
					"(Ljava/lang/String;)V");
		}
	}

}
