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
import org.jacoco.core.test.validation.java5.targets.ControlStructureBeforeSuperConstructorTarget;
import org.junit.Test;

/**
 * Test of probes before the super constructor call.
 */
public class ControlStructureBeforeSuperConstructorTest extends ValidationTestBase {

	public ControlStructureBeforeSuperConstructorTest() {
		super(ControlStructureBeforeSuperConstructorTarget.class);
	}

	@Test
	public void testCoverageResult() {

		assertLine("super", ICounter.PARTLY_COVERED, 3, 1);

	}

}
