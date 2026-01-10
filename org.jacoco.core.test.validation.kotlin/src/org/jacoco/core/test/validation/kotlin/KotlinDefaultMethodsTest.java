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

import org.jacoco.core.test.validation.ValidationTestBase;
import org.jacoco.core.test.validation.kotlin.targets.KotlinDefaultMethodsTarget;

/**
 * Test of code coverage in {@link KotlinDefaultMethodsTarget}.
 */
public class KotlinDefaultMethodsTest extends ValidationTestBase {

	public KotlinDefaultMethodsTest() {
		super(KotlinDefaultMethodsTarget.class);
	}

	@Override
	protected Collection<String> additionalClassesForAnalysis() {
		return Collections.singletonList(
				"org.jacoco.core.test.validation.kotlin.targets.KotlinDefaultMethodsTarget$I$DefaultImpls");
	}
}
