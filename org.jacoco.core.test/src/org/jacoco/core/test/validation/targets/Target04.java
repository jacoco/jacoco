/*******************************************************************************
 * Copyright (c) 2009, 2010 Mountainminds GmbH & Co. KG and Contributors
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
package org.jacoco.core.test.validation.targets;

import static org.jacoco.core.test.validation.targets.Stubs.i1;
import static org.jacoco.core.test.validation.targets.Stubs.i2;

/**
 * This test target is an interface with a class initializer.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public interface Target04 {

	public static final int CONST1 = i1(); // $line-const1$

	public static final int CONST2 = i2(); // $line-const2$

	public static final Object CONST3 = new Object(); // $line-const3$

}
