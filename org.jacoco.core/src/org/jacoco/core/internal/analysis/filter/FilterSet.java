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
 * Filter that combines other filters.
 */
final class FilterSet implements IFilter {

	private final IFilter[] filters;

	FilterSet(final IFilter... filters) {
		this.filters = filters;
	}

	public void filter(final MethodNode methodNode,
			final IFilterContext context, final IFilterOutput output) {
		for (final IFilter filter : filters) {
			filter.filter(methodNode, context, output);
		}
	}

}
