/*******************************************************************************
 * Copyright (c) 2009, 2018 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *
 *******************************************************************************/
package org.jacoco.core.test.validation.java5;

import org.jacoco.core.analysis.ICounter;
import org.jacoco.core.test.validation.ValidationTestBase;
import org.jacoco.core.test.validation.java5.targets.AssertTarget;
import org.junit.Test;

/**
 * Test of filtering of a bytecode that is generated for an assert statement.
 */
public class AssertTest extends ValidationTestBase {

	public AssertTest() {
		super(AssertTarget.class);
	}

	@Test
	public void clinit() {
		assertMethod("<clinit>", ICounter.FULLY_COVERED);
	}

}
