/*******************************************************************************
 * Copyright (c) 2009, 2013 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Keeping - initial implementation
 *
 *******************************************************************************/
package org.jacoco.cli;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.IBundleCoverage;
import org.jacoco.core.data.ExecutionDataReader;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.data.SessionInfoStore;
import org.jacoco.report.DirectorySourceFileLocator;
import org.jacoco.report.FileMultiReportOutput;
import org.jacoco.report.IReportVisitor;
import org.jacoco.report.csv.CSVFormatter;
import org.jacoco.report.html.HTMLFormatter;
import org.jacoco.report.xml.XMLFormatter;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

/**
 * Command-line "report" command implementation.
 */
public class Report {

	private final ReportOptions options;
	private final File inputFile;
	private final File classes;
	private final String title;
	private ExecutionDataStore dataStore;
	private SessionInfoStore infoStore;
	private IBundleCoverage coverage;

	/**
	 * Constructor.
	 * 
	 * @param options
	 *            the options to be applied when generating the report(s).
	 */
	public Report(final ReportOptions options) {
		this.options = options;
		this.inputFile = options.getInput();
		this.classes = options.getClasses();
		this.title = options.getTitle();
	}

	/**
	 * Runs the report generation task.
	 * 
	 * @throws IOException
	 *             if any I/O operations fail.
	 */
	public void run() throws IOException {
		loadCoverage();

		final File csv = options.getCsv();
		if (null != csv) {
			final CSVFormatter formatter = new CSVFormatter();
			final OutputStream output = new FileOutputStream(csv);
			try {
				generateReport(formatter.createVisitor(output));
			} catch (final IOException e) {
				csv.deleteOnExit();
				throw e;
			} finally {
				output.close();
			}
		}

		final File html = options.getHtml();
		if (null != html) {
			final HTMLFormatter formatter = new HTMLFormatter();
			generateReport(formatter.createVisitor(new FileMultiReportOutput(
					html)));
		}

		final File xml = options.getXml();
		if (null != xml) {
			final XMLFormatter formatter = new XMLFormatter();
			final OutputStream output = new FileOutputStream(xml);
			try {
				generateReport(formatter.createVisitor(output));
			} catch (final IOException e) {
				xml.deleteOnExit();
				throw e;
			} finally {
				output.close();
			}
		}
	}

	private void generateReport(final IReportVisitor visitor)
			throws IOException {
		visitor.visitInfo(infoStore.getInfos(), dataStore.getContents());

		// Populate the report structure with the bundle coverage information.
		// Call visitGroup if you need groups in your report.
		visitor.visitBundle(
				coverage,
				new DirectorySourceFileLocator(options.getSource(), options
						.getSourceEncoding(), options.getTabWidth()));

		// Signal end of structure information to allow report to write all
		// information out
		visitor.visitEnd();

	}

	private void loadCoverage() throws IOException {
		final InputStream input = new FileInputStream(inputFile);
		try {
			final ExecutionDataReader reader = new ExecutionDataReader(input);
			dataStore = new ExecutionDataStore();
			infoStore = new SessionInfoStore();

			reader.setExecutionDataVisitor(dataStore);
			reader.setSessionInfoVisitor(infoStore);

			while (reader.read()) {
				// Just read until we're done.
			}

			final CoverageBuilder coverageBuilder = new CoverageBuilder();
			final Analyzer analyzer = new Analyzer(dataStore, coverageBuilder);
			analyzer.analyzeAll(classes);

			coverage = coverageBuilder.getBundle(title);
		} finally {
			input.close();
		}
	}

	/**
	 * @param args
	 *            the command-line arguments.
	 */
	public static void main(final String[] args) {
		final ReportOptions options = new ReportOptions();
		final CmdLineParser parser = new CmdLineParser(options);
		try {
			parser.parseArgument(args);
		} catch (final CmdLineException e) {
			System.err.println(e.getLocalizedMessage());
			parser.printUsage(System.err);
			System.exit(1);
		}

		try {
			new Report(options).run();
		} catch (final IOException e) {
			System.err.println("Failed: " + e.getLocalizedMessage());
			System.exit(1);
		}
	}

}
