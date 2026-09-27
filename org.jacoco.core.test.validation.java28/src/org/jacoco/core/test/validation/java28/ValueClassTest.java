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
package org.jacoco.core.test.validation.java28;

import org.jacoco.core.test.validation.ValidationTestBase;
import org.jacoco.core.test.validation.java28.targets.ValueClassTarget;

/**
 * Test of code coverage in {@link ValueClassTarget}.
 */
public class ValueClassTest extends ValidationTestBase {

	public ValueClassTest() {
		super(ValueClassTarget.class);
	}

}
