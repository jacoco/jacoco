/*******************************************************************************
 * Copyright (c) 2009, 2016 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Omer Azmon - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.ebigo.core;

import static org.jacoco.ebigo.internal.util.ValidationUtils.validateNotNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Map.Entry;

import org.jacoco.core.data.ExecutionDataReader;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.data.ExecutionDataWriter;
import org.jacoco.core.data.SessionInfoStore;
import org.jacoco.core.runtime.RemoteControlReader;
import org.jacoco.core.runtime.RemoteControlWriter;

/**
 * Contains execution information from a single workload applied to the software
 * under test, and the attribute value(s) associated with this workload. When
 * performing an Empirical Big-O fit to an attribute, the X-value identifying
 * this workload comes for that attribute's value.
 * 
 * @author Omer Azmon
 */
public class EmpiricalBigOWorkload {
	private final WorkloadAttributeMap attributeMap;
	private final ExecutionDataStore executionDataStore;
	private final SessionInfoStore sessionInfoStore;

	/**
	 * Construct an Empirical Big-O workload results container
	 * 
	 * @param attributeMap
	 *            a map containing a value for each attribute in the enumerator
	 *            associated with this class.
	 * @param executionDataStore
	 *            the execution data store containing the raw execution data
	 *            from probing the running application.
	 * @param sessionInfoStore
	 *            the session data from the probing session.
	 */
	public EmpiricalBigOWorkload(final WorkloadAttributeMap attributeMap,
			final ExecutionDataStore executionDataStore,
			final SessionInfoStore sessionInfoStore) {
		validateNotNull("attributeMap", attributeMap);
		validateNotNull("executionDataStore", executionDataStore);
		validateNotNull("sessionInfoStore", sessionInfoStore);
		this.attributeMap = attributeMap;
		this.executionDataStore = executionDataStore;
		this.sessionInfoStore = sessionInfoStore;
	}

	/**
	 * Get the immutable map of X-axis attributes and their values.
	 * 
	 * @return the immutable map of X-axis attributes and their values.
	 */
	public WorkloadAttributeMap getattributeMap() {
		return attributeMap;
	}

	/**
	 * Get the execution data store retrieved from the JaCoCo run.
	 * 
	 * @return the execution data store retrieved from the JaCoCo run.
	 */
	public ExecutionDataStore getExecutionDataStore() {
		return executionDataStore;
	}

	/**
	 * Get the session data retrieved from the JaCoCo run.
	 * 
	 * @return the session data retrieved from the JaCoCo run.
	 */
	public SessionInfoStore getSessionInfo() {
		return sessionInfoStore;
	}

	/**
	 * Write the workload. Two files are created. One with '.map' extension, and
	 * one with '.exec' extension. The workload can be read back in using read
	 * method.
	 * 
	 * @param resultsDir
	 *            the directory to place the workload
	 * @param localFileName
	 *            the file name minus the extension.
	 * @throws IOException
	 *             on any failure to write
	 */
	public void write(final File resultsDir, final String localFileName)
			throws IOException {
		final PrintStream mapPrinter = new PrintStream(new FileOutputStream(
				new File(resultsDir, localFileName + ".map")));
		for (final Entry<String, Integer> entry : attributeMap.entrySet()) {
			mapPrinter.print(entry.getKey());
			mapPrinter.print('=');
			mapPrinter.println(entry.getValue());
		}
		mapPrinter.close();

		final FileOutputStream localFileStream = new FileOutputStream(new File(
				resultsDir, localFileName + ".exec"));
		try {
			final ExecutionDataWriter localWriter = new ExecutionDataWriter(
					localFileStream);
			sessionInfoStore.accept(localWriter);
			executionDataStore.accept(localWriter);
			localFileStream.close();
		} catch (IOException e) {
			if (localFileStream != null) {
				localFileStream.close();
			}
			throw e;
		}
	}

	// For read
	private static WorkloadAttributeMap readattributeMap(final File resultsDir,
			final String localFileName) throws IOException {
		final BufferedReader mapReader = new BufferedReader(new FileReader(
				new File(resultsDir, localFileName + ".map")));
		final WorkloadAttributeMapBuilder builder = WorkloadAttributeMapBuilder
				.create();
		try {
			for (;;) {
				String line = mapReader.readLine();
				if (line == null) {
					break;
				}

				line = line.trim();
				if (line.startsWith("#")) {
					continue;
				}

				final String[] parts = line.split("[=]", 2);
				if (parts.length != 2) {
					continue;
				}

				try {
					final String key = parts[0].trim();
					final Integer value = Integer.parseInt(parts[1].trim());
					builder.add(key, value);
				} catch (IllegalArgumentException ex) {
					IOException e = new IOException("Bad attribute map entry");
					e.initCause(ex);
					throw e;
				}
			}
		} finally {
			mapReader.close();
		}
		return builder.build();
	}

	/**
	 * Read a workload from disk
	 * 
	 * @param resultsDir
	 *            the directory containing the two workload files.
	 * @param localFileName
	 *            the file name of both workload files (with .map, and .exec
	 *            extensions)
	 * @return a workload from desk.
	 * @throws IOException
	 *             on any failure to read
	 */
	public static EmpiricalBigOWorkload read(final File resultsDir,
			final String localFileName) throws IOException {

		final WorkloadAttributeMap attributeMap = readattributeMap(resultsDir,
				localFileName);

		final ExecutionDataStore executionDataStore = new ExecutionDataStore();
		final SessionInfoStore sessionInfoStore = new SessionInfoStore();
		final FileInputStream localFileStream = new FileInputStream(new File(
				resultsDir, localFileName + ".exec"));
		final ExecutionDataReader reader = new ExecutionDataReader(
				localFileStream);
		reader.setHeaderVisitor(executionDataStore);
		reader.setExecutionDataVisitor(executionDataStore);
		reader.setSessionInfoVisitor(sessionInfoStore);
		reader.read();
		localFileStream.close();

		return new EmpiricalBigOWorkload(attributeMap, executionDataStore,
				sessionInfoStore);
	}

	/**
	 * Read a workload from a running instance JaCoCo that is running in server
	 * mode.
	 * 
	 * @param attributeMap
	 *            a map containing a value for each attribute in the enumerator.
	 * @param writer
	 *            a JaCoCo remote control writer to send to request for the
	 *            data.
	 * @param reader
	 *            a JaCoCo remote control reader to receive the data.
	 * @return a workload from a running instance JaCoCo that is running in
	 *         server mode.
	 * @throws IOException
	 *             on any failure to read
	 */
	public static EmpiricalBigOWorkload readRemote(
			final WorkloadAttributeMap attributeMap,
			final RemoteControlWriter writer, final RemoteControlReader reader)
			throws IOException {

		final SessionInfoStore sessionInfoStore = new SessionInfoStore();
		reader.setSessionInfoVisitor(sessionInfoStore);
		final ExecutionDataStore executionDataStore = new ExecutionDataStore();
		reader.setHeaderVisitor(executionDataStore);
		reader.setExecutionDataVisitor(executionDataStore);

		// Send a dump command and read the response:
		writer.visitDumpCommand(true, true);
		reader.read();

		return new EmpiricalBigOWorkload(attributeMap, executionDataStore,
				sessionInfoStore);
	}
}