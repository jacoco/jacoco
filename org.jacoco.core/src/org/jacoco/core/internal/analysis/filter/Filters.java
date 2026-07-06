/*******************************************************************************
 * Copyright (c) 2009, 2026 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0
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

	/**
	 * Descriptor of annotation present in all class files produced by the
	 * Kotlin compiler.
	 *
	 * @see <a href=
	 *      "https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-metadata/">documentation
	 *      of kotlin.Metadata annotation</a>
	 */
	public static final String KOTLIN_METADATA_DESC = "Lkotlin/Metadata;";

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
				new SyntheticClassFilter(), //
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
				new EnumSwitchFilter(), //
				new SyntheticFilter());
	}

	private static IFilter allKotlinFilters() {
		return new FilterSet( //
				new KotlinGeneratedFilter(), //
				new KotlinSyntheticAccessorsFilter(), //
				new KotlinSerializableFilter(), //
				new KotlinEnumFilter(), //
				new KotlinJvmOverloadsFilter(), //
				new KotlinJvmStaticFilter(), //
				new KotlinSafeCallOperatorFilter(), //
				new KotlinLateinitFilter(), //
				new KotlinWhenFilter(), //
				new KotlinWhenStringFilter(), //
				new KotlinUnsafeCastOperatorFilter(), //
				new KotlinNotNullOperatorFilter(), //
				new KotlinInlineClassFilter(), //
				new KotlinExposeBoxedFilter(), //
				new KotlinDefaultArgumentsFilter(), //
				new KotlinInlineFilter(), //
				new KotlinCoroutineFilter(), //
				new KotlinDefaultMethodsFilter(), //
				new KotlinComposeFilter());
	}

	/**
	 * Returns {@code true} if the class corresponding to this context has
	 * {@link #KOTLIN_METADATA_DESC kotlin.Metadata} annotation.
	 *
	 * @param context
	 *            context information
	 * @return {@code true} if the class corresponding to this context has
	 *         {@link #KOTLIN_METADATA_DESC kotlin.Metadata} annotation
	 */
	private static boolean isKotlinClass(final IFilterContext context) {
		return context.getClassAnnotations().contains(KOTLIN_METADATA_DESC);
	}

}
