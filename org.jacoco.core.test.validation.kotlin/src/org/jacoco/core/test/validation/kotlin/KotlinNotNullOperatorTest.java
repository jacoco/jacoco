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

import org.jacoco.core.test.validation.kotlin.targets.KotlinNotNullOperatorTarget;
import org.junit.Test;

/**
 * Test of not-null assertion operator.
 */
public class KotlinNotNullOperatorTest extends KotlinValidationTestBase {

	public KotlinNotNullOperatorTest() {
		super(KotlinNotNullOperatorTarget.class);
	}

	/** Starting from {@link #KOTLIN_1_3} */
	@Test
	public void bytecodeSnapshots() throws Exception {
		assertSnapshot(KotlinNotNullOperatorTarget.class, "example",
				"not_null_assertion_operator.txt",
				// https://github.com/JetBrains/kotlin/commit/a7c8fdcbe2e260e5265aaf5121c5987206a676c9
				KOTLIN_1_4, KOTLIN_1_3);
	}

}
