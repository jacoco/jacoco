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

import org.jacoco.core.test.validation.kotlin.targets.KotlinSynchronizedTarget;
import org.junit.Test;

/**
 * Test of code coverage in {@link KotlinSynchronizedTarget}
 */
public class KotlinSynchronizedTest extends KotlinValidationTestBase {

	public KotlinSynchronizedTest() {
		super(KotlinSynchronizedTarget.class);
	}

	/** Starting from {@link #KOTLIN_1_3} */
	@Test
	public void bytecodeSnapshot() throws Exception {
		assertSnapshot(KotlinSynchronizedTarget.class, "normal", "normal.txt",
				KOTLIN_2_1, KOTLIN_1_6, KOTLIN_1_5, KOTLIN_1_4, KOTLIN_1_3);
		assertSnapshot(KotlinSynchronizedTarget.class, "explicitException",
				"only_exception.txt", //
				KOTLIN_2_1, KOTLIN_1_6, KOTLIN_1_5, KOTLIN_1_4, KOTLIN_1_3);
	}

}
