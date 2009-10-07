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

import static org.jacoco.core.test.targets.Stubs.nop;
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
		if (t()) { // ........... 31
			nop(); // ................ 32
		} else {
			nop(); // ................ 34
		}
	} // ............................ 36

}
