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

import static org.jacoco.core.test.validation.targets.Stubs.f;
import static org.jacoco.core.test.validation.targets.Stubs.nop;

/**
 * This test target needs an explicit initial frame as the first instruction
 * already is a jump target.
 */
public class Target11 implements Runnable {

	public void run() {
		do {
			nop(); // $line-dowhilebody$
		} while (f());
	}

	public static void main(String[] args) {
		new Target11().run();
	}

}
