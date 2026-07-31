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

import static org.junit.Assert.fail;

import org.junit.Test;

/**
 * Unit test for {@link MethodSnapshotVisitor}.
 */
public class MethodSnapshotVisitorTest {

	@Test
	public void visitTryCatchBlock() {
		final MethodSnapshotVisitor m = new MethodSnapshotVisitor(null);
		try {
			m.visitTryCatchBlock(null, null, null, "null");
			fail("UnsupportedOperationException expected");
		} catch (final UnsupportedOperationException e) {
			// expected
		}
	}

}
