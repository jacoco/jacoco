/*******************************************************************************
 * Copyright (c) 2009, 2019 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
