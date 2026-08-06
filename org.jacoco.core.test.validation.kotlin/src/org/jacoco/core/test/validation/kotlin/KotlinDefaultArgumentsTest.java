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
import org.jacoco.core.test.validation.kotlin.targets.KotlinDefaultArgumentsTarget;
import org.junit.Test;

/**
 * Test of functions with default arguments.
 */
public class KotlinDefaultArgumentsTest extends ValidationTestBase {

	public KotlinDefaultArgumentsTest() {
		super(KotlinDefaultArgumentsTarget.class);
	}

	@Test
	public void bytecodeSnapshots() throws Exception {
		assertSnapshot(KotlinDefaultArgumentsTarget.class,
				"longParameter$default", "long_parameter.txt");
		assertSnapshot(KotlinDefaultArgumentsTarget.class, "branch$default",
				"branch.txt");
		assertSnapshot(KotlinDefaultArgumentsTarget.Open.class, "f$default",
				"open_function.txt");
		// TODO multiple methods
		// assertSnapshot(KotlinDefaultArgumentsTarget.MoreThan32Parameters.class,
		// "<init>", "more_than_32_parameters.txt");
	}

}
