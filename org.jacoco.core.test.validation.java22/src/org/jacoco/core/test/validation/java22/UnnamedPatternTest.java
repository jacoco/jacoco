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
package org.jacoco.core.test.validation.java22;

import org.jacoco.core.test.validation.Source;
import org.jacoco.core.test.validation.ValidationTestBase;
import org.jacoco.core.test.validation.java22.targets.UnnamedPatternTarget;
import org.junit.Test;

/**
 * Test of code coverage in {@link UnnamedPatternTarget}.
 */
public class UnnamedPatternTest extends ValidationTestBase {

	public UnnamedPatternTest() {
		super(UnnamedPatternTarget.class);
	}

	@Test
	public void bytecodeSnapshots() throws Exception {
		assertSnapshot(UnnamedPatternTarget.class, "multiplePatternsInCase",
				"multiple_patterns_in_case.txt");
		assertSnapshot(UnnamedPatternTarget.class, "multiplePatternsInCaseWithGuard",
				"multiple_patterns_in_case_with_guard.txt");
	}

	public void assertJavacPartly(final Source.Line line,
			final int missedBranches, final int coveredBranches) {
		if (isJDKCompiler) {
			assertPartlyCovered(line, missedBranches, coveredBranches);
		}
	}

	public void assertEcjPartly(final Source.Line line,
			final int missedBranches, final int coveredBranches) {
		if (!isJDKCompiler) {
			assertPartlyCovered(line, missedBranches, coveredBranches);
		}
	}

}
