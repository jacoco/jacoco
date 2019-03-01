/*******************************************************************************
 * Copyright (c) 2009, 2019 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.test.validation.java8.targets;

/**
 * This test target is an interface with only default methods.
 */
public interface InterfaceOnlyDefaultMethodsTarget {

	/* no <clinit>, only default methods: */

	default void m1() {
		return; // assertFullyCovered()
	}

	default void m2() {
		return; // assertNotCovered()
	}

	public class Impl implements InterfaceOnlyDefaultMethodsTarget {

		public Impl() {
			m1();
		}
	}

	public static void main(String[] args) {
		new Impl();
	}

}
