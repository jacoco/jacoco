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
package org.jacoco.core.test.validation.kotlin;

import org.jacoco.core.analysis.ICounter;
import org.jacoco.core.test.validation.ValidationTestBase;
import org.jacoco.core.test.validation.kotlin.targets.KotlinTopLevelFunctionTargetKt;
import org.junit.Test;

/**
 * Test of top level function.
 */
public class KotlinTopLevelFunctionTest extends ValidationTestBase {

	public KotlinTopLevelFunctionTest() {
		super(KotlinTopLevelFunctionTargetKt.class);
	}

	@Test
	public void test() {
		assertLine("fun", ICounter.FULLY_COVERED);
	}

}
