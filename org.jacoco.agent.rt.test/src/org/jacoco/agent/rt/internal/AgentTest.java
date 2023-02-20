/*******************************************************************************
 * Copyright (c) 2009, 2023 Mountainminds GmbH & Co. KG and Contributors
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
package org.jacoco.agent.rt.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.lang.management.ManagementFactory;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.jacoco.agent.rt.internal.output.FileOutput;
import org.jacoco.agent.rt.internal.output.IAgentOutput;
import org.jacoco.agent.rt.internal.output.NoneOutput;
import org.jacoco.agent.rt.internal.output.TcpClientOutput;
import org.jacoco.agent.rt.internal.output.TcpServerOutput;
import org.jacoco.core.JaCoCo;
import org.jacoco.core.runtime.AgentOptions;
import org.jacoco.core.runtime.AgentOptions.OutputMode;
import org.jacoco.core.runtime.RuntimeData;
import org.jacoco.core.tools.ExecFileLoader;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link Agent}.
 */
public class AgentTest implements IExceptionLogger, IAgentOutput {

	private AgentOptions options;

	private Boolean writeExecutionDataReset;

	private Exception loggedException;

	@Before
	public void setup() {
		options = new AgentOptions();
		options.setOutput(OutputMode.file);

		// avoid network access (DNS lookup for id generation):
		options.setSessionId("test");
	}

	@Test
	public void createController_should_create_defined_controller_type() {
		Agent agent = new Agent(options, this);

		options.setOutput(OutputMode.file);
		assertEquals(FileOutput.class, agent.createAgentOutput().getClass());

		options.setOutput(OutputMode.tcpserver);
		assertEquals(TcpServerOutput.class,
				agent.createAgentOutput().getClass());

		options.setOutput(OutputMode.tcpclient);
		assertEquals(TcpClientOutput.class,
				agent.createAgentOutput().getClass());

		options.setOutput(OutputMode.none);
		assertEquals(NoneOutput.class, agent.createAgentOutput().getClass());
	}

	@Test
	public void startup_should_set_defined_session_id() throws Exception {
		Agent agent = createAgent();

		agent.startup();

		assertEquals("test", agent.getData().getSessionId());
		assertNull(loggedException);
	}

	@Test
	public void startup_should_create_random_session_id_when_undefined()
			throws Exception {
		options.setSessionId(null);

		Agent agent = createAgent();
		agent.startup();
		final String id1 = agent.getData().getSessionId();

		agent = createAgent();
		agent.startup();
		final String id2 = agent.getData().getSessionId();

		assertFalse(id1.equals(id2));
		assertNull(loggedException);
	}

	@Test
	public void startup_should_log_and_rethrow_exception() throws Exception {
		final Exception expected = new Exception();

		Agent agent = new Agent(options, this) {
			@Override
			IAgentOutput createAgentOutput() {
				return new IAgentOutput() {
					public void startup(AgentOptions options, RuntimeData data)
							throws Exception {
						throw expected;
					}

					public void shutdown() {
					}

					public void writeExecutionData(boolean reset) {
					}
				};
			}
		};

		try {
			agent.startup();
			fail("Exception expected");
		} catch (Exception actual) {
			assertSame(expected, actual);
			assertSame(expected, loggedException);
		}
	}

	@Test
	public void startup_should_register_mbean_when_enabled() throws Exception {
		options.setJmx(true);
		Agent agent = createAgent();

		agent.startup();

		ObjectName objectName = new ObjectName("org.jacoco:type=Runtime");
		final MBeanServer server = ManagementFactory.getPlatformMBeanServer();
		assertEquals(JaCoCo.VERSION,
				server.getAttribute(objectName, "Version"));

		// cleanup as MBean is registered globally
		agent.shutdown();
	}

	@Test
	public void startup_should_not_register_mbean_when_disabled()
			throws Exception {
		Agent agent = createAgent();

		agent.startup();

		ObjectName objectName = new ObjectName("org.jacoco:type=Runtime");

		try {
			ManagementFactory.getPlatformMBeanServer().getMBeanInfo(objectName);
			fail("InstanceNotFoundException expected");
		} catch (InstanceNotFoundException e) {
		}
	}

	@Test
	public void shutdown_should_write_execution_data_when_enabled()
			throws Exception {
		Agent agent = createAgent();
		agent.startup();

		agent.shutdown();

		assertEquals(Boolean.FALSE, writeExecutionDataReset);
		assertNull(loggedException);
	}

