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
package org.jacoco.core.test.validation.kotlin;

import org.jacoco.core.test.validation.ValidationTestBase;
import org.jacoco.core.test.validation.kotlin.targets.KotlinNotNullOperatorTarget;
import org.junit.Test;

import kotlin.KotlinVersion;

/**
 * Test of not-null assertion operator.
 */
public class KotlinNotNullOperatorTest extends ValidationTestBase {

	public KotlinNotNullOperatorTest() {
		super(KotlinNotNullOperatorTarget.class);
	}

	@Test
	public void bytecodeSnapshots() throws Exception {
		if (KotlinVersion.CURRENT.isAtLeast(1, 4)) {
			// https://github.com/JetBrains/kotlin/commit/a7c8fdcbe2e260e5265aaf5121c5987206a676c9
			assertSnapshot(KotlinNotNullOperatorTarget.class, "example",
					"not_null_assertion_operator.txt");
		} else {
			assertSnapshot(KotlinNotNullOperatorTarget.class, "example",
					"1.3/not_null_assertion_operator.txt");
		}
	}

}
