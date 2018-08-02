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

import org.jacoco.core.analysis.ICounter;
import org.jacoco.core.test.validation.ValidationTestBase;
import org.jacoco.core.test.validation.java5.targets.ConstructorsTarget;
import org.junit.Test;

/**
 * Tests for different constructors. Private empty constructors without
 * arguments are filtered.
 */
public class ConstructorsTest extends ValidationTestBase {

	public ConstructorsTest() {
		super(ConstructorsTarget.class);
	}

	@Test
	public void testCoverageResult() {
		// not filtered because not private:
		assertLine("packageLocal", ICounter.FULLY_COVERED);

		// not filtered because has argument:
		assertLine("arg", ICounter.FULLY_COVERED);

		// not filtered because not empty - prepares arguments for super
		// constructor:
		assertLine("super", ICounter.FULLY_COVERED);

		// not filtered because contains initialization of a field to hold
		// reference to an instance of outer class that is passed as an
		// argument:
		assertLine("inner", ICounter.FULLY_COVERED);

		// not filtered because not empty - contains initialization of
		// a field:
		assertLine("innerStatic", ICounter.FULLY_COVERED);

		// not filtered because default constructor for not private inner
		// classes is not private:
		assertLine("publicDefault", ICounter.FULLY_COVERED);
		assertLine("packageLocalDefault", ICounter.FULLY_COVERED);

		assertLine("privateDefault", ICounter.EMPTY);

		assertLine("privateNonEmptyNoArg", ICounter.FULLY_COVERED);

		assertLine("privateEmptyNoArg", ICounter.EMPTY);
		assertLine("return", ICounter.EMPTY);
	}

}
