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
package org.jacoco.core.test.validation.java8;

import org.jacoco.core.analysis.ICounter;
import org.jacoco.core.test.validation.ValidationTestBase;
import org.junit.Test;

/**
 * Tests of basic Java control structures.
 */
public class LambdaExpressionsTest extends ValidationTestBase {

	public LambdaExpressionsTest() {
		super(LambdaExpressionsTarget.class);
	}

	@Override
	protected void run(final Class<?> targetClass) throws Exception {
		final Object instance = targetClass.newInstance();
		((Runnable) instance).run();
	}

	@Test
	public void testCoverageResult() {

		// Coverage of lambda bodies
		assertLine("executedlambdabody", ICounter.FULLY_COVERED);
		assertLine("notexecutedlambdabody", ICounter.NOT_COVERED);

	}

}
