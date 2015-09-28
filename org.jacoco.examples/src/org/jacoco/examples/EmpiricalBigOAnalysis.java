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
package org.jacoco.examples;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;

import org.jacoco.ebigo.core.EmpiricalBigOJacocoAgentConnection;
import org.jacoco.ebigo.core.EmpiricalBigOWorkloadStore;

/**
 * This example performs an Empirical Big-O analysis. It sets up a client
 * connection to a JaCoCo agent installed in some web application server. It
 * runs 5 samples measuring the coverage of each independently. The samples are
 * of 1 to 5, respectively. The results are then analyzed and dumped.
 * <p>
 * The example does not include the service under test, nor show the instrument
 * instrumentation by the agent.
 * <p>
 * The agent 'probemode' attribute must be set to 'count' or 'parallelcount' for
 * the results to be available.
 *
 * 
 * @author Omer Azmon
 */
public class EmpiricalBigOAnalysis {
	private static final String JACOCO_HOST = "127.0.0.1";
	private static final int JACOCO_PORT = 8081;
	private static final String SERVLET_URL = "http://localhost/somepath";
	private static final File SOURCE_DIR = new File("src/main/java");
	private static final File CLASS_DIR = new File("target/classes");
	private static final File OUTPUT_DIR = new File("target/jacoco/report");
	private static final String BUNDLE_NAME = "EmpiricalBigO Example";
	private static final String X_AXIS_ATTRIUBTE = "REQUESTS";

	/** The connection to the remote Jacoco Agent */
	private EmpiricalBigOJacocoAgentConnection connection;

	/** Where we keep the results of each work load we run */
	private EmpiricalBigOWorkloadStore workloadStore;

	/**
	 * Setup the connection to the remote JaCoCo Agent. This is similar to the
	 * ExecutionDataClient example, but has some extra stuff we need for big-o
	 * analysis.
	 * 
	 * @throws UnknownHostException
	 *             on failure to resolve host to IP address
	 * @throws IOException
	 *             on any other failure
	 */
	public void initEmpiricalBigO() throws UnknownHostException, IOException {
		connection = new EmpiricalBigOJacocoAgentConnection(JACOCO_HOST,
				JACOCO_PORT);
		workloadStore = new EmpiricalBigOWorkloadStore(X_AXIS_ATTRIUBTE);
	}

	/**
	 * Reset all coverage counters to zero. We should do this before we run a
	 * sample, so each workload measurement is pure.
	 * 
	 * @throws IOException
	 *             on any issues
	 */
	public void clearCoverageCounters() throws IOException {
		connection.resetCoverage();
	}

	/**
	 * Here we run the sample workloads for which we want to have coverage
	 * 
	 * @param sampleSize
	 *            how many requests in the sample
	 * @throws IOException
	 *             on any failure but URL issues
	 * @throws MalformedURLException
	 *             on any URL issues
	 */
	public void runOneSample(final int sampleSize)
			throws MalformedURLException, IOException {
		final String testTitle = X_AXIS_ATTRIUBTE + " size=" + sampleSize;

		for (int attempt = 1; attempt <= sampleSize; attempt++) {
			System.out.println(testTitle + " Request #" + attempt);

			doHttpGet(new URL(SERVLET_URL));

		}
		System.out.println();
	}

	/**
	 * An oversimplified HTTP GET implementation (so we don't make this example
	 * dependent on any tool such as, REST-assured or Apache httpClient).
	 * 
	 * @param url
	 *            url to read
	 * @return what we got
	 * @throws IOException
	 *             on any failure
	 */
	private String doHttpGet(final URL url) throws IOException {
		BufferedReader reader = null;
		final StringBuilder result = new StringBuilder();
		try {
			final HttpURLConnection connection = (HttpURLConnection) url
					.openConnection();
			connection.setRequestMethod("GET");
			reader = new BufferedReader(new InputStreamReader(
					connection.getInputStream()));
			String line;
			while ((line = reader.readLine()) != null) {
				result.append(line);
			}
			reader.close();
		} finally {
			if (reader != null) {
				reader.close();
			}
		}
		return result.toString();
	}

	/**
	 * Here we fetch the coverage information for the current worklaod and store
	 * it both to disk and a memory store. We don't have to do both, but good
	 * for demo.
	 * 
	 * @param sampleSize
	 *            how many requests are in the sample
	 * @throws IOException
	 *             on any failure
	 */
	public void fetchWorkloadCoverage(final int sampleSize) throws IOException {
		workloadStore.put(connection.fetchWorkloadCoverage(X_AXIS_ATTRIUBTE,
				sampleSize));
	}

	/**
	 * Here we take the workloads we generated, analyze, and dump a result.
	 * Nothing fancy.
	 * 
	 * @throws IOException
	 *             on any failures
	 */
	public void analyzeAndDumpResults() throws IOException {
		final EmpiricalBigOReportGenerator reportGenerator = new EmpiricalBigOReportGenerator(
				SOURCE_DIR, CLASS_DIR, OUTPUT_DIR);
		reportGenerator.generateReports(workloadStore, BUNDLE_NAME);
	}

	/**
	 * Here we clean up the connection
	 * 
	 * @throws IOException
	 *             on any failure
	 */
	public void close() throws IOException {
		connection.close();
	}

	/**
	 * Entry point to run this examples as a Java application.
	 * 
	 * @param args
	 *            list of program arguments (none here)
	 * @throws Exception
	 */
	public static void main(final String[] args) throws Exception {

		// Setup
		final EmpiricalBigOAnalysis instance = new EmpiricalBigOAnalysis();
		instance.initEmpiricalBigO();

		// Run 5 samples and measure the coverage for each
		for (int sampleSize = 1; sampleSize <= 5; sampleSize++) {
			instance.clearCoverageCounters();
			instance.runOneSample(sampleSize);
			instance.fetchWorkloadCoverage(sampleSize);
		}

		// Combine the results in an Empirical Big-O analysis and dump
		instance.analyzeAndDumpResults();
		instance.close();
	}
}
