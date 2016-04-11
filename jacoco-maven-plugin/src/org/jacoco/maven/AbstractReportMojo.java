/*******************************************************************************
 * Copyright (c) 2009, 2016 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.maven;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

import org.apache.maven.doxia.siterenderer.Renderer;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.reporting.AbstractMavenReport;
import org.apache.maven.reporting.MavenReportException;
import org.jacoco.report.IReportGroupVisitor;
import org.jacoco.report.IReportVisitor;

/**
 * Base class for creating a code coverage report for tests of a single project
 * in multiple formats (HTML, XML, and CSV).
 */
public abstract class AbstractReportMojo extends AbstractMavenReport {

	/**
	 * Encoding of the generated reports.
	 * 
	 * @parameter property="project.reporting.outputEncoding"
	 *            default-value="UTF-8"
	 */
	String outputEncoding;

	/**
	 * Encoding of the source files.
	 * 
	 * @parameter property="project.build.sourceEncoding" default-value="UTF-8"
	 */
	String sourceEncoding;

	/**
	 * A list of class files to include in the report. May use wildcard
	 * characters (* and ?). When not specified everything will be included.
	 * 
	 * @parameter
	 */
	List<String> includes;

	/**
	 * A list of class files to exclude from the report. May use wildcard
	 * characters (* and ?). When not specified nothing will be excluded.
	 * 
	 * @parameter
	 */
	List<String> excludes;

	/**
	 * Flag used to suppress execution.
	 * 
	 * @parameter property="jacoco.skip" default-value="false"
	 */
	boolean skip;

	/**
	 * Maven project.
	 * 
	 * @parameter property="project"
	 * @readonly
	 */
	MavenProject project;

	/**
	 * Doxia Site Renderer.
	 * 
	 * @component
	 */
	Renderer siteRenderer;

	public String getDescription(final Locale locale) {
		return getName(locale) + " Coverage Report.";
	}

	@Override
	public boolean isExternalReport() {
		return true;
	}

	@Override
	protected MavenProject getProject() {
		return project;
	}

	@Override
	protected Renderer getSiteRenderer() {
		return siteRenderer;
	}

	/**
	 * Returns the list of class files to include in the report.
	 * 
	 * @return class files to include, may contain wildcard characters
	 */
	List<String> getIncludes() {
		return includes;
	}

	/**
	 * Returns the list of class files to exclude from the report.
	 * 
	 * @return class files to exclude, may contain wildcard characters
	 */
	List<String> getExcludes() {
		return excludes;
	}

	@Override
	public boolean canGenerateReport() {
		if (skip) {
			getLog().info(
					"Skipping JaCoCo execution because property jacoco.skip is set.");
			return false;
		}
		if (!getDataFile().exists()) {
			getLog().info(
					"Skipping JaCoCo execution due to missing execution data file:"
							+ getDataFile());
			return false;
		}
		final File classesDirectory = new File(getProject().getBuild()
				.getOutputDirectory());
		if (!classesDirectory.exists()) {
			getLog().info(
					"Skipping JaCoCo execution due to missing classes directory:"
							+ classesDirectory);
			return false;
		}
		return true;
	}

	/**
	 * This method is called when the report generation is invoked directly as a
	 * standalone Mojo.
	 */
	@Override
	public void execute() throws MojoExecutionException {
		if (!canGenerateReport()) {
			return;
		}
		try {
			executeReport(Locale.getDefault());
		} catch (final MavenReportException e) {
			throw new MojoExecutionException("An error has occurred in "
					+ getName(Locale.ENGLISH) + " report generation.", e);
		}
	}

	@Override
	protected void executeReport(final Locale locale)
			throws MavenReportException {
		try {
			final ReportSupport support = new ReportSupport(getLog());
			loadExecutionData(support);
			final IReportVisitor visitor = createVisitor(support, locale);
			createReport(visitor, support);
			visitor.visitEnd();
		} catch (final IOException e) {
			throw new MavenReportException("Error while creating report: "
					+ e.getMessage(), e);
		}
	}

	void loadExecutionData(final ReportSupport support) throws IOException {
		support.loadExecutionData(getDataFile());
	}

	void createReport(final IReportGroupVisitor visitor,
			final ReportSupport support) throws IOException {
		support.processProject(visitor, getProject(), getIncludes(),
				getExcludes(), sourceEncoding);
	}

	IReportVisitor createVisitor(final ReportSupport support,
			final Locale locale) throws IOException {
		getOutputDirectoryFile().mkdirs();
		support.addXmlFormatter(getOutputDirectoryFile(), "jacoco.xml",
				outputEncoding);
		support.addCsvFormatter(getOutputDirectoryFile(), "jacoco.csv",
				outputEncoding);
		support.addHtmlFormatter(getOutputDirectoryFile(), outputEncoding,
				locale);
		return support.initRootVisitor();
	}

	abstract File getDataFile();

	abstract File getOutputDirectoryFile();

}
