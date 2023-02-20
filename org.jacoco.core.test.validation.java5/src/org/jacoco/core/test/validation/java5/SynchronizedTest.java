/*******************************************************************************
 * Copyright (c) 2009, 2023 Mountainminds GmbH & Co. KG and Contributors
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
package org.jacoco.core.test.validation.java5;

import org.jacoco.core.test.validation.Source.Line;
import org.jacoco.core.test.validation.ValidationTestBase;
import org.jacoco.core.test.validation.java5.targets.SynchronizedTarget;

/**
 * Test of filtering of a bytecode that is generated for a synchronized
 * statement.
 */
public class SynchronizedTest extends ValidationTestBase {

	public SynchronizedTest() {
		super(SynchronizedTarget.class);
	}

	public void assertMonitorEnterImplicitException(final Line line) {
		if (isJDKCompiler) {
			assertFullyCovered(line);
		} else {
			assertPartlyCovered(line);
		}
	}

	public void assertMonitorExit(final Line line) {
		if (isJDKCompiler) {
			// without filter next line covered partly:
			assertFullyCovered(line);
		} else {
			assertEmpty(line);
		}
	}

	public void assertMonitorExitImplicitException(final Line line) {
		if (isJDKCompiler) {
			// without filter next line covered partly:
			assertNotCovered(line);
		} else {
			assertEmpty(line);
		}
	}

}
