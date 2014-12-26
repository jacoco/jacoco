/*******************************************************************************
 * Copyright (c) 2009, 2014 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.agent.rt.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Properties;

import org.junit.Test;

/**
 * Unit tests for {@link ConfigLoader}.
 */
public class ConfigLoaderTest {

	@Test
	public void testResource() {
		Properties system = new Properties();
		Properties config = ConfigLoader.load(
				"/org/jacoco/agent/rt/internal/agent-test.properties", system);

		assertEquals("tcpclient", config.get("output"));
	}

	@Test
	public void testNoResource() {
		Properties system = new Properties();
		Properties config = ConfigLoader.load("does-not-exist.properties",
				system);

		assertTrue(config.isEmpty());
	}

	@Test
	public void testSystemProperties() {
		Properties system = new Properties();
		system.setProperty("jacoco-agent.output", "mbean");
		system.setProperty("output", "tcpserver"); // no prefix
		system.setProperty("jacoco-agent.sessionid", "testid");
		Properties config = ConfigLoader.load(
				"/org/jacoco/agent/rt/internal/agent-test.properties", system);

		assertEquals("mbean", config.get("output"));
		assertEquals("testid", config.get("sessionid"));
	}

	@Test
	public void testEnvironmentVariableReplacement() {
		Properties system = new Properties();
		system.setProperty("user.home", "/user/home");
		system.setProperty("java.version", "1.2.3");

		Properties config = ConfigLoader.load(
				"/org/jacoco/agent/rt/internal/agent-env-test.properties",
				system);

		assertEquals("/user/home/coverage/unit-test-agent-1.2.3.exec",
				config.get("destfile"));
	}

	@Test
	public void testSystemPropertiesEscpaingDollarCharacter() {
		Properties system = new Properties();
		system.setProperty("user.home", "/user/gerrit");
		system.setProperty("jacoco-agent.replaceproperties", "true");

		setSystemPropAndAssertEquals(system, "\\\\${ my test \\{ \\}",
				"\\\\${ my test \\{ \\}");
		setSystemPropAndAssertEquals(system, "${mytest", "${mytest");
		setSystemPropAndAssertEquals(system, "$${user.home}", "$/user/gerrit");
		setSystemPropAndAssertEquals(system, "\\${user.home}", "\\/user/gerrit");
		setSystemPropAndAssertEquals(system, "${user.home}/test/path",
				"/user/gerrit/test/path");
	}

	@Test
	public void testSystemPropertiesNoPropertiesReplacement() {
		Properties system = new Properties();
		system.setProperty("user.home", "/user/gerrit");
		system.setProperty("jacoco-agent.replaceproperties", "false");

		setSystemPropAndAssertEquals(system, "$${user.home}", "$${user.home}");
		setSystemPropAndAssertEquals(system, "\\${user.home}", "\\${user.home}");
		setSystemPropAndAssertEquals(system, "${user.home}/test/path",
				"${user.home}/test/path");
	}

	/**
	 * @param systemPropertyValue
	 *            Sets <code>system</code>'s &quot;jacoco-agent.destfile&quot;
	 *            in <code>system</code> properties
	 * @param expectedValue
	 *            <code>expectedValue</code> returned by
	 *            {@link ConfigLoader#load(String, Properties)} for key
	 *            &quot;destfile&quot;
	 */
	private void setSystemPropAndAssertEquals(Properties system,
			String systemPropertyValue, String expectedValue) {
		system.setProperty("jacoco-agent.destfile", systemPropertyValue);
		Properties config = ConfigLoader.load(
				"/org/jacoco/agent/rt/internal/agent-env-test.properties",
				system);
		assertEquals(expectedValue, config.get("destfile"));
	}
}