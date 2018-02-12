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
package org.jacoco.core.test.filter.targets;

import static org.jacoco.core.test.validation.targets.Stubs.nop;

/**
 * This test target is an enum constructor.
 */
public class EnumConstructor {

	private enum ImplicitConstructor { // $line-implicitConstructor$
	}

	private enum ExplicitNonEmptyConstructor {
		;

		ExplicitNonEmptyConstructor() {
			nop(); // $line-explicitNonEmptyConstructor$
		}
	}

	@SuppressWarnings("unused")
	private enum ExplicitEmptyConstructor {
		;

		ExplicitEmptyConstructor() {
		} // $line-explicitEmptyConstructor$

		ExplicitEmptyConstructor(Object p) {
		} // $line-explicitEmptyConstructorWithParameter$
	}

	public static void main(String[] args) {
		ImplicitConstructor.values();
		ExplicitEmptyConstructor.values();
		ExplicitNonEmptyConstructor.values();
	}

}
