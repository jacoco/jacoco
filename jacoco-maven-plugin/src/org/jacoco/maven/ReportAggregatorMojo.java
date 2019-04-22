/*******************************************************************************
 * Copyright (c) 2009, 2019 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Oliver, Marc R. Hoffmann, Jan Wloka - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.maven;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.jacoco.report.IReportGroupVisitor;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * <p>
 * Creates a structured code coverage report (HTML, XML, and CSV) from multiple
 * projects within reactor. The report is created from all modules in the reactor.
 * From those projects class and source files as well as JaCoCo
 * execution data files will be collected. In addition execution data is
 * collected from the project itself. This also allows to create coverage
 * reports when tests are in separate projects than the code under test, for
 * example in case of integration tests.
 * </p>
 * 
 * @since 0.8.4
 */
@Mojo(name = "report-aggregator", threadSafe = true, aggregator = true)
public class ReportAggregatorMojo extends AbstractReportMojo {


	@Parameter(defaultValue = "${session}", required = true, readonly = true)
	MavenSession session;

	/**
	 * A list of execution data files to include in the report from each
	 * project. May use wildcard characters (* and ?). When not specified all
	 * *.exec files from the target folder will be included.
	 */
	@Parameter(property = "jacoco.dataFileIncludes")
	List<String> dataFileIncludes;

	/**
	 * A list of execution data files to exclude from the report. May use
	 * wildcard characters (* and ?). When not specified nothing will be
	 * excluded.
	 */
	@Parameter(property = "jacoco.dataFileExcludes")
	List<String> dataFileExcludes;

	/**
	 * Output directory for the reports. Note that this parameter is only
	 * relevant if the goal is run from the command line or from the default
	 * build lifecycle. If the goal is run indirectly as part of a site
	 * generation, the output directory configured in the Maven Site Plugin is
	 * used instead.
	 */
	@Parameter(property = "jacoco.outputDirectory", defaultValue = "${project.reporting.outputDirectory}/jacoco-aggregate")
	private File outputDirectory;

	@Parameter(defaultValue = "${mojoExecution}", required = true, readonly = true)
	private MojoExecution mojoExecution;

	@Override
	boolean canGenerateReportRegardingDataFiles() {
		return true;
	}

	@Override
	boolean canGenerateReportRegardingClassesDirectory() {
		return true;
	}

	@Override
	public void execute() throws MojoExecutionException {
		if (shouldDelayExecution()) {
			getLog().info("Delaying JaCoCo report generation to the end of multi-module project");
			return;
		}

		super.execute();
	}

	@Override
	void loadExecutionData(final ReportSupport support) throws IOException {
		// https://issues.apache.org/jira/browse/MNG-5440
		if (dataFileIncludes == null) {
			dataFileIncludes = Arrays.asList("target/*.exec");
		}

		final FileFilter filter = new FileFilter(dataFileIncludes,
				dataFileExcludes);
		loadExecutionData(support, filter, getProject().getBasedir());
		for (final MavenProject dependency : session.getProjectDependencyGraph().getSortedProjects()) {
			loadExecutionData(support, filter, dependency.getBasedir());
		}
	}

	private void loadExecutionData(final ReportSupport support,
			final FileFilter filter, final File basedir) throws IOException {
		for (final File execFile : filter.getFiles(basedir)) {
			support.loadExecutionData(execFile);
		}
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
		final IReportGroupVisitor group = visitor.visitGroup(title);
		for (final MavenProject dependency : session.getProjectDependencyGraph().getSortedProjects()) {
			support.processProject(group, dependency.getArtifactId(),
					dependency, getIncludes(), getExcludes(), sourceEncoding);
		}
	}

	@Override
	protected String getOutputDirectory() {
		return outputDirectory.getAbsolutePath();
	}

	@Override
	public void setReportOutputDirectory(final File reportOutputDirectory) {
		if (reportOutputDirectory != null
				&& !reportOutputDirectory.getAbsolutePath().endsWith(
						"jacoco-aggregate")) {
			outputDirectory = new File(reportOutputDirectory,
					"jacoco-aggregate");
		} else {
			outputDirectory = reportOutputDirectory;
		}
	}

	public String getOutputName() {
		return "jacoco-aggregate/index";
	}

	public String getName(final Locale locale) {
		return "JaCoCo Aggregate";
	}

	/**
	 * Should scanner be delayed?
	 * @return true if goal is attached to phase and not last in a multi-module project
	 */
	private boolean shouldDelayExecution() {
		return !isDetachedGoal() && !isLastProjectInReactor();
	}

	/**
	 * Is this execution a 'detached' goal run from the cli.  e.g. mvn jacoco:report-aggregator
	 *
	 * See <a href="https://maven.apache.org/guides/mini/guide-default-execution-ids.html#Default_executionIds_for_Implied_Executions">
	 Default executionIds for Implied Executions</a>
	 * for explanation of command line execution id.
	 *
	 * @return true if this execution is from the command line
	 */
	private boolean isDetachedGoal() {
		return "default-cli".equals(mojoExecution.getExecutionId());
	}

	/**
	 * Is this project the last project in the reactor?
	 *
	 * @return true if last project (including only project)
	 */
	private boolean isLastProjectInReactor() {
		List<MavenProject> sortedProjects = session.getProjectDependencyGraph().getSortedProjects();

		MavenProject lastProject = sortedProjects.isEmpty()
				? session.getCurrentProject()
				: sortedProjects.get( sortedProjects.size() - 1 );

		if ( getLog().isDebugEnabled() ) {
			getLog().debug( "Current project: '" + session.getCurrentProject().getName() +
					"', Last project to execute based on dependency graph: '" + lastProject.getName() + "'" );
		}

		return session.getCurrentProject().equals( lastProject );
	}

}
