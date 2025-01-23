/*******************************************************************************
 * Copyright (c) 2009, 2025 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.ant;

import java.io.File;

import org.apache.ant.antunit.junit3.AntUnitSuite;
import org.apache.ant.antunit.junit4.AntUnitSuiteRunner;
import org.jacoco.core.test.validation.JavaVersion;
import org.junit.runner.RunWith;

import junit.framework.TestSuite;

@RunWith(AntUnitSuiteRunner.class)
public class SecurityManagerTest {

	public static TestSuite suite() {
		if (JavaVersion.current().isBefore("24")) {
			System.setProperty("org.jacoco.ant.securityManagerTest.classes.dir",
					TestTarget.getClassPath());
			final File file = new File(
					"src/org/jacoco/ant/SecurityManagerTest.xml");
			return new AntUnitSuite(file, SecurityManagerTest.class);
		}
		// Ability to enable Security Manager was removed in Java 24
		// https://openjdk.org/jeps/486
		final File file = new File("src/org/jacoco/ant/empty.xml");
		return new AntUnitSuite(file, SecurityManagerTest.class);
	}

}