	@Test
	public void shutdown_should_not_write_execution_data_when_disabled()
			throws Exception {
		options.setDumpOnExit(false);
		Agent agent = createAgent();
		agent.startup();

		agent.shutdown();

		assertNull(writeExecutionDataReset);
		assertNull(loggedException);
	}

	@Test
	public void shutdown_should_log_exception() throws Exception {
		final Exception expected = new Exception();
		Agent agent = new Agent(options, this) {
			@Override
			IAgentOutput createAgentOutput() {
				return new IAgentOutput() {
					public void startup(AgentOptions options,
							RuntimeData data) {
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

		assertSame(expected, loggedException);
	}

	@Test
	public void shutdown_should_deregister_mbean_when_enabled()
			throws Exception {
		options.setJmx(true);
		Agent agent = createAgent();
		agent.startup();

		agent.shutdown();

		ObjectName objectName = new ObjectName("org.jacoco:type=Runtime");

		try {
			ManagementFactory.getPlatformMBeanServer().getMBeanInfo(objectName);
			fail("InstanceNotFoundException expected");
		} catch (InstanceNotFoundException e) {
		}
	}

	@Test
	public void getVersion_should_return_current_version() {
		Agent agent = createAgent();
		assertEquals(JaCoCo.VERSION, agent.getVersion());
	}

	@Test
	public void getSessionId_should_return_session_id() throws Exception {
		Agent agent = createAgent();

		agent.startup();

		assertEquals("test", agent.getSessionId());
	}

	@Test
	public void setSessionId_should_modify_session_id() throws Exception {
		Agent agent = createAgent();
		agent.startup();

		agent.setSessionId("new_id");

		assertEquals("new_id", agent.getSessionId());
	}

	@Test
	public void reset_should_reset_probes() {
		Agent agent = createAgent();
		boolean[] probes = agent.getData()
				.getExecutionData(Long.valueOf(0x12345678), "Foo", 1)
				.getProbes();
		probes[0] = true;

		agent.reset();

		assertFalse(probes[0]);
	}

	@Test
	public void getExecutionData_should_return_probes_and_session_id()
			throws Exception {
		Agent agent = createAgent();
		agent.startup();
		agent.getData().getExecutionData(Long.valueOf(0x12345678), "Foo", 1)
				.getProbes()[0] = true;

		byte[] data = agent.getExecutionData(true);

		ExecFileLoader loader = new ExecFileLoader();
		loader.load(new ByteArrayInputStream(data));
		assertEquals("Foo",
				loader.getExecutionDataStore().get(0x12345678).getName());
		assertEquals("test",
				loader.getSessionInfoStore().getInfos().get(0).getId());
	}

	@Test
	public void getExecutionData_should_reset_probes_when_enabled()
			throws Exception {
		Agent agent = createAgent();
		agent.startup();
		final boolean[] probes = agent.getData()
				.getExecutionData(Long.valueOf(0x12345678), "Foo", 1)
				.getProbes();
		probes[0] = true;

		agent.getExecutionData(true);

		assertFalse(probes[0]);
	}

	@Test
	public void getExecutionData_should_not_reset_probes_when_disabled()
			throws Exception {
		Agent agent = createAgent();
		agent.startup();
		final boolean[] probes = agent.getData()
				.getExecutionData(Long.valueOf(0x12345678), "Foo", 1)
				.getProbes();
		probes[0] = true;

		agent.getExecutionData(false);

		assertTrue(probes[0]);
	}

	@Test
	public void dump_should_trigger_writeExecutionData_with_reset()
			throws Exception {
		Agent agent = createAgent();
		agent.startup();

		agent.dump(true);

		assertEquals(Boolean.TRUE, writeExecutionDataReset);
		assertNull(loggedException);
	}

	@Test
	public void dump_should_trigger_writeExecutionData_without_reset()
			throws Exception {
		Agent agent = createAgent();
		agent.startup();

		agent.dump(false);

		assertEquals(Boolean.FALSE, writeExecutionDataReset);
		assertNull(loggedException);
	}

	private Agent createAgent() {
		return new Agent(options, this) {
			@Override
			IAgentOutput createAgentOutput() {
				return AgentTest.this;
			}
		};
	}

	// === IExceptionLogger ===

	public void logExeption(Exception ex) {
		loggedException = ex;
	}

	// === IAgentOutput ===

	public void startup(AgentOptions options, RuntimeData data) {
	}

	public void shutdown() {
	}

	public void writeExecutionData(boolean reset) {
		writeExecutionDataReset = Boolean.valueOf(reset);
	}

}
