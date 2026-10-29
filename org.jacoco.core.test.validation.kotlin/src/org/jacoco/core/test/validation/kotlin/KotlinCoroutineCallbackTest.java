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

import org.jacoco.core.test.validation.kotlin.targets.KotlinCoroutineCallbackTarget;
import org.junit.Test;

/**
 * Test of code coverage in {@link KotlinCoroutineCallbackTarget}.
 */
public class KotlinCoroutineCallbackTest extends KotlinValidationTestBase {

	public KotlinCoroutineCallbackTest() {
		super(KotlinCoroutineCallbackTarget.class);
	}

	/** Starting from {@link #KOTLIN_2_0} */
	@Test
	public void bytecodeSnapshots() throws Exception {
		assertSnapshot(KotlinCoroutineCallbackTarget.class, "example",
				"example.txt", //
				KOTLIN_2_0);
		assertSnapshot(KotlinCoroutineCallbackTarget.class,
				"withoutTailCallOptimization",
				"without_tail_call_optimization.txt", //
				KOTLIN_2_2, KOTLIN_2_0);
	}

}
