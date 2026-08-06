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
import org.jacoco.core.test.validation.kotlin.targets.KotlinCoroutineTarget;
import org.junit.Test;

import kotlin.KotlinVersion;

/**
 * Test of coroutines.
 */
public class KotlinCoroutineTest extends ValidationTestBase {

	public KotlinCoroutineTest() {
		super(KotlinCoroutineTarget.class);
	}

	@Test
	public void bytecodeSnapshots() throws Exception {
		assertSnapshot(KotlinCoroutineTarget.class, "suspendingFunction",
				"suspending_function.txt");
		assertSnapshot(KotlinCoroutineTarget.class,
				"suspendingFunctionWithTailCallOptimization",
				(KotlinVersion.CURRENT.isAtLeast(2, 4) ? "2.4.0/" : "")
						+ "tail_call_optimization.txt");
		assertSnapshot(
				Class.forName(
						KotlinCoroutineTarget.class.getName() + "$main$1"),
				"invokeSuspend", "suspending_lambda.txt");
		assertSnapshot(
				Class.forName(KotlinCoroutineTarget.class.getName()
						+ "$suspendingLambdaWithoutSuspensionPoints$1"),
				"invokeSuspend",
				"suspending_lambda_withous_suspension_points.txt");
	}

}
