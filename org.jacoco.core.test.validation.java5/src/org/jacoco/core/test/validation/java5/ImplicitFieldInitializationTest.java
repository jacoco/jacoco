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
import org.jacoco.core.test.validation.java5.targets.ImplicitFieldInitializationTarget;
import org.junit.Test;

/**
 * Test of a implicit field initialization.
 */
public class ImplicitFieldInitializationTest extends ValidationTestBase {

	public ImplicitFieldInitializationTest() {
		super(ImplicitFieldInitializationTarget.class);
	}

	@Test
	public void testCoverageResult() {

		assertLine("classdef", ICounter.FULLY_COVERED);
		assertLine("field1", ICounter.EMPTY);
		assertLine("field2", ICounter.FULLY_COVERED);
		assertLine("field3", ICounter.EMPTY);
		assertLine("field4", ICounter.FULLY_COVERED);

	}

}
