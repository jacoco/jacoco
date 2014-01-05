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
				"/org/jacoco/agent/rt/agent-test.properties", system);

		assertEquals("mbean", config.get("output"));
		assertEquals("testid", config.get("sessionid"));
	}

}
