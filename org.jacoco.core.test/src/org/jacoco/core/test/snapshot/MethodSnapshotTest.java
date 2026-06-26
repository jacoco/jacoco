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
package org.jacoco.core.test.snapshot;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;

import org.junit.ComparisonFailure;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Unit test for {@link MethodSnapshotTest}.
 */
public final class MethodSnapshotTest {

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	@Test
	public void testCompare() throws Exception {
		final File s1 = new File(folder.getRoot(), "s1.txt");
		final File s2 = new File(folder.getRoot(), "s2.txt");

		try {
			MethodSnapshot.compare(MethodSnapshotTest.class, "<init>", s1, s2);
			fail("ComparisonFailure expected");
		} catch (final ComparisonFailure e) {
			// expected
			assertEquals("// NOT FOUND", e.getExpected());
		}
		assertTrue(s2.exists());

		MethodSnapshot.compare(MethodSnapshotTest.class, "<init>", s2, s1);
		assertFalse(s1.exists());
	}

}
