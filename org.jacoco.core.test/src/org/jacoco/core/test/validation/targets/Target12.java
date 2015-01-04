/*******************************************************************************
 * Copyright (c) 2009, 2015 Mountainminds GmbH & Co. KG and Contributors
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

import static org.jacoco.core.test.validation.targets.Stubs.nop;

/**
 * This target uses synchronized blocks which compile to try/catch statements.
 */
public class Target12 implements Runnable {

	public void run() {
		simple();
		nested();
	}

	void simple() {
		synchronized (this) {
			nop();
		}
	}

	void nested() {
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
		new Target12().run();
	}

}
