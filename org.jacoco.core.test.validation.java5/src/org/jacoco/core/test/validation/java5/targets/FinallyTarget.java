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
package org.jacoco.core.test.validation.java5.targets;

import static org.jacoco.core.test.validation.targets.Stubs.ex;
import static org.jacoco.core.test.validation.targets.Stubs.f;
import static org.jacoco.core.test.validation.targets.Stubs.nop;
import static org.jacoco.core.test.validation.targets.Stubs.t;

public class FinallyTarget {

	/**
	 * <pre>
	 *   InputStream in = null;
	 *   try {
	 *     in = ...;
	 *     ...
	 *   } finally {
	 *     if (in != null) {
	 *       in.close();
	 *     }
	 *   }
	 * </pre>
	 */
	private static void example(boolean t) {
		Object in = null;
		try {
			in = open(t);
		} finally { // assertFinally() tag("example.0")
			if (in != null) { // assertFullyCovered(0, 2)
				nop(); // assertFullyCovered() tag("example.2")
			} // assertEmpty()
		} // assertEmpty()
	}

	private static Object open(boolean t) {
		ex(t);
		return new Object();
	}

	/**
	 * GOTO instructions at the end of duplicates of finally block might have
	 * line number of a last instruction of finally block and hence lead to
	 * unexpected coverage results, like for example in case of ECJ for
	 * {@link FinallyTarget#catchNotExecuted()},
	 * {@link FinallyTarget#emptyCatch()}. So we decided to ignore them, even if
	 * they can correspond to a real break statement.
	 * <p>
	 * See also <a href=
	 * "https://bugs.openjdk.java.net/browse/JDK-8180141">JDK-8180141</a> and
	 * <a href=
	 * "https://bugs.openjdk.java.net/browse/JDK-7008643">JDK-7008643</a>.
	 */
	private static void breakStatement() {
		for (int i = 0; i < 1; i++) { // tag("breakStatement.for")
			try {
				if (f()) {
					break; // assertEmpty() tag("breakStatement")
				}
			} finally {
				nop("finally"); // assertFullyCovered() tag("breakStatement.1")
			} // assertEmpty() tag("breakStatement.2")
		}
	}

	private static void catchNotExecuted() {
		try {
			nop("try");
		} catch (Exception e) { // tag("catchNotExecuted")
			nop("catch"); // assertNotCovered()
		} finally { // assertEmpty()
			nop("finally"); // assertFullyCovered() tag("catchNotExecuted.1")
		} // assertEmpty() tag("catchNotExecuted.2")
	}

	private static void emptyCatch() {
		try {
			nop("try");
		} catch (Exception e) { // tag("emptyCatch")
			/* empty */
		} finally { // assertEmpty()
			nop("finally"); // assertFullyCovered() tag("emptyCatch.1")
		} // assertEmpty() tag("emptyCatch.2")
	}

	private static void twoRegions() {
		try {
			/* jump to another region associated with same handler: */
			if (t()) { // assertFullyCovered(1, 1)
				nop(); // assertFullyCovered()
				return; // assertTwoRegionsReturn1()
			} else {
				nop(); // assertNotCovered()
				return; // assertTwoRegionsReturn2()
			}
		} finally { // assertEmpty()
			nop(); // assertTwoRegions1()
		} // assertEmpty()
	}

	private static void nested() {
		try {
			nop();
		} finally { // assertFinally() tag("nested.0")
			try { // assertEmpty()
				nop(); // assertFullyCovered()
			} finally { // assertFinally() tag("nested.3")
				nop(); // assertFullyCovered()
			} // assertEmpty() tag("nested.5")
		} // assertEmpty() tag("nested.6")
	}

	private static void emptyTry() {
		try {
			/* empty */
		} finally { // assertEmpty()
			nop(); // assertEmptyTry1()
		} // assertEmptyTry2() tag("emptyTry.2")
	}

	@SuppressWarnings("finally")
	private static void alwaysCompletesAbruptly() {
		try {
			nop();
		} finally { // assertAlwaysCompletesAbruptly0()tag("alwaysCompletesAbruptly.0")
			return; // assertAlwaysCompletesAbruptly1()
		} // assertEmpty()
	}

	public static void main(String[] args) {
		example(false);
		try {
			example(true);
		} catch (Exception ignore) {
		}

		breakStatement();

		catchNotExecuted();

		emptyCatch();

		twoRegions();

		nested();

		emptyTry();

		alwaysCompletesAbruptly();
	}

}
