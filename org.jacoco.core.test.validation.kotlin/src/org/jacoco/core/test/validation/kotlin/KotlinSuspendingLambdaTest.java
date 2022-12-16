/*******************************************************************************
 * Copyright (c) 2009, 2022 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Lukas Rössler - initial implementation
 *
 *******************************************************************************/
package org.jacoco.core.test.validation.kotlin;

import org.jacoco.core.test.validation.ValidationTestBase;
import org.jacoco.core.test.validation.kotlin.targets.KotlinSuspendingLambdaTarget;

/**
 * Test of suspending lambdas.
 */
public class KotlinSuspendingLambdaTest extends ValidationTestBase {

	public KotlinSuspendingLambdaTest() {
		super(KotlinSuspendingLambdaTarget.class);
	}

}
