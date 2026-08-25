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
import org.jacoco.core.test.validation.kotlin.targets.KotlinUnsafeCastOperatorTarget;
import org.junit.Test;

import kotlin.KotlinVersion;

/**
 * Test of "unsafe" cast operator.
 */
public class KotlinUnsafeCastOperatorTest extends ValidationTestBase {

	public KotlinUnsafeCastOperatorTest() {
		super(KotlinUnsafeCastOperatorTarget.class);
	}

	@Test
	public void bytecodeSnapshots() throws Exception {
		if (KotlinVersion.CURRENT.isAtLeast(1, 7)) {
			assertSnapshot(KotlinUnsafeCastOperatorTarget.class, "example",
					"unsafe_cast_operator.txt");
		} else if (KotlinVersion.CURRENT.isAtLeast(1, 6)) {
			// https://github.com/JetBrains/kotlin/commit/041773fd2584bc279813361eb7fc11ae84c214fd
			assertSnapshot(KotlinUnsafeCastOperatorTarget.class, "example",
					"1.6/unsafe_cast_operator.txt");
		} else if (KotlinVersion.CURRENT.isAtLeast(1, 5)) {
			assertSnapshot(KotlinUnsafeCastOperatorTarget.class, "example",
					"1.5/unsafe_cast_operator.txt");
		} else if (KotlinVersion.CURRENT.isAtLeast(1, 4)) {
			assertSnapshot(KotlinUnsafeCastOperatorTarget.class, "example",
					"1.4/unsafe_cast_operator.txt");
		} else {
			assertSnapshot(KotlinUnsafeCastOperatorTarget.class, "example",
					"1.3/unsafe_cast_operator.txt");
		}
	}

}
