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

import org.jacoco.core.test.validation.kotlin.targets.KotlinJvmStaticTarget;
import org.junit.Test;

/**
 * Test of code coverage in {@link KotlinJvmStaticTarget}.
 */
public class KotlinJvmStaticTest extends KotlinValidationTestBase {

	public KotlinJvmStaticTest() {
		super(KotlinJvmStaticTarget.class);
	}

	/** Starting from {@link #KOTLIN_1_3} */
	@Test
	public void bytecodeSnapshots() throws Exception {
		assertSnapshot(KotlinJvmStaticTarget.Interface.class, "target",
				"interface.txt", //
				KOTLIN_2_0, KOTLIN_1_3);
		assertSnapshot(KotlinJvmStaticTarget.Interface.Companion.class,
				"target", "interface_companion.txt", //
				KOTLIN_1_3);
		assertSnapshot(KotlinJvmStaticTarget.Class.class, "target", "class.txt", //
				KOTLIN_2_0, KOTLIN_1_3);
		assertSnapshot(KotlinJvmStaticTarget.Class.Companion.class, "target",
				"class_companion.txt", //
				KOTLIN_1_3);
		assertSnapshot(KotlinJvmStaticTarget.NamedObject.class, "target",
				"named_object.txt", //
				KOTLIN_1_3);
	}

}
