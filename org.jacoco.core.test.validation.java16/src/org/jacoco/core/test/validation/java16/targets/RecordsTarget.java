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
package org.jacoco.core.test.validation.java16.targets;

/**
 * This target exercises records
 * (<a href="https://openjdk.java.net/jeps/395">JEP 395</a>).
 */
public class RecordsTarget {

	record WithoutFields() { // assertJavacFully() assertEcjEmpty()
	} // assertEmpty()

	record WithFields( // assertJavacFully() assertEcjEmpty()
			int x // assertEmpty()
	) { // assertEmpty()
	} // assertEmpty()

	record WithCustomMethods(int x) { // assertJavacFully() assertEcjEmpty()
		@Override // assertEmpty()
		public int x() { // assertEmpty()
			return x; // assertEmpty()
		} // assertEmpty()

		@Override // assertEmpty()
		public String toString() { // assertEmpty()
			return ""; // assertNotCovered()
		} // assertEmpty()

		@Override // assertEmpty()
		public int hashCode() { // assertEmpty()
			return 0; // assertNotCovered()
		} // assertEmpty()

		@Override // assertEmpty()
		public boolean equals(Object object) { // assertEmpty()
			return false; // assertNotCovered()
		} // assertEmpty()
	}

	public static void main(String[] args) {
		new WithoutFields();
		new WithFields(42);
		new WithCustomMethods(42);
	}

}
