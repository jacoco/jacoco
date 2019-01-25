/*******************************************************************************
 * Copyright (c) 2009, 2019 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.core.test.validation.java8.targets;

import static org.jacoco.core.test.validation.targets.Stubs.i1;

/**
 * This test target is an interface with a class initializer and default
 * methods.
 */
public interface InterfaceDefaultMethodsTarget {

	public static final int CONST = i1(); // assertFullyCovered()

	default void m1() {
		return; // assertFullyCovered()
	}

	default void m2() {
		return; // assertNotCovered()
	}

	public class Impl implements InterfaceDefaultMethodsTarget {

		public Impl() {
			m1();
		}
	}

	public static void main(String[] args) {
		new Impl();
	}

}
