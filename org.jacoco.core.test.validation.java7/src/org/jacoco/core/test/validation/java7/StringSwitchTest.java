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

import org.jacoco.core.test.validation.Source.Line;
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

	public void assertSwitchCovered(final Line line) {
		if (isJDKCompiler) {
			assertFullyCovered(line, 0, 4);
		} else {
			// Filtering for ECJ not yet implemented:
			assertPartlyCovered(line, 2, 7);
		}
	}

	public void assertSwitchNotCovered(final Line line) {
		if (isJDKCompiler) {
			assertNotCovered(line, 4, 0);
		} else {
			// Filtering for ECJ not yet implemented:
			assertNotCovered(line, 9, 0);
		}
	}

	public void assertLookupswitch(final Line line) {
		if (isJDKCompiler) {
			assertNotCovered(line, 3, 0);
		} else {
			// Filtering for ECJ not yet implemented:
			assertNotCovered(line, 7, 0);
		}
	}

}
