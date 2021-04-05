/*******************************************************************************
 * Copyright (c) 2009, 2021 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Andres Almiray - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.test.validation.groovy;

import org.jacoco.core.test.validation.ValidationTestBase;
import org.jacoco.core.test.validation.groovy.targets.GroovyCanonicalClassTarget;
import org.junit.Test;

/**
 * Test of class with {@link groovy.transform.Canonical} annotation.
 */
public class GroovyCanonicalClassTest extends ValidationTestBase {
	public GroovyCanonicalClassTest() {
		super(GroovyCanonicalClassTarget.class);
	}

	@Test
	public void test_method_count() {
		assertMethodCount(1);
	}
}
