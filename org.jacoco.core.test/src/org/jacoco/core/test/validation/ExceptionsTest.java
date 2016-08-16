/*******************************************************************************
 * Copyright (c) 2009, 2016 Mountainminds GmbH & Co. KG and Contributors
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
import org.jacoco.core.test.validation.targets.Target03;
import org.junit.Test;

/**
 * Tests of exception based control flow.
 */
public class ExceptionsTest extends ValidationTestBase {

	public ExceptionsTest() {
		super(Target03.class);
	}

	@Override
	protected void run(final Class<?> targetClass) throws Exception {
		final Object instance = targetClass.newInstance();
		((Runnable) instance).run();
	}

	@Test
	public void testCoverageResult() {

		// 1. Implicit Exception
		// Currently no coverage at all, as we don't see when a block aborts
		// somewhere in the middle.
		assertLine("implicitException.before", ICounter.FULLY_COVERED);
		assertLine("implicitException.exception", ICounter.NOT_COVERED);
		assertLine("implicitException.after", ICounter.NOT_COVERED);

		// 2. Explicit Exception
		// Full coverage, as we recognize throw statements as block boundaries.
		assertLine("explicitException.before", ICounter.FULLY_COVERED);
		assertLine("explicitException.throw", ICounter.FULLY_COVERED);

		// 3. Try/Catch Block Without Exception Thrown
		assertLine("noExceptionTryCatch.beforeBlock", ICounter.FULLY_COVERED);
		assertLine("noExceptionTryCatch.tryBlock", ICounter.FULLY_COVERED);
		assertLine("noExceptionTryCatch.catchBlock", ICounter.NOT_COVERED);

		// 4. Try/Catch Block With Exception Thrown Implicitly
		// As always with implicit exceptions we don't see when a block aborts
		// somewhere in the middle.
		assertLine("implicitExceptionTryCatch.beforeBlock",
				ICounter.FULLY_COVERED);
		assertLine("implicitExceptionTryCatch.before", ICounter.FULLY_COVERED);
		assertLine("implicitExceptionTryCatch.exception", ICounter.NOT_COVERED);
		assertLine("implicitExceptionTryCatch.after", ICounter.NOT_COVERED);
		assertLine("implicitExceptionTryCatch.catchBlock",
				ICounter.FULLY_COVERED);

		// 5. Try/Catch Block With Exception Thrown Implicitly After Condition
		// As the try/catch block is entered at one branch of the condition
		// should be marked as executed
		assertLine("implicitExceptionTryCatchAfterCondition.condition",
				ICounter.FULLY_COVERED, 1, 1);
		assertLine("implicitExceptionTryCatchAfterCondition.exception",
				ICounter.NOT_COVERED);
		assertLine("implicitExceptionTryCatchAfterCondition.catchBlock",
				ICounter.FULLY_COVERED);

		// 6. Try/Catch Block With Exception Thrown Explicitly
		assertLine("explicitExceptionTryCatch.beforeBlock",
				ICounter.FULLY_COVERED);
		assertLine("explicitExceptionTryCatch.before", ICounter.FULLY_COVERED);
		assertLine("explicitExceptionTryCatch.throw", ICounter.FULLY_COVERED);
		assertLine("explicitExceptionTryCatch.catchBlock",
				ICounter.FULLY_COVERED);

		// 7. Finally Block Without Exception Thrown
		// Finally block is yellow as the exception path is missing.
		assertLine("noExceptionFinally.beforeBlock", ICounter.FULLY_COVERED);
		assertLine("noExceptionFinally.tryBlock", ICounter.FULLY_COVERED);
		assertLine("noExceptionFinally.finallyBlock", ICounter.PARTLY_COVERED);

		// 8. Finally Block With Implicit Exception
		// Finally block is yellow as the non-exception path is missing.
		assertLine("implicitExceptionFinally.beforeBlock",
				ICounter.FULLY_COVERED);
		assertLine("implicitExceptionFinally.before", ICounter.FULLY_COVERED);
		assertLine("implicitExceptionFinally.exception", ICounter.NOT_COVERED);
		assertLine("implicitExceptionFinally.after", ICounter.NOT_COVERED);
		assertLine("implicitExceptionFinally.finallyBlock",
				ICounter.PARTLY_COVERED);

		// 9. Finally Block With Exception Thrown Explicitly
		assertLine("explicitExceptionFinally.beforeBlock",
				ICounter.FULLY_COVERED);
		assertLine("explicitExceptionFinally.before", ICounter.FULLY_COVERED);
		assertLine("explicitExceptionFinally.throw", ICounter.FULLY_COVERED);
		assertLine("explicitExceptionFinally.finallyBlock",
				ICounter.FULLY_COVERED);

		// TODO(Godin): why fully covered? it looks strange, but the same in
		// already presented cases above:
		assertLine("implicitExceptionAfterBranch.if", ICounter.FULLY_COVERED, 1,
				1);
		assertLine("implicitExceptionAfterBranch.ifbody", ICounter.NOT_COVERED);
		assertLine("implicitExceptionAfterBranch.exception",
				ICounter.NOT_COVERED);
	}

}
