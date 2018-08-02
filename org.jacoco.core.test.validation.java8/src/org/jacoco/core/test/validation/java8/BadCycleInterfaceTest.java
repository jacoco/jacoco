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
package org.jacoco.core.test.validation.java8;

import org.jacoco.core.analysis.ICounter;
import org.jacoco.core.test.validation.ValidationTestBase;
import org.jacoco.core.test.validation.java8.targets.BadCycleInterfaceTarget;
import org.junit.Test;

/**
 * Test of "bad cycles" with interfaces.
 */
public class BadCycleInterfaceTest extends ValidationTestBase {

	public BadCycleInterfaceTest() throws Exception {
		super(BadCycleInterfaceTarget.class);
	}

	@Test
	public void test() throws Exception {
		if (JAVA_VERSION.isBefore("1.8.0_152")) {
			// Incorrect interpetation of JVMS 5.5 in JDK 8 causes a default
			// method to be called before the static initializer of an interface
			// (see JDK-8098557 and JDK-8164302):
			assertLine("baseclinit", ICounter.FULLY_COVERED);
			assertLine("childdefault", ICounter.FULLY_COVERED);

			assertLogEvents("baseclinit", "childdefaultmethod", "childclinit",
					"childstaticmethod");
		} else {
			// This shouldn't happen with JDK 9 (see also JDK-8043275)
			// and starting with JDK 8u152 (see JDK-8167607):
			assertLine("baseclinit", ICounter.EMPTY);
			assertLine("childdefault", ICounter.NOT_COVERED);
			assertLogEvents("childclinit", "childstaticmethod");
		}
		assertLine("childclinit", ICounter.FULLY_COVERED);
		assertLine("childstatic", ICounter.FULLY_COVERED);
	}

}
