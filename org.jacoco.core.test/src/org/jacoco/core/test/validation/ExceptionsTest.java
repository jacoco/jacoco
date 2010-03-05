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
 * $Id: $
 *******************************************************************************/
package org.jacoco.core.test.validation;

import static org.jacoco.core.analysis.ILines.FULLY_COVERED;
import static org.jacoco.core.analysis.ILines.NOT_COVERED;
import static org.jacoco.core.analysis.ILines.PARTLY_COVERED;

import org.jacoco.core.test.validation.targets.Target03;
import org.junit.Test;

/**
 * Tests of exception based control flow.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
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
		assertLine("implicitException.before", NOT_COVERED);
		assertLine("implicitException.exception", NOT_COVERED);
		assertLine("implicitException.after", NOT_COVERED);

		// 2. Explicit Exception
		// Full coverage, as we recognize throw statements as block boundaries.
		assertLine("explicitException.before", FULLY_COVERED);
		assertLine("explicitException.throw", FULLY_COVERED);

		// 3. Try/Catch Block Without Exception Thrown
		assertLine("noExceptionTryCatch.beforeBlock", FULLY_COVERED);
		assertLine("noExceptionTryCatch.tryBlock", FULLY_COVERED);
		assertLine("noExceptionTryCatch.catchBlock", NOT_COVERED);

		// 4. Try/Catch Block Without a Implicit Exception Thrown
		// As always with implicit exceptions we don't see when a block aborts
		// somewhere in the middle.
		assertLine("implicitExceptionTryCatch.beforeBlock", FULLY_COVERED);
		assertLine("implicitExceptionTryCatch.before", NOT_COVERED);
		assertLine("implicitExceptionTryCatch.exception", NOT_COVERED);
		assertLine("implicitExceptionTryCatch.after", NOT_COVERED);
		assertLine("implicitExceptionTryCatch.catchBlock", FULLY_COVERED);

		// 5. Try/Catch Block With Exception Thrown Explicitly
		assertLine("explicitExceptionTryCatch.beforeBlock", FULLY_COVERED);
		assertLine("explicitExceptionTryCatch.before", FULLY_COVERED);
		assertLine("explicitExceptionTryCatch.throw", FULLY_COVERED);
		assertLine("explicitExceptionTryCatch.catchBlock", FULLY_COVERED);

		// 6. Finally Block Without Exception Thrown
		// Finally block is yellow as the exception path is missing.
		assertLine("noExceptionFinally.beforeBlock", FULLY_COVERED);
		assertLine("noExceptionFinally.tryBlock", FULLY_COVERED);
		assertLine("noExceptionFinally.finallyBlock", PARTLY_COVERED);

		// 7. Finally Block With Implicit Exception
		// Finally block is yellow as the non-exception path is missing.
		assertLine("implicitExceptionFinally.beforeBlock", FULLY_COVERED);
		assertLine("implicitExceptionFinally.before", NOT_COVERED);
		assertLine("implicitExceptionFinally.exception", NOT_COVERED);
		assertLine("implicitExceptionFinally.after", NOT_COVERED);
		assertLine("implicitExceptionFinally.finallyBlock", PARTLY_COVERED);

		// 8. Finally Block With Exception Thrown Explicitly
		assertLine("explicitExceptionFinally.beforeBlock", FULLY_COVERED);
		assertLine("explicitExceptionFinally.before", FULLY_COVERED);
		assertLine("explicitExceptionFinally.throw", FULLY_COVERED);
		assertLine("explicitExceptionFinally.finallyBlock", FULLY_COVERED);

	}

}
