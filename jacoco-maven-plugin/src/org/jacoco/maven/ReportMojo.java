/*******************************************************************************
 * Copyright (c) 2009, 2014 Mountainminds GmbH & Co. KG and Contributors
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
import java.util.List;
import java.util.Locale;

import org.apache.maven.doxia.siterenderer.Renderer;
import org.apache.maven.project.MavenProject;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.data.SessionInfoStore;

/**
 * Creates a code coverage report for tests of a single project in multiple
 * formats (HTML, XML, and CSV).
 * 
 * @phase verify
 * @goal report
 * @requiresProject true
 * @threadSafe
 * @since 0.5.3
 */
public class ReportMojo extends AbstractReportMojo {

	/**
	 * Output directory for the reports. Note that this parameter is only
	 * relevant if the goal is run from the command line or from the default
	 * build lifecycle. If the goal is run indirectly as part of a site
	 * generation, the output directory configured in the Maven Site Plugin is
	 * used instead.
	 * 
	 * @parameter default-value="${project.reporting.outputDirectory}/jacoco"
	 */
	private File outputDirectory;

	/**
	 * File with execution data.
	 * 
	 * @parameter default-value="${project.build.directory}/jacoco.exec"
	 */
	protected File dataFile;

	/**
	 * A list of class files to include in the report. May use wildcard
	 * characters (* and ?). When not specified everything will be included.
	 * 
	 * @parameter
	 */
	private List<String> includes;

	/**
	 * A list of class files to exclude from the report. May use wildcard
	 * characters (* and ?). When not specified nothing will be excluded.
	 * 
	 * @parameter
	 */
	private List<String> excludes;

	/**
	 * Maven project.
	 * 
	 * @parameter expression="${project}"
	 * @readonly
	 */
	private MavenProject project;

	/**
	 * Doxia Site Renderer.
	 * 
	 * @component
	 */
	private Renderer siteRenderer;

	/**
	 * A list of source folders in addition to the current projects source
	 * folder to be scanned for source files.
	 * 
	 * @parameter
	 */
	private List<String> sourceFolders;

	private SessionInfoStore sessionInfoStore;

	private ExecutionDataStore executionDataStore;

	@Override
	public String getOutputName() {
		return "jacoco/index";
	}

	@Override
	public String getName(final Locale locale) {
		return "JaCoCo";
	}

	@Override
	public String getDescription(final Locale locale) {
		return "JaCoCo Test Coverage Report.";
	}

	@Override
	public boolean isExternalReport() {
		return true;
	}

	@Override
	protected String getOutputDirectory() {
		return outputDirectory.getAbsolutePath();
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
	@Override
	protected List<String> getIncludes() {
		return includes;
	}

	/**
	 * Returns the list of class files to exclude from the report.
	 * 
	 * @return class files to exclude, may contain wildcard characters
	 */
	@Override
	protected List<String> getExcludes() {
		return excludes;
	}

	/**
	 * @param dataFile
	 */
	protected void setDataFile(final File dataFile) {
		this.dataFile = dataFile;
	}

	@Override
	public void setReportOutputDirectory(final File reportOutputDirectory) {
		if (reportOutputDirectory != null
				&& !reportOutputDirectory.getAbsolutePath().endsWith("jacoco")) {
			outputDirectory = new File(reportOutputDirectory, "jacoco");
		} else {
			outputDirectory = reportOutputDirectory;
		}
	}

	@Override
	File getDataFile() {
		return dataFile;
	}

	@Override
	File getOutputDirectoryFile() {
		return outputDirectory;
	}

	public List<String> getSourceFolders() {
		return sourceFolders;
	}

	public void setSourceFolders(final List<String> sourceFolders) {
		this.sourceFolders = sourceFolders;
	}

}
