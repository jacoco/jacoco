/*******************************************************************************
 * Copyright (c) 2009, 2017 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.test.validation;

import org.jacoco.core.analysis.ICounter;
import org.jacoco.core.test.validation.targets.BadCycleClass;
import org.junit.Test;

/**
 * Test of "bad cycles" with classes.
 */
public class BadCycleClassTest extends ValidationTestBase {

	public BadCycleClassTest() throws Exception {
		super(BadCycleClass.class);
	}

	@Test
	public void test() throws Exception {
		assertLine("childinit", ICounter.FULLY_COVERED);
		assertLine("childsomeMethod", ICounter.FULLY_COVERED);
		assertLine("childclinit", ICounter.FULLY_COVERED);

		// The cycle causes a constructor and instance method to be called
		// before the static initializer of a class:
		assertLogEvents("childinit", "childsomeMethod", "childclinit",
				"childinit");
	}

}
