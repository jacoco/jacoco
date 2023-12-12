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
package org.jacoco.core.test.validation.java21.targets;

import static org.jacoco.core.test.validation.targets.Stubs.nop;

/**
 * This target exercises pattern matching for switch
 * (<a href="https://openjdk.org/jeps/441">JEP 441</a>).
 */
public class SwitchPatternMatchingTarget {

	private static void example(Object o) {
		switch (o) { // assertFullyCovered(1, 2)
		case String s when s.length() == 0 -> // assertFullyCovered(0, 2)
			nop(s); // assertFullyCovered()
		case String s -> // assertFullyCovered()
			nop(s); // assertFullyCovered()
		default -> // assertEmpty()
			nop(); // assertNotCovered()
		}
	}

	public static void main(String[] args) {
		example("");
		example("a");
	}

}
