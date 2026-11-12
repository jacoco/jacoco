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
 * Unit tests for {@link RecordPatternFilter}.
 */
public class RecordPatternFilterTest extends FilterTestBase {

	private final IFilter filter = new RecordPatternFilter();

	@Test
	public void should_filter_instanceof() {
		assertSnapshot(filter,
				"snapshots/RecordPatternsTarget/21/instanceof.txt");
		assertSnapshot(filter,
				"snapshots/RecordPatternsTarget/23/instanceof.txt");
		assertSnapshot(filter,
				"snapshots/RecordPatternsTarget/26/instanceof.txt");
	}

	@Test
	public void should_filter_switch() {
		assertSnapshot(filter, "snapshots/RecordPatternsTarget/21/nested.txt");

		assertSnapshot(filter, "snapshots/RecordPatternsTarget/21/switch.txt");
		assertSnapshot(filter, "snapshots/RecordPatternsTarget/23/switch.txt");
		assertSnapshot(filter, "snapshots/RecordPatternsTarget/26/switch.txt");
	}

}
