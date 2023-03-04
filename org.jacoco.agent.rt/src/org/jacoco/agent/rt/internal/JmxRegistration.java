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

import java.lang.management.ManagementFactory;
import java.util.concurrent.Callable;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.StandardMBean;

import org.jacoco.agent.rt.IAgent;

/**
 * Access to JMX APIs are encapsulated in this class to allow the JaCoCo runtime
 * on platforms without JMX support (e.g Android).
 */
class JmxRegistration implements Callable<Void> {

	private static final String JMX_NAME = "org.jacoco:type=Runtime";

	private final MBeanServer server;
	private final ObjectName name;

	JmxRegistration(final IAgent agent) throws Exception {
		server = ManagementFactory.getPlatformMBeanServer();
		name = new ObjectName(JMX_NAME);
		server.registerMBean(new StandardMBean(agent, IAgent.class), name);
	}

	/**
	 * De-register the agent again.
	 */
	public Void call() throws Exception {
		server.unregisterMBean(name);
		return null;
	}

}
