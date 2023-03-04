/*******************************************************************************
 * Copyright (c) 2009, 2023 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.test.validation.java14;

import org.jacoco.core.test.validation.Source.Line;
import org.jacoco.core.test.validation.ValidationTestBase;
import org.jacoco.core.test.validation.java14.targets.SwitchExpressionsTarget;

/**
 * Test of code coverage for switch expressions.
 */
public class SwitchExpressionsTest extends ValidationTestBase {

	public SwitchExpressionsTest() {
		super(SwitchExpressionsTarget.class);
	}

	public void assertExhaustiveSwitchExpression(Line line) {
		if (isJDKCompiler) {
			assertPartlyCovered(line, 1, 3);
		} else {
			assertFullyCovered(line, 1, 3);
		}
	}

	public void assertExhaustiveSwitchExpressionLastCase(Line line) {
		if (isJDKCompiler) {
			assertFullyCovered(line);
		} else {
			assertPartlyCovered(line);
		}
	}

}
