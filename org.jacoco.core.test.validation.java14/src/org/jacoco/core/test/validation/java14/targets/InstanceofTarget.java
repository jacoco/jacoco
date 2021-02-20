/*******************************************************************************
 * Copyright (c) 2009, 2021 Mountainminds GmbH & Co. KG and Contributors
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
package org.jacoco.core.test.validation.java14.targets;

import static org.jacoco.core.test.validation.targets.Stubs.nop;

/**
 * This target exercises pattern matching for instanceof (JEP 305).
 */
public class InstanceofTarget {

	private static void ifInstanceof(Object e) {
		if (e instanceof String s) { // assertInstanceof()
			nop(s);
		}
	}

	public static void main(String[] args) {
		ifInstanceof(new Object());
		ifInstanceof("string");
	}

}
