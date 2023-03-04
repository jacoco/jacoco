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
package org.jacoco.core.test.validation.java5.targets;

import org.jacoco.core.test.validation.targets.Stubs;

public enum EnumImplicitMethodsTarget { // assertFullyCovered()

	CONST(Stubs.f() ? new Object() : new Object()); // assertPartlyCovered(1, 1)

	static {
	} // assertFullyCovered()

	/**
	 * Unlike in {@link ConstructorsTarget regular classes}, even if enum has
	 * explicit constructor, {@code clinit} method in any case has a reference
	 * to the line of enum definition.
	 */
	EnumImplicitMethodsTarget(Object o) { // assertFullyCovered()
	} // assertFullyCovered()

	/**
	 * This method should not be excluded from analysis unlike implicitly
	 * created {@link #valueOf(String)} method that refers to the line of enum
	 * definition in case of javac and to the first line in case of ECJ.
	 */
	public void valueOf() {
	} // assertNotCovered()

	/**
	 * This method should not be excluded from analysis unlike implicitly
	 * created {@link #values()} method that refers to the line of enum
	 * definition in case of javac and to the first line in case of ECJ.
	 */
	public void values(Object o) {
	} // assertNotCovered()

	public static void main(String[] args) {
	}

}
