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
import org.jacoco.core.test.validation.kotlin.targets.KotlinWhenSealedTarget;
import org.junit.Test;

/**
 * Test of code coverage in {@link KotlinWhenSealedTarget}.
 */
public class KotlinWhenSealedTest extends ValidationTestBase {

	public KotlinWhenSealedTest() {
		super(KotlinWhenSealedTarget.class);
	}

	@Test
	public void bytecodeSnapshots() throws Exception {
		assertSnapshot(KotlinWhenSealedTarget.class, "expression",
				"expression.txt");
		assertSnapshot(KotlinWhenSealedTarget.class, "statement",
				"statement.txt");
		assertSnapshot(KotlinWhenSealedTarget.class, "indistinguishable",
				"indistinguishable.txt");
		assertSnapshot(KotlinWhenSealedTarget.class, "singleCase",
				"indistinguishable_single_case.txt");
		assertSnapshot(KotlinWhenSealedTarget.class,
				"indistinguishableSingleCase",
				"indistinguishable_single_case.txt");
	}

}
