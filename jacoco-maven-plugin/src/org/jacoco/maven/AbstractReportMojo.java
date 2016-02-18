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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.apache.maven.doxia.siterenderer.Renderer;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.reporting.AbstractMavenReport;
import org.apache.maven.reporting.MavenReportException;

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
	 * @parameter property="project.build.sourceEncoding"
	 *            default-value="UTF-8"
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

	private final MavenReportGenerator reportGenerator;

	/**
	 * 
	 */
	public AbstractReportMojo() {
		this.reportGenerator = new MavenReportGenerator(this);
	}

	public abstract String getOutputName();

	public abstract String getName(final Locale locale);

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
	public abstract void setReportOutputDirectory(
			final File reportOutputDirectory);

	/**
	 * Returns the {@code File} from the execution data is read.
	 * 
	 * @return the {@code File} to which the execution data is written and from
	 *         which it is read.
	 */
	abstract File getDataFile();

	/**
	 * Returns the directory where the reports will be written
	 * 
	 * @return the directory where the reports will be written
	 */
	abstract File getOutputDirectoryFile();

	/**
	 * @return
	 */
	public abstract boolean isEBigOEnabled();

	/**
	 * @return
	 */
	public abstract String getEBigOAttribute();

	@Override
	public boolean canGenerateReport() {
		return reportGenerator.canGenerateReport(skip, getDataFile(),
				getClassesDirectories());
	}

	/**
	 * This method is called when the report generation is invoked directly as a
	 * standalone Mojo.
	 */
	@Override
	public void execute() throws MojoExecutionException {
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

		reportGenerator.setName(this.project.getName());
		reportGenerator.setLocale(locale);
		reportGenerator.setSkip(skip);

		reportGenerator.setDataFile(getDataFile());

		reportGenerator.setSourceEncoding(sourceEncoding);
		reportGenerator.setSourceRoots(getCompileSourceRoots());

		reportGenerator.setOutputEncoding(outputEncoding);
		reportGenerator
				.setReportOutputDirectory(new File(getOutputDirectory()));

		reportGenerator.setClassesDirectories(getClassesDirectories());
		reportGenerator.setExcludes(excludes);
		reportGenerator.setIncludes(includes);

		reportGenerator.setEBigOAttribute(
				isEBigOEnabled() ? getEBigOAttribute() : null);

		try {
			reportGenerator.execute();
		} catch (final IOException e) {
			throw new MavenReportException(
					"Error while creating report: " + e.getMessage(), e);
		}
	}

	private List<File> getClassesDirectories() {
		return Arrays.asList(new File[] {
				new File(getProject().getBuild().getOutputDirectory()) });
	}

	private List<File> getCompileSourceRoots() {
		final List<File> result = new ArrayList<File>();
		for (final Object path : getProject().getCompileSourceRoots()) {
			result.add(resolvePath((String) path));
		}
		return result;
	}

	private File resolvePath(final String path) {
		File file = new File(path);
		if (!file.isAbsolute()) {
			file = new File(getProject().getBasedir(), path);
		}
		return file;
	}
}
