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
package org.jacoco.core.test.validation.java7;

import org.jacoco.core.analysis.ICounter;
import org.jacoco.core.test.validation.ValidationTestBase;
import org.jacoco.core.test.validation.java7.targets.StringSwitchTarget;
import org.junit.Test;

/**
 * Test of filtering of a bytecode that is generated for a String in switch
 * statement.
 */
public class StringSwitchTest extends ValidationTestBase {

	public StringSwitchTest() {
		super(StringSwitchTarget.class);
	}

	/**
	 * {@link StringSwitchTarget#covered(Object)}
	 */
	@Test
	public void covered() {
		if (isJDKCompiler) {
			assertLine("covered.switch", ICounter.FULLY_COVERED, 0, 4);
		} else {
			assertLine("covered.switch", ICounter.PARTLY_COVERED, 2, 7);
		}
		assertLine("covered.case1", ICounter.FULLY_COVERED);
		assertLine("covered.case2", ICounter.FULLY_COVERED);
		assertLine("covered.case3", ICounter.FULLY_COVERED);
		assertLine("covered.default", ICounter.FULLY_COVERED);
	}

	/**
	 * {@link StringSwitchTarget#notCovered(Object)}
	 */
	@Test
	public void notCovered() {
		assertLine("notCovered", ICounter.NOT_COVERED, isJDKCompiler ? 4 : 9,
				0);
	}

	/**
	 * {@link StringSwitchTarget#handwritten(String)}
	 */
	@Test
	public void handwritten() {
		assertLine("handwritten.firstSwitch", ICounter.FULLY_COVERED, 2, 1);
		assertLine("handwritten.ignored", ICounter.FULLY_COVERED, 1, 1);
		assertLine("handwritten.secondSwitch", ICounter.FULLY_COVERED, 3, 1);
		assertLine("handwritten.case1", ICounter.FULLY_COVERED);
		assertLine("handwritten.case2", ICounter.NOT_COVERED);
	}

}
