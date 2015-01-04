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
import org.jacoco.core.test.validation.targets.Target09;
import org.junit.Test;

/**
 * Test of field initialization in two constructors.
 */
public class FieldInitializationInTwoConstructorsTest extends
		ValidationTestBase {

	public FieldInitializationInTwoConstructorsTest() {
		super(Target09.class);
	}

	@Override
	protected void run(final Class<?> targetClass) throws Exception {
		targetClass.newInstance();
	}

	@Test
	public void testCoverageResult() {

		assertLine("field1", ICounter.PARTLY_COVERED);
		assertLine("field2", ICounter.PARTLY_COVERED);
		assertLine("constr1", ICounter.FULLY_COVERED);
		assertLine("constr2", ICounter.NOT_COVERED);

	}

}
