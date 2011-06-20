/*******************************************************************************
 * Copyright (c) 2009, 2011 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.agent.rt.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.jacoco.agent.rt.StubRuntime;
import org.jacoco.core.JaCoCo;
import org.jacoco.core.data.ExecutionDataReader;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.data.SessionInfo;
import org.jacoco.core.data.SessionInfoStore;
import org.jacoco.core.runtime.AgentOptions;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.lang.management.ManagementFactory;
import java.util.List;

import javax.management.*;

/**
 * Unit tests for {@link MBeanController}.
 */
public class MBeanControllerTest {

	private StubRuntime runtime;

	private MBeanController controller;

	@Before
	public void setup() throws Exception {
		runtime = new StubRuntime();
		controller = new MBeanController();
		controller.startup(new AgentOptions(), runtime);
	}

	@Test
	public void testStartupAndShutdown() throws Exception {
		final ObjectName objectName = new ObjectName("org.jacoco:type=Runtime");

		final MBeanInfo info = ManagementFactory.getPlatformMBeanServer()
				.getMBeanInfo(objectName);

		final MBeanAttributeInfo[] attributes = info.getAttributes();
		assertEquals(2, attributes.length);

		final MBeanAttributeInfo sessionIdAttribute = attributes[0];
		assertEquals("SessionId", sessionIdAttribute.getName());
		assertEquals(String.class.getName(), sessionIdAttribute.getType());

		final MBeanAttributeInfo versionAttribute = attributes[1];
		assertEquals("Version", versionAttribute.getName());
		assertEquals(String.class.getName(), versionAttribute.getType());

		final MBeanOperationInfo[] operations = info.getOperations();
		assertEquals(2, info.getOperations().length);

		final MBeanOperationInfo resetOperation = operations[0];
		assertEquals("reset", resetOperation.getName());
		assertEquals(void.class.getName(), resetOperation.getReturnType());
		assertEquals(0, resetOperation.getSignature().length);

		final MBeanOperationInfo dumpOperation = operations[1];
		assertEquals("dump", dumpOperation.getName());
		assertEquals(byte[].class.getName(), dumpOperation.getReturnType());
		assertEquals(1, dumpOperation.getSignature().length);
		assertEquals(boolean.class.getName(),
				dumpOperation.getSignature()[0].getType());

		controller.shutdown();

		try {
			ManagementFactory.getPlatformMBeanServer().getMBeanInfo(objectName);
			fail("MBean was not deregistered");
		} catch (InstanceNotFoundException e) {
		}
	}

	@Test
	public void testDump() throws Exception {
		final byte[] dump = controller.dump(false);
		final ByteArrayInputStream input = new ByteArrayInputStream(dump);

		final ExecutionDataReader reader = new ExecutionDataReader(input);
		final ExecutionDataStore execStore = new ExecutionDataStore();
		reader.setExecutionDataVisitor(execStore);
		final SessionInfoStore infoStore = new SessionInfoStore();
		reader.setSessionInfoVisitor(infoStore);
		reader.read();

		assertEquals("Foo", execStore.get(0x12345678).getName());

		final List<SessionInfo> infos = infoStore.getInfos();
		assertEquals(1, infos.size());
		assertEquals("stubid", infos.get(0).getId());

		controller.shutdown();
	}

	@Test
	public void testDumpWithReset() throws Exception {
		controller.dump(true);
		runtime.assertNoProbes();

		controller.shutdown();
	}

	@Test
	public void testReset() throws Exception {
		controller.reset();
		runtime.assertNoProbes();

		controller.shutdown();
	}

	@Test
	public void testGetSessionId() throws Exception {
		assertEquals("stubid", controller.getSessionId());

		controller.shutdown();
	}

	@Test
	public void testSetSessionId() throws Exception {
		controller.setSessionId("newid");
		assertEquals("newid", runtime.getSessionId());

		controller.shutdown();
	}

	@Test
	public void testGetVersion() throws Exception {
		assertEquals(JaCoCo.VERSION, controller.getVersion());

		controller.shutdown();
	}
}
