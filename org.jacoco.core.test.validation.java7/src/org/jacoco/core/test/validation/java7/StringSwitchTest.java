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

import org.jacoco.core.test.validation.ValidationTestBase;
import org.jacoco.core.test.validation.java7.targets.StringSwitchTarget;

/**
 * Test of filtering of a bytecode that is generated for a String in switch
 * statement.
 */
public class StringSwitchTest extends ValidationTestBase {

	public StringSwitchTest() {
		super(StringSwitchTarget.class);
	}

	public void assertSwitchCovered(int nr) {
		if (isJDKCompiler) {
			assertFullyCovered(nr, 0, 4);
		} else {
			// Filtering for ECJ not yet implemented:
			assertPartlyCovered(nr, 2, 7);
		}
	}

	public void assertSwitchNotCovered(int nr) {
		if (isJDKCompiler) {
			assertNotCovered(nr, 4, 0);
		} else {
			// Filtering for ECJ not yet implemented:
			assertNotCovered(nr, 9, 0);
		}
	}

}
