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

import java.io.IOException;

import org.jacoco.core.internal.instr.InstrSupport;
import org.jacoco.core.test.TargetLoader;
import org.jacoco.core.test.validation.Source;
import org.jacoco.core.test.validation.kotlin.targets.KotlinWhenSealedTarget;
import org.junit.Test;
import org.objectweb.asm.Opcodes;

/**
 * Test of code coverage in {@link KotlinWhenSealedTarget}.
 */
public class KotlinWhenSealedTest extends KotlinValidationTestBase {

	public KotlinWhenSealedTest() {
		super(KotlinWhenSealedTarget.class);
	}

	public void assertIndyFullyCovered(final Source.Line line,
			final int missingBranches, final int coveredBranches) {
		if (isIndy()) {
			assertFullyCovered(line, missingBranches, coveredBranches);
		}
	}

	public void assertNonIndyFullyCovered(final Source.Line line,
			final int missingBranches, final int coveredBranches) {
		if (!isIndy()) {
			assertFullyCovered(line, missingBranches, coveredBranches);
		}
	}

	private static boolean isIndy() {
		try {
			return InstrSupport
					.getMajorVersion(TargetLoader.getClassDataAsBytes(
							KotlinWhenSealedTarget.class)) >= Opcodes.V21;
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	static class KotlinVersionWithIndy extends KotlinVersion {
		public KotlinVersionWithIndy(final int major, final int minor,
				final int patch) {
			super(major, minor, patch);
		}

		@Override
		public boolean match() {
			return super.match() && isIndy();
		}

		@Override
		public String name() {
			return super.name() + "-indy";
		}
	}

	private static final KotlinVersion KOTLIN_2_4_20_INDY = new KotlinVersionWithIndy(
			2, 4, 20);

	/** Starting from {@link #KOTLIN_1_4} */
	@Test
	public void bytecodeSnapshots() throws Exception {
		assertSnapshot(KotlinWhenSealedTarget.class, "expression",
				"expression.txt", //
				KOTLIN_2_4_20_INDY, KOTLIN_2_0, KOTLIN_1_4);
		assertSnapshot(KotlinWhenSealedTarget.class,
				"expressionWithRedundantElse",
				"expression_with_redundant_else.txt", //
				KOTLIN_2_4_20_INDY, KOTLIN_1_4);
		assertSnapshot(KotlinWhenSealedTarget.class,
				"expressionWithNonRedundantElse",
				"expression_with_non_redundant_else.txt", //
				KOTLIN_2_4_20_INDY, KOTLIN_1_5, KOTLIN_1_4);
		assertSnapshot(KotlinWhenSealedTarget.class, "nonSealedWhen",
				"non_sealed_when.txt", //
				KOTLIN_2_4_20_INDY, KOTLIN_1_5, KOTLIN_1_4);
		assertSnapshot(KotlinWhenSealedTarget.class, "nonSealedIf",
				"non_sealed_if.txt", //
				KOTLIN_2_4_20_INDY, KOTLIN_1_4);
		assertSnapshot(KotlinWhenSealedTarget.class, "statement",
				"statement.txt", //
				KOTLIN_2_4_20_INDY, KOTLIN_2_0, KOTLIN_1_5, KOTLIN_1_4);
		assertSnapshot(KotlinWhenSealedTarget.class, "indistinguishable",
				"indistinguishable.txt", //
				KOTLIN_2_4_20_INDY, KOTLIN_1_5, KOTLIN_1_4);
		assertSnapshot(KotlinWhenSealedTarget.class, "singleCase",
				"single_case.txt", //
				KOTLIN_1_6, KOTLIN_1_4);
		assertSnapshot(KotlinWhenSealedTarget.class,
				"indistinguishableSingleCase",
				"indistinguishable_single_case.txt", //
				KOTLIN_1_5, KOTLIN_1_4);
	}

}
