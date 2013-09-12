/*******************************************************************************
 * Copyright (c) 2009, 2013 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Chas Honton - initial implementation
 *
 *******************************************************************************/
package org.jacoco.maven;

import java.io.IOException;

import org.apache.maven.plugin.MojoExecutionException;
import org.jacoco.core.runtime.AgentOptions;
import org.jacoco.core.runtime.AgentOptions.OutputMode;

/**
 * Start a jacoco tcp server collector
 * 
 * @goal start-tcpserver-collector
 * 
 * @phase process-test-classes
 */
public class StartServerCollectorMojo extends AbstractCollectorMojo {

	/**
	 * Execution Id
	 * 
	 * @parameter default-value="${mojoExecution.executionId}"
	 * @required
	 * @readonly
	 */
	private String executionId;

	@Override
	public void executeMojo() throws MojoExecutionException {
		try {
			final AgentOptions options = getConfiguration(OutputMode.tcpclient);
			final ServerCollector server = new ServerCollector(options,
					getLog());
			saveInstance(server);
			server.startListener();
		} catch (final IOException e) {
			throw new MojoExecutionException("Error starting tcp-server", e);
		}
	}

	/**
	 * Save the instance so that {@link StopCollectorMajo} can call
	 * {@link ServerCollector#stop(long)}
	 * 
	 * @param instance
	 *            The CollectorServer instance
	 */
	private void saveInstance(final Object instance) {
		final String instanceName = instance.getClass().getCanonicalName()
				+ '#' + executionId;
		projectProperties.put(instanceName, instance);
	}
}
