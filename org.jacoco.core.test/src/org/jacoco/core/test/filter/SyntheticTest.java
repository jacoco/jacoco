/*******************************************************************************
 * Copyright (c) 2009, 2018 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.test.filter;

import org.jacoco.core.analysis.ICounter;
import org.jacoco.core.test.filter.targets.Synthetic;
import org.jacoco.core.test.validation.ValidationTestBase;
import org.junit.Test;

/**
 * Test of filtering of synthetic methods.
 */
public class SyntheticTest extends ValidationTestBase {

	public SyntheticTest() {
		super(Synthetic.class);
	}

	@Test
	public void testCoverageResult() {
		assertMethodCount(5);

		assertLine("classdef", ICounter.EMPTY);
		assertLine("field", ICounter.EMPTY);

		assertLine("inner.classdef", ICounter.EMPTY);
	}

}
