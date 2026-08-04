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
import org.jacoco.core.test.validation.kotlin.targets.KotlinConditionalCompilationTarget;
import org.junit.Test;

/**
 * Test of code coverage in {@link KotlinConditionalCompilationTarget}.
 */
public class KotlinConditionalCompilationTest extends ValidationTestBase {

	public KotlinConditionalCompilationTest() {
		super(KotlinConditionalCompilationTarget.class);
	}

	@Test
	public void bytecodeSnapshots() throws Exception {
		assertSnapshot(KotlinConditionalCompilationTarget.class,
				"conditionFalse", "condition_constant_false.txt");
		assertSnapshot(KotlinConditionalCompilationTarget.class,
				"conditionTrue", "condition_constant_true.txt");
	}

}
