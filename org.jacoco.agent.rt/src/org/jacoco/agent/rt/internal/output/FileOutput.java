/*******************************************************************************
 * Copyright (c) 2009, 2023 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Brock Janiczak - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.agent.rt.internal.output;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.nio.channels.OverlappingFileLockException;

import org.jacoco.core.data.ExecutionDataWriter;
import org.jacoco.core.runtime.AgentOptions;
import org.jacoco.core.runtime.RuntimeData;

/**
 * Local only agent output that will write coverage data to the filesystem. This
 * controller uses the following agent options:
 * <ul>
 * <li>destfile</li>
 * <li>append</li>
 * </ul>
 */
public class FileOutput implements IAgentOutput {

	private static final int LOCK_RETRY_COUNT = 30;

	private static final long LOCK_RETRY_WAIT_TIME_MS = 100;

	private RuntimeData data;

	private File destFile;

	private boolean append;

	public final void startup(final AgentOptions options,
			final RuntimeData data) throws IOException {
		this.data = data;
		this.destFile = new File(options.getDestfile()).getAbsoluteFile();
		this.append = options.getAppend();
		final File folder = destFile.getParentFile();
		if (folder != null) {
			folder.mkdirs();
		}
		// Make sure we can write to the file:
		openFile().close();
	}

	public void writeExecutionData(final boolean reset) throws IOException {
		final OutputStream output = openFile();
		try {
			final ExecutionDataWriter writer = new ExecutionDataWriter(output);
			data.collect(writer, writer, reset);
		} finally {
			output.close();
		}
	}

	public void shutdown() throws IOException {
		// Nothing to do
	}

	private OutputStream openFile() throws IOException {
		final FileOutputStream file = new FileOutputStream(destFile, append);
		// Avoid concurrent writes from different agents running in parallel:
		final FileChannel fc = file.getChannel();
		int retries = 0;
		while (true) {
			try {
				// An agent from another JVM might have a lock. In this case
				// this method blocks until the lock is freed.
				fc.lock();
				return file;
			} catch (final OverlappingFileLockException e) {
				// In the case of multiple class loaders there can be multiple
				// JaCoCo runtimes even in the same VM. In this case we get an
				// OverlappingFileLockException and retry lock acquisition:
				if (retries++ > LOCK_RETRY_COUNT) {
					throw e;
				}
			}
			try {
				Thread.sleep(LOCK_RETRY_WAIT_TIME_MS);
			} catch (final InterruptedException e) {
				throw new InterruptedIOException();
			}
		}
	}

}
