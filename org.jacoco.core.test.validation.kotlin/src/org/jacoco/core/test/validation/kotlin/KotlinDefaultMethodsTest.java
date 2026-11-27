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

import java.util.Collection;
import java.util.Collections;

import org.jacoco.core.test.validation.kotlin.targets.KotlinDefaultMethodsTarget;
import org.junit.Test;

/**
 * Test of code coverage in {@link KotlinDefaultMethodsTarget}.
 */
public class KotlinDefaultMethodsTest extends KotlinValidationTestBase {

	public KotlinDefaultMethodsTest() {
		super(KotlinDefaultMethodsTarget.class);
	}

	@Override
	protected Collection<String> additionalClassesForAnalysis() {
		return Collections.singletonList(
				"org.jacoco.core.test.validation.kotlin.targets.KotlinDefaultMethodsTarget$I$DefaultImpls");
	}

	/** Starting from {@link #KOTLIN_2_0} */
	@Test
	public void bytecodeSnapshots() throws Exception {
		assertSnapshot(KotlinDefaultMethodsTarget.I.class, "m2",
				"interface.txt", //
				KOTLIN_2_2, KOTLIN_2_0);
		assertSnapshot(KotlinDefaultMethodsTarget.C.class, "m2", "bridge.txt", //
				// marked as bridge since 2.2.20
				KOTLIN_2_2, KOTLIN_2_0);
		assertSnapshot(
				Class.forName(KotlinDefaultMethodsTarget.I.class.getName()
						+ "$DefaultImpls"),
				"m2", "impl.txt", //
				KOTLIN_2_2, KOTLIN_2_0);
		assertSnapshot(KotlinDefaultMethodsTarget.I.class, "access\\$m2\\$jd",
				"accessor.txt", //
				KOTLIN_2_2);
	}

}
