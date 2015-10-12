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
public class EmpiricalBigOJacocoAgentConnection implements IEBigOConnection {

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

	public EmpiricalBigOWorkload fetchWorkloadCoverage(
			final WorkloadAttributeMap attributeMap) throws IOException {
		final EmpiricalBigOWorkload workload = EmpiricalBigOWorkload
				.readRemote(attributeMap, writer, reader);
		return workload;
	}

	public EmpiricalBigOWorkload fetchWorkloadCoverage(final int attributeValue)
			throws IOException {
		final WorkloadAttributeMap attributeMap = WorkloadAttributeMapBuilder
				.create(attributeValue).build();
		return fetchWorkloadCoverage(attributeMap);
	}

	public EmpiricalBigOWorkload fetchWorkloadCoverage(
			final String attributeName, final int attributeValue)
			throws IOException {
		final WorkloadAttributeMap attributeMap = WorkloadAttributeMapBuilder
				.create(attributeName, attributeValue).build();
		return fetchWorkloadCoverage(attributeMap);
	}

	public void resetCoverage() throws IOException {
		writer.visitDumpCommand(false, true);
		reader.read();
	}

	public final void close() throws IOException {
		socket.close();
	}

}
