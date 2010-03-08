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
import static org.jacoco.core.analysis.ILines.NO_CODE;

import org.jacoco.core.test.validation.targets.Target05;
import org.junit.Test;

/**
 * Tests of static initializer in classes.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public class ClassInitializerTest extends ValidationTestBase {

	public ClassInitializerTest() {
		super(Target05.class);
	}

	@Override
	protected void run(final Class<?> targetClass) throws Exception {
		// Force class initialization
		targetClass.getField("CONST1").get(null);
	}

	@Test
	public void testCoverageResult() {

		assertLine("const1", NO_CODE);
		assertLine("const2", NO_CODE);

		assertLine("const3", FULLY_COVERED);
		assertLine("const4", FULLY_COVERED);

		assertLine("field1", FULLY_COVERED);
		assertLine("field2", FULLY_COVERED);
		assertLine("field3", FULLY_COVERED);
		assertLine("field4", FULLY_COVERED);

		assertLine("staticblock", FULLY_COVERED);
	}

}
