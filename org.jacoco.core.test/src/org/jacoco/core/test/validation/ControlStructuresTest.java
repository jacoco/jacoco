/*******************************************************************************
 * Copyright (c) 2009 Mountainminds GmbH & Co. KG and others
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

import org.jacoco.core.test.validation.targets.Target01;
import org.junit.Test;

/**
 * Tests of basic Java control structures.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public class ControlStructuresTest extends ValidationTestBase {

	public ControlStructuresTest() {
		super(Target01.class);
	}

	@Test
	public void testCoverageResult() {

		// 1. Direct unconditional execution
		assertLine("unconditional", FULLY_COVERED);

		// 2. Missed if block
		assertLine("missedif", NOT_COVERED);
		assertLine("executedelse", FULLY_COVERED);

		// 3. Executed if block
		assertLine("executedif", FULLY_COVERED);
		assertLine("missedelse", NOT_COVERED);

		// 4. Missed while block
		assertLine("missedwhile", NOT_COVERED);

		// 5. Executed while block
		assertLine("executedwhile", FULLY_COVERED);

		// 6. Executed do while block
		assertLine("executeddowhile", FULLY_COVERED);

		// 7. Missed for block
		assertLine("missedforincrementer", PARTLY_COVERED);
		assertLine("missedfor", NOT_COVERED);

		// 8. Executed for block
		assertLine("executedforincrementer", FULLY_COVERED);
		assertLine("executedfor", FULLY_COVERED);

		// 9. Missed for each block
		assertLine("missedforeachincrementer", PARTLY_COVERED);
		assertLine("missedforeach", NOT_COVERED);

		// 10. Executed for each block
		assertLine("executedforeachincrementer", FULLY_COVERED);
		assertLine("executedforeach", FULLY_COVERED);

		// 11. Table switch with hit
		assertLine("tswitch1case1", NOT_COVERED);
		assertLine("tswitch1case2", FULLY_COVERED);
		assertLine("tswitch1case3", NOT_COVERED);
		assertLine("tswitch1default", NOT_COVERED);

		// 12. Continued table switch with hit
		assertLine("tswitch2case1", NOT_COVERED);
		assertLine("tswitch2case2", FULLY_COVERED);
		assertLine("tswitch2case3", FULLY_COVERED);
		assertLine("tswitch2default", FULLY_COVERED);

		// 13. Table switch without hit
		assertLine("tswitch3case1", NOT_COVERED);
		assertLine("tswitch3case2", NOT_COVERED);
		assertLine("tswitch3case3", NOT_COVERED);
		assertLine("tswitch3default", FULLY_COVERED);

		// 14. Lookup switch with hit
		assertLine("lswitch1case1", NOT_COVERED);
		assertLine("lswitch1case2", FULLY_COVERED);
		assertLine("lswitch1case3", NOT_COVERED);
		assertLine("lswitch1default", NOT_COVERED);

		// 15. Continued lookup switch with hit
		assertLine("lswitch2case1", NOT_COVERED);
		assertLine("lswitch2case2", FULLY_COVERED);
		assertLine("lswitch2case3", FULLY_COVERED);
		assertLine("lswitch2default", FULLY_COVERED);

		// 16. Lookup switch without hit
		assertLine("lswitch3case1", NOT_COVERED);
		assertLine("lswitch3case2", NOT_COVERED);
		assertLine("lswitch3case3", NOT_COVERED);
		assertLine("lswitch3default", FULLY_COVERED);

		// 17. Break statement
		assertLine("executedbreak", FULLY_COVERED);
		assertLine("missedafterbreak", NOT_COVERED);

		// 18. Continue statement
		assertLine("executedcontinue", FULLY_COVERED);
		assertLine("missedaftercontinue", NOT_COVERED);

	}
}
