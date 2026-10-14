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
 * Unit tests for {@link KotlinSafeCallOperatorFilter}.
 */
public class KotlinSafeCallOperatorFilterTest extends FilterTestBase {

	private final IFilter filter = new KotlinSafeCallOperatorFilter();

	/**
	 * https://github.com/JetBrains/kotlin/commit/0a67ab54fec635f82e0507cbdd4299ae0dbe71b0
	 */
	@Test
	public void should_filter_optimized_safe_call_chain() {
		assertSnapshot(filter,
				"snapshots/KotlinSafeCallOperatorTarget/safe_call_chain.txt");
	}

	@Test
	public void should_filter_unoptimized_safe_call_chain() {
		assertSnapshot(filter,
				"snapshots/KotlinSafeCallOperatorTarget/safe_call_chain_multiline.txt");
	}

	@Test
	public void should_filter_safe_call_followed_by_elvis() {
		assertSnapshot(filter,
				"snapshots/KotlinSafeCallOperatorTarget/safe_call_followed_by_elvis.txt");
	}

	@Test
	public void should_filter_safe_call_chain_followed_by_elvis() {
		assertSnapshot(filter,
				"snapshots/KotlinSafeCallOperatorTarget/safe_call_chain_followed_by_elvis.txt");
	}

	@Test
	public void should_filter_unoptimized_safe_call_followed_by_elvis() {
		assertSnapshot(filter,
				"snapshots/KotlinSafeCallOperatorTarget/safe_call_followed_by_elvis_multiline.txt");
	}

	@Test
	public void should_filter_unoptimized_safe_call_chain_followed_by_elvis() {
		assertSnapshot(filter,
				"snapshots/KotlinSafeCallOperatorTarget/safe_call_chain_followed_by_elvis_multiline.txt");
	}

}
