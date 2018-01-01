/*******************************************************************************
 * Copyright (c) 2009, 2018 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
public final class Filters implements IFilter {

	/**
	 * Filter that does nothing.
	 */
	public static final IFilter NONE = new Filters();

	/**
	 * Filter that combines all other filters.
	 */
	public static final IFilter ALL = new Filters(new EnumFilter(),
			new SyntheticFilter(), new SynchronizedFilter(),
			new TryWithResourcesJavacFilter(), new TryWithResourcesEcjFilter(),
			new FinallyFilter(), new PrivateEmptyNoArgConstructorFilter(),
			new StringSwitchJavacFilter(), new LombokGeneratedFilter(),
			new GroovyGeneratedFilter());

	private final IFilter[] filters;

	private Filters(final IFilter... filters) {
		this.filters = filters;
	}

	public void filter(final String className, final String superClassName,
			final MethodNode methodNode, final IFilterOutput output) {
		for (final IFilter filter : filters) {
			filter.filter(className, superClassName, methodNode, output);
		}
	}

}
