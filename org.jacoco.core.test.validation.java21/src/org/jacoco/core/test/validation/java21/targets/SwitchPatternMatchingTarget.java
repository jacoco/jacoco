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

	/**
	 * <blockquote>
	 * <p>
	 * To align with pattern {@code switch} semantics, {@code switch}
	 * expressions over enum classes now throw {@code MatchException} rather
	 * than {@code IncompatibleClassChangeError} when no switch label applies at
	 * run time.
	 * </p>
	 * </blockquote>
	 */
	private static void handwrittenMatchException(Stubs.Enum e) {
		switch (e) { // assertFullyCovered(0, 3)
		case A -> // assertEmpty()
			nop("case A"); // assertFullyCovered()
		case B -> // assertEmpty()
			nop("case B"); // assertFullyCovered()
		default -> // assertEmpty()
			throw new MatchException(null, null); // assertFullyCovered()
		} // assertEmpty()
	}

	public static void main(String[] args) {
		example("");
		example("a");

		handwrittenMatchException(Stubs.Enum.A);
		handwrittenMatchException(Stubs.Enum.B);
		try {
			handwrittenMatchException(Stubs.Enum.C);
		} catch (MatchException ignore) {
		}
	}

}
