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

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;

/**
 * Start a jacoco tcp server collector
 * 
 * @goal start-tcpserver-collector
 * 
 * @phase process-test-classes
 */
public class StartServerCollectorMojo extends AbstractJacocoMojo {
	/**
	 * Location of the single execution file.
	 * 
	 * @parameter expression="${project.build.directory}/jacoco.exec"
	 * @required
	 */
	private File outputFile;

	/**
	 * Server binding address
	 * 
	 * @parameter expression="localhost"
	 * @required
	 */
	private String address;

	/**
	 * Server binding address
	 * 
	 * @parameter expression="6300"
	 * @required
	 */
	private int port;

	/**
	 * Collector name
	 * 
	 * @parameter expression="default"
	 * @required
	 */
	private String name;

	@Override
	public void executeMojo() throws MojoExecutionException {
		final Log log = getLog();
		try {
			final SocketAddress socketAddress = new InetSocketAddress(
					InetAddress.getByName(address), port);
			getProject().getProperties().put("jacoco-collector-server." + name,
					new CollectorServer(outputFile, socketAddress, log));
		} catch (final IOException e) {
			log.error(e);
		}
	}
}
