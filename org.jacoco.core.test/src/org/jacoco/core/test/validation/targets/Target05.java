/*******************************************************************************
 * Copyright (c) 2009, 2015 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.core.test.validation.targets;

import static org.jacoco.core.test.validation.targets.Stubs.i1;

/**
 * This test target is a class with a static initializer.
 */
public class Target05 { // $line-classdef$

	// No code required to initialize these fields:

	public static final int CONST1 = 3; // $line-const1$

	public static final String CONST2 = "Hello"; // $line-const2$

	// These fields are initialized within <clinit>

	public static final int CONST3 = i1(); // $line-const3$

	public static final Object CONST4 = new Object(); // $line-const4$

	public static int field1 = 3; // $line-field1$

	public static String field2 = "Hello"; // $line-field2$

	public static int field3 = i1(); // $line-field3$

	public static Object field4 = new Object(); // $line-field4$

	static {
		Stubs.nop(); // $line-staticblock$
	}

	private Target05() {
	}

	public static void main(String[] args) {
	}

}
