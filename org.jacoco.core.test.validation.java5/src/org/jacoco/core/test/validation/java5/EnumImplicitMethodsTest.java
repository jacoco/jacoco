/*******************************************************************************
 * Copyright (c) 2009, 2018 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.test.validation.java5;

import org.jacoco.core.test.validation.ValidationTestBase;
import org.jacoco.core.test.validation.java5.targets.EnumImplicitMethodsTarget;
import org.junit.Test;

/**
 * Test of an implicit methods and static initializer in enums.
 */
public class EnumImplicitMethodsTest extends ValidationTestBase {

	public EnumImplicitMethodsTest() {
		super(EnumImplicitMethodsTarget.class);
	}

	@Test
	public void test_method_count() {
		assertMethodCount(5);
	}

}
