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
package org.jacoco.core.test.validation.java21.targets;

import static org.jacoco.core.test.validation.targets.Stubs.nop;

import org.jacoco.core.test.validation.targets.Stubs;

/**
 * This target exercises pattern matching for switch
 * (<a href="https://openjdk.org/jeps/441">JEP 441</a>).
 */
public class SwitchPatternMatchingTarget {

	private static void guard(Object o) {
		switch (o) { // assertFullyCovered(1, 2)
		case String s when s.length() == 0 -> // assertFullyCovered(0, 2)
			nop(s); // assertFullyCovered()
		case String s -> // assertFullyCovered()
			nop(s); // assertFullyCovered()
		default -> // assertEmpty()
			nop(); // assertNotCovered()
		}
	}

	private static void nullCase(Object o) {
		switch (o) { // assertFullyCovered(0, 3)
		case String s -> // assertFullyCovered()
			nop(s); // assertFullyCovered()
		case null -> // assertEmpty()
			nop("null"); // assertFullyCovered()
		default -> // assertEmpty()
			nop("default"); // assertFullyCovered()
		} // assertEmpty()
	}

	private static void nullDefault(Object o) {
		switch (o) { // assertFullyCovered(0, 2)
		case String s -> // assertFullyCovered()
			nop(s); // assertFullyCovered()
		case null, default -> // assertEmpty()
			nop("default"); // assertFullyCovered()
		} // assertEmpty()
	}

	private static void enumSwitch(Stubs.Enum e) {
		switch (e) { // assertFullyCovered(0, 3)
		case null -> // assertEmpty()
			nop("null"); // assertFullyCovered()
		case A, C -> // assertEmpty()
			nop("A, C"); // assertFullyCovered()
		case B -> // assertEmpty()
			nop("B"); // assertFullyCovered()
		} // assertEmpty()
	}

	private static void stringSwitch(String s) {
		switch (s) { // assertFullyCovered(3, 1)
		case null -> // assertEmpty()
			nop("null"); // assertNotCovered()
		case "a", "c" -> // assertEmpty()
			nop("a, c"); // assertFullyCovered()
		case "b" -> // assertEmpty()
			nop("b"); // assertNotCovered()
		default -> // assertEmpty()
			nop("default"); // assertNotCovered()
		} // assertEmpty()
	}

	public static void main(String[] args) {
		guard("");
		guard("a");

		nullCase(null);
		nullCase("");
		nullCase(new Object());

		nullDefault(null);
		nullDefault("");

		enumSwitch(null);
		enumSwitch(Stubs.Enum.A);
		enumSwitch(Stubs.Enum.B);

		stringSwitch("a");
	}

}
