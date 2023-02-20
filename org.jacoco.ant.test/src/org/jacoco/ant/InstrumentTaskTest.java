/*******************************************************************************
 * Copyright (c) 2009, 2023 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Brock Janiczak - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.ant;

import java.io.File;
import java.io.IOException;

import org.apache.ant.antunit.junit3.AntUnitSuite;
import org.apache.ant.antunit.junit4.AntUnitSuiteRunner;
import org.jacoco.agent.AgentJar;
import org.junit.runner.RunWith;

import junit.framework.TestSuite;

@RunWith(AntUnitSuiteRunner.class)
public class InstrumentTaskTest {

	public static TestSuite suite() throws IOException {
		System.setProperty("org.jacoco.ant.instrumentTaskTest.classes.dir",
				TestTarget.getClassPath());
		System.setProperty("org.jacoco.ant.instrumentTaskTest.agent.file",
				AgentJar.extractToTempLocation().getAbsolutePath());
		final File file = new File("src/org/jacoco/ant/InstrumentTaskTest.xml");
		return new AntUnitSuite(file, InstrumentTaskTest.class);
	}

}
