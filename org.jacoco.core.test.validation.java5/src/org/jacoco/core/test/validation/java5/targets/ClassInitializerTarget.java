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

import static org.jacoco.core.test.validation.targets.Stubs.i1;

import org.jacoco.core.test.validation.targets.Stubs;

/**
 * This test target is a class with a static initializer.
 */
public class ClassInitializerTarget {

	/* No code required to initialize these fields: */

	public static final int CONST1 = 3; // assertEmpty()

	public static final String CONST2 = "Hello"; // assertEmpty()

	/* These fields are initialized within <clinit> */

	public static final int CONST3 = i1(); // assertFullyCovered()

	public static final Object CONST4 = new Object(); // assertFullyCovered()

	public static int field1 = 3; // assertFullyCovered()

	public static String field2 = "Hello"; // assertFullyCovered()

	public static int field3 = i1(); // assertFullyCovered()

	public static Object field4 = new Object(); // assertFullyCovered()

	static {
		Stubs.nop(); // assertFullyCovered()
	}

	private ClassInitializerTarget() {
	}

	public static void main(String[] args) {
	}

}
