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
import org.jacoco.core.test.validation.targets.Target01;
import org.junit.Test;

/**
 * Tests of basic Java control structures.
 */
public class ControlStructuresTest extends ValidationTestBase {

	public ControlStructuresTest() {
		super(Target01.class);
	}

	@Override
	protected void run(final Class<?> targetClass) throws Exception {
		final Object instance = targetClass.newInstance();
		((Runnable) instance).run();
	}

	@Test
	public void testCoverageResult() {

		// 1. Direct unconditional execution
		assertLine("unconditional", ICounter.FULLY_COVERED);

		// 2. Missed if block
		assertLine("iffalse", ICounter.FULLY_COVERED, 1, 1);
		assertLine("missedif", ICounter.NOT_COVERED);
		assertLine("executedelse", ICounter.FULLY_COVERED);

		// 3. Executed if block
		assertLine("iftrue", ICounter.FULLY_COVERED, 1, 1);
		assertLine("executedif", ICounter.FULLY_COVERED);
		assertLine("missedelse", ICounter.NOT_COVERED);

		// 4. Missed while block
		// ECJ and javac produce different status here
		assertLine("whilefalse", 1, 1);
		assertLine("missedwhile", ICounter.NOT_COVERED);

		// 5. Always true while block
		assertLine("whiletrue", ICounter.FULLY_COVERED, 1, 1);

		// 6. Executed while block
		assertLine("whiletruefalse", ICounter.FULLY_COVERED, 0, 2);
		assertLine("executedwhile", ICounter.FULLY_COVERED);

		// 7. Executed do while block
		assertLine("executeddowhile", ICounter.FULLY_COVERED);

		// 8. Missed for block
		assertLine("missedforincrementer", ICounter.PARTLY_COVERED, 1, 1);
		assertLine("missedfor", ICounter.NOT_COVERED);

		// 9. Executed for block
		assertLine("executedforincrementer", ICounter.FULLY_COVERED, 0, 2);
		assertLine("executedfor", ICounter.FULLY_COVERED);

		// 10. Missed for each block
		assertLine("missedforeachincrementer", ICounter.PARTLY_COVERED, 1, 1);
		assertLine("missedforeach", ICounter.NOT_COVERED);

		// 11. Executed for each block
		assertLine("executedforeachincrementer", ICounter.FULLY_COVERED, 0, 2);
		assertLine("executedforeach", ICounter.FULLY_COVERED);

		// 12. Table switch with hit
		assertLine("tswitch1", ICounter.FULLY_COVERED, 3, 1);
		assertLine("tswitch1case1", ICounter.NOT_COVERED);
		assertLine("tswitch1case2", ICounter.FULLY_COVERED);
		assertLine("tswitch1case3", ICounter.NOT_COVERED);
		assertLine("tswitch1default", ICounter.NOT_COVERED);

		// 13. Continued table switch with hit
		assertLine("tswitch2", ICounter.FULLY_COVERED, 3, 1);
		assertLine("tswitch2case1", ICounter.NOT_COVERED);
		assertLine("tswitch2case2", ICounter.FULLY_COVERED);
		assertLine("tswitch2case3", ICounter.FULLY_COVERED);
		assertLine("tswitch2default", ICounter.FULLY_COVERED);

		// 14. Table switch without hit
		assertLine("tswitch2", ICounter.FULLY_COVERED, 3, 1);
		assertLine("tswitch3case1", ICounter.NOT_COVERED);
		assertLine("tswitch3case2", ICounter.NOT_COVERED);
		assertLine("tswitch3case3", ICounter.NOT_COVERED);
		assertLine("tswitch3default", ICounter.FULLY_COVERED);

		// 15. Lookup switch with hit
		assertLine("lswitch1", ICounter.FULLY_COVERED, 3, 1);
		assertLine("lswitch1case1", ICounter.NOT_COVERED);
		assertLine("lswitch1case2", ICounter.FULLY_COVERED);
		assertLine("lswitch1case3", ICounter.NOT_COVERED);
		assertLine("lswitch1default", ICounter.NOT_COVERED);

		// 16. Continued lookup switch with hit
		assertLine("lswitch2", ICounter.FULLY_COVERED, 3, 1);
		assertLine("lswitch2case1", ICounter.NOT_COVERED);
		assertLine("lswitch2case2", ICounter.FULLY_COVERED);
		assertLine("lswitch2case3", ICounter.FULLY_COVERED);
		assertLine("lswitch2default", ICounter.FULLY_COVERED);

		// 17. Lookup switch without hit
		assertLine("lswitch3", ICounter.FULLY_COVERED, 3, 1);
		assertLine("lswitch3case1", ICounter.NOT_COVERED);
		assertLine("lswitch3case2", ICounter.NOT_COVERED);
		assertLine("lswitch3case3", ICounter.NOT_COVERED);
		assertLine("lswitch3default", ICounter.FULLY_COVERED);

		// 18. Break statement
		assertLine("executedbreak", ICounter.FULLY_COVERED);
		assertLine("missedafterbreak", ICounter.NOT_COVERED);

		// 19. Continue statement
		assertLine("executedcontinue", ICounter.FULLY_COVERED);
		assertLine("missedaftercontinue", ICounter.NOT_COVERED);

		// 20. Return statement
		assertLine("return", ICounter.FULLY_COVERED);
		assertLine("afterreturn", ICounter.NOT_COVERED);

		// 21. Implicit return
		assertLine("implicitreturn", ICounter.FULLY_COVERED);

	}

}
