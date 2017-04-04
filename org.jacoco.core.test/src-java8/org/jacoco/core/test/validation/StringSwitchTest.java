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
import org.jacoco.core.test.validation.targets.StringSwitch;
import org.junit.Test;

/**
 * Test of filtering of a bytecode that is generated for a String in switch
 * statement (Java 7).
 */
public class StringSwitchTest extends ValidationTestBase {

	public StringSwitchTest() throws Exception {
		super("src-java8", StringSwitch.class);
	}

	@Test
	public void test() throws Exception {
		assertLine("beforeSwitch", ICounter.FULLY_COVERED);
		assertLine("switch", ICounter.FULLY_COVERED, 3, 1);
		assertLine("caseA", ICounter.NOT_COVERED);
		assertLine("caseB", ICounter.NOT_COVERED);
		assertLine("case0A", ICounter.NOT_COVERED);
		assertLine("default", ICounter.FULLY_COVERED);
		assertLine("afterSwitch", ICounter.FULLY_COVERED, 1, 1);

		assertLine("coveredSwitch", ICounter.FULLY_COVERED, 0, 4);

		// Filter is quite aggressive
		assertLine("switchByStringHashCode", ICounter.NOT_COVERED, 0, 0);
	}

}
