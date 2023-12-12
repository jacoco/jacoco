/*******************************************************************************
 * Copyright (c) 2009, 2023 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.tools;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.jacoco.core.data.ExecutionDataReader;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.data.ExecutionDataWriter;
import org.jacoco.core.data.SessionInfoStore;

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
	 * Saves the current content into the given output stream.
	 *
	 * @param stream
	 *            stream to save content to
	 * @throws IOException
	 *             in case of problems while writing to the stream
	 */
	public void save(final OutputStream stream) throws IOException {
		final ExecutionDataWriter dataWriter = new ExecutionDataWriter(stream);
		sessionInfos.accept(dataWriter);
		executionData.accept(dataWriter);
	}

	/**
	 * Saves the current content into the given file. Parent directories are
	 * created as needed. Also a files system lock is acquired to avoid
	 * concurrent write access.
	 *
	 * @param file
	 *            file to save content to
	 * @param append
	 *            <code>true</code> if the content should be appended, otherwise
	 *            the file is overwritten.
	 * @throws IOException
	 *             in case of problems while writing to the stream
	 */
	public void save(final File file, final boolean append) throws IOException {
		final File folder = file.getParentFile();
		if (folder != null) {
			folder.mkdirs();
		}
		final FileOutputStream fileStream = new FileOutputStream(file, append);
		// Avoid concurrent writes from other processes:
		fileStream.getChannel().lock();
		final OutputStream bufferedStream = new BufferedOutputStream(
				fileStream);
		try {
			save(bufferedStream);
		} finally {
			bufferedStream.close();
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
