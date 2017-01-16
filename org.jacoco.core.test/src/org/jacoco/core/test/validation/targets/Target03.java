/*******************************************************************************
 * Copyright (c) 2009, 2017 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.core.test.validation.targets;

import static org.jacoco.core.test.validation.targets.Stubs.ex;
import static org.jacoco.core.test.validation.targets.Stubs.f;
import static org.jacoco.core.test.validation.targets.Stubs.nop;

import org.jacoco.core.test.validation.targets.Stubs.StubException;

/**
 * This target produces exception based control flow examples.
 */
public class Target03 {

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

	private static void implicitNullPointerException(int[] a) {
		nop(); // $line-implicitNullPointerException.before$
		a[0] = 0; // $line-implicitNullPointerException.exception$
		nop(); // $line-implicitNullPointerException.after$
	}

	private static void implicitException() {
		nop(); // $line-implicitException.before$
		ex(); // $line-implicitException.exception$
		nop(); // $line-implicitException.after$
	}

	private static void explicitException() {
		nop(); // $line-explicitException.before$
		throw new StubException(); // $line-explicitException.throw$
	}

	private static void noExceptionTryCatch() {
		nop(); // $line-noExceptionTryCatch.beforeBlock$
		try {
			nop(); // $line-noExceptionTryCatch.tryBlock$
		} catch (StubException e) { // $line-noExceptionTryCatch.catch$
			nop(); // $line-noExceptionTryCatch.catchBlock$
		} // $line-noExceptionTryCatch.catchBlockEnd$
	} // $line-noExceptionTryCatch.afterBlock$

	private static void implicitExceptionTryCatch() {
		nop(); // $line-implicitExceptionTryCatch.beforeBlock$
		try {
			nop(); // $line-implicitExceptionTryCatch.before$
			ex(); // $line-implicitExceptionTryCatch.exception$
			nop(); // $line-implicitExceptionTryCatch.after$
		} catch (StubException e) { // $line-implicitExceptionTryCatch.catch$
			nop(); // $line-implicitExceptionTryCatch.catchBlock$
		} // $line-implicitExceptionTryCatch.catchBlockEnd$
	} // $line-implicitExceptionTryCatch.afterBlock$

	private static void implicitExceptionTryCatchAfterCondition() {
		if (f()) { // $line-implicitExceptionTryCatchAfterCondition.condition$
			return;
		}
		try {
			ex(); // $line-implicitExceptionTryCatchAfterCondition.exception$
		} catch (StubException e) {
			nop(); // $line-implicitExceptionTryCatchAfterCondition.catchBlock$
		}
	}

	private static void explicitExceptionTryCatch() {
		nop(); // $line-explicitExceptionTryCatch.beforeBlock$
		try {
			nop(); // $line-explicitExceptionTryCatch.before$
			throw new StubException(); // $line-explicitExceptionTryCatch.throw$
		} catch (StubException e) { // $line-explicitExceptionTryCatch.catch$
			nop(); // $line-explicitExceptionTryCatch.catchBlock$
		} // $line-explicitExceptionTryCatch.catchBlockEnd$
	} // $line-explicitExceptionTryCatch.afterBlock$

	private static void noExceptionFinally() {
		nop(); // $line-noExceptionFinally.beforeBlock$
		try {
			nop(); // $line-noExceptionFinally.tryBlock$
		} finally { // $line-noExceptionFinally.finally$
			nop(); // $line-noExceptionFinally.finallyBlock$
		} // $line-noExceptionFinally.finallyBlockEnd$
	} // $line-noExceptionFinally.afterBlock$

	private static void implicitExceptionFinally() {
		nop(); // $line-implicitExceptionFinally.beforeBlock$
		try {
			nop(); // $line-implicitExceptionFinally.before$
			ex(); // $line-implicitExceptionFinally.exception$
			nop(); // $line-implicitExceptionFinally.after$
		} finally { // $line-implicitExceptionFinally.finally$
			nop(); // $line-implicitExceptionFinally.finallyBlock$
		} // $line-implicitExceptionFinally.finallyBlockEnd$
	} // $line-implicitExceptionFinally.afterBlock$

	private static void explicitExceptionFinally() {
		nop(); // $line-explicitExceptionFinally.beforeBlock$
		try {
			nop(); // $line-explicitExceptionFinally.before$
			throw new StubException(); // $line-explicitExceptionFinally.throw$
		} finally { // $line-explicitExceptionFinally.finally$
			nop(); // $line-explicitExceptionFinally.finallyBlock$
		} // $line-explicitExceptionFinally.finallyBlockEnd$
	} // $line-explicitExceptionFinally.afterBlock$

}
