/*******************************************************************************
 * Copyright (c) 2009, 2017 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *    Kyle Lieber - implementation of CheckMojo
 *
 *******************************************************************************/
package org.jacoco.maven;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.jacoco.report.IReportGroupVisitor;

/**
 * Same as <code>report</code>, but provides default values suitable for
 * integration-tests:
 * <ul>
 * <li>bound to <code>report-integration</code> phase</li>
 * <li>different <code>dataFile</code></li>
 * </ul>
 * 
 * @since 0.6.4
 */
@Mojo(name = "report-integration", defaultPhase = LifecyclePhase.VERIFY, threadSafe = true)
public class ReportITMojo extends AbstractReportMojo {

	/**
	 * Output directory for the reports. Note that this parameter is only
	 * relevant if the goal is run from the command line or from the default
	 * build lifecycle. If the goal is run indirectly as part of a site
	 * generation, the output directory configured in the Maven Site Plugin is
	 * used instead.
	 */
	@Parameter(defaultValue = "${project.reporting.outputDirectory}/jacoco-it")
	private File outputDirectory;

	/**
	 * File with execution data.
	 */
	@Parameter(defaultValue = "${project.build.directory}/jacoco-it.exec")
	private File dataFile;

	@Override
	boolean canGenerateReportRegardingDataFiles() {
		return dataFile.exists();
	}

	@Override
	boolean canGenerateReportRegardingClassesDirectory() {
		return new File(getProject().getBuild().getOutputDirectory()).exists();
	}

	@Override
	void loadExecutionData(final ReportSupport support) throws IOException {
		support.loadExecutionData(dataFile);
	}

	@Override
	void addFormatters(final ReportSupport support, final Locale locale)
			throws IOException {
		support.addAllFormatters(outputDirectory, outputEncoding, footer,
				locale);
	}

	@Override
	void createReport(final IReportGroupVisitor visitor,
			final ReportSupport support) throws IOException {
		support.processProject(visitor, title, getProject(), getIncludes(),
				getExcludes(), sourceEncoding);
	}

	@Override
	protected String getOutputDirectory() {
		return outputDirectory.getAbsolutePath();
	}

	@Override
	public void setReportOutputDirectory(final File reportOutputDirectory) {
		if (reportOutputDirectory != null
				&& !reportOutputDirectory.getAbsolutePath().endsWith(
						"jacoco-it")) {
			outputDirectory = new File(reportOutputDirectory, "jacoco-it");
		} else {
			outputDirectory = reportOutputDirectory;
		}
	}

	public String getOutputName() {
		return "jacoco-it/index";
	}

	public String getName(final Locale locale) {
		return "JaCoCo IT";
	}
}
