/*******************************************************************************
 * Copyright (c) 2009, 2025 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.test.validation.java22.targets;

import static org.jacoco.core.test.validation.targets.Stubs.nop;

/**
 * <a href="https://openjdk.org/jeps/456">JEP 456</a>
 */
public class UnnamedPatternTarget {

	private record Point(int x, int y) {
	}

	private static void instanceofOperator(Object o) {
        if (o instanceof Point(_, _)) { // assertFullyCovered(0, 2)
            nop("Point"); // assertFullyCovered()
            return; // assertFullyCovered()
        } // assertEmpty()
        nop(); // assertFullyCovered()
    }

	private static void switchStatement(Object o) {
		switch (o) { // assertFullyCovered(0, 2)
		case Point(_, _) -> nop("Point"); // assertSwitchStatementLastCase()
		default -> nop("default"); // assertFullyCovered()
		} // assertEmpty()
	}

	public static void main(String[] args) {
		instanceofOperator(new Point(1, 2));
		instanceofOperator(new Object());

		switchStatement(new Point(1, 2));
		switchStatement(new Object());
	}

}
