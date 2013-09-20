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
import java.util.Properties;

import org.apache.maven.plugin.MojoExecutionException;

/**
 * Start a jacoco tcp server collector
 * 
 * @goal dump
 * 
 * @phase post-integration-test
 */
public class DumpMojo extends AbstractJacocoMojo {

	/**
	 * Project properties
	 * 
	 * @parameter expression="${project.properties}"
	 * @required
	 * @readonly
	 */
	protected Properties projectProperties;

	/**
	 * Path to the output file for execution data.
	 * 
	 * @parameter expression="${jacoco.destFile}"
	 *            default-value="${project.build.directory}/jacoco.exec"
	 */
	private File destFile;

	/**
	 * If set to true and the execution data file already exists, coverage data
	 * is appended to the existing file. If set to false, an existing execution
	 * data file will be replaced.
	 * 
	 * @parameter expression="${jacoco.append}" default-value="true"
	 */
	private boolean append;

	/**
	 * IP address or hostname of the jacoco tcpserver collector.
	 * 
	 * @parameter expression="${jacoco.address}" default-value="127.0.0.1"
	 */
	private String address;

	/**
	 * Port of the jacoco tcpserver collector. If multiple JaCoCo agents should
	 * run on the same machine, different ports have to be specified. If this
	 * value is 0, the property setup by the prepare-agent goal will be parsed
	 * to determine the os allocated port.
	 * 
	 * @parameter expression="${jacoco.port}" default-value="6300"
	 */
	private int port;

	/**
	 * Specify the property which contains settings for JaCoCo Agent. If not
	 * specified, then "argLine" will be used. The value of this property is
	 * used when portNumber is zero to find the previously assigned port number
	 * .
	 * 
	 * @parameter expression="${jacoco.propertyName}" default-value="argLine"
	 */
	private String propertyName;

	@Override
	public void executeMojo() throws MojoExecutionException {
		try {
			final ClientCollector client = new ClientCollector(address,
					getPort(), destFile, append);
			client.dump();
		} catch (final IOException e) {
			throw new MojoExecutionException("Error in dump", e);
		}
	}

	private int getPort() throws MojoExecutionException {
		if (port != 0) {
			return port;
		}
		// get value saved by prepare-agent goal
		final Object value = projectProperties.get(propertyName + ".port");
		if (!(value instanceof Integer)) {
			throw new MojoExecutionException("Property '" + propertyName
					+ "' (" + value + ") does not specify port");
		}
		return ((Integer) value).intValue();
	}
}
