/*******************************************************************************
 * Copyright (c) 2009, 2023 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.test.validation.java8;

import org.jacoco.core.test.validation.JavaVersion;
import org.jacoco.core.test.validation.Source.Line;
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
	public void method_execution_sequence() throws Exception {
		if (JavaVersion.current().isBefore("1.8.0_152")) {
			assertLogEvents("baseclinit", "childdefaultmethod", "childclinit",
					"childstaticmethod");
		} else {
			assertLogEvents("childclinit", "childstaticmethod");
		}
	}

	public void assertBaseClInit(final Line line) {
		if (JavaVersion.current().isBefore("1.8.0_152")) {
			// Incorrect interpetation of JVMS 5.5 in JDK 8 causes a default
			// method to be called before the static initializer of an interface
			// (see JDK-8098557 and JDK-8164302):
			assertFullyCovered(line);

		} else {
			// This shouldn't happen with JDK 9 (see also JDK-8043275)
			// and starting with JDK 8u152 (see JDK-8167607):
			assertEmpty(line);
		}
	}

	public void assertChildDefault(final Line line) throws Exception {
		if (JavaVersion.current().isBefore("1.8.0_152")) {
			// Incorrect interpetation of JVMS 5.5 in JDK 8 causes a default
			// method to be called before the static initializer of an interface
			// (see JDK-8098557 and JDK-8164302):
			assertFullyCovered(line);

		} else {
			// This shouldn't happen with JDK 9 (see also JDK-8043275)
			// and starting with JDK 8u152 (see JDK-8167607):
			assertNotCovered(line);
		}
	}

}
