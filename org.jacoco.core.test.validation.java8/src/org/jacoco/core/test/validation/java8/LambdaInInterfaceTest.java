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
package org.jacoco.core.test.validation.java8;

import org.jacoco.core.test.validation.ValidationTestBase;
import org.jacoco.core.test.validation.java8.targets.LambdaInInterfaceTarget;

/**
 * Tests a constant with a lambda value in an interface.
 */
public class LambdaInInterfaceTest extends ValidationTestBase {

	public LambdaInInterfaceTest() {
		super(LambdaInInterfaceTarget.class);
	}

	@Override
	protected void run(final Class<?> targetClass) throws Exception {
		((Runnable) targetClass.getField("RUN").get(null)).run();
	}

}
