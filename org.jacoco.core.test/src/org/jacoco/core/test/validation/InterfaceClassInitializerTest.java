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
package org.jacoco.core.test.validation;

import org.jacoco.core.analysis.ICounter;
import org.jacoco.core.test.validation.targets.Target04;
import org.junit.Test;

/**
 * Tests of static initializer in interfaces.
 */
public class InterfaceClassInitializerTest extends ValidationTestBase {

	public InterfaceClassInitializerTest() {
		super(Target04.class);
	}

	@Override
	protected void run(final Class<?> targetClass) throws Exception {
		// Force class initialization
		targetClass.getField("CONST1").get(null);
	}

	@Test
	public void testCoverageResult() {

		assertLine("const1", ICounter.EMPTY);
		assertLine("const2", ICounter.EMPTY);

		assertLine("const3", ICounter.FULLY_COVERED);
		assertLine("const4", ICounter.FULLY_COVERED);
	}

}
