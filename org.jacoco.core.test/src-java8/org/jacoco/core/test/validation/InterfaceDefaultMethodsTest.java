/*******************************************************************************
 * Copyright (c) 2009, 2017 Mountainminds GmbH & Co. KG and Contributors
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
import org.jacoco.core.test.validation.targets.InterfaceDefaultMethodsTarget;
import org.junit.Test;

/**
 * Tests of static initializer and default methods in interfaces.
 */
public class InterfaceDefaultMethodsTest extends ValidationTestBase {

	public InterfaceDefaultMethodsTest() {
		super("src-java8", InterfaceDefaultMethodsTarget.class);
	}

	@Test
	public void testCoverageResult() {
		assertLine("clinit", ICounter.FULLY_COVERED);
		assertLine("m1", ICounter.FULLY_COVERED);
		assertLine("m2", ICounter.NOT_COVERED);
	}

}
