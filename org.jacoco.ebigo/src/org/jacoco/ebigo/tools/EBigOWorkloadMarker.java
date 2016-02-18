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
package org.jacoco.ebigo.tools;

import java.io.File;
import java.io.IOException;

import org.jacoco.core.data.ProbeMode;
import org.jacoco.core.internal.instr.ProbeArrayService;
import org.jacoco.core.runtime.AgentOptions;
import org.jacoco.ebigo.core.EmpiricalBigOJacocoAgentConnection;
import org.jacoco.ebigo.core.EmpiricalBigOWorkload;
import org.jacoco.ebigo.core.IEBigOConnection;
import org.jacoco.ebigo.core.WorkloadAttributeMapBuilder;

/**
 * <p>
 * Turn on ebigo=true, probe=count|parallelcount, output=tcpserver.
 * 
 * <ul>
 * <li>project.build.directory
 * <li>jacoco.hostname
 * <li>jacoco.port
 * <li>jacoco.destfile
 * <li>jacoco.ebigoAttribute
 * </ul>
 * 
 */
public final class EBigOWorkloadMarker {

	private static EBigOWorkloadMarker instance;

	/**
	 * Get a singleton instance of a workload marker with probe mode defaulting
	 * to <code>ProbeMode.parallelcount</code>.
	 * <p>
	 * Multiple calls to this method return the same object, until the
	 * {@code close} method is invoked on it. Invoking this method again at that
	 * point will produce a new marker.
	 * 
	 * @return the singleton instance
	 */
	public static EBigOWorkloadMarker getInstance() {
		return getInstance(ProbeMode.parallelcount);
	}

	/**
	 * Get a singleton instance of a workload marker.
	 * <p>
	 * Multiple calls to this method return the same object, until the
	 * {@code close} method is invoked on it. Invoking this method again at that
	 * point will produce a new marker.
	 * 
	 * @param requestedProbeMode
	 *            the designed probe. Should be either
	 *            <code>ProbeMode.count</code> or
	 *            <code>ProbeMode.parallelcount</code> and match whatever was
	 *            setup server-side.
	 * @return the singleton instance
	 */
	public static EBigOWorkloadMarker getInstance(ProbeMode requestedProbeMode) {
		if (instance == null) {
			synchronized (EBigOWorkloadMarker.class) {
				if (instance == null) {
					ProbeArrayService.configure(requestedProbeMode);
					instance = new EBigOWorkloadMarker();
				}
			}
		}
		return instance;
	}

	/* External Properties */
	private String hostname;
	private int port;
	private String destfile;
	private String projectBuildDir;
	private String ebigoAttribute;

	/** The connection to the remote Jacoco Agent */
	private IEBigOConnection connection;

	private EBigOWorkloadMarker() {
		this.hostname = System.getProperty("jacoco.hostname", "localhost");
		this.port = Integer.parseInt(System.getProperty("jacoco.hostname",
				Integer.toString(AgentOptions.DEFAULT_PORT)));
		this.projectBuildDir = System.getProperty("project.build.directory",
				"target");
		this.destfile = System.getProperty("jacoco.destfile", "jacoco.exec");
		this.ebigoAttribute = System.getProperty("jacoco.ebigoAttribute",
				WorkloadAttributeMapBuilder.DEFAULT_ATTRIBUTE);
	}

	// We are doing this like this because we need late connection initiation
	// So user has a chance to change attributes before connection
	private IEBigOConnection getConnection()
			throws IOException {
		if (connection == null) {
			synchronized (EBigOWorkloadMarker.class) {
				if (connection == null) {
					connection = new EmpiricalBigOJacocoAgentConnection(
							hostname, port);
				}

			}
		}
		return connection;
	}

	/**
	 * Inform the JaCoCo agent that a workload is about to begin. The agent is
	 * expected to reset all its counters to zero at this point.
	 * 
	 * @throws IOException
	 *             on failure to communicate with the agent.
	 */
	public void beginWorkload() throws IOException {
		getConnection().resetCoverage();
	}

	/**
	 * Inform the JaCoCo agent that a workload has finished. The agent is
	 * expected to send back the execution data, which is stored destination
	 * directory.
	 * 
	 * @param value
	 *            the value that is attached to this workload. The number of
	 *            requests, bytes, etc. in this workload. The values associated
	 *            with all the workloads participating in a single Empirical
	 *            Big-O analysis, must of course be of the same kind. Each
	 *            workload in the analysis should be of a different size, and so
	 *            this value would be different. The object is to get a trend.
	 * @throws IOException
	 *             on failure to communicate with the agent.
	 */
	public void endWorkload(int value) throws IOException {
		final EmpiricalBigOWorkload workload = getConnection()
				.fetchWorkloadCoverage(ebigoAttribute, value);
		File resultsFile = getResultsFile(value);
		workload.write(resultsFile.getParentFile(), resultsFile.getName());
	}

	private String stripExtention(String filename) {
		if (filename.endsWith(".exec")) {
			filename = filename.substring(0,
					filename.length() - ".exec".length());
		}
		return filename;
	}

	private File getResultsFile(int value) {
		File resultsDir = new File(projectBuildDir, destfile);
		String localFileName = "jacoco";
		if (!resultsDir.exists()) {
			resultsDir.mkdirs();
		} else if (resultsDir.isFile()) {
			localFileName = stripExtention(resultsDir.getName());
			resultsDir = resultsDir.getParentFile();
		} else if (!resultsDir.isDirectory()) {
			throw new IllegalArgumentException(
					"Unable to write execution results to " + resultsDir);
		}

		return new File(resultsDir, localFileName + '-' + value);
	}

	public String getHostname() {
		return hostname;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getDestfile() {
		return destfile;
	}

	public void setDestfile(String destfile) {
		this.destfile = destfile;
	}

	public String getProjectBuildDir() {
		return projectBuildDir;
	}

	public void setProjectBuildDir(String projectBuildDir) {
		this.projectBuildDir = projectBuildDir;
	}

	public String getEbigoAttribute() {
		return ebigoAttribute;
	}

	public void setEbigoAttribute(String ebigoAttribute) {
		this.ebigoAttribute = ebigoAttribute;
	}

	/**
	 * Close this marker and its connection to the agent. This invalidate the
	 * marker for further use, and causes the singelton method above to generate
	 * a fresh instance when it is next invoked.
	 */
	public void close() {
		if (connection != null) {
			try {
				connection.close();
			} catch (IOException e) {
				// Ignore
			}
		}
		instance = null;
	}
}