/*******************************************************************************
 * Copyright (c) 2009, 2021 Mountainminds GmbH & Co. KG and Contributors
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
 * Interface for filter implementations. Instances of filters are created for
 * analysis of each class and so can have per-class state.
 */
public interface IFilter {

	/**
	 * This method is called for every method. The filter implementation is
	 * expected to inspect the provided method and report its result to the
	 * given {@link IFilterOutput} instance.
	 *
	 * @param methodNode
	 *            method to inspect
	 * @param context
	 *            context information for the method
	 * @param output
	 *            callback to report filtering results to
	 */
	void filter(MethodNode methodNode, IFilterContext context,
			IFilterOutput output);
}
