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
package org.jacoco.core.test.validation.java5.targets;

import static org.jacoco.core.test.validation.targets.Stubs.nop;

/**
 * This test target is an enum constructor.
 */
public class EnumConstructorTarget {

	/*
	 * Implicit constructor should be filtered. without filter next line is
	 * partly covered:
	 */
	private enum ImplicitConstructor { // assertFullyCovered()
	}

	/* Explicit non empty constructor should not be filtered: */
	private enum ExplicitNonEmptyConstructor {
		;

		ExplicitNonEmptyConstructor() {
			nop(); // assertNotCovered()
		}
	}

	/* Explicit empty constructor should be filtered: */
	private enum ExplicitEmptyConstructor {
		;

		ExplicitEmptyConstructor() {
			/* without filter next line is not covered: */
		} // assertEmpty()

		ExplicitEmptyConstructor(Object p) {
		} // assertNotCovered()
	}

	public static void main(String[] args) {
		ImplicitConstructor.values();
		ExplicitEmptyConstructor.values();
		ExplicitNonEmptyConstructor.values();
	}

}
