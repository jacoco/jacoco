/*******************************************************************************
 * Copyright (c) 2009, 2011 Mountainminds GmbH & Co. KG and Contributors
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

import static org.jacoco.core.analysis.ILine.NOT_COVERED;
import static org.jacoco.core.analysis.ILine.NO_CODE;

import org.jacoco.core.test.validation.targets.Target07;
import org.junit.Test;

/**
 * Test of a private empty default constructor.
 * 
 * @author Marc R. Hoffmann
 * @version $qualified.bundle.version$
 */
public class PrivateEmptyDefaultConstructorTest extends ValidationTestBase {

	public PrivateEmptyDefaultConstructorTest() {
		super(Target07.class);
	}

	@Override
	protected void run(final Class<?> targetClass) throws Exception {
		// Force class initialization
		targetClass.getField("CONST").get(null);
	}

	@Test
	public void testCoverageResult() {

		assertLine("classdef", NO_CODE);
		assertLine("constructor", NOT_COVERED);

	}

}
