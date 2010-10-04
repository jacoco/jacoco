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

import static org.jacoco.core.analysis.ILines.FULLY_COVERED;
import static org.jacoco.core.analysis.ILines.PARTLY_COVERED;

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
		assertLine("booleancmp1", PARTLY_COVERED);

		// 2. Boolean comparison result (both cases)
		assertLine("booleancmp2", FULLY_COVERED);

		// 3. And
		assertLine("andFF", FULLY_COVERED);
		assertLine("andFT", FULLY_COVERED);
		assertLine("andTF", FULLY_COVERED);
		assertLine("andTT", FULLY_COVERED);

		// 4. Conditional And
		assertLine("conditionalandFF", PARTLY_COVERED);
		assertLine("conditionalandFT", PARTLY_COVERED);
		assertLine("conditionalandTF", FULLY_COVERED);
		assertLine("conditionalandTT", FULLY_COVERED);

		// 5. Or
		assertLine("orFF", FULLY_COVERED);
		assertLine("orFT", FULLY_COVERED);
		assertLine("orTF", FULLY_COVERED);
		assertLine("orTT", FULLY_COVERED);

		// 6. Conditional Or
		assertLine("conditionalorFF", FULLY_COVERED);
		assertLine("conditionalorFT", FULLY_COVERED);
		assertLine("conditionalorTF", PARTLY_COVERED);
		assertLine("conditionalorTT", PARTLY_COVERED);

		// 7. Exclusive Or
		assertLine("xorFF", FULLY_COVERED);
		assertLine("xorFT", FULLY_COVERED);
		assertLine("xorTF", FULLY_COVERED);
		assertLine("xorTT", FULLY_COVERED);

		// 8. Conditional Operator
		assertLine("condT", PARTLY_COVERED);
		assertLine("condF", PARTLY_COVERED);

		// 9. Not (one case)
		assertLine("notT", PARTLY_COVERED);
		assertLine("notF", PARTLY_COVERED);

		// 10. Not (both cases)
		assertLine("notTF", FULLY_COVERED);

	}

}
