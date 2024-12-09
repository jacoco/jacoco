/*******************************************************************************
 * Copyright (c) 2009, 2024 Mountainminds GmbH & Co. KG and Contributors
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
 * Filters method <code>getEntries</code> that Kotlin compiler creates for
 * enums.
 */
final class KotlinEnumFilter implements IFilter {

	public void filter(final MethodNode methodNode,
			final IFilterContext context, final IFilterOutput output) {
		if (!"java/lang/Enum".equals(context.getSuperClassName())) {
			return;
		}
		if (!Filters.isKotlinClass(context)) {
			return;
		}
		if (!"getEntries".equals(methodNode.name)) {
			return;
		}
		if (!"()Lkotlin/enums/EnumEntries;".equals(methodNode.desc)) {
			return;
		}
		output.ignore(methodNode.instructions.getFirst(),
				methodNode.instructions.getLast());
	}

}
