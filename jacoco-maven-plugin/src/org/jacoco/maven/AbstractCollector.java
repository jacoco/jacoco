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

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import org.apache.maven.plugin.MojoExecutionException;
import org.jacoco.core.runtime.AgentOptions;

/**
 * Base class for tcp collectors
 */
public class AbstractCollector {

	final AgentOptions options;

	AbstractCollector(final AgentOptions options) {
		this.options = options;
	}

	InetSocketAddress getSocketAddress(final boolean localHostAsDefault)
			throws MojoExecutionException {
		InetAddress inetAddress = null;
		String address = options.getAddress();
		if (address == null && localHostAsDefault) {
			address = "127.0.0.1";
		}
		if (address != null) {
			try {
				inetAddress = InetAddress.getByName(address);
			} catch (final UnknownHostException e) {
				throw new MojoExecutionException("Can not resolve " + address,
						e);
			}
		}
		final InetSocketAddress endpoint = new InetSocketAddress(inetAddress,
				options.getPort());
		return endpoint;
	}

	FileOutputStream createFileOutputStream() throws MojoExecutionException {
		final String destFile = options.getDestfile();
		final boolean append = options.getAppend();
		try {
			final FileOutputStream fs = new FileOutputStream(destFile, append);
			fs.getChannel().lock();
			return fs;
		} catch (final FileNotFoundException e) {
			throw new MojoExecutionException("Can not find " + destFile, e);
		} catch (final IOException e) {
			throw new MojoExecutionException("Can not lock " + destFile, e);
		}
	}
}