/*******************************************************************************
 * Copyright (c) 2009, 2020 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Gergely Fábián - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.internal.analysis.filter;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodNode;

/**
 * Filters methods that Scala compiler generates for case classes.
 */
public final class ScalaLambdaGeneratedMethodsFilter implements IFilter {
	private static boolean isScalaClass(final IFilterContext context) {
		return context.getClassAttributes().contains("ScalaSig")
				|| context.getClassAttributes().contains("Scala");
	}

	public void filter(final MethodNode methodNode,
			final IFilterContext context, final IFilterOutput output) {
		if (!isScalaClass(context) || !context.getSuperClassName()
				.startsWith("scala/runtime/AbstractFunction1")) {
			return;
		}

		final Matcher matcher = new Matcher();
		if (matcher.isInit(methodNode)) {
			output.ignore(methodNode.instructions.getFirst(),
					methodNode.instructions.getLast());
		}
	}

	private static class Matcher extends AbstractMatcher {
		boolean isInit(final MethodNode m) {
			if (!"<init>".equals(m.name)
					|| (m.access & Opcodes.ACC_PUBLIC) == 0) {
				return false;
			}
			return true;
		}
	}

}
