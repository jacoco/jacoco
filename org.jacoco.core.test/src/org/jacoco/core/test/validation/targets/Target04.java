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

import static org.jacoco.core.test.validation.targets.Stubs.i1;

/**
 * This test target is an interface with a class initializer.
 */
public interface Target04 {

	// No code required to initialize these fields:

	static final int CONST1 = 12345; // $line-const1$

	static final String CONST2 = "const"; // $line-const2$

	// These fields are initialized within <clinit>

	static final int CONST3 = i1(); // $line-const3$

	static final Object CONST4 = new Object(); // $line-const4$

}
