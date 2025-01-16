/*******************************************************************************
 * Copyright (c) 2009, 2025 Mountainminds GmbH & Co. KG and Contributors
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
 * enums. They are not filtered by {@link KotlinGeneratedFilter} due to
 * <a href= "https://youtrack.jetbrains.com/issue/KT-74091">regression in Kotlin
 * compiler version 2.0</a>, which at best might be fixed in version <a href=
 * "https://github.com/jacoco/jacoco/issues/1752#issuecomment-2560095584">2.2.0</a>
 */
final class KotlinEnumFilter implements IFilter {

	public void filter(final MethodNode methodNode,
			final IFilterContext context, final IFilterOutput output) {
		if (!"java/lang/Enum".equals(context.getSuperClassName())) {
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
