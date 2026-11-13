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
package org.jacoco.core.test.validation.java16;

import org.jacoco.core.test.validation.Source;
import org.jacoco.core.test.validation.ValidationTestBase;
import org.jacoco.core.test.validation.java16.targets.RecordsTarget;
import org.junit.Test;

/**
 * Test of code coverage for records.
 */
public class RecordsTest extends ValidationTestBase {

	public RecordsTest() {
		super(RecordsTarget.class);
	}

	public void assertJavacFully(final Source.Line line) {
		if (isJDKCompiler) {
			assertFullyCovered(line);
		}
	}

	public void assertEcjEmpty(final Source.Line line) {
		if (!isJDKCompiler) {
			assertEmpty(line);
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
		final String version = !isJDKCompiler ? "ecj" : "";
		assertSnapshot(RecordsTarget.WithoutFields.class, "hashCode",
				version + "/hashCode_without_fields.txt");
		assertSnapshot(RecordsTarget.WithoutFields.class, "toString",
				version + "/toString_without_fields.txt");
		assertSnapshot(RecordsTarget.WithoutFields.class, "equals",
				version + "/equals_without_fields.txt");
		assertSnapshot(RecordsTarget.WithFields.class, "hashCode",
				version + "/hashCode_with_fields.txt");
		assertSnapshot(RecordsTarget.WithFields.class, "toString",
				version + "/toString_with_fields.txt");
		assertSnapshot(RecordsTarget.WithFields.class, "equals",
				version + "/equals_with_fields.txt");
	}

}
