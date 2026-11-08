/*******************************************************************************
 * Copyright (c) 2009, 2026 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Fabian Mastenbroek - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.internal.analysis.filter;

import org.junit.Test;

/**
 * Unit tests for {@link KotlinLateinitFilter}.
 */
public class KotlinLateinitFilterTest extends FilterTestBase {

	private final KotlinLateinitFilter filter = new KotlinLateinitFilter();

	@Test
	public void should_filter_Kotlin_1_2() {
		assertSnapshot(filter,
				"snapshots/KotlinLateinitTarget/1.2/private.txt");
		assertSnapshot(filter,
				"snapshots/KotlinLateinitTarget/1.2/private_generic.txt");
		assertSnapshot(filter, "snapshots/KotlinLateinitTarget/1.2/public.txt");
		assertSnapshot(filter,
				"snapshots/KotlinLateinitTarget/1.2/public_generic.txt");
		assertSnapshot(filter,
				"snapshots/KotlinLateinitTarget/1.2/public_getter.txt");
		assertSnapshot(filter,
				"snapshots/KotlinLateinitTarget/1.2/public_generic_getter.txt");
	}

	/** Nothing to filter */
	@Test
	public void should_not_filter() {
		assertSnapshot(filter,
				"snapshots/KotlinLateinitTarget/1.2/public_outside.txt");
		assertSnapshot(filter,
				"snapshots/KotlinLateinitTarget/1.2/public_generic_outside.txt");
		assertSnapshot(filter,
				"snapshots/KotlinLateinitTarget/1.4/public_outside.txt");
		assertSnapshot(filter,
				"snapshots/KotlinLateinitTarget/1.4/public_generic_outside.txt");
		assertSnapshot(filter, "snapshots/KotlinLateinitTarget/1.5/public.txt");
		assertSnapshot(filter,
				"snapshots/KotlinLateinitTarget/1.5/public_generic.txt");
	}

	/**
	 * https://github.com/JetBrains/kotlin/commit/7c9578116bfa4aebac5a62c4bdaa1a11dd82426c
	 */
	@Test
	public void should_filter_Kotlin_1_5_0_public() {
		assertSnapshot(filter,
				"snapshots/KotlinLateinitTarget/1.5/public_getter.txt");
		assertSnapshot(filter,
				"snapshots/KotlinLateinitTarget/1.5/public_generic_getter.txt");
	}

	/**
	 * https://github.com/JetBrains/kotlin/commit/7c9578116bfa4aebac5a62c4bdaa1a11dd82426c
	 */
	@Test
	public void should_filter_Kotlin_1_5_0_private() {
		assertSnapshot(filter,
				"snapshots/KotlinLateinitTarget/1.5/private.txt");
		assertSnapshot(filter,
				"snapshots/KotlinLateinitTarget/1.5/private_generic.txt");
	}

	/**
	 * https://github.com/JetBrains/kotlin/commit/81b09ca09f012ed42bb9e90d4b6d802c697c07b6
	 */
	@Test
	public void should_filter_Kotlin_1_5_30_public() {
		assertSnapshot(filter,
				"snapshots/KotlinLateinitTarget/1.5.30/public_getter.txt");
	}

	/**
	 * https://github.com/JetBrains/kotlin/commit/81b09ca09f012ed42bb9e90d4b6d802c697c07b6
	 */
	@Test
	public void should_filter_Kotlin_1_5_30_public_generic() {
		assertSnapshot(filter,
				"snapshots/KotlinLateinitTarget/1.5.30/public_generic_getter.txt");
	}

	/**
	 * https://github.com/JetBrains/kotlin/commit/81b09ca09f012ed42bb9e90d4b6d802c697c07b6
	 */
	@Test
	public void should_filter_Kotlin_1_5_30_private() {
		assertSnapshot(filter,
				"snapshots/KotlinLateinitTarget/1.5.30/private.txt");
	}

	/**
	 * https://github.com/JetBrains/kotlin/commit/81b09ca09f012ed42bb9e90d4b6d802c697c07b6
	 */
	@Test
	public void should_filter_Kotlin_1_5_30_private_generic() {
		assertSnapshot(filter,
				"snapshots/KotlinLateinitTarget/1.5.30/private_generic.txt");
	}

	/**
	 * https://github.com/JetBrains/kotlin/commit/041773fd2584bc279813361eb7fc11ae84c214fd
	 */
	@Test
	public void should_filter_Kotlin_1_6_0_private() {
		assertSnapshot(filter,
				"snapshots/KotlinLateinitTarget/1.6/private.txt");
	}

	/**
	 * https://github.com/JetBrains/kotlin/commit/041773fd2584bc279813361eb7fc11ae84c214fd
	 */
	@Test
	public void should_filter_Kotlin_1_6_0_private_generic() {
		assertSnapshot(filter,
				"snapshots/KotlinLateinitTarget/1.6/private_generic.txt");
	}

	/**
	 * https://github.com/jetBrains/kotlin/commit/0a67ab54fec635f82e0507cbdd4299ae0dbe71b0
	 */
	@Test
	public void should_filter_Kotlin_1_6_20_public() {
		assertSnapshot(filter,
				"snapshots/KotlinLateinitTarget/1.6.20/public_getter.txt");
	}

	/**
	 * https://github.com/jetBrains/kotlin/commit/0a67ab54fec635f82e0507cbdd4299ae0dbe71b0
	 */
	@Test
	public void should_filter_Kotlin_1_6_20_public_generic() {
		assertSnapshot(filter,
				"snapshots/KotlinLateinitTarget/1.6.20/public_generic_getter.txt");
	}

}
