/*******************************************************************************
 * Copyright (c) 2009, 2024 Mountainminds GmbH & Co. KG and Contributors
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
package org.jacoco.ant;

import java.io.File;
import java.io.IOException;

import org.apache.ant.antunit.junit3.AntUnitSuite;
import org.apache.ant.antunit.junit4.AntUnitSuiteRunner;
import org.jacoco.agent.AgentJar;
import org.junit.runner.RunWith;

import junit.framework.TestSuite;

@RunWith(AntUnitSuiteRunner.class)
public class AgentClassLoaderTest {

	public static TestSuite suite() throws IOException {
		System.setProperty("org.jacoco.ant.agentClassLoaderTest.classes.dir",
				TestTarget.getClassPath());
		System.setProperty("org.jacoco.ant.agentClassLoaderTest.agent.jar",
				AgentJar.extractToTempLocation().getAbsolutePath());
		final File file = new File(
				"src/org/jacoco/ant/AgentClassLoaderTest.xml");
		return new AntUnitSuite(file, AgentClassLoaderTest.class);
	}

}
