/*******************************************************************************
 * Copyright (c) 2009, 2015 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.agent.rt.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.jacoco.agent.rt.internal.output.IAgentOutput;
import org.jacoco.agent.rt.internal.output.FileOutput;
import org.jacoco.agent.rt.internal.output.NoneOutput;
import org.jacoco.agent.rt.internal.output.TcpClientOutput;
import org.jacoco.agent.rt.internal.output.TcpServerOutput;
import org.jacoco.core.JaCoCo;
import org.jacoco.core.data.ExecutionDataReader;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.data.SessionInfoStore;
import org.jacoco.core.runtime.AgentOptions;
import org.jacoco.core.runtime.AgentOptions.OutputMode;
import org.jacoco.core.runtime.RuntimeData;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Unit tests for {@link Agent}.
 */
public class AgentTest implements IExceptionLogger {

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	private AgentOptions options;
	private File execfile;

	private Exception exception;

	@Before
	public void setup() {
		options = new AgentOptions();
		execfile = new File(folder.getRoot(), "jacoco.exec");
		options.setOutput(OutputMode.file);
		options.setDestfile(execfile.getAbsolutePath());
	}

	@Test
	public void testCreateController() {
		Agent agent = new Agent(options, this);

		options.setOutput(OutputMode.file);
		assertEquals(FileOutput.class, agent.createAgentOutput()
				.getClass());

		options.setOutput(OutputMode.tcpserver);
		assertEquals(TcpServerOutput.class, agent.createAgentOutput()
				.getClass());

		options.setOutput(OutputMode.tcpclient);
		assertEquals(TcpClientOutput.class, agent.createAgentOutput()
				.getClass());

		options.setOutput(OutputMode.none);
		assertEquals(NoneOutput.class, agent.createAgentOutput()
				.getClass());
	}

	@Test
	public void testStartupShutdown() throws Exception {
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
	public void testShutdownWithException() throws Exception {
		final Exception expected = new Exception();
		Agent agent = new Agent(options, this) {
			@Override
			IAgentOutput createAgentOutput() {
				return new IAgentOutput() {
					public void startup(AgentOptions options, RuntimeData data) {
					}

					public void shutdown() throws Exception {
						throw expected;
					}

					public void writeExecutionData(boolean reset) {
					}
				};
			}
		};
		agent.startup();

		agent.shutdown();

		assertSame(expected, exception);
	}

	@Test
	public void testNoSessionId() throws Exception {
		Agent agent = new Agent(options, this);

		final String defaultId = agent.getData().getSessionId();

		agent.startup();

		assertFalse(defaultId.equals(agent.getData().getSessionId()));
		assertNull(exception);
	}

	@Test
	public void testNoDumpOnExit() throws Exception {
		options.setDumpOnExit(false);
		Agent agent = new Agent(options, this);

		agent.startup();
		agent.shutdown();

		assertEquals(0, execfile.length());
		assertNull(exception);
	}

	@Test
	public void testInvalidExecFile() throws Exception {
		options.setDestfile(folder.getRoot().getAbsolutePath());
		Agent agent = new Agent(options, this);

		agent.startup();

		assertTrue(exception instanceof IOException);
	}

	@Test
	public void testGetVersion() {
		Agent agent = new Agent(options, this);
		assertEquals(JaCoCo.VERSION, agent.getVersion());
	}

	@Test
	public void testGetSetSessionId() throws IOException {
		Agent agent = new Agent(options, this);
		agent.startup();
		agent.setSessionId("agenttestid");
		assertEquals("agenttestid", agent.getSessionId());

		SessionInfoStore sessionStore = new SessionInfoStore();
		ExecutionDataReader reader = new ExecutionDataReader(
				new ByteArrayInputStream(agent.getExecutionData(false)));
		reader.setSessionInfoVisitor(sessionStore);
		reader.read();
		assertEquals("agenttestid", sessionStore.getInfos().get(0).getId());
	}

	@Test
	public void testReset() {
		Agent agent = new Agent(options, this);

		boolean[] probes = agent.getData()
				.getExecutionData(Long.valueOf(0x12345678), "Foo", 1)
				.getProbes();
		probes[0] = true;

		agent.reset();

		assertFalse(probes[0]);
	}

	@Test
	public void testGetExecutionData() throws IOException {
		options.setSessionId("agenttestid");
		Agent agent = new Agent(options, this);
		agent.startup();

		boolean[] probes = agent.getData()
				.getExecutionData(Long.valueOf(0x12345678), "Foo", 1)
				.getProbes();
		probes[0] = true;

		byte[] data = agent.getExecutionData(true);

		// ensure reset has been executed
		assertFalse(probes[0]);

		ExecutionDataStore execStore = new ExecutionDataStore();
		SessionInfoStore sessionStore = new SessionInfoStore();

		ExecutionDataReader reader = new ExecutionDataReader(
				new ByteArrayInputStream(data));
		reader.setExecutionDataVisitor(execStore);
		reader.setSessionInfoVisitor(sessionStore);
		reader.read();

		assertEquals("Foo", execStore.get(0x12345678).getName());
		assertEquals(1, sessionStore.getInfos().size());
		assertEquals("agenttestid", sessionStore.getInfos().get(0).getId());
	}

	@Test
	public void testDump() throws Exception {
		final boolean[] called = new boolean[1];
		Agent agent = new Agent(options, this) {
			@Override
			IAgentOutput createAgentOutput() {
				return new IAgentOutput() {
					public void startup(AgentOptions options, RuntimeData data) {
					}

					public void shutdown() throws Exception {
					}

					public void writeExecutionData(boolean reset) {
						assertTrue(reset);
						called[0] = true;
					}
				};
			}
		};
		agent.startup();

		agent.dump(true);

		assertTrue(called[0]);
	}

	@Test
	public void testJmx() throws Exception {
		options.setJmx(true);
		Agent agent = new Agent(options, this);

		agent.startup();

		ObjectName objectName = new ObjectName("org.jacoco:type=Runtime");
		final MBeanServer server = ManagementFactory.getPlatformMBeanServer();
		assertEquals(JaCoCo.VERSION, server.getAttribute(objectName, "Version"));

		agent.shutdown();

		try {
			server.getMBeanInfo(objectName);
			fail("InstanceNotFoundException expected");
		} catch (InstanceNotFoundException expected) {
		}
	}

	@Test(expected = InstanceNotFoundException.class)
	public void testNoJmx() throws Exception {
		Agent agent = new Agent(options, this);
		agent.startup();

		ObjectName objectName = new ObjectName("org.jacoco:type=Runtime");
		ManagementFactory.getPlatformMBeanServer().getMBeanInfo(objectName);
	}

	// === IExceptionLogger ===

	public void logExeption(Exception ex) {
		exception = ex;
	}

}
