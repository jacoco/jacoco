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

import java.util.HashMap;

import javax.management.MBeanServerConnection;
import javax.management.MBeanServerInvocationHandler;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

public class JmxTarget {

	public static void main(String[] args) throws Exception {
		final JMXServiceURL url = new JMXServiceURL(
				"service:jmx:rmi:///jndi/rmi://127.0.0.1:9999/jmxrmi");
		final HashMap<String, Object> environment = new HashMap<String, Object>();
		environment.put("jmx.remote.credentials",
				new String[] { "user", "password" });
		final JMXConnector jmxConnector = JMXConnectorFactory.connect(url,
				environment);
		final MBeanServerConnection mBeanServerConnection = jmxConnector
				.getMBeanServerConnection();
		final IProxy proxy = (IProxy) MBeanServerInvocationHandler
				.newProxyInstance(mBeanServerConnection,
						new ObjectName("org.jacoco:type=Runtime"), IProxy.class,
						false);
		// Note that "user" must have "readwrite" permission to perform all
		// operations
		proxy.getExecutionData(false);
		// "readonly" permission gives access only to plain getters such as
		System.out.println("Target executed: " + proxy.getSessionId());
		jmxConnector.close();
	}

	public interface IProxy {
		String getSessionId();

		byte[] getExecutionData(boolean reset);
	}

}
