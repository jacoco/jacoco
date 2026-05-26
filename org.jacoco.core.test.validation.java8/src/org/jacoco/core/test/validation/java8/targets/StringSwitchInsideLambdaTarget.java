/*******************************************************************************
 * Copyright (c) 2009, 2026 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.test.validation.java8.targets;

import static org.jacoco.core.test.validation.targets.Stubs.nop;

import java.util.Arrays;
import java.util.List;

/**
 * <code>switch</code> statement with selector expression of type
 * <code>String</code> inside lambda.
 */
public class StringSwitchInsideLambdaTarget {

	/**
	 * javac versions from 24 to 26 generate names "s0$" and "tmp1$" for
	 * compiler-generated temporary variables.
	 */
	private static void example(List<String> strings) {
		strings.forEach(s -> {

			switch (s) { // assertFullyCovered(0, 4)
			case "a": // assertEmpty()
				nop("a"); // assertFullyCovered()
				break; // assertFullyCovered()
			case "b": // assertEmpty()
				nop("b"); // assertFullyCovered()
				break; // assertFullyCovered()
			case "c": // assertEmpty()
				nop("c"); // assertFullyCovered()
				break; // assertFullyCovered()
			default: // assertEmpty()
				nop("default"); // assertFullyCovered()
				break; // assertEmpty()
			} // assertEmpty()

		});
	}

	public static void main(String[] args) {
		example(Arrays.asList("", "a", "b", "c"));
	}

}
