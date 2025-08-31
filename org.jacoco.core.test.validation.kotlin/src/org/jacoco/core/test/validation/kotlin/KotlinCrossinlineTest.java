/*******************************************************************************
 * Copyright (c) 2009, 2025 Mountainminds GmbH & Co. KG and Contributors
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
package org.jacoco.core.test.validation.kotlin;

import java.util.Arrays;
import java.util.Collection;

import org.jacoco.core.test.validation.ValidationTestBase;
import org.jacoco.core.test.validation.kotlin.targets.KotlinCrossinlineTarget;

/**
 * Test of code coverage in {@link KotlinCrossinlineTarget}.
 */
public class KotlinCrossinlineTest extends ValidationTestBase {

	public KotlinCrossinlineTest() {
		super(KotlinCrossinlineTarget.class);
	}

	@Override
	protected Collection<String> additionalClassesForAnalysis() {
		// Analyze SMAPs in non executed classes:
		return Arrays.asList(
				"org.jacoco.core.test.validation.kotlin.targets.KotlinCrossinlineTarget$example$1",
				"org.jacoco.core.test.validation.kotlin.targets.KotlinCrossinlineTarget$example$1$1");
	}

}
