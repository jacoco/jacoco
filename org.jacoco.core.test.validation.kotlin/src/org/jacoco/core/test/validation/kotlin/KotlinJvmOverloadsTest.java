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

import org.jacoco.core.test.validation.kotlin.targets.KotlinJvmOverloadsTarget;
import org.junit.Test;

/**
 * Test of code coverage in {@link KotlinJvmOverloadsTarget}.
 */
public class KotlinJvmOverloadsTest extends KotlinValidationTestBase {

	public KotlinJvmOverloadsTest() {
		super(KotlinJvmOverloadsTarget.class);
	}

	@Test
	public void bytecodeSnapshots() throws Exception {
		assertSnapshot(KotlinJvmOverloadsTarget.class, "example\\(\\)V",
				"example0.txt", //
				KOTLIN_2_2, KOTLIN_2_0);
		assertSnapshot(KotlinJvmOverloadsTarget.class,
				"example\\(Ljava/lang/String;\\)V", "example1.txt", //
				KOTLIN_2_2, KOTLIN_2_0);
		assertSnapshot(KotlinJvmOverloadsTarget.class, "use",
				"example_callsite.txt", //
				KOTLIN_2_0);
		assertSnapshot(KotlinJvmOverloadsTarget.Example.class, "<init>\\(\\)V",
				"constructor0.txt", //
				KOTLIN_2_2, KOTLIN_2_0);
		assertSnapshot(KotlinJvmOverloadsTarget.Example.class,
				"<init>\\(Ljava/lang/String;\\)V", "constructor1.txt", //
				KOTLIN_2_2, KOTLIN_2_0);
		assertSnapshot(KotlinJvmOverloadsTarget.Example.class,
				"<init>\\(Ljava/lang/Object;\\)V", "constructor_callsite.txt", //
				KOTLIN_2_0);
	}

}
