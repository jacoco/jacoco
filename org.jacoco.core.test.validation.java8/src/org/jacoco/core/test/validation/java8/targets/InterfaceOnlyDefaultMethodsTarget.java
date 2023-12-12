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
