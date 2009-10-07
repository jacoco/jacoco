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

import static org.jacoco.core.test.targets.Stubs.f;
import static org.jacoco.core.test.targets.Stubs.t;

/**
 * Blocks finished before the super constructor is called.
 * 
 * CHANGING LINE NUMBERS WILL BREAK TESTS!
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public class Target_init_05 extends Stubs.Base {

	public Target_init_05() {
		super(t() ? t() : f()); // ........ 29:
	} // .......................................... 30:

}
