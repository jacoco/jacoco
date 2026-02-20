/*******************************************************************************
 * Copyright (c) 2009, 2017 Mountainminds GmbH & Co. KG and Contributors
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
import org.jacoco.core.test.filter.targets.Goto;
import org.jacoco.core.test.validation.ValidationTestBase;
import org.junit.Test;

public class GotoTest extends ValidationTestBase {

	public GotoTest() {
		super(Goto.class);
	}

	@Test
	public void testCoverageResult() {
		assertLine("ifTrue", ICounter.FULLY_COVERED, 1, 1);
		assertLine("executedIf", ICounter.FULLY_COVERED);
		assertLine("else",
				isJDKCompiler ? ICounter.EMPTY : ICounter.FULLY_COVERED);
		assertLine("missedElse", ICounter.NOT_COVERED);
		assertLine("while", isJDKCompiler ? ICounter.FULLY_COVERED
				: ICounter.PARTLY_COVERED, 1, 1);
		assertLine("missedWhile", ICounter.NOT_COVERED);
	}

}
