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
 *******************************************************************************/
package org.jacoco.core.test.validation;

import static org.jacoco.core.analysis.ILine.FULLY_COVERED;
import static org.jacoco.core.analysis.ILine.PARTLY_COVERED;

import org.jacoco.core.test.validation.targets.Target02;
import org.junit.Test;

/**
 * Tests of basic Java boolean expressions.
 * 
 * @author Marc R. Hoffmann
 * @version $qualified.bundle.version$
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
		assertLine("booleancmp1", PARTLY_COVERED, 1, 1);

		// 2. Boolean comparison result (both cases)
		assertLine("booleancmp2", FULLY_COVERED, 0, 2);

		// 3. And
		assertLine("andFF", FULLY_COVERED, 1, 1);
		assertLine("andFT", FULLY_COVERED, 1, 1);
		assertLine("andTF", FULLY_COVERED, 1, 1);
		assertLine("andTT", FULLY_COVERED, 1, 1);

		// 4. Conditional And
		assertLine("conditionalandFF", PARTLY_COVERED, 3, 1);
		assertLine("conditionalandFT", PARTLY_COVERED, 3, 1);
		assertLine("conditionalandTF", FULLY_COVERED, 2, 2);
		assertLine("conditionalandTT", FULLY_COVERED, 2, 2);

		// 5. Or
		assertLine("orFF", FULLY_COVERED, 1, 1);
		assertLine("orFT", FULLY_COVERED, 1, 1);
		assertLine("orTF", FULLY_COVERED, 1, 1);
		assertLine("orTT", FULLY_COVERED, 1, 1);

		// 6. Conditional Or
		assertLine("conditionalorFF", FULLY_COVERED, 2, 2);
		assertLine("conditionalorFT", FULLY_COVERED, 2, 2);
		assertLine("conditionalorTF", PARTLY_COVERED, 3, 1);
		assertLine("conditionalorTT", PARTLY_COVERED, 3, 1);

		// 7. Exclusive Or
		assertLine("xorFF", FULLY_COVERED, 1, 1);
		assertLine("xorFT", FULLY_COVERED, 1, 1);
		assertLine("xorTF", FULLY_COVERED, 1, 1);
		assertLine("xorTT", FULLY_COVERED, 1, 1);

		// 8. Conditional Operator
		assertLine("condT", PARTLY_COVERED, 1, 1);
		assertLine("condF", PARTLY_COVERED, 1, 1);

		// 9. Not (one case)
		assertLine("notT", PARTLY_COVERED, 1, 1);
		assertLine("notF", PARTLY_COVERED, 1, 1);

		// 10. Not (both cases)
		assertLine("notTF", FULLY_COVERED);

	}

}
