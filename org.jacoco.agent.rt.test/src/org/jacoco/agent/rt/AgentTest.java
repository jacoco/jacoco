/*******************************************************************************
 * Copyright (c) 2009, 2012 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.agent.rt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.jacoco.agent.rt.controller.LocalController;
import org.jacoco.agent.rt.controller.MBeanController;
import org.jacoco.agent.rt.controller.TcpClientController;
import org.jacoco.agent.rt.controller.TcpServerController;
import org.jacoco.core.runtime.AgentOptions;
import org.jacoco.core.runtime.AgentOptions.OutputMode;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Unit tests for {@link Agent}.
 */
public class AgentTest implements IExceptionLogger {

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();
	private Exception exception;

	@Test
	public void testCreateController() {
		AgentOptions options = new AgentOptions();
		Agent agent = new Agent(options, this);

		options.setOutput(OutputMode.file);
		assertTrue(agent.createAgentController() instanceof LocalController);

		options.setOutput(OutputMode.tcpserver);
		assertTrue(agent.createAgentController() instanceof TcpServerController);

		options.setOutput(OutputMode.tcpclient);
		assertTrue(agent.createAgentController() instanceof TcpClientController);

		options.setOutput(OutputMode.mbean);
		assertTrue(agent.createAgentController() instanceof MBeanController);
	}

	@Test
	public void testStartupShutdown() throws Exception {
		final File execfile = new File(folder.getRoot(), "jacoco.exec");
		AgentOptions options = new AgentOptions();
		options.setOutput(OutputMode.file);
		options.setDestfile(execfile.getAbsolutePath());
		options.setSessionId("testsession");
		Agent agent = new Agent(options, this);
		agent.startup();

		assertEquals("testsession", agent.getData().getSessionId());

		agent.shutdown();

		assertTrue(execfile.isFile());
		assertTrue(execfile.length() > 0);
		assertNull(exception);
	}

	@Test
	public void testNoSessionId() throws Exception {
		final File execfile = new File(folder.getRoot(), "jacoco.exec");
		AgentOptions options = new AgentOptions();
		options.setOutput(OutputMode.file);
		options.setDestfile(execfile.getAbsolutePath());
		Agent agent = new Agent(options, this);

		final String defaultId = agent.getData().getSessionId();

		agent.startup();

		assertFalse(defaultId.equals(agent.getData().getSessionId()));
		assertNull(exception);
	}

	@Test
	public void testNoDumpOnExit() throws Exception {
		final File execfile = new File(folder.getRoot(), "jacoco.exec");
		AgentOptions options = new AgentOptions();
		options.setOutput(OutputMode.file);
		options.setDestfile(execfile.getAbsolutePath());
		options.setDumpOnExit(false);
		Agent agent = new Agent(options, this);

		agent.startup();
		agent.shutdown();

		assertEquals(0, execfile.length());
		assertNull(exception);
	}

	@Test
	public void testInvalidExecFile() throws Exception {
		AgentOptions options = new AgentOptions();
		options.setOutput(OutputMode.file);
		options.setDestfile(folder.getRoot().getAbsolutePath());
		Agent agent = new Agent(options, this);

		agent.startup();

		assertTrue(exception instanceof IOException);
	}

	// === IExceptionLogger ===

	public void logExeption(Exception ex) {
		exception = ex;
	}

}
