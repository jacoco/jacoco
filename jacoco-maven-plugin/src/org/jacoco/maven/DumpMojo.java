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
 * @goal dump
 * 
 * @phase post-integration-test
 */
public class DumpMojo extends AbstractCollectorMojo {

	@Override
	public void executeMojo() throws MojoExecutionException {
		try {
			final AgentOptions options = getConfiguration(OutputMode.tcpserver);
			final ClientCollector client = new ClientCollector(options);
			client.dump();
		} catch (final IOException e) {
			throw new MojoExecutionException("Error in dump", e);
		}
	}
}
