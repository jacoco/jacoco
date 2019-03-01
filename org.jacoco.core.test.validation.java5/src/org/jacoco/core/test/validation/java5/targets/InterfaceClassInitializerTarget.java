/*******************************************************************************
 * Copyright (c) 2009, 2019 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.core.test.validation.java5.targets;

import static org.jacoco.core.test.validation.targets.Stubs.i1;

/**
 * This test target is an interface with a class initializer.
 */
public interface InterfaceClassInitializerTarget {

	/* No code required to initialize these fields: */

	static final int CONST1 = 12345; // assertEmpty()

	static final String CONST2 = "const"; // assertEmpty()

	/* These fields are initialized within <clinit> */

	static final int CONST3 = i1(); // assertFullyCovered()

	static final Object CONST4 = new Object(); // assertFullyCovered()

}
