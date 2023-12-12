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

/**
 * This test target results in synthetic methods.
 */
public class SyntheticTarget { // assertEmpty()

	private static int counter; // assertEmpty()

	/**
	 * {@link org.jacoco.core.test.validation.java5.targets.ConstructorsTarget
	 * Default constructor will refer to a line of class definition}, so that we
	 * define constructor explicitly in order to verify that we filter all other
	 * constructions here that might refer to line of class definition.
	 */
	private SyntheticTarget() {
	}

	static class Inner extends SyntheticTarget { // assertEmpty()

		Inner() {
		}

		/**
		 * Access to private field of outer class causes creation of synthetic
		 * methods in it. In case of javac those methods refer to the line of
		 * outer class definition, in case of ECJ - to the line of field.
		 */
		private static void inc() {
			counter = counter + 2;
		}

		/**
		 * Difference of return type with overridden method causes creation of
		 * synthetic bridge method in this class. In case of javac this method
		 * refers to the line of inner class definition, in case of EJC - to the
		 * first line of file.
		 */
		@Override
		public String get() {
			return null;
		}
	}

	public Object get() {
		return null;
	}

	public static void main(String[] args) {
		Inner.inc();
	}

}
