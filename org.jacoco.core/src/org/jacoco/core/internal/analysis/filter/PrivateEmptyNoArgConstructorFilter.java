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
import org.objectweb.asm.tree.MethodNode;

/**
 * Filters private empty constructors that do not have arguments.
 */
public final class PrivateEmptyNoArgConstructorFilter implements IFilter {

	private static final String CONSTRUCTOR_NAME = "<init>";
	private static final String CONSTRUCTOR_DESC = "()V";

	public void filter(final MethodNode methodNode,
			final IFilterContext context, final IFilterOutput output) {
		if ((methodNode.access & Opcodes.ACC_PRIVATE) != 0
				&& CONSTRUCTOR_NAME.equals(methodNode.name)
				&& CONSTRUCTOR_DESC.equals(methodNode.desc) && new Matcher()
						.match(methodNode, context.getSuperClassName())) {
			output.ignore(methodNode.instructions.getFirst(),
					methodNode.instructions.getLast());
		}
	}

	private static class Matcher extends AbstractMatcher {
		private boolean match(final MethodNode methodNode,
				final String superClassName) {
			firstIsALoad0(methodNode);
			nextIsInvoke(Opcodes.INVOKESPECIAL, superClassName,
					CONSTRUCTOR_NAME, CONSTRUCTOR_DESC);
			nextIs(Opcodes.RETURN);
			return cursor != null;
		}
	}

}
