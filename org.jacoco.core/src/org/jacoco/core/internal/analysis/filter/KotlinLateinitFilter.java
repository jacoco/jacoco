/*******************************************************************************
 * Copyright (c) 2009, 2020 Mountainminds GmbH & Co. KG and Contributors
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
			matcher.match(node, output);
		}
	}

	private static class Matcher extends AbstractMatcher {
		public void match(final AbstractInsnNode start,
				final IFilterOutput output) {

			if (Opcodes.IFNONNULL != start.getOpcode()) {
				return;
			}
			cursor = start;

			nextIs(Opcodes.LDC);
			nextIsInvoke(Opcodes.INVOKESTATIC, "kotlin/jvm/internal/Intrinsics",
					"throwUninitializedPropertyAccessException",
					"(Ljava/lang/String;)V");

			if (cursor != null) {
				output.ignore(start, cursor);
			}
		}
	}
}
