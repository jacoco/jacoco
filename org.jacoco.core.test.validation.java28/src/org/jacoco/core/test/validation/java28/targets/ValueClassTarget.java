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
package org.jacoco.core.test.validation.java28.targets;

/**
 * <a href="https://openjdk.org/jeps/401">JEP 401: Value Classes and Objects
 * (Preview)</a>
 */
public class ValueClassTarget {

	private value record R(int v) { // assertFullyCovered()
	} // assertEmpty()

	private static value class C { // assertEmpty()
		private int v; // assertEmpty()
		C(int v) { // assertFullyCovered()
			this.v = v; // assertFullyCovered()
		} // assertFullyCovered()
	} // assertEmpty()

	public static void main(String[] args) {
		new R(1);
		new C(1);
	}

}
