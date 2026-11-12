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
package org.jacoco.core.test.validation.java21;

import org.jacoco.core.test.validation.JavaVersion;
import org.jacoco.core.test.validation.Source.Line;
import org.jacoco.core.test.validation.ValidationTestBase;
import org.jacoco.core.test.validation.java21.targets.RecordPatternsTarget;
import org.junit.Test;

/**
 * Test of code coverage in {@link RecordPatternsTarget}.
 */
public class RecordPatternsTest extends ValidationTestBase {

	public RecordPatternsTest() {
		super(RecordPatternsTarget.class);
	}

	public void assertInstanceof(final Line line) {
		if (JavaVersion.current().isBefore("23") || !isJDKCompiler) {
			assertFullyCovered(line, 0, 2);
		} else {
			// TODO https://bugs.openjdk.org/browse/JDK-8303374
			assertFullyCovered(line, 2, 4);
		}
	}

	public void assertSwitchStatementLastCase(final Line line) {
		if (JavaVersion.current().isBefore("23") || !isJDKCompiler) {
			assertFullyCovered(line);
		} else {
			// TODO https://bugs.openjdk.org/browse/JDK-8303374
			assertPartlyCovered(line, 2, 2);
		}
	}

	public void assertJavacFully(final Line line, final int missedBranches,
			final int coveredBranches) {
		if (isJDKCompiler) {
			assertFullyCovered(line, missedBranches, coveredBranches);
		}
	}

	public void assertJavacPartly(final Line line, final int missedBranches,
			final int coveredBranches) {
		if (isJDKCompiler) {
			assertPartlyCovered(line, missedBranches, coveredBranches);
		}
	}

	public void assertEcjFully(final Line line, final int missedBranches,
			final int coveredBranches) {
		if (!isJDKCompiler) {
			assertFullyCovered(line, missedBranches, coveredBranches);
		}
	}

	public void assertEcjPartly(final Line line, final int missedBranches,
			final int coveredBranches) {
		if (!isJDKCompiler) {
			assertPartlyCovered(line, missedBranches, coveredBranches);
		}
	}

	@Override
	public void first_line_in_coverage_data_should_be_greater_than_one() {
		// ECJ generates zero line number in record constructor
		if (isJDKCompiler) {
			super.first_line_in_coverage_data_should_be_greater_than_one();
		}
	}

	@Test
	public void bytecodeSnapshots() throws Exception {
		assertSnapshot(RecordPatternsTarget.class, "instanceofOperator",
				(!isJDKCompiler ? "ecj"
						: (JavaVersion.current().isBefore("23") ? "21"
								: (JavaVersion.current().isBefore("26") ? "23"
										: "26")))
						+ "/instanceof.txt");
		assertSnapshot(RecordPatternsTarget.class, "switchStatement",
				(!isJDKCompiler ? "ecj"
						: (JavaVersion.current().isBefore("23") ? "21"
								: (JavaVersion.current().isBefore("26") ? "23"
										: "26")))
						+ "/switch.txt");
		assertSnapshot(RecordPatternsTarget.class, "nestedRecordPattern",
				(!isJDKCompiler ? "ecj" : "21") + "/nested.txt");
	}

}
