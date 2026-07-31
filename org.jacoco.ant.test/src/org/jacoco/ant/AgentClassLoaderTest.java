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

import static org.jacoco.ant.AntUnitSuiteFactory.suiteFor;

import java.io.IOException;

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
		return suiteFor(AgentClassLoaderTest.class);
	}

}
