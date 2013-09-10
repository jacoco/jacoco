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

import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.apache.maven.plugin.MojoExecutionException;

/**
 * Stop a jacoco tcp server collector
 * 
 * @goal stop-tcpserver-collector
 * 
 * @phase post-integration-test
 */
public class StopServerCollectorMojo extends AbstractJacocoMojo {

	/**
	 * Project properties
	 * 
	 * @parameter expression="${project.properties}"
	 * @required
	 * @readonly
	 */
	private Properties projectProperties;

	/**
	 * Execution Id
	 * 
	 * @parameter default-value="${mojoExecution.executionId}"
	 * @required
	 * @readonly
	 */
	private String executionId;

	/**
	 * Maximum seconds to wait for socket shutdown
	 * 
	 * @parameter expression="10"
	 * @required
	 */
	private int waitTime;

	@Override
	public void executeMojo() throws MojoExecutionException {
		final CollectorServer collectorServer = getInstance(CollectorServer.class);
		collectorServer.stop(TimeUnit.SECONDS.toMillis(waitTime));
	}

	private <T> T getInstance(final Class<T> clss)
			throws MojoExecutionException {
		final String instanceName = clss.getCanonicalName() + '#' + executionId;
		final Object optValue = projectProperties.get(instanceName);
		if (!clss.isInstance(optValue)) {
			throw new MojoExecutionException("Property '" + instanceName
					+ "' (" + optValue + ") is not a "
					+ clss.getCanonicalName());
		}
		return clss.cast(optValue);
	}
}
