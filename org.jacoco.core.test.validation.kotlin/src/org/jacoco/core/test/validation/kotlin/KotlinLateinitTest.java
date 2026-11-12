/*******************************************************************************
 * Copyright (c) 2009, 2026 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Fabian Mastenbroek - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.test.validation.kotlin;

import org.jacoco.core.test.validation.kotlin.targets.KotlinLateinitTarget;
import org.junit.Test;

/**
 * Test of code coverage in {@link KotlinLateinitTarget}.
 */
public class KotlinLateinitTest extends KotlinValidationTestBase {

	public KotlinLateinitTest() {
		super(KotlinLateinitTarget.class);
	}

	/** Starting from {@link #KOTLIN_1_2} */
	@Test
	public void bytecodeSnapshots() throws Exception {
		assertSnapshot(KotlinLateinitTarget.Example.class, "readPublic",
				"public.txt", //
				KOTLIN_1_5, KOTLIN_1_2);
		assertSnapshot(KotlinLateinitTarget.Example.class, "readPublicGeneric",
				"public_generic.txt", //
				KOTLIN_1_5, KOTLIN_1_2);
		assertSnapshot(KotlinLateinitTarget.Example.class, "readPrivate",
				"private.txt", //
				KOTLIN_1_6, KOTLIN_1_5_30, KOTLIN_1_5, KOTLIN_1_2);
		assertSnapshot(KotlinLateinitTarget.Example.class, "readPrivateGeneric",
				"private_generic.txt", //
				KOTLIN_1_6, KOTLIN_1_5_30, KOTLIN_1_5, KOTLIN_1_2);
		assertSnapshot(KotlinLateinitTarget.Example.class, "getPublicMember",
				"public_getter.txt", //
				KOTLIN_1_6_20, KOTLIN_1_5_30, KOTLIN_1_5, KOTLIN_1_2);
		assertSnapshot(KotlinLateinitTarget.class, "readPublicOutside",
				"public_outside.txt", //
				KOTLIN_1_4, KOTLIN_1_2);
		assertSnapshot(KotlinLateinitTarget.Example.class,
				"getPublicGenericMember", //
				"public_generic_getter.txt", //
				KOTLIN_1_6_20, KOTLIN_1_5_30, KOTLIN_1_5, KOTLIN_1_2);
		assertSnapshot(KotlinLateinitTarget.class, "readPublicGenericOutside",
				"public_generic_outside.txt", //
				KOTLIN_1_4, KOTLIN_1_2);
	}

}
