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

import org.junit.Test;

/**
 * Unit tests for {@link KotlinWhenFilter}.
 */
public class KotlinWhenFilterTest extends FilterTestBase {

	private final KotlinWhenFilter filter = new KotlinWhenFilter();

	@Test
	public void should_filter_implicit_else() {
		assertSnapshot(filter,
				"snapshots/KotlinWhenSealedTarget/1.4/expression.txt");
		assertSnapshot(filter,
				"snapshots/KotlinWhenSealedTarget/2.0/expression.txt");
	}

	@Test
	public void should_not_filter_redundant_else() {
		assertSnapshot(filter,
				"snapshots/KotlinWhenSealedTarget/1.4/expression_with_redundant_else.txt");
	}

	@Test
	public void should_not_filter_non_redundant_else_prior_to_1_5() {
		assertSnapshot(filter,
				"snapshots/KotlinWhenSealedTarget/1.4/expression_with_non_redundant_else.txt");
	}

	/**
	 * Unfortunately indistinguishable from
	 * {@link #should_filter_implicit_else() implicit exception}, but
	 * {@link #should_not_filter_non_redundant_else_prior_to_1_5() was
	 * distinguishable prior to 1.5}.
	 */
	@Test
	public void should_filter_non_redundant_else_starting_from_1_5() {
		assertSnapshot(filter,
				"snapshots/KotlinWhenSealedTarget/1.5/expression_with_non_redundant_else.txt");
	}

	@Test
	public void should_not_filter_non_sealed_when() {
		assertSnapshot(filter,
				"snapshots/KotlinWhenSealedTarget/1.4/non_sealed_when.txt");
		assertSnapshot(filter,
				"snapshots/KotlinWhenSealedTarget/1.5/non_sealed_when.txt");
	}

	@Test
	public void should_not_filter_non_sealed_if() {
		assertSnapshot(filter,
				"snapshots/KotlinWhenSealedTarget/1.4/non_sealed_if.txt");
	}

	// TODO indistinguishable.txt

	/**
	 * Unfortunately indistinguishable from
	 * {@link #should_not_filter_non_sealed_when() non-sealed {@code when}} and
	 * {@link #should_not_filter_non_sealed_if() non-sealed {@code if}}, but
	 * {@link #should_filter_statement_starting_from_2_0() became
	 * distinguishable starting from Kotlin 2.0}.
	 */
	@Test
	public void should_not_filter_statement_prior_to_2_0() {
		assertSnapshot(filter,
				"snapshots/KotlinWhenSealedTarget/1.4/statement.txt");
		assertSnapshot(filter,
				"snapshots/KotlinWhenSealedTarget/1.5/statement.txt");
	}

	@Test
	public void should_filter_statement_starting_from_2_0() {
		assertSnapshot(filter,
				"snapshots/KotlinWhenSealedTarget/2.0/statement.txt");
	}

	@Test
	public void should_filter_implicit_default() {
		assertSnapshot(filter,
				"snapshots/KotlinWhenEnumTarget/1.6/without_else.txt");
		assertSnapshot(filter,
				"snapshots/KotlinWhenEnumTarget/2.0/without_else.txt");
	}

	/**
	 * <pre>
	 * enum class E { A, B }
	 * fun example(e: E?) = when (e) {
	 *     E.A -> "a"
	 *     E.B -> "b"
	 *     null -> "null"
	 * }
	 * </pre>
	 */
	@Test
	public void should_filter_when_by_nullable_enum_with_null_case_and_without_else() {
		assertSnapshot(filter,
				"snapshots/KotlinWhenEnumTarget/1.6/nullable_case_without_else.txt");
		assertSnapshot(filter,
				"snapshots/KotlinWhenEnumTarget/2.0/nullable_case_without_else.txt");
	}

	/**
	 * <pre>
	 * enum class E { A, B }
	 * fun example(e: E?) = when (e) {
	 *     E.A -> "a"
	 *     E.B -> "b"
	 *     else -> "else"
	 * }
	 * </pre>
	 */
	@Test
	public void should_filter_when_by_nullable_enum_without_null_case_and_with_else() {
		assertSnapshot(filter,
				"snapshots/KotlinWhenEnumTarget/1.6/nullable_else.txt");
	}

	/**
	 * <pre>
	 * enum class E { A, B }
	 * fun example(e: E?) = when (e) {
	 *     E.A -> "a"
	 *     null -> "null"
	 *     else -> "else"
	 * }
	 * </pre>
	 */
	@Test
	public void should_filter_when_by_nullable_enum_with_null_and_else_cases() {
		assertSnapshot(filter,
				"snapshots/KotlinWhenEnumTarget/1.6/nullable_case_with_else.txt");
	}

}
