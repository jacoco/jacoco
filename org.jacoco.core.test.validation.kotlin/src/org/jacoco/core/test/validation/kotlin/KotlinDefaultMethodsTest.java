/*******************************************************************************
 * Copyright (c) 2009, 2021 Mountainminds GmbH & Co. KG and Contributors
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
	public void all_missed_instructions_should_have_line_number() {
		// instructions without line numbers
		// corresponding to non-executed default implementations
	}

}
