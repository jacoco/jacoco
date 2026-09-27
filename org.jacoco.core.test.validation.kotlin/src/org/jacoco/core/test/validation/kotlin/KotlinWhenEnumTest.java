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

import org.jacoco.core.test.validation.kotlin.targets.KotlinWhenEnumTarget;
import org.junit.Test;

/**
 * Test of code coverage in {@link KotlinWhenEnumTarget}.
 */
public class KotlinWhenEnumTest extends KotlinValidationTestBase {

	public KotlinWhenEnumTest() {
		super(KotlinWhenEnumTarget.class);
	}

	/** Starting from {@link #KOTLIN_1_6} */
	@Test
	public void bytecodeSnapshots() throws Exception {
		assertSnapshot(KotlinWhenEnumTarget.class, "whenEnum",
				"without_else.txt", //
				KOTLIN_2_0, KOTLIN_1_6);
		assertSnapshot(KotlinWhenEnumTarget.class, "whenEnumRedundantElse",
				"redundant_else.txt", //
				KOTLIN_1_6);
		assertSnapshot(KotlinWhenEnumTarget.class,
				"whenByNullableEnumWithNullCaseAndWithoutElse",
				"nullable_case_without_else.txt", //
				KOTLIN_2_0, KOTLIN_1_6);
		assertSnapshot(KotlinWhenEnumTarget.class,
				"whenByNullableEnumWithoutNullCaseAndWithElse",
				"nullable_else.txt", //
				KOTLIN_1_6);
		assertSnapshot(KotlinWhenEnumTarget.class,
				"whenByNullableEnumWithNullAndElseCases",
				"nullable_case_with_else.txt", //
				KOTLIN_1_6);
	}

}
