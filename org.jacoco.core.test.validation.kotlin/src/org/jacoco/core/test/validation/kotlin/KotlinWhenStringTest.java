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

import org.jacoco.core.test.validation.kotlin.targets.KotlinWhenStringTarget;
import org.junit.Test;

/**
 * Test of code coverage in {@link KotlinWhenStringTarget}.
 */
public class KotlinWhenStringTest extends KotlinValidationTestBase {

	public KotlinWhenStringTest() {
		super(KotlinWhenStringTarget.class);
	}

	/** Starting from {@link #KOTLIN_1_3} */
	@Test
	public void bytecodeSnapshots() throws Exception {
		assertSnapshot(KotlinWhenStringTarget.class, "whenString",
				"example.txt", //
				KOTLIN_1_5, KOTLIN_1_4, KOTLIN_1_3);
		assertSnapshot(KotlinWhenStringTarget.class,
				"whenStringNullableDefault", "nullable_else.txt", //
				KOTLIN_1_5, KOTLIN_1_4, KOTLIN_1_3);
		assertSnapshot(KotlinWhenStringTarget.class, "whenStringNullableCase",
				"nullable_case.txt", //
				KOTLIN_1_7, KOTLIN_1_5, KOTLIN_1_4, KOTLIN_1_3);
		assertSnapshot(KotlinWhenStringTarget.class, "implicitElseNotExecuted",
				"implicit_else.txt", //
				KOTLIN_1_5, KOTLIN_1_4, KOTLIN_1_3);
		assertSnapshot(KotlinWhenStringTarget.class,
				"whenStringBiggestHashCodeFirst", "biggest_hashCode_first.txt",
				KOTLIN_1_7, KOTLIN_1_5, KOTLIN_1_4, KOTLIN_1_3);
	}

}
