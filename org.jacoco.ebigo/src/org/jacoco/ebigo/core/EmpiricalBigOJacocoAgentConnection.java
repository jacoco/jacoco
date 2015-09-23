/*******************************************************************************
 * Copyright (c) 2009, 2015 Mountainminds GmbH & Co. KG and Contributors
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

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import org.jacoco.core.runtime.RemoteControlReader;
import org.jacoco.core.runtime.RemoteControlWriter;

/**
 * A JaCoCo agent connection that can be used to collect workload coverage
 * information for use in Empirical Big-O analysis. The agent's output mode mus
 * be 'tcpserver'. See package-info for more details on setting up the agent.
 * 
 * @author Omer Azmon
 */
public class EmpiricalBigOJacocoAgentConnection {

	private final Socket socket;
	private final RemoteControlWriter writer;
	private final RemoteControlReader reader;

	/**
	 * Construct a new connection
	 * 
	 * @param jacocoHost
	 *            the host or ip address where a JaCoCo agent in 'tcpserver'
	 *            mode is running.
	 * @param jacocoPort
	 *            the port on which the JaCoCo agent is listening.
	 * @throws UnknownHostException
	 *             if the @{code jacocoHost} cannot be resolved
	 * @throws IOException
	 *             on any failure to connect to the JaCoCo agent.
	 */
	public EmpiricalBigOJacocoAgentConnection(final String jacocoHost,
			final int jacocoPort) throws UnknownHostException, IOException {

		socket = new Socket(InetAddress.getByName(jacocoHost), jacocoPort);
		writer = new RemoteControlWriter(socket.getOutputStream());
		reader = new RemoteControlReader(socket.getInputStream());
	}

	/**
	 * Collect workload coverage information. The coverage information is since
	 * the agent has started or the last reset. One should invoke the @{code
	 * reset} method of this class just before starting a workload to ensure the
	 * purity of the coverage data.
	 * 
	 * @param attributeMap
	 *            a map of X-axis attributes and values that will be associated
	 *            with this workload.
	 * @return the workload
	 * @throws IOException
	 *             on any communication failure
	 */
	public EmpiricalBigOWorkload fetchWorkloadCoverage(
			final WorkloadAttributeMap attributeMap) throws IOException {
		final EmpiricalBigOWorkload workload = EmpiricalBigOWorkload
				.readRemote(attributeMap, writer, reader);
		return workload;
	}

	/**
	 * Collect workload coverage information. The coverage information is since
	 * the agent has started or the last reset. One should invoke the @{code
	 * reset} method of this class just before starting a workload to ensure the
	 * purity of the coverage data. The attribute used is the
	 * {@code WorkloadAttributeMapBuilder.DEFAULT_ATTRIBUTE}
	 * 
	 * @param attributeValue
	 *            the value of the attribute named
	 * @return the workload
	 * @throws IOException
	 *             on any communication failure
	 */
	public EmpiricalBigOWorkload fetchWorkloadCoverage(final int attributeValue)
			throws IOException {
		final WorkloadAttributeMap attributeMap = WorkloadAttributeMapBuilder
				.create(attributeValue).build();
		return fetchWorkloadCoverage(attributeMap);
	}

	/**
	 * Collect workload coverage information. The coverage information is since
	 * the agent has started or the last reset. One should invoke the @{code
	 * reset} method of this class just before starting a workload to ensure the
	 * purity of the coverage data.
	 * 
	 * @param attributeName
	 *            the name of a single X-axis attribute that will be associated
	 *            with this workload.
	 * @param attributeValue
	 *            the value of the attribute named
	 * @return the workload
	 * @throws IOException
	 *             on any communication failure
	 */
	public EmpiricalBigOWorkload fetchWorkloadCoverage(
			final String attributeName, final int attributeValue)
			throws IOException {
		final WorkloadAttributeMap attributeMap = WorkloadAttributeMapBuilder
				.create(attributeName, attributeValue).build();
		return fetchWorkloadCoverage(attributeMap);
	}

	/**
	 * Reset all coverage counters in the agent to zero.
	 * 
	 * @throws IOException
	 *             on any communication failure
	 */
	public final void resetCoverage() throws IOException {
		writer.visitDumpCommand(false, true);
		reader.read();
	}

	/**
	 * Close the connection. Once closed, it cannot be reopened. A new
	 * connection must be established.
	 * 
	 * @throws IOException
	 *             on any failure to close the underlying socket
	 */
	public final void close() throws IOException {
		socket.close();
	}

}
