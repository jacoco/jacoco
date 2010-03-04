/*******************************************************************************
 * Copyright (c) 2009, 2010 Mountainminds GmbH & Co. KG and others
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

import org.jacoco.core.test.validation.targets.Target04;
import org.junit.Test;

/**
 * Tests of static initializer in interfaces.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public class InterfaceClassInitializerTest extends ValidationTestBase {

	public InterfaceClassInitializerTest() {
		super(Target04.class);
	}

	@Override
	protected void run(final Class<?> targetClass) throws Exception {
		// Force class initialization
		targetClass.getField("CONST1").get(null);
	}

	@Test
	public void testCoverageResult() {

		// All constant fields are initialized:
		assertLine("const1", FULLY_COVERED);
		assertLine("const2", FULLY_COVERED);
		assertLine("const2", FULLY_COVERED);
	}

}
