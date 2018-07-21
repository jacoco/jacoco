/*******************************************************************************
 * Copyright (c) 2009, 2018 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.test.filter.targets;

import static org.jacoco.core.test.validation.targets.Stubs.ex;
import static org.jacoco.core.test.validation.targets.Stubs.f;
import static org.jacoco.core.test.validation.targets.Stubs.nop;
import static org.jacoco.core.test.validation.targets.Stubs.t;

public class Finally {

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
		} finally { // $line-example.0$
			if (in != null) { // $line-example.1$
				nop(); // $line-example.2$
			} // $line-example.3$
		} // $line-example.4$
	}

	private static Object open(boolean t) {
		ex(t);
		return new Object();
	}

	private static void breakStatement() {
		for (int i = 0; i < 1; i++) { // $line-breakStatement.for$
			try {
				if (f()) {
					break; // $line-breakStatement$
				}
			} finally {
				nop("finally"); // $line-breakStatement.1$
			} // $line-breakStatement.2$
		}
	}

	private static void catchNotExecuted() {
		try {
			nop("try");
		} catch (Exception e) { // $line-catchNotExecuted$
			nop("catch"); // $line-catchNotExecuted.catch$
		} finally { // $line-catchNotExecuted.0$
			nop("finally"); // $line-catchNotExecuted.1$
		} // $line-catchNotExecuted.2$
	}

	private static void emptyCatch() {
		try {
			nop("try");
		} catch (Exception e) { // $line-emptyCatch$
			// empty
		} finally { // $line-emptyCatch.0$
			nop("finally"); // $line-emptyCatch.1$
		} // $line-emptyCatch.2$
	}

	private static void twoRegions() {
		try {
			// jump to another region associated with same handler:
			if (t()) { // $line-twoRegions.if$
				nop(); // $line-twoRegions.region.1$
				return; // $line-twoRegions.return.1$
			} else {
				nop(); // $line-twoRegions.region.2$
				return; // $line-twoRegions.return.2$
			}
		} finally { // $line-twoRegions.0$
			nop(); // $line-twoRegions.1$
		} // $line-twoRegions.2$
	}

	private static void nested() {
		try {
			nop();
		} finally { // $line-nested.0$
			try { // $line-nested.1$
				nop(); // $line-nested.2$
			} finally { // $line-nested.3$
				nop(); // $line-nested.4$
			} // $line-nested.5$
		} // $line-nested.6$
	}

	private static void emptyTry() {
		try {
			// empty
		} finally { // $line-emptyTry.0$
			nop(); // $line-emptyTry.1$
		} // $line-emptyTry.2$
	}

	@SuppressWarnings("finally")
	private static void alwaysCompletesAbruptly() {
		try {
			nop();
		} finally { // $line-alwaysCompletesAbruptly.0$
			return; // $line-alwaysCompletesAbruptly.1$
		} // $line-alwaysCompletesAbruptly.2$
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
