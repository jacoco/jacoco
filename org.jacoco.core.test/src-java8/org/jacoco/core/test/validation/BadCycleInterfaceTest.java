/*******************************************************************************
 * Copyright (c) 2009, 2016 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.test.validation;

import org.jacoco.core.analysis.ICounter;
import org.jacoco.core.test.validation.targets.BadCycleInterface;
import org.junit.Test;

/**
 * Test of "bad cycles" with interfaces.
 */
public class BadCycleInterfaceTest extends ValidationTestBase {

	public BadCycleInterfaceTest() throws Exception {
		super("src-java8", BadCycleInterface.class);
	}

	@Test
	public void test() throws Exception {
		if (System.getProperty("java.version").startsWith("9-ea")) {
			// JDK-9042842
			assertLine("baseclinit", ICounter.EMPTY);
			assertLine("childdefault", ICounter.NOT_COVERED);
			assertLogEvents("childclinit", "childstaticmethod");
		} else {
			assertLine("baseclinit", ICounter.FULLY_COVERED);
			assertLine("childdefault", ICounter.FULLY_COVERED);

			// The cycle causes a default method to be called before the static
			// initializer of a interface:
			assertLogEvents("baseclinit", "childdefaultmethod", "childclinit",
					"childstaticmethod");
		}
		assertLine("childclinit", ICounter.FULLY_COVERED);
		assertLine("childstatic", ICounter.FULLY_COVERED);

	}
}
