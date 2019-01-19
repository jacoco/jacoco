/*******************************************************************************
 * Copyright (c) 2009, 2019 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.examples;

import java.io.FileOutputStream;

import javax.management.MBeanServerConnection;
import javax.management.MBeanServerInvocationHandler;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

/**
 * This example connects to a JaCoCo agent that runs with the option
 * <code>jmx=yes</code> and requests execution data. The collected data is
 * dumped to a local file.
 */
public final class MBeanClient {

	private static final String DESTFILE = "jacoco-client.exec";

	private static final String SERVICE_URL = "service:jmx:rmi:///jndi/rmi://localhost:9999/jmxrmi";

	/**
	 * Execute the example.
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(final String[] args) throws Exception {
		// Open connection to the coverage agent:
		final JMXServiceURL url = new JMXServiceURL(SERVICE_URL);
		final JMXConnector jmxc = JMXConnectorFactory.connect(url, null);
		final MBeanServerConnection connection = jmxc
				.getMBeanServerConnection();

		final IProxy proxy = (IProxy) MBeanServerInvocationHandler
				.newProxyInstance(connection, new ObjectName(
						"org.jacoco:type=Runtime"), IProxy.class, false);

		// Retrieve JaCoCo version and session id:
		System.out.println("Version: " + proxy.getVersion());
		System.out.println("Session: " + proxy.getSessionId());

		// Retrieve dump and write to file:
		final byte[] data = proxy.getExecutionData(false);
		final FileOutputStream localFile = new FileOutputStream(DESTFILE);
		localFile.write(data);
		localFile.close();

		// Close connection:
		jmxc.close();
	}

	interface IProxy {
		String getVersion();

		String getSessionId();

		void setSessionId(String id);

		byte[] getExecutionData(boolean reset);

		void dump(boolean reset);

		void reset();
	}

	private MBeanClient() {
	}
}
