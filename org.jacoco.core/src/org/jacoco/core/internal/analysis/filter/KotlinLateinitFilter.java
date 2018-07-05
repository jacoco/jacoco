/*******************************************************************************
 * Copyright (c) 2009, 2018 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

	private final static String OWNER = "kotlin/jvm/internal/Intrinsics";
	private final static String NAME = "throwUninitializedPropertyAccessException";

	public void filter(final MethodNode methodNode, final IFilterContext context,
			final IFilterOutput output) {
		for (AbstractInsnNode i = methodNode.instructions
				.getFirst(); i != null; i = i.getNext()) {
			if (i.getOpcode() != Opcodes.IFNONNULL) {
				continue;
			}

			final AbstractInsnNode end = new Matcher(i).match();

			if (end != null) {
				output.ignore(i, end);
			}
		}
	}

	private static class Matcher extends AbstractMatcher {
		private final AbstractInsnNode start;

		private Matcher(final AbstractInsnNode start) {
			this.start = start;
		}

		private AbstractInsnNode match() {
			cursor = start;
			nextIs(Opcodes.LDC);
			nextIsInvokeStatic(OWNER, NAME);
			return cursor;
		}
	}
}
