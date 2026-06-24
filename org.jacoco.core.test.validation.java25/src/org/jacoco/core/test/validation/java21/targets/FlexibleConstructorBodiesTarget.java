/*******************************************************************************
 * Copyright (c) 2009, 2026 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.test.validation.java21.targets;

import static org.jacoco.core.test.validation.targets.Stubs.nop;

import org.jacoco.core.test.validation.targets.Stubs;

/**
 * Test target with Flexible Constructor Bodies
 * (<a href="https://openjdk.org/jeps/513">JEP 513</a>).
 */
public class FlexibleConstructorBodiesTarget {

	private static class Base {
		Base() { // assertFullyCovered()
		} // assertFullyCovered()
	}

	private static class Child extends Base {
		Child(int v) { // assertEmpty()
			if (v < 0) { // assertFullyCovered(0, 2)
				throw new IllegalArgumentException(); // assertFullyCovered()
			} // assertEmpty()
			super(); // assertFullyCovered()
		} // assertFullyCovered()
	}

	public static void main(String[] args) {
		new Child(1);
		try {
			new Child(-1);
		} catch (IllegalArgumentException e) {
			/* expected */
		}
	}

}
