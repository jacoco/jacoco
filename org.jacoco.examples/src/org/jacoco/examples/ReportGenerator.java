/*******************************************************************************
 * Copyright (c) 2009, 2011 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Brock Janiczak - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.examples;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.IBundleCoverage;
import org.jacoco.core.data.ExecutionDataReader;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.data.SessionInfoStore;
import org.jacoco.report.DirectorySourceFileLocator;
import org.jacoco.report.FileMultiReportOutput;
import org.jacoco.report.IReportVisitor;
import org.jacoco.report.html.HTMLFormatter;

/**
 * This example creates a HTML report for eclipse like projects based on a
 * single execution data store called jacoco.exec. The report contains no
 * grouping information
 */
public class ReportGenerator {

	private static ExecutionDataStore executionDataStore;
	private static SessionInfoStore sessionInfoStore;

	/**
	 * Starts the report generation process
	 * 
	 * @param args
	 *            Arguments to the application. This will be the location of the
	 *            eclipse projects that will be used to generate the report
	 * @throws Exception
	 */
	public static void main(final String[] args) throws Exception {
		final List<File> projectDirectories = new ArrayList<File>();
		for (int i = 0; i < args.length; i++) {
			projectDirectories.add(new File(args[i]));
		}

		for (final File projectDirectory : projectDirectories) {
			final File sourceDirectory = new File(projectDirectory, "src");
			final File classesDirectory = new File(projectDirectory, "bin");
			final File reportDirectory = new File(projectDirectory, "html");
			final File executionDataFile = new File(projectDirectory,
					"jacoco.exec");

			// read the jacoco.exec file. Multiple data stores could be merged
			// at this point
			loadExecutionData(executionDataFile);

			// create a concrete report visitor based on some supplied
			// configuration. In this case we use the defaults
			final IReportVisitor reportVisitor = createReport(reportDirectory);

			// Initialise the report with all of the execution and session
			// information. At this point the report doesn't konw about the
			// structure of the report being created
			reportVisitor.visitInfo(sessionInfoStore.getInfos(),
					executionDataStore.getContents());

			// Run the structure analyzer on a single class folder to build up
			// the coverage model. The process would be similar if your classes
			// were in a jar file. Typically you would create a bundle for each
			// class folder and each jar you want in your report. If you have
			// more than one bundle you will need to add a grouping node to your
			// report
			final IBundleCoverage bundleCoverage = analyzeStructure(
					projectDirectory.getName(), classesDirectory);

			// Populate the report structure with the bundle coverage
			// information. Call visitGroup if you need groups in your report
			reportVisitor.visitBundle(bundleCoverage,
					new DirectorySourceFileLocator(sourceDirectory, "utf-8"));

			// Signal end of structure information to allow report to write all
			// information out
			reportVisitor.visitEnd();

		}

	}

	private static IReportVisitor createReport(final File reportDirectory)
			throws IOException {
		final HTMLFormatter htmlFormatter = new HTMLFormatter();
		return htmlFormatter.createVisitor(new FileMultiReportOutput(
				reportDirectory));
	}

	private static void loadExecutionData(final File executionDataFile)
			throws IOException {
		final FileInputStream fis = new FileInputStream(executionDataFile);
		final ExecutionDataReader executionDataReader = new ExecutionDataReader(
				fis);
		executionDataStore = new ExecutionDataStore();
		sessionInfoStore = new SessionInfoStore();

		executionDataReader.setExecutionDataVisitor(executionDataStore);
		executionDataReader.setSessionInfoVisitor(sessionInfoStore);

		while (executionDataReader.read()) {
		}

		fis.close();
	}

	private static IBundleCoverage analyzeStructure(final String bundleName,
			final File classesDirectory) throws IOException {
		final CoverageBuilder coverageBuilder = new CoverageBuilder();
		final Analyzer analyzer = new Analyzer(executionDataStore,
				coverageBuilder);

		analyzer.analyzeAll(classesDirectory);

		return coverageBuilder.getBundle(bundleName);
	}
}
