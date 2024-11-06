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
 * Factory for all JaCoCo filters.
 */
public final class Filters {

	private Filters() {
		// no instances
	}

	/**
	 * Filter that does nothing.
	 */
	public static final IFilter NONE = new FilterSet();

	/**
	 * Creates a filter that combines all filters.
	 *
	 * @return filter that combines all filters
	 */
	public static IFilter all() {
		final IFilter allCommonFilters = allCommonFilters();
		final IFilter allKotlinFilters = allKotlinFilters();
		final IFilter allNonKotlinFilters = allNonKotlinFilters();
		return new IFilter() {
			public void filter(final MethodNode methodNode,
					final IFilterContext context, final IFilterOutput output) {
				allCommonFilters.filter(methodNode, context, output);
				if (isKotlinClass(context)) {
					allKotlinFilters.filter(methodNode, context, output);
				} else {
					allNonKotlinFilters.filter(methodNode, context, output);
				}
			}
		};
	}

	private static IFilter allCommonFilters() {
		return new FilterSet( //
				new EnumFilter(), //
				new BridgeFilter(), //
				new SynchronizedFilter(), //
				new TryWithResourcesJavac11Filter(), //
				new TryWithResourcesJavacFilter(), //
				new TryWithResourcesEcjFilter(), //
				new FinallyFilter(), //
				new PrivateEmptyNoArgConstructorFilter(), //
				new AssertFilter(), //
				new StringSwitchJavacFilter(), //
				new StringSwitchFilter(), //
				new EnumEmptyConstructorFilter(), //
				new RecordsFilter(), //
				new ExhaustiveSwitchFilter(), //
				new RecordPatternFilter(), //
				new AnnotationGeneratedFilter());
	}

	private static IFilter allNonKotlinFilters() {
		return new FilterSet( //
				new SyntheticFilter());
	}

	private static IFilter allKotlinFilters() {
		return new FilterSet( //
				new KotlinGeneratedFilter(), //
				new KotlinSyntheticAccessorsFilter(), //
				new KotlinEnumFilter(), //
				new KotlinJvmOverloadsFilter(), //
				new KotlinSafeCallOperatorFilter(), //
				new KotlinLateinitFilter(), //
				new KotlinWhenFilter(), //
				new KotlinWhenStringFilter(), //
				new KotlinUnsafeCastOperatorFilter(), //
				new KotlinNotNullOperatorFilter(), //
				new KotlinInlineClassFilter(), //
				new KotlinDefaultArgumentsFilter(), //
				new KotlinInlineFilter(), //
				new KotlinCoroutineFilter(), //
				new KotlinDefaultMethodsFilter(), //
				new KotlinComposeFilter());
	}

	/**
	 * Checks whether the class corresponding to the given context has
	 * <code>kotlin/Metadata</code> annotation.
	 *
	 * @param context
	 *            context information
	 * @return <code>true</code> if the class corresponding to the given context
	 *         has <code>kotlin/Metadata</code> annotation
	 */
	public static boolean isKotlinClass(final IFilterContext context) {
		return context.getClassAnnotations()
				.contains(KotlinGeneratedFilter.KOTLIN_METADATA_DESC);
	}

}
