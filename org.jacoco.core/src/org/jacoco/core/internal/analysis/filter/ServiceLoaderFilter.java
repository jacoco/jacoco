/*******************************************************************************
 * Copyright (c) 2009, 2019 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Sergey Zhemzhitsky - initial API and implementation
 *
 *******************************************************************************/

package org.jacoco.core.internal.analysis.filter;

import java.util.ServiceLoader;

import org.objectweb.asm.tree.MethodNode;

/**
 * Loads additional filters by means of {@link ServiceLoader}.
 * For the additional filters to be found they should be accessible by the same
 * {@code ClassLoader} that loads this class.
 */
public class ServiceLoaderFilter implements IFilter {

	private final ServiceLoader<IFilter> filters =
			ServiceLoader.load(IFilter.class, getClass().getClassLoader());

	public void filter(final MethodNode methodNode,
			final IFilterContext context, final IFilterOutput output) {
		for (final IFilter filter : filters) {
			filter.filter(methodNode, context, output);
		}
	}

	// for test purposes only
	void reset() {
		filters.reload();
	}

}
