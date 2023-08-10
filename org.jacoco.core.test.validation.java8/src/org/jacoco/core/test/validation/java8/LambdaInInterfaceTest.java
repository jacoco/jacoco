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
