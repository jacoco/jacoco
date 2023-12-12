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

import static org.jacoco.core.test.validation.targets.Stubs.nop;

/**
 * This target uses synchronized blocks which compile to try/catch statements.
 */
public class StructuredLockingTarget {

	static void simple() {
		Object lock1 = new Object();
		synchronized (lock1) {
			nop();
		}
	}

	static void nested() {
		Object lock1 = new Object();
		synchronized (lock1) {
			nop();
			Object lock2 = new Object();
			synchronized (lock2) {
				nop();
			}
			nop();
		}

	}

	public static void main(String[] args) {
		simple();
		nested();
	}

}
