/*******************************************************************************
 * Copyright (c) 2009, 2017 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *
 *******************************************************************************/
package org.jacoco.core.test.filter;

import org.jacoco.core.analysis.ICounter;
import org.jacoco.core.test.filter.targets.Assert;
import org.jacoco.core.test.validation.ValidationTestBase;
import org.junit.Test;

/**
 * Test of filtering of a bytecode that is generated for an assert statement.
 */
public class AssertTest extends ValidationTestBase {

	public AssertTest() {
		super(Assert.class);
	}

	/**
	 * {@link Assert}
	 */
	@Test
	public void clinit() {
		assertMethod("<clinit>", ICounter.FULLY_COVERED);
	}

	/**
	 * {@link Assert#simple()}
	 */
	@Test
	public void simple() {
		assertLine("simple", ICounter.PARTLY_COVERED, 1, 1, 4, 2);
	}

	/**
	 * {@link Assert#message()}
	 */
	@Test
	public void message() {
		assertLine("message", ICounter.PARTLY_COVERED, 1, 1, 5, 2);
	}

	/**
	 * {@link Assert.SimpleClinit}
	 */
	@Test
	public void clinit_simple() {
		assertLine("clinit", ICounter.PARTLY_COVERED, 1, 1, 4, 2);
	}
}
