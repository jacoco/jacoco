/*******************************************************************************
 * Copyright (c) 2009, 2026 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
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
public class JmxTest {

	public static TestSuite suite() {
		System.setProperty("org.jacoco.ant.jmxTest.classes.dir",
				TestTarget.getClassPath());
		final File file = new File("src/org/jacoco/ant/JmxTest.xml");
		return new AntUnitSuite(file, JmxTest.class);
	}

}
