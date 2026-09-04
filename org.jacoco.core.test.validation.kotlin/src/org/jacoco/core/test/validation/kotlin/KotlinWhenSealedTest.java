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

import org.jacoco.core.test.validation.kotlin.targets.KotlinWhenSealedTarget;
import org.junit.Test;

/**
 * Test of code coverage in {@link KotlinWhenSealedTarget}.
 */
public class KotlinWhenSealedTest extends KotlinValidationTestBase {

	public KotlinWhenSealedTest() {
		super(KotlinWhenSealedTarget.class);
	}

	/** Starting from {@link #KOTLIN_1_4} */
	@Test
	public void bytecodeSnapshots() throws Exception {
		assertSnapshot(KotlinWhenSealedTarget.class, "expression",
				"expression.txt", //
				KOTLIN_2_0, KOTLIN_1_4);
		assertSnapshot(KotlinWhenSealedTarget.class,
				"expressionWithRedundantElse",
				"expression_with_redundant_else.txt", //
				KOTLIN_1_4);
		assertSnapshot(KotlinWhenSealedTarget.class,
				"expressionWithNonRedundantElse",
				"expression_with_non_redundant_else.txt", //
				KOTLIN_1_5, KOTLIN_1_4);
		assertSnapshot(KotlinWhenSealedTarget.class, "statement",
				"statement.txt", //
				KOTLIN_2_0, KOTLIN_1_5, KOTLIN_1_4);
		assertSnapshot(KotlinWhenSealedTarget.class, "indistinguishable",
				"indistinguishable.txt", //
				KOTLIN_1_5, KOTLIN_1_4);
		assertSnapshot(KotlinWhenSealedTarget.class, "singleCase",
				"single_case.txt", //
				KOTLIN_1_6, KOTLIN_1_4);
		assertSnapshot(KotlinWhenSealedTarget.class,
				"indistinguishableSingleCase",
				"indistinguishable_single_case.txt", //
				KOTLIN_1_5, KOTLIN_1_4);
	}

}
