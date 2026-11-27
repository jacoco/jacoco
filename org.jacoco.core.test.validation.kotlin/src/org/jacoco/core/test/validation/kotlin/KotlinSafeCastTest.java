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

import org.jacoco.core.test.validation.kotlin.targets.KotlinSafeCastTarget;
import org.junit.Test;

/**
 * Test of "safe" cast operator.
 */
public class KotlinSafeCastTest extends KotlinValidationTestBase {

	public KotlinSafeCastTest() {
		super(KotlinSafeCastTarget.class);
	}

	/** Starting from {@link #KOTLIN_1_3} */
	@Test
	public void bytecodeSnapshots() throws Exception {
		assertSnapshot(KotlinSafeCastTarget.class, "example", "example.txt",
				KOTLIN_1_5, KOTLIN_1_3);
	}

}
