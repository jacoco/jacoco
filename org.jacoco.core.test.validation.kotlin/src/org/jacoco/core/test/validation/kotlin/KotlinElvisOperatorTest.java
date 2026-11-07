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

import org.jacoco.core.test.validation.kotlin.targets.KotlinElvisOperatorTarget;
import org.junit.Test;

/**
 * Test of code coverage in {@link KotlinElvisOperatorTarget}.
 */
public class KotlinElvisOperatorTest extends KotlinValidationTestBase {

	public KotlinElvisOperatorTest() {
		super(KotlinElvisOperatorTarget.class);
	}

	/** Starting from {@link #KOTLIN_1_7} */
	@Test
	public void bytecodeSnapshots() throws Exception {
		assertSnapshot(KotlinElvisOperatorTarget.class,
				"elvis\\$fullCoverage([-$][0-9]++)?", "elvis.txt", //
				KOTLIN_1_7);
		assertSnapshot(KotlinElvisOperatorTarget.class,
				"elvisChain\\$fullCoverage([-$][0-9]++)?", "elvis_chain.txt", //
				KOTLIN_1_7);
	}

}
