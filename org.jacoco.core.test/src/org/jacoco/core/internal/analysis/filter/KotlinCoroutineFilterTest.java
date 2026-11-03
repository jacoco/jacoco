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
package org.jacoco.core.internal.analysis.filter;

import org.junit.Test;

/**
 * Unit test for {@link KotlinCoroutineFilter}.
 */
public class KotlinCoroutineFilterTest extends FilterTestBase {

	private final IFilter filter = new KotlinCoroutineFilter();

	@Test
	public void should_filter_suspending_lambdas() {
		assertSnapshot(filter,
				"snapshots/KotlinCoroutineTarget/1.3/suspending_lambda.txt");
		assertSnapshot(filter,
				"snapshots/KotlinCoroutineTarget/1.3.30/suspending_lambda.txt");
		assertSnapshot(filter,
				"snapshots/KotlinCoroutineTarget/1.3.40/suspending_lambda.txt");
		assertSnapshot(filter,
				"snapshots/KotlinCoroutineTarget/1.5/suspending_lambda.txt");
		assertSnapshot(filter,
				"snapshots/KotlinCoroutineTarget/1.5.30/suspending_lambda.txt");
		assertSnapshot(filter,
				"snapshots/KotlinCoroutineTarget/1.6/suspending_lambda.txt");
		assertSnapshot(filter,
				"snapshots/KotlinCoroutineTarget/1.7.20/suspending_lambda.txt");
		assertSnapshot(filter,
				"snapshots/KotlinCoroutineTarget/2.0/suspending_lambda.txt");
		assertSnapshot(filter,
				"snapshots/KotlinCoroutineTarget/2.1/suspending_lambda.txt");
		assertSnapshot(filter,
				"snapshots/KotlinCoroutineTarget/2.2/suspending_lambda.txt");
	}

	@Test
	public void should_filter_suspending_lambdas_with_parameters() {
		assertSnapshot(filter,
				"snapshots/KotlinCoroutineSuspendingLambdaTarget/2.1/suspending_lambda_with_parameter.txt");
		assertSnapshot(filter,
				// https://github.com/JetBrains/kotlin/commit/93782ff35da93ddd603dbf0fac208d7a47593eaa
				"snapshots/KotlinCoroutineSuspendingLambdaTarget/2.2/suspending_lambda_with_parameter.txt");
	}

	@Test
	public void should_filter_suspending_functions() {
		assertSnapshot(filter,
				"snapshots/KotlinCoroutineTarget/1.3/suspending_function.txt");
		assertSnapshot(filter,
				"snapshots/KotlinCoroutineTarget/1.3.30/suspending_function.txt");
		assertSnapshot(filter,
				"snapshots/KotlinCoroutineTarget/1.3.40/suspending_function.txt");
		assertSnapshot(filter,
				"snapshots/KotlinCoroutineTarget/1.5/suspending_function.txt");
		assertSnapshot(filter,
				"snapshots/KotlinCoroutineTarget/1.7.20/suspending_function.txt");
		assertSnapshot(filter,
				"snapshots/KotlinCoroutineTarget/2.0/suspending_function.txt");
		assertSnapshot(filter,
				"snapshots/KotlinCoroutineTarget/2.2/suspending_function.txt");
	}

	@Test
	public void should_filter_suspending_functions_with_tail_call_optimization() {
		assertSnapshot(filter,
				"snapshots/KotlinCoroutineTarget/1.3/suspending_function_with_tail_call_optimization.txt");
		assertSnapshot(filter,
				"snapshots/KotlinCoroutineTarget/1.3.30/suspending_function_with_tail_call_optimization.txt");
		assertSnapshot(filter,
				"snapshots/KotlinCoroutineTarget/1.3.60/suspending_function_with_tail_call_optimization.txt");
		assertSnapshot(filter,
				"snapshots/KotlinCoroutineTarget/1.5/suspending_function_with_tail_call_optimization.txt");
		assertSnapshot(filter,
				"snapshots/KotlinCoroutineTarget/2.4/suspending_function_with_tail_call_optimization.txt");
		assertSnapshot(filter,
				"snapshots/KotlinCoroutineTarget/2.4.20/suspending_function_with_tail_call_optimization.txt");
	}

	/**
	 * @see #should_filter_suspending_lambdas()
	 */
	@Test
	public void should_filter_suspending_lambda_without_suspension_points() {
		assertSnapshot(filter,
				"snapshots/KotlinCoroutineTarget/1.3/suspending_lambda_without_suspension_points.txt");
		assertSnapshot(filter,
				"snapshots/KotlinCoroutineTarget/1.3.30/suspending_lambda_without_suspension_points.txt");
		assertSnapshot(filter,
				"snapshots/KotlinCoroutineTarget/1.3.40/suspending_lambda_without_suspension_points.txt");
		assertSnapshot(filter,
				"snapshots/KotlinCoroutineTarget/1.5/suspending_lambda_without_suspension_points.txt");
		assertSnapshot(filter,
				// https://github.com/JetBrains/kotlin/commit/f4a1e27124f77b2ffca576f7393218373c6ae085
				"snapshots/KotlinCoroutineTarget/1.6/suspending_lambda_without_suspension_points.txt");
		assertSnapshot(filter,
				"snapshots/KotlinCoroutineTarget/1.7.20/suspending_lambda_without_suspension_points.txt");
		assertSnapshot(filter,
				"snapshots/KotlinCoroutineTarget/2.0/suspending_lambda_without_suspension_points.txt");
		assertSnapshot(filter,
				"snapshots/KotlinCoroutineTarget/2.1/suspending_lambda_without_suspension_points.txt");
		assertSnapshot(filter,
				"snapshots/KotlinCoroutineTarget/2.2/suspending_lambda_without_suspension_points.txt");
	}

	@Test
	public void should_filter_suspending_lambdas_and_functions_when_suspension_point_returns_inline_value_class() {
		assertSnapshot(filter,
				"snapshots/KotlinCoroutineInlineValueClassTarget/2.0/suspending_lambda.txt");
		assertSnapshot(filter,
				"snapshots/KotlinCoroutineInlineValueClassTarget/2.0/suspending_function.txt");
		assertSnapshot(filter,
				"snapshots/KotlinCoroutineInlineValueClassTarget/2.1/suspending_lambda.txt");
		assertSnapshot(filter,
				"snapshots/KotlinCoroutineInlineValueClassTarget/2.2/suspending_lambda.txt");
		assertSnapshot(filter,
				"snapshots/KotlinCoroutineInlineValueClassTarget/2.2/suspending_function.txt");
	}

	/**
	 * @see #should_filter_suspendCoroutineUninterceptedOrReturn_when_no_tail_call_optimization()
	 */
	@Test
	public void should_filter_suspendCoroutineUninterceptedOrReturn() {
		assertSnapshot(filter,
				"snapshots/KotlinCoroutineCallbackTarget/2.0/example.txt");
	}

	/**
	 * @see #should_filter_suspendCoroutineUninterceptedOrReturn()
	 */
	@Test
	public void should_filter_suspendCoroutineUninterceptedOrReturn_when_no_tail_call_optimization() {
		assertSnapshot(filter,
				"snapshots/KotlinCoroutineCallbackTarget/2.0/without_tail_call_optimization.txt");
		assertSnapshot(filter,
				"snapshots/KotlinCoroutineCallbackTarget/2.2/without_tail_call_optimization.txt");
	}

}
