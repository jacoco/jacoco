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
import static org.jacoco.core.test.validation.targets.Stubs.nop;

import org.jacoco.core.test.validation.targets.Stubs.StubException;

/**
 * This test target is a synchronized statement.
 */
public class Synchronized {

	private static final Object lock = new Object();

	private static void normal() {
		nop(); // $line-before$
		synchronized (lock) { // $line-monitorEnter$
			nop(); // $line-body$
		} // $line-monitorExit$
		nop(); // $line-after$
	}

	private static void explicitException() {
		synchronized (lock) { // $line-explicitException.monitorEnter$
			throw new StubException(); // $line-explicitException.exception$
		} // $line-explicitException.monitorExit$
	}

	private static void implicitException() {
		synchronized (lock) { // $line-implicitException.monitorEnter$
			ex(); // $line-implicitException.exception$
		} // $line-implicitException.monitorExit$
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
