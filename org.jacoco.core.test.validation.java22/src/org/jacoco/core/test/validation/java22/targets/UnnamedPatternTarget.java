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
package org.jacoco.core.test.validation.java22.targets;

import static org.jacoco.core.test.validation.targets.Stubs.nop;

/**
 * <a href="https://openjdk.org/jeps/456">JEP 456</a>
 */
public class UnnamedPatternTarget {

	record R(Object c) {
	}

	private static String multiplePatternsInCase(Object o) {
		return switch (o) { // assertFullyCovered(0,2)
		case R(Float _), R(Double _) -> // assertJavacEmpty() assertEcjPartly(6,2)
			"case"; // assertFullyCovered()
		default -> // assertEmpty()
			"default"; // assertFullyCovered()
		}; // assertEmpty()
	}

	private static String multiplePatternsInCaseWithGuard(Object o, int x) {
		return switch (o) { // assertFullyCovered(0,2)
		case R(Float _), R(Double _) when x > 0 -> // assertJavacFully(1,1) assertEcjPartly(7,3)
			"case"; // assertFullyCovered()
		default -> // assertEmpty()
			"default"; // assertFullyCovered()
		}; // assertEmpty()
	}

	record RP(int c) {
	}

	private static String rp(Object o) {
		return switch (o) {
		case RP(int x) -> //
			"case";
		default -> //
			"default";
		};
	}

	public static void main(String[] args) {
		nop(multiplePatternsInCase(new R(1F)));
		nop(multiplePatternsInCase(""));

		nop(multiplePatternsInCaseWithGuard(new R(1F), 1));
		nop(multiplePatternsInCaseWithGuard("", 2));
	}

}
