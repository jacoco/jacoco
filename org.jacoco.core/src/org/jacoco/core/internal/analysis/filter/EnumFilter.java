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

import org.objectweb.asm.tree.MethodNode;

/**
 * Filters methods <code>values</code> and <code>valueOf</code> that compiler
 * creates for enums.
 */
public final class EnumFilter implements IFilter {

	public void filter(final MethodNode methodNode,
			final IFilterContext context, final IFilterOutput output) {
		if (isMethodFiltered(context.getClassName(),
				context.getSuperClassName(), methodNode.name,
				methodNode.desc)) {
			output.ignore(methodNode.instructions.getFirst(),
					methodNode.instructions.getLast());
		}
	}

	private boolean isMethodFiltered(final String className,
			final String superClassName, final String methodName,
			final String methodDesc) {
		if ("java/lang/Enum".equals(superClassName)) {
			if ("values".equals(methodName)
					&& ("()[L" + className + ";").equals(methodDesc)) {
				return true;
			}
			if ("valueOf".equals(methodName)
					&& ("(Ljava/lang/String;)L" + className + ";")
							.equals(methodDesc)) {
				return true;
			}
		}
		return false;
	}

}
