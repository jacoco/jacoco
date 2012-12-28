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
package org.jacoco.agent.rt.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.lang.management.ManagementFactory;
import java.util.List;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.ObjectName;

import org.jacoco.core.JaCoCo;
import org.jacoco.core.data.ExecutionDataReader;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.data.SessionInfo;
import org.jacoco.core.data.SessionInfoStore;
import org.jacoco.core.runtime.AgentOptions;
import org.jacoco.core.runtime.RuntimeData;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link MBeanController}.
 */
public class MBeanControllerTest {

	private RuntimeData data;

	private MBeanController controller;

	@Before
	public void setup() throws Exception {
		data = new RuntimeData();
		controller = new MBeanController();
		controller.startup(new AgentOptions(), data);
	}

	@Test
	public void testStartupAndShutdown() throws Exception {
		final ObjectName objectName = new ObjectName("org.jacoco:type=Runtime");

		final MBeanInfo info = ManagementFactory.getPlatformMBeanServer()
				.getMBeanInfo(objectName);

		final MBeanAttributeInfo[] attributes = info.getAttributes();
		assertEquals(2, attributes.length);

		for (MBeanAttributeInfo attribute : attributes) {
			if ("SessionId".equals(attribute.getName())) {
				assertEquals(String.class.getName(), attribute.getType());
			} else if ("Version".equals(attribute.getName())) {
				assertEquals(String.class.getName(), attribute.getType());
			} else {
				fail("Unexpected attribute: " + attribute.getName());
			}
		}

		final MBeanOperationInfo[] operations = info.getOperations();
		assertEquals(2, info.getOperations().length);

		for (MBeanOperationInfo operation : operations) {
			if ("reset".equals(operation.getName())) {
				assertEquals(void.class.getName(), operation.getReturnType());
				assertEquals(0, operation.getSignature().length);
			} else if ("dump".equals(operation.getName())) {
				assertEquals(byte[].class.getName(), operation.getReturnType());
				assertEquals(1, operation.getSignature().length);
				assertEquals(boolean.class.getName(),
						operation.getSignature()[0].getType());
			} else {
				fail("Unexpected operation: " + operation.getName());
			}
		}

		controller.shutdown();

		try {
			ManagementFactory.getPlatformMBeanServer().getMBeanInfo(objectName);
			fail("MBean was not deregistered");
		} catch (InstanceNotFoundException e) {
		}
	}

	@Test
	public void testDump() throws Exception {
		data.getExecutionData(Long.valueOf(0x12345678), "Foo", 42);
		data.setSessionId("stubid");

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
		assertNoProbes();

		controller.shutdown();
	}

	@Test
	public void testReset() throws Exception {
		controller.reset();
		assertNoProbes();

		controller.shutdown();
	}

	@Test
	public void testGetSessionId() throws Exception {
		data.setSessionId("stubid");

		assertEquals("stubid", controller.getSessionId());

		controller.shutdown();
	}

	@Test
	public void testSetSessionId() throws Exception {
		controller.setSessionId("newid");
		assertEquals("newid", data.getSessionId());

		controller.shutdown();
	}

	@Test
	public void testGetVersion() throws Exception {
		assertEquals(JaCoCo.VERSION, controller.getVersion());

		controller.shutdown();
	}

	private void assertNoProbes() {
		ExecutionDataStore store = new ExecutionDataStore();
		data.collect(store, new SessionInfoStore(), false);
		assertTrue(store.getContents().isEmpty());
	}
}
