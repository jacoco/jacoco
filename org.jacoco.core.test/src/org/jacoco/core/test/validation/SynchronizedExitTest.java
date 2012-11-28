/*******************************************************************************
 * Copyright (c) 2009, 2012 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Martin Hare Robertson - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.core.test.validation;

import java.lang.reflect.Method;

import org.jacoco.core.analysis.ICounter;
import org.jacoco.core.test.validation.targets.Target12;
import org.junit.Test;

public class SynchronizedExitTest extends ValidationTestBase {

	public SynchronizedExitTest() {
		super(Target12.class);
	}

	@Override
	protected void run(final Class<?> targetClass) throws Exception {
		// Load one of the enum constants
		Method mainMethod = targetClass.getMethod("main", String[].class);
		mainMethod.invoke(Target12.class, new Object[1]);
	}

	@Test
	public void testCoverageResult() {

		assertLine("sync", ICounter.FULLY_COVERED);

	}

}
