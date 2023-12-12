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
package org.jacoco.core.test.validation.java16.targets;

import static org.jacoco.core.test.validation.targets.Stubs.nop;

/**
 * This target exercises pattern matching for instanceof
 * (<a href="https://openjdk.java.net/jeps/394">JEP 394</a>).
 */
public class InstanceofTarget {

	private static void ifInstanceof(Object e) {
		if (e instanceof String s) { // assertFullyCovered(0, 2)
			nop(s);
		}
	}

	public static void main(String[] args) {
		ifInstanceof(new Object());
		ifInstanceof("string");
	}

}
