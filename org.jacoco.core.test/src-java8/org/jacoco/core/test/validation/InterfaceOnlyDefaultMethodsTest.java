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
import org.jacoco.core.test.validation.targets.InterfaceOnlyDefaultMethodsTarget;
import org.junit.Test;

/**
 * Tests of default methods in interfaces.
 */
public class InterfaceOnlyDefaultMethodsTest extends ValidationTestBase {

	public InterfaceOnlyDefaultMethodsTest() {
		super("src-java8", InterfaceOnlyDefaultMethodsTarget.class);
	}

	@Test
	public void testCoverageResult() {
		assertLine("m1", ICounter.FULLY_COVERED);
		assertLine("m2", ICounter.NOT_COVERED);
	}

}
