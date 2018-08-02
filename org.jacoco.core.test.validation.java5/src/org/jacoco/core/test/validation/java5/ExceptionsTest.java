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
package org.jacoco.core.test.validation.java5;

import org.jacoco.core.analysis.ICounter;
import org.jacoco.core.test.validation.ValidationTestBase;
import org.jacoco.core.test.validation.java5.targets.ExceptionsTarget;
import org.junit.Test;

/**
 * Tests of exception based control flow.
 */
public class ExceptionsTest extends ValidationTestBase {

	public ExceptionsTest() {
		super(ExceptionsTarget.class);
	}

	@Test
	public void testCoverageResult() {

		// 0. Implicit NullPointerException
		// Currently no coverage at all, as we don't see when a block aborts
		// somewhere in the middle.
		assertLine("implicitNullPointerException.before", ICounter.NOT_COVERED);
		assertLine("implicitNullPointerException.exception",
				ICounter.NOT_COVERED);
		assertLine("implicitNullPointerException.after", ICounter.NOT_COVERED);

		// 1. Implicit Exception
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
		assertLine("noExceptionTryCatch.catch",
				isJDKCompiler ? ICounter.NOT_COVERED : ICounter.PARTLY_COVERED);
		assertLine("noExceptionTryCatch.catchBlock", ICounter.NOT_COVERED);
		assertLine("noExceptionTryCatch.catchBlockEnd",
				isJDKCompiler ? ICounter.FULLY_COVERED : ICounter.EMPTY);
		assertLine("noExceptionTryCatch.afterBlock", ICounter.FULLY_COVERED);

		// 4. Try/Catch Block With Exception Thrown Implicitly
		assertLine("implicitExceptionTryCatch.beforeBlock",
				ICounter.FULLY_COVERED);
		assertLine("implicitExceptionTryCatch.before", ICounter.FULLY_COVERED);
		assertLine("implicitExceptionTryCatch.exception", ICounter.NOT_COVERED);
		assertLine("implicitExceptionTryCatch.after", ICounter.NOT_COVERED);
		assertLine("implicitExceptionTryCatch.catch", isJDKCompiler
				? ICounter.FULLY_COVERED : ICounter.PARTLY_COVERED);
		assertLine("implicitExceptionTryCatch.catchBlock",
				ICounter.FULLY_COVERED);
		assertLine("implicitExceptionTryCatch.catchBlockEnd",
				isJDKCompiler ? ICounter.NOT_COVERED : ICounter.EMPTY);
		assertLine("implicitExceptionTryCatch.afterBlock",
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
		assertLine("explicitExceptionTryCatch.catch", ICounter.FULLY_COVERED);
		assertLine("explicitExceptionTryCatch.catchBlock",
				ICounter.FULLY_COVERED);
		assertLine("explicitExceptionTryCatch.catchBlockEnd", ICounter.EMPTY);
		assertLine("explicitExceptionTryCatch.afterBlock",
				ICounter.FULLY_COVERED);

		// 7. Finally Block Without Exception Thrown
		assertLine("noExceptionFinally.beforeBlock", ICounter.FULLY_COVERED);
		assertLine("noExceptionFinally.tryBlock", ICounter.FULLY_COVERED);
		assertLine("noExceptionFinally.finally",
				isJDKCompiler ? ICounter.EMPTY : ICounter.FULLY_COVERED);
		assertLine("noExceptionFinally.finallyBlock", ICounter.FULLY_COVERED);
		assertLine("noExceptionFinally.finallyBlockEnd", ICounter.EMPTY);
		assertLine("noExceptionFinally.afterBlock", ICounter.FULLY_COVERED);

		// 8. Finally Block With Implicit Exception
		assertLine("implicitExceptionFinally.beforeBlock",
				ICounter.FULLY_COVERED);
		assertLine("implicitExceptionFinally.before", ICounter.FULLY_COVERED);
		assertLine("implicitExceptionFinally.exception", ICounter.NOT_COVERED);
		assertLine("implicitExceptionFinally.after", ICounter.NOT_COVERED);
		assertLine("implicitExceptionFinally.finally",
				isJDKCompiler ? ICounter.EMPTY : ICounter.NOT_COVERED);
		assertLine("implicitExceptionFinally.finallyBlock",
				ICounter.FULLY_COVERED);
		assertLine("implicitExceptionFinally.finallyBlockEnd", ICounter.EMPTY);
		assertLine("implicitExceptionFinally.afterBlock", ICounter.NOT_COVERED);

		// 9. Finally Block With Exception Thrown Explicitly
		assertLine("explicitExceptionFinally.beforeBlock",
				ICounter.FULLY_COVERED);
		assertLine("explicitExceptionFinally.before", ICounter.FULLY_COVERED);
		assertLine("explicitExceptionFinally.throw", ICounter.FULLY_COVERED);
		assertLine("explicitExceptionFinally.finally",
				isJDKCompiler ? ICounter.EMPTY : ICounter.FULLY_COVERED);
		assertLine("explicitExceptionFinally.finallyBlock",
				ICounter.FULLY_COVERED);
		assertLine("explicitExceptionFinally.finallyBlockEnd",
				isJDKCompiler ? ICounter.EMPTY : ICounter.FULLY_COVERED);
		assertLine("explicitExceptionFinally.afterBlock", ICounter.EMPTY);

	}

}
