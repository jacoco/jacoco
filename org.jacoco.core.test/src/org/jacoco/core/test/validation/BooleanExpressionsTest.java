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
import org.jacoco.core.test.validation.targets.Target02;
import org.junit.Test;

/**
 * Tests of basic Java boolean expressions.
 */
public class BooleanExpressionsTest extends ValidationTestBase {

	public BooleanExpressionsTest() {
		super(Target02.class);
	}

	@Override
	protected void run(final Class<?> targetClass) throws Exception {
		final Object instance = targetClass.newInstance();
		((Runnable) instance).run();
	}

	@Test
	public void testCoverageResult() {

		// 1. Boolean comparison result (one case)
		assertLine("booleancmp1", ICounter.PARTLY_COVERED, 1, 1);

		// 2. Boolean comparison result (both cases)
		assertLine("booleancmp2", ICounter.FULLY_COVERED, 0, 2);

		// 3. And
		assertLine("andFF", ICounter.FULLY_COVERED, 1, 1);
		assertLine("andFT", ICounter.FULLY_COVERED, 1, 1);
		assertLine("andTF", ICounter.FULLY_COVERED, 1, 1);
		assertLine("andTT", ICounter.FULLY_COVERED, 1, 1);

		// 4. Conditional And
		assertLine("conditionalandFF", ICounter.PARTLY_COVERED, 3, 1);
		assertLine("conditionalandFT", ICounter.PARTLY_COVERED, 3, 1);
		assertLine("conditionalandTF", ICounter.FULLY_COVERED, 2, 2);
		assertLine("conditionalandTT", ICounter.FULLY_COVERED, 2, 2);

		// 5. Or
		assertLine("orFF", ICounter.FULLY_COVERED, 1, 1);
		assertLine("orFT", ICounter.FULLY_COVERED, 1, 1);
		assertLine("orTF", ICounter.FULLY_COVERED, 1, 1);
		assertLine("orTT", ICounter.FULLY_COVERED, 1, 1);

		// 6. Conditional Or
		assertLine("conditionalorFF", ICounter.FULLY_COVERED, 2, 2);
		assertLine("conditionalorFT", ICounter.FULLY_COVERED, 2, 2);
		assertLine("conditionalorTF", ICounter.PARTLY_COVERED, 3, 1);
		assertLine("conditionalorTT", ICounter.PARTLY_COVERED, 3, 1);

		// 7. Exclusive Or
		assertLine("xorFF", ICounter.FULLY_COVERED, 1, 1);
		assertLine("xorFT", ICounter.FULLY_COVERED, 1, 1);
		assertLine("xorTF", ICounter.FULLY_COVERED, 1, 1);
		assertLine("xorTT", ICounter.FULLY_COVERED, 1, 1);

		// 8. Conditional Operator
		assertLine("condT", ICounter.PARTLY_COVERED, 1, 1);
		assertLine("condF", ICounter.PARTLY_COVERED, 1, 1);

		// 9. Not (one case)
		assertLine("notT", ICounter.PARTLY_COVERED, 1, 1);
		assertLine("notF", ICounter.PARTLY_COVERED, 1, 1);

		// 10. Not (both cases)
		assertLine("notTF", ICounter.FULLY_COVERED, 0, 2);

	}

}
