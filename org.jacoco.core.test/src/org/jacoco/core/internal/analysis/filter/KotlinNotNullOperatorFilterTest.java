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
 * Unit tests for {@link KotlinNotNullOperatorFilter}.
 */
public class KotlinNotNullOperatorFilterTest extends FilterTestBase {

	private final KotlinNotNullOperatorFilter filter = new KotlinNotNullOperatorFilter();

	@Test
	public void should_filter() {
		assertSnapshot(filter,
				"snapshots/KotlinNotNullOperatorTarget/1.3/not_null_assertion_operator.txt");
	}

}
