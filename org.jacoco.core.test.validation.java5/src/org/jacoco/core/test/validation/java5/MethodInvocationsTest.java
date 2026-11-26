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
package org.jacoco.core.test.validation.java5;

import org.jacoco.core.test.validation.JavaVersion;
import org.jacoco.core.test.validation.Source;
import org.jacoco.core.test.validation.ValidationTestBase;
import org.jacoco.core.test.validation.java5.targets.MethodInvocationsTarget;
import org.junit.Test;

/**
 * Test of code coverage in {@link MethodInvocationsTarget}.
 */
public class MethodInvocationsTest extends ValidationTestBase {

	public MethodInvocationsTest() {
		super(MethodInvocationsTarget.class);
	}

	public void assertEmptyWhenJavacBelow8OtherwiseFully(
			final Source.Line line) {
		if (isJDKCompiler && JavaVersion.current().isBefore("8")) {
			// https://bugs.openjdk.org/browse/JDK-7024096
			assertEmpty(line);
		} else {
			assertFullyCovered(line);
		}
	}

	public void assertEmptyWhenJavacOtherwiseFully(final Source.Line line) {
		if (isJDKCompiler) {
			assertEmpty(line);
		} else {
			assertFullyCovered(line);
		}
	}

	@Test
	public void bytecodeSnapshots() throws Exception {
		final String version = !isJDKCompiler ? "ecj"
				: JavaVersion.current().isBefore("8") ? "5" : "";
		assertSnapshot(MethodInvocationsTarget.class, "chained",
				version + "/chained.txt");
		assertSnapshot(MethodInvocationsTarget.class, "nested",
				version + "/nested.txt");
	}

}
