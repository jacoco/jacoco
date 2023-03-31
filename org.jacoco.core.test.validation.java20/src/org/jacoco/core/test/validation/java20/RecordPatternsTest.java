/*******************************************************************************
 * Copyright (c) 2009, 2023 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.test.validation.java20;

import org.jacoco.core.test.validation.JavaVersion;
import org.jacoco.core.test.validation.Source.Line;
import org.jacoco.core.test.validation.ValidationTestBase;
import org.jacoco.core.test.validation.java20.targets.RecordPatternsTarget;

/**
 * Test of code coverage in {@link RecordPatterns}.
 */
public class RecordPatternsTest extends ValidationTestBase {

	public RecordPatternsTest() {
		super(RecordPatternsTarget.class);
	}

	public void assertSwitchStatementCase(Line line) {
		if (JavaVersion.current().isBefore("21")) {
			assertFullyCovered(line);
		} else {
			// https://github.com/openjdk/jdk/commit/138cdc9283ae8f3367e51f0fe7e27833118dd7cb
			assertPartlyCovered(line);
		}
	}

	public void assertSwitchStatementDefault(Line line) {
		if (JavaVersion.current().isBefore("21")) {
			assertPartlyCovered(line);
		} else {
			// https://github.com/openjdk/jdk/commit/138cdc9283ae8f3367e51f0fe7e27833118dd7cb
			assertFullyCovered(line);
		}
	}

}
