/*******************************************************************************
 * Copyright (c) 2009, 2021 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Jan Wloka - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.test.validation.groovy;

import org.jacoco.core.test.validation.ValidationTestBase;
import org.jacoco.core.test.validation.groovy.targets.GroovyDelegateClassTarget;
import org.junit.Test;

/**
 * Test of class with field annotated by {@link groovy.lang.Delegate}.
 */
public class GroovyDelegateClassTest extends ValidationTestBase {
	public GroovyDelegateClassTest() {
		super(GroovyDelegateClassTarget.class);
	}

	@Test
	public void test_method_count() {
		assertMethodCount(4);
	}
}
