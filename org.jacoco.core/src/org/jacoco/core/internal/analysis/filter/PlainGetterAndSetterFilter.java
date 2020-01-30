/*******************************************************************************
 * Copyright (c) 2009, 2020 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Daniel Kraft - initial API and implementation
 *
 *******************************************************************************/

package org.jacoco.core.internal.analysis.filter;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodNode;

/**
 * Filters plain Getters and Setters.
 */
public class PlainGetterAndSetterFilter implements IFilter {

	public void filter(final MethodNode methodNode,
			final IFilterContext context, final IFilterOutput output) {

		if (isCandidate(methodNode, context)) {
			output.ignore(methodNode.instructions.getFirst(),
					methodNode.instructions.getLast());
		}
	}

	private boolean isCandidate(final MethodNode methodNode,
			final IFilterContext context) {
		return methodNode.instructions.getFirst() != null
				&& (isGetter(methodNode, context)
						|| isSetter(methodNode, context));
	}

	private boolean dependsOnAttribute(final MethodNode methodNode,
			final IFilterContext context, final int split) {
		final String attributeName = methodNode.name.substring(split, split + 1)
				.toLowerCase() + methodNode.name.substring(split + 1);
		return context.getClassFields().contains(attributeName);
	}

	private boolean isGetter(final MethodNode methodNode,
			final IFilterContext context) {
		if (methodNode.name.startsWith("get") && methodNode.name.length() > 3) {
			return dependsOnAttribute(methodNode, context, 3)
					&& new GetterMatcher().match(methodNode);
		} else if (methodNode.name.startsWith("is")
				&& methodNode.name.length() > 2) {
			return dependsOnAttribute(methodNode, context, 2)
					&& new GetterMatcher().match(methodNode);
		}
		return false;
	}

	private boolean isSetter(final MethodNode methodNode,
			final IFilterContext context) {
		return methodNode.name.startsWith("set") && methodNode.name.length() > 3
				&& dependsOnAttribute(methodNode, context, 3)
				&& new SetterMatcher().match(methodNode);
	}

	private static class GetterMatcher extends AbstractMatcher {
		private boolean match(final MethodNode methodNode) {
			firstIsALoad0(methodNode);
			nextIs(Opcodes.GETFIELD);
			nextIsReturn();
			return cursor != null;
		}

		private void nextIsReturn() {
			next();
			if (cursor == null) {
				return;
			}
			if (cursor.getOpcode() < Opcodes.IRETURN
					|| cursor.getOpcode() > Opcodes.ARETURN) {
				cursor = null;
			}
		}
	}

	private static class SetterMatcher extends AbstractMatcher {
		private boolean match(final MethodNode methodNode) {
			firstIsALoad0(methodNode);
			nextIsLoad1();
			nextIs(Opcodes.PUTFIELD);
			nextIs(Opcodes.RETURN);
			return cursor != null;
		}

		private void nextIsLoad1() {
			next();
			if (cursor == null) {
				return;
			}
			if (cursor.getOpcode() < Opcodes.ILOAD
					|| cursor.getOpcode() > Opcodes.ALOAD) {
				cursor = null;
			}
		}
	}
}
