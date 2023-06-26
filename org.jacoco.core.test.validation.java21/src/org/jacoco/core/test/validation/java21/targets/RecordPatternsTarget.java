/*******************************************************************************
 * Copyright (c) 2009, 2023 Mountainminds GmbH & Co. KG and Contributors
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
package org.jacoco.core.test.validation.java21.targets;

import static org.jacoco.core.test.validation.targets.Stubs.nop;

/**
 * This target exercises Record Patterns
 * (<a href="https://openjdk.org/jeps/440">JEP 440</a>).
 */
public class RecordPatternsTarget {

	private record Point(int x, int y) {
	}

	private static void instanceofOperator(Object o) {
		if (o instanceof Point(int x, int y)) { // assertFullyCovered(0, 2)
			nop(x + y); // assertFullyCovered()
		} // assertEmpty()
	}

	private static void switchStatement(Object p) {
		switch (p) { // assertFullyCovered(0, 2)
		case Point(int x, int y) -> nop(x + y); // assertFullyCovered()
		default -> nop(); // assertFullyCovered()
		} // assertEmpty()
	}

	public static void main(String[] args) {
		instanceofOperator(new Point(1, 2));
		instanceofOperator(new Object());

		switchStatement(new Point(1, 2));
		switchStatement(new Object());
	}

}
