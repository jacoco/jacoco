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

	private final IFilter[] filters;

	/**
	 * Creates filter that combines all other filters.
	 * 
	 * @return filter that combines all other filters
	 */
	public static IFilter all() {
		return new Filters(new EnumFilter(), new SyntheticFilter(),
				new SynchronizedFilter(), new TryWithResourcesJavac11Filter(),
				new TryWithResourcesJavacFilter(),
				new TryWithResourcesEcjFilter(), new FinallyFilter(),
				new PrivateEmptyNoArgConstructorFilter(),
				new StringSwitchJavacFilter(), new StringSwitchEcjFilter(),
				new EnumEmptyConstructorFilter(),
				new AnnotationGeneratedFilter(), new KotlinGeneratedFilter(),
				new KotlinLateinitFilter(), new KotlinWhenFilter(),
				new KotlinWhenStringFilter(),
				new KotlinUnsafeCastOperatorFilter(),
				new KotlinDefaultArgumentsFilter(), new KotlinInlineFilter());
	}

	private Filters(final IFilter... filters) {
		this.filters = filters;
	}

	public void filter(final MethodNode methodNode,
			final IFilterContext context, final IFilterOutput output) {
		for (final IFilter filter : filters) {
			filter.filter(methodNode, context, output);
		}
	}

}
