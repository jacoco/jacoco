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

/**
 * This target exercises Record Patterns
 * (<a href="https://openjdk.org/jeps/440">JEP 440</a>).
 */
public class RecordPatternsTarget {

	private record Point(int x, int y) {
	}

	private static void instanceofOperator(Object o) {
		if (o instanceof Point(int x, int y)) { // assertInstanceof()
			nop(x + y); // assertFullyCovered()
		} // assertEmpty()
	}

	private static void switchStatement(Object p) {
		switch (p) { // assertFullyCovered(0, 2)
		case Point(int x, int y) -> nop(x + y); // assertSwitchStatementLastCase()
		default -> nop(); // assertFullyCovered()
		} // assertEmpty()
	}

	record R(Object component) {
	}

	private static String nestedRecordPattern(Object o) {
		return switch (o) { // assertJavacFully(0,4) assertEcjFully(0,7)
		case R(String c) -> // assertJavacPartly(1,2) assertEcjFully(0,2)
			"R(String)"; // assertFullyCovered()
		case R(Integer c) -> // assertJavacFully(0,0) assertEcjFully(0,2)
			"R(Integer)"; // assertFullyCovered()
		case R(R(String c)) -> // assertJavacFully(0,0) assertEcjPartly(1,3)
			"R(R(String))"; // assertFullyCovered()
		case R(R(Integer c)) -> // assertJavacFully(0,0) assertEcjPartly(2,2)
			"R(R(Integer))"; // assertFullyCovered()
		case String c -> // assertFullyCovered()
			"String"; // assertFullyCovered()
		case Integer i -> // assertFullyCovered()
			"Integer"; // assertFullyCovered()
		default -> // assertEmpty()
			"default"; // assertJavacPartly(1,3) assertEcjFully(0,0)
		}; // assertEmpty()
	}

	public static void main(String[] args) {
		instanceofOperator(new Point(1, 2));
		instanceofOperator(new Object());

		switchStatement(new Point(1, 2));
		switchStatement(new Object());

		for (Object o : new Object[] { //
				new R("String"), //
				new R(Integer.valueOf(42)), //
				new R(new R("String")), //
				new R(new R(Integer.valueOf(42))), //
				"String", //
				Integer.valueOf(42), //
				new Object(), //
		}) {
			nop(nestedRecordPattern(o));
		}
	}

}
