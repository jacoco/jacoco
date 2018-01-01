/*******************************************************************************
 * Copyright (c) 2009, 2018 Mountainminds GmbH & Co. KG and Contributors
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
import org.jacoco.core.test.validation.targets.LambdaExpressionsTarget;
import org.junit.Test;

/**
 * Tests for different lambda expressions.
 */
public class LambdaExpressionsTest extends ValidationTestBase {

	public LambdaExpressionsTest() {
		super("src-java8", LambdaExpressionsTarget.class);
	}

	@Test
	public void testCoverageResult() {

		// Coverage of lambda bodies
		assertLine("executedlambdabody", ICounter.FULLY_COVERED);
		assertLine("notexecutedlambdabody", ICounter.NOT_COVERED);

	}

}
