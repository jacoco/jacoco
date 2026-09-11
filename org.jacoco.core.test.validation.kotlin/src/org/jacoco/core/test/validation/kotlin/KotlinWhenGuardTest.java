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

import org.jacoco.core.test.validation.kotlin.targets.KotlinWhenGuardTarget;
import org.junit.Test;

/**
 * Test of code coverage in {@link KotlinWhenGuardTarget}.
 */
public class KotlinWhenGuardTest extends KotlinValidationTestBase {

	public KotlinWhenGuardTest() {
		super(KotlinWhenGuardTarget.class);
	}

	/** Starting from {@link #KOTLIN_2_2} */
	@Test
	public void bytecodeSnapshots() throws Exception {
		assertSnapshot(KotlinWhenGuardTarget.class, //
				"example", "guard.txt", //
				KOTLIN_2_2);
	}

}
