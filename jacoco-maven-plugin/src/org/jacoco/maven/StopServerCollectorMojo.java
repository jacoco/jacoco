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
	 * Collector name
	 * 
	 * @parameter expression="default"
	 * @required
	 */
	private String name;

	/**
	 * Maximum seconds to wait for socket shutdown
	 * 
	 * @parameter expression="10"
	 * @required
	 */
	private int waitTime;

	@Override
	public void executeMojo() throws MojoExecutionException {
		final Object c = getProject().getProperties().get(
				"jacoco-collector-server." + name);
		if (c instanceof CollectorServer) {
			final CollectorServer server = (CollectorServer) c;
			server.stop(TimeUnit.SECONDS.toMillis(waitTime));
		} else {
			getLog().info("Jacoco Collector " + name + " not found");
		}
	}
}
