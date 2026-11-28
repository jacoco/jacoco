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

import kotlin.text.StringsKt;
import org.jacoco.core.test.validation.kotlin.targets.KotlinDefaultArgumentsTarget;
import org.junit.Test;

/**
 * Test of functions with default arguments.
 */
public class KotlinDefaultArgumentsTest extends KotlinValidationTestBase {

	public KotlinDefaultArgumentsTest() {
		super(KotlinDefaultArgumentsTarget.class);
	}

	@Test
	public void bytecodeSnapshots() throws Exception {
		assertSnapshot(KotlinDefaultArgumentsTarget.class, "f\\$default",
				"f.txt", //
				KOTLIN_1_5);
		assertSnapshot(KotlinDefaultArgumentsTarget.class,
				"longParameter\\$default", "long_parameter.txt", //
				KOTLIN_1_5);
		assertSnapshot(KotlinDefaultArgumentsTarget.class, "branch\\$default",
				"branch.txt", //
				KOTLIN_1_5);
		assertSnapshot(KotlinDefaultArgumentsTarget.Open.class, "f\\$default",
				"open_function.txt", //
				KOTLIN_1_5);
		assertSnapshot(KotlinDefaultArgumentsTarget.Constructor.class,
				"<init>\\(ZLjava/lang/String;ILkotlin/jvm/internal/DefaultConstructorMarker;\\)V",
				"constructor.txt", //
				KOTLIN_1_5);
		assertSnapshot(KotlinDefaultArgumentsTarget.MoreThan32Parameters.class,
				"<init>\\(" + StringsKt.repeat("Ljava/lang/String;", 33)
						+ "IILkotlin/jvm/internal/DefaultConstructorMarker;\\)V",
				"more_than_32_parameters.txt", //
				KOTLIN_1_5);
	}

}
