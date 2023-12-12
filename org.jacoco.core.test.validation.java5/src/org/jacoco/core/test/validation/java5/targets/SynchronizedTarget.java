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
import static org.jacoco.core.test.validation.targets.Stubs.nop;

import org.jacoco.core.test.validation.targets.Stubs.StubException;

/**
 * This test target is a synchronized statement.
 */
public class SynchronizedTarget {

	private static final Object lock = new Object();

	private static void normal() {
		nop(); // assertFullyCovered()
		/* when compiled with ECJ next line covered partly without filter: */
		synchronized (lock) { // assertFullyCovered()
			nop(); // assertFullyCovered()
		} // assertMonitorExit()
		nop(); // assertFullyCovered()
	}

	private static void explicitException() {
		synchronized (lock) { // assertFullyCovered()
			throw new StubException(); // assertFullyCovered()
		} // assertEmpty()
	}

	private static void implicitException() {
		synchronized (lock) { // assertMonitorEnterImplicitException()
			ex(); // assertNotCovered()
		} // assertMonitorExitImplicitException()
	}

	public static void main(String[] args) {
		normal();

		try {
			explicitException();
		} catch (StubException e) {
		}

		try {
			implicitException();
		} catch (StubException e) {
		}
	}

}
