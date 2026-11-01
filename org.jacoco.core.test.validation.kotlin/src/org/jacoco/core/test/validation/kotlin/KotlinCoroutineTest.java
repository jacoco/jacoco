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

import org.jacoco.core.test.validation.kotlin.targets.KotlinCoroutineTarget;
import org.junit.Test;

/**
 * Test of coroutines.
 */
public class KotlinCoroutineTest extends KotlinValidationTestBase {

	public KotlinCoroutineTest() {
		super(KotlinCoroutineTarget.class);
	}

	/** Starting from {@link #KOTLIN_1_3} */
	@Test
	public void bytecodeSnapshots() throws Exception {
		assertSnapshot(KotlinCoroutineTarget.class, "suspendingFunction",
				"suspending_function.txt", //
				KOTLIN_2_2, KOTLIN_2_0, KOTLIN_1_7_20, KOTLIN_1_5,
				KOTLIN_1_3_40, KOTLIN_1_3_30, KOTLIN_1_3);
		assertSnapshot(
				Class.forName(
						KotlinCoroutineTarget.class.getName() + "$main$1"),
				"invokeSuspend", "suspending_lambda.txt", //
				KOTLIN_2_2, KOTLIN_2_1, KOTLIN_2_0, KOTLIN_1_7_20, KOTLIN_1_6,
				KOTLIN_1_5_30, KOTLIN_1_5, KOTLIN_1_3_40, KOTLIN_1_3_30,
				KOTLIN_1_3);
		assertSnapshot(
				Class.forName(KotlinCoroutineTarget.class.getName()
						+ "$suspendingLambdaWithoutSuspensionPoints$1"),
				"invokeSuspend",
				"suspending_lambda_without_suspension_points.txt", //
				KOTLIN_2_2, KOTLIN_2_1, KOTLIN_2_0, KOTLIN_1_7_20, KOTLIN_1_6,
				KOTLIN_1_5, KOTLIN_1_3_40, KOTLIN_1_3_30, KOTLIN_1_3);
		assertSnapshot(KotlinCoroutineTarget.class,
				"suspendingFunctionWithTailCallOptimization",
				"suspending_function_with_tail_call_optimization.txt",
				KOTLIN_2_4_20, KOTLIN_2_4, KOTLIN_1_5, KOTLIN_1_3_60,
				KOTLIN_1_3_30, KOTLIN_1_3);
	}

}
