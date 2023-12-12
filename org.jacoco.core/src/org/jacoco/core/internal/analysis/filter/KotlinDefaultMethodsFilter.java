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
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * Filters methods that Kotlin compiler generates for non-overridden
 * non-abstract methods of interfaces.
 */
final class KotlinDefaultMethodsFilter implements IFilter {

	public void filter(final MethodNode methodNode,
			final IFilterContext context, final IFilterOutput output) {
		if (!KotlinGeneratedFilter.isKotlinClass(context)) {
			return;
		}
		new Matcher().match(methodNode, output);
	}

	private static class Matcher extends AbstractMatcher {
		private void match(final MethodNode methodNode,
				final IFilterOutput output) {
			firstIsALoad0(methodNode);
			nextIs(Opcodes.INVOKESTATIC);
			if (cursor != null && ((MethodInsnNode) cursor).owner
					.endsWith("$DefaultImpls")) {
				output.ignore(methodNode.instructions.getFirst(),
						methodNode.instructions.getLast());
			}
		}
	}

}
