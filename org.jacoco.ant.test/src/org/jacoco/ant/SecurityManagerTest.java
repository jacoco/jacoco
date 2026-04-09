/*******************************************************************************
 * Copyright (c) 2009, 2026 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.ant;

import static org.jacoco.ant.AntUnitSuiteFactory.skip;
import static org.jacoco.ant.AntUnitSuiteFactory.suiteFor;

import org.apache.ant.antunit.junit4.AntUnitSuiteRunner;
import org.jacoco.core.test.validation.JavaVersion;
import org.junit.runner.RunWith;

import junit.framework.TestSuite;

@RunWith(AntUnitSuiteRunner.class)
public class SecurityManagerTest {

	public static TestSuite suite() {
		if (!JavaVersion.current().isBefore("24")) {
			// Ability to enable Security Manager was removed in Java 24
			// https://openjdk.org/jeps/486
			return skip(SecurityManagerTest.class);
		}
		System.setProperty("org.jacoco.ant.securityManagerTest.classes.dir",
				TestTarget.getClassPath());
		return suiteFor(SecurityManagerTest.class);
	}

}
