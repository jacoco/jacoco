/*******************************************************************************
 * Copyright (c) 2009, 2023 Mountainminds GmbH & Co. KG and Contributors
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
package org.jacoco.core.test.validation.java5;

import org.jacoco.core.test.validation.ValidationTestBase;
import org.jacoco.core.test.validation.java5.targets.SyntheticTarget;
import org.junit.Test;

/**
 * Test of filtering of synthetic methods.
 */
public class SyntheticTest extends ValidationTestBase {

	public SyntheticTest() {
		super(SyntheticTarget.class);
	}

	@Test
	public void test_method_count() {
		assertMethodCount(5);
	}

}
