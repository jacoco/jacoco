/*******************************************************************************
 * Copyright (c) 2009, 2012 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.core.data;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Convenience utility for loading *.exec files into a
 * {@link ExecutionDataStore} and a {@link SessionInfoStore}.
 */
public class ExecFileLoader {

	private final SessionInfoStore sessionInfos;
	private final ExecutionDataStore executionData;

	/**
	 * New instance to combine session infos and execution data from multiple
	 * files.
	 */
	public ExecFileLoader() {
		sessionInfos = new SessionInfoStore();
		executionData = new ExecutionDataStore();
	}

	/**
	 * Reads all data from given input stream.
	 * 
	 * @param stream
	 *            Stream to read data from
	 * @throws IOException
	 *             in case of problems while reading from the stream
	 */
	public void load(final InputStream stream) throws IOException {
		final ExecutionDataReader reader = new ExecutionDataReader(
				new BufferedInputStream(stream));
		reader.setExecutionDataVisitor(executionData);
		reader.setSessionInfoVisitor(sessionInfos);
		reader.read();
	}

	/**
	 * Reads all data from given input stream.
	 * 
	 * @param file
	 *            file to read data from
	 * @throws IOException
	 *             in case of problems while reading from the stream
	 */
	public void load(final File file) throws IOException {
		final InputStream stream = new FileInputStream(file);
		try {
			load(stream);
		} finally {
			stream.close();
		}
	}

	/**
	 * Returns the session info store with all loaded sessions.
	 * 
	 * @return session info store
	 */
	public SessionInfoStore getSessionInfoStore() {
		return sessionInfos;
	}

	/**
	 * Returns the execution data store with data for all loaded classes.
	 * 
	 * @return execution data store
	 */
	public ExecutionDataStore getExecutionDataStore() {
		return executionData;
	}

}
