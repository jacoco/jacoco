/*******************************************************************************
 *  Copyright (c) 2009, 2016 Mountainminds GmbH & Co. KG and Contributors
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

import static org.jacoco.ebigo.internal.util.ValidationUtils.validateNotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jacoco.core.analysis.IBundleCoverage;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.data.SessionInfoStore;
import org.jacoco.ebigo.analysis.EmpiricalBigOAnalyzer;
import org.jacoco.ebigo.analysis.EmpiricalBigOBuilder;
import org.jacoco.ebigo.core.EmpiricalBigOWorkloadStore;
import org.jacoco.ebigo.fit.FitType;
import org.jacoco.report.DirectorySourceFileLocator;
import org.jacoco.report.FileMultiReportOutput;
import org.jacoco.report.IReportVisitor;
import org.jacoco.report.ISourceFileLocator;
import org.jacoco.report.MultiReportVisitor;
import org.jacoco.report.csv.CSVFormatter;
import org.jacoco.report.html.HTMLFormatter;
import org.jacoco.report.xml.XMLFormatter;

/**
 * Sample Report Generation for E-BigO. This report generator generates XML,
 * CSV, and HTML reports.
 */
public class EmpiricalBigOReportGenerator {
	private final File sourceDir;
	private final File classDir;
	private final File outputDir;

	/**
	 * Construct Report Generator
	 * 
	 * @param sourceDir
	 *            base directory for Java source files
	 * @param classDir
	 *            base directory for class files
	 * @param outputDir
	 *            base directory for the reports
	 */
	public EmpiricalBigOReportGenerator(final File sourceDir,
			final File classDir, final File outputDir) {
		validateNotNull("sourceDir", sourceDir);
		validateNotNull("classDir", classDir);
		validateNotNull("outputDir", outputDir);
		this.sourceDir = sourceDir;
		this.classDir = classDir;
		this.outputDir = outputDir;
	}

	/**
	 * Generate the reports for a specific bundle using the default X-Axis
	 * attributes.
	 * 
	 * @param workloadStore
	 *            the source data for the reports
	 * @param bundleName
	 *            the bundle name in the report
	 * @throws IOException
	 *             on any failure to write
	 */
	public void generateReports(final EmpiricalBigOWorkloadStore workloadStore,
			final String bundleName) throws IOException {
		validateNotNull("workloadStore", workloadStore);
		generateReports(workloadStore, bundleName,
				workloadStore.getDefaultAttribute());
	}

	/**
	 * Generate the reports for a specific bundle.
	 * 
	 * @param workloadStore
	 *            the source data for the reports
	 * @param bundleName
	 *            the bundle name in the report
	 * @param xAxisName
	 *            the X-Axis attribute to use for analysis and reporting
	 * @throws IOException
	 *             on any failure to write
	 */
	public void generateReports(final EmpiricalBigOWorkloadStore workloadStore,
			final String bundleName, final String xAxisName) throws IOException {
		validateNotNull("workloadStore", workloadStore);
		validateNotNull("bundleName", bundleName);
		validateNotNull("xAxisName", xAxisName);

		final IBundleCoverage bundleCoverage = getCoverageBundle(workloadStore,
				bundleName, xAxisName);
		final ISourceFileLocator locator = new DirectorySourceFileLocator(
				sourceDir, null, 4);

		final IReportVisitor visitor = createReportVisitor(
				workloadStore.getMergedExecutionDataStore(),
				workloadStore.getMergedSessionInfoStore());
		visitor.visitBundle(bundleCoverage, locator);

		visitor.visitEnd();
	}

	private IReportVisitor createReportVisitor(
			final ExecutionDataStore executionDataStore,
			final SessionInfoStore sessionInfoStore) throws IOException {
		outputDir.mkdirs();

		final List<IReportVisitor> visitors = new ArrayList<IReportVisitor>();

		final XMLFormatter xmlFormatter = new XMLFormatter();
		visitors.add(xmlFormatter.createVisitor(new FileOutputStream(new File(
				outputDir, "jacoco.xml"))));

		final CSVFormatter csvFormatter = new CSVFormatter();
		visitors.add(csvFormatter.createVisitor(new FileOutputStream(new File(
				outputDir, "jacoco.csv"))));

		final HTMLFormatter htmlFormatter = new HTMLFormatter();
		visitors.add(htmlFormatter.createVisitor(new FileMultiReportOutput(
				outputDir)));

		final IReportVisitor visitor = new MultiReportVisitor(visitors);
		visitor.visitInfo(sessionInfoStore.getInfos(),
				executionDataStore.getContents());

		return visitor;
	}

	private IBundleCoverage getCoverageBundle(
			final EmpiricalBigOWorkloadStore workloadStore,
			final String bundleName, final String xAxisName) throws IOException {
		final EmpiricalBigOBuilder coverageVisitor = new EmpiricalBigOBuilder(
				FitType.values(), xAxisName);
		final EmpiricalBigOAnalyzer analyzer = new EmpiricalBigOAnalyzer(
				workloadStore, coverageVisitor);
		analyzer.analyzeAll(classDir);
		return coverageVisitor.getBundle(bundleName);
	}
}
