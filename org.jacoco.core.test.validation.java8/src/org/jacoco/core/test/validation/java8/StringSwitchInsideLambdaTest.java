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
package org.jacoco.core.test.validation.java8;

import org.jacoco.core.test.validation.JavaVersion;
import org.jacoco.core.test.validation.ValidationTestBase;
import org.jacoco.core.test.validation.java8.targets.StringSwitchInsideLambdaTarget;
import org.junit.Test;

/**
 * Test of code coverage in {@link StringSwitchInsideLambdaTarget}.
 */
public class StringSwitchInsideLambdaTest extends ValidationTestBase {

	public StringSwitchInsideLambdaTest() {
		super(StringSwitchInsideLambdaTarget.class);
	}

	@Test
	public void bytecodeSnapshots() throws Exception {
		if (!isJDKCompiler) {
			assertSnapshot(StringSwitchInsideLambdaTarget.class, "lambda$0",
					"ecj/example.txt");
		} else {
			assertSnapshot(StringSwitchInsideLambdaTarget.class,
					"lambda$example$0",
					(!JavaVersion.current().isBefore("24")
							&& JavaVersion.current().isBefore("27") ? "24-26/"
									: "")
							+ "example.txt");
		}
	}

}
