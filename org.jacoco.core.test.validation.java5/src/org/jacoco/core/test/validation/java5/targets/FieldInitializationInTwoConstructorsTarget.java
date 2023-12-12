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
package org.jacoco.core.test.validation.java5.targets;

/**
 * This test target has instance members with initialization in two
 * constructors.
 */
public class FieldInitializationInTwoConstructorsTarget {

	Object field1 = null; // assertPartlyCovered()

	int field2 = 123; // assertPartlyCovered()

	public FieldInitializationInTwoConstructorsTarget() {
	} // assertFullyCovered()

	public FieldInitializationInTwoConstructorsTarget(String arg) {
	} // assertNotCovered()

	public static void main(String[] args) {
		new FieldInitializationInTwoConstructorsTarget();
	}

}
