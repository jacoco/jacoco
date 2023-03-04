/*******************************************************************************
 * Copyright (c) 2009, 2023 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Stephan Classen
 *    Vadim Bauer
 *
 *******************************************************************************/
package org.jacoco.core.test.validation.groovy;

import org.jacoco.core.test.validation.ValidationTestBase;
import org.jacoco.core.test.validation.groovy.targets.GroovySortableClassTarget;
import org.junit.Test;

/**
 * Test of class with {@link groovy.transform.Sortable} annotation.
 */
public class GroovySortableClassTest extends ValidationTestBase {
	public GroovySortableClassTest() {
		super(GroovySortableClassTarget.class);
	}

	@Test
	public void test_method_count() {
		// main method and static initializer
		assertMethodCount(2);
	}
}
