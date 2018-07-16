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
package org.jacoco.core.test.validation.targets;

public enum EnumImplicitMethods { // $line-classdef$

	CONST(Stubs.f() ? new Object() : new Object()); // $line-const$

	static {
	} // $line-staticblock$

	/**
	 * Unlike in {@link Target07 regular classes}, even if enum has explicit
	 * constructor, {@code clinit} method in any case has a reference to the
	 * line of enum definition.
	 */
	EnumImplicitMethods(Object o) { // $line-super$
	} // $line-constructor$

	/**
	 * This method should not be excluded from analysis unlike implicitly
	 * created {@link #valueOf(String)} method that refers to the line of enum
	 * definition in case of javac and to the first line in case of ECJ.
	 */
	public void valueOf() {
	} // $line-customValueOfMethod$

	/**
	 * This method should not be excluded from analysis unlike implicitly
	 * created {@link #values()} method that refers to the line of enum
	 * definition in case of javac and to the first line in case of ECJ.
	 */
	public void values(Object o) {
	} // $line-customValuesMethod$

	public static void main(String[] args) {
	}

}
