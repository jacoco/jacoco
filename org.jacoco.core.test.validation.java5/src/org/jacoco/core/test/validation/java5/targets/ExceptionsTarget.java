/*******************************************************************************
 * Copyright (c) 2009, 2023 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.test.validation.java5.targets;

import static org.jacoco.core.test.validation.targets.Stubs.ex;
import static org.jacoco.core.test.validation.targets.Stubs.f;
import static org.jacoco.core.test.validation.targets.Stubs.nop;

import org.jacoco.core.test.validation.targets.Stubs.StubException;

/**
 * This target produces exception based control flow examples.
 */
public class ExceptionsTarget {

	public static void main(String[] args) {

		try {
			implicitNullPointerException(null);
		} catch (NullPointerException e) {
		}
		try {
			implicitException();
		} catch (StubException e) {
		}
		try {
			explicitException();
		} catch (StubException e) {
		}
		noExceptionTryCatch();
		implicitExceptionTryCatch();
		implicitExceptionTryCatchAfterCondition();
		explicitExceptionTryCatch();
		noExceptionFinally();
		try {
			explicitExceptionFinally();
		} catch (StubException e) {
		}
		try {
			implicitExceptionFinally();
		} catch (StubException e) {
		}
	}

	/**
	 * Currently no coverage at all, as we don't see when a block aborts
	 * somewhere in the middle.
	 */
	private static void implicitNullPointerException(int[] a) {
		nop(); // assertNotCovered()
		a[0] = 0; // assertNotCovered()
		nop(); // assertNotCovered()
	}

	/**
	 * For each line with method invocations a extra probe is inserted.
	 * Therefore the lines before the exception are marked as covered.
	 */
	private static void implicitException() {
		nop(); // assertFullyCovered()
		ex(); // assertNotCovered()
		nop(); // assertNotCovered()
	}

	private static void explicitException() {
		nop(); // assertFullyCovered()
		throw new StubException(); // assertFullyCovered()
	}

	private static void noExceptionTryCatch() {
		nop(); // assertFullyCovered()
		try {
			nop(); // assertFullyCovered()
		} catch (StubException e) { // assertCatchNoException()
			nop(); // assertNotCovered()
		} // assertCatchBlockEndNoException()
	} // assertFullyCovered()

	private static void implicitExceptionTryCatch() {
		nop(); // assertFullyCovered()
		try {
			nop(); // assertFullyCovered()
			ex(); // assertNotCovered()
			nop(); // assertNotCovered()
		} catch (StubException e) { // assertCatchImplicitException()
			nop(); // assertFullyCovered()
		} // assertCatchBlockEndImplicitException()
	} // assertFullyCovered()

	/**
	 * As the try/catch block is entered at one branch of the condition should
	 * be marked as executed
	 */
	private static void implicitExceptionTryCatchAfterCondition() {
		if (f()) { // assertFullyCovered(1, 1)
			return;
		}
		try {
			ex(); // assertNotCovered()
		} catch (StubException e) {
			nop(); // assertFullyCovered()
		}
	}

	private static void explicitExceptionTryCatch() {
		nop(); // assertFullyCovered()
		try {
			nop(); // assertFullyCovered()
			throw new StubException(); // assertFullyCovered()
		} catch (StubException e) { // assertFullyCovered()
			nop(); // assertFullyCovered()
		} // assertEmpty()
	} // assertFullyCovered()

	private static void noExceptionFinally() {
		nop(); // assertFullyCovered()
		try {
			nop(); // assertFullyCovered()
		} finally { // assertFinally()
			nop(); // assertFullyCovered()
		} // assertEmpty()
	} // assertFullyCovered()

	private static void implicitExceptionFinally() {
		nop(); // assertFullyCovered()
		try {
			nop(); // assertFullyCovered()
			ex(); // assertNotCovered()
			nop(); // assertNotCovered()
		} finally { // assertFinallyImplicitException()
			nop(); // assertFullyCovered()
		} // assertEmpty()
	} // assertNotCovered()

	private static void explicitExceptionFinally() {
		nop(); // assertFullyCovered()
		try {
			nop(); // assertFullyCovered()
			throw new StubException(); // assertFullyCovered()
		} finally { // assertFinally()
			nop(); // assertFullyCovered()
		} // assertBlockEndImplicitException()
	} // assertEmpty()

}
