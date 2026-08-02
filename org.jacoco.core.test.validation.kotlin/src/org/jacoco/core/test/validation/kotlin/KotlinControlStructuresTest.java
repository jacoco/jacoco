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
import org.jacoco.core.test.validation.kotlin.targets.KotlinControlStructuresTarget;
import org.junit.Test;

/**
 * Tests of Kotlin control structures.
 */
public class KotlinControlStructuresTest extends ValidationTestBase {

	public KotlinControlStructuresTest() {
		super(KotlinControlStructuresTarget.class);
	}

	@Test
	public void bytecodeSnapshots() throws Exception {
		assertSnapshot(KotlinControlStructuresTarget.class, "missedIfBlock",
				"if.txt");
		assertSnapshot(KotlinControlStructuresTarget.class, "missedWhileBlock",
				"while.txt");
		assertSnapshot(KotlinControlStructuresTarget.class, "missedForBlock",
				"for.txt");
		assertSnapshot(KotlinControlStructuresTarget.class,
				"missedForEachBlock", "for_each.txt");
		assertSnapshot(KotlinControlStructuresTarget.class, "whenExpression",
				"when.txt");
		assertSnapshot(KotlinControlStructuresTarget.class, "breakStatement",
				"break.txt");
		assertSnapshot(KotlinControlStructuresTarget.class, "continueStatement",
				"continue.txt");
		// implicit/explicit return
	}

}
