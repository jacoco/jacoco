/*******************************************************************************
 * Copyright (c) 2009, 2023 Mountainminds GmbH & Co. KG and Contributors
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
		assertEquals("3333", config.get("port"));
		assertEquals("testid", config.get("sessionid"));
	}

	@Test
	public void testSubstituteProperties() {
		Properties system = new Properties();
		system.setProperty("user.home", "/home/jacoco");
		system.setProperty("java.version", "1.5.0");
		Properties config = ConfigLoader.load(
				"/org/jacoco/agent/rt/internal/agent-subst-test.properties",
				system);

		assertEquals("no$replace}", config.get("key0"));
		assertEquals("/home/jacoco/coverage/jacoco-1.5.0.exec",
				config.get("key1"));
		assertEquals("$/home/jacoco", config.get("key2"));
		assertEquals("/home/jacoco}}", config.get("key3"));
		assertEquals("${does.not.exist}", config.get("key4"));
	}
}
