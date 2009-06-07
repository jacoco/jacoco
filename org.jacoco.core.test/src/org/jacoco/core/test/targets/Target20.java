/*******************************************************************************
 * Copyright (c) 2009 Mountainminds GmbH & Co. KG and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 * $Id: $
 *******************************************************************************/
package org.jacoco.core.test.targets;

import static org.jacoco.core.test.targets.Stubs.m1;
import static org.jacoco.core.test.targets.Stubs.m2;
import static org.jacoco.core.test.targets.Stubs.m3;
import static org.jacoco.core.test.targets.Stubs.t;

/**
 * Empty class.
 * 
 * CHANGING LINE NUMBERS WILL BREAK TESTS!
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public class Target20 implements Runnable {

	public void run() {
		if (m1(t())) { // ........... 31
			m2(); // ................ 32
		} else {
			m3(); // ................ 34
		}
	} // ............................ 36

}
