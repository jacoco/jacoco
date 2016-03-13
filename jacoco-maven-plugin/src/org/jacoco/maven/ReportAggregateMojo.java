/*******************************************************************************
 * Copyright (c) 2009, 2016 Mountainminds GmbH & Co. KG and Contributors
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

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

import org.apache.maven.model.Dependency;
import org.apache.maven.project.MavenProject;
import org.apache.maven.reporting.MavenReportException;
import org.jacoco.core.analysis.IBundleCoverage;
import org.jacoco.core.tools.ExecFileLoader;
import org.jacoco.report.IReportGroupVisitor;
import org.jacoco.report.IReportVisitor;

/**
 * Creates a structured code coverage report from multiple projects (HTML, XML,
 * and CSV). The report is created from all modules this project depends on.
 * From those projects class and source files as well as JaCoCo execution data
 * files will be collected. This also allows to create coverage reports when
 * tests are in separate projects than the code under test, for example in case
 * of integration tests.
 * 
 * @goal report-aggregate
 * @since 0.7.7
 */
public class ReportAggregateMojo extends ReportMojo {

	/**
	 * The projects in the reactor.
	 * 
	 * @parameter expression="${reactorProjects}"
	 * @readonly
	 */
	private List<MavenProject> reactorProjects;

	@Override
	public boolean canGenerateReport() {
		if (skip) {
			getLog().info("Skipping JaCoCo execution");
			return false;
		}
		return true;
	}

	@Override
	protected void executeReport(final Locale locale)
			throws MavenReportException {
		loadExecutionData();
		try {
			final IReportVisitor visitor = createVisitor(locale);
			visitor.visitInfo(sessionInfoStore.getInfos(),
					executionDataStore.getContents());
			createReport(visitor);
			visitor.visitEnd();
		} catch (final IOException e) {
			throw new MavenReportException("Error while creating report: "
					+ e.getMessage(), e);
		}
	}

	@Override
	void createReport(final IReportGroupVisitor visitor) throws IOException {
		final IReportGroupVisitor group = visitor.visitGroup(getProject()
				.getName());
		for (final Object dependencyObject : getProject().getDependencies()) {
			final Dependency dependency = (Dependency) dependencyObject;
			final MavenProject project = findProjectFromReactor(dependency);
			if (project != null) {
				createReportForProject(project, group);
			}
		}
	}

	void createReportForProject(final MavenProject project,
			final IReportGroupVisitor visitor) throws IOException {

		final FileFilter fileFilter = new FileFilter(this.getIncludes(),
				this.getExcludes());
		final BundleCreator creator = new BundleCreator(project, fileFilter,
				getLog());
		final IBundleCoverage bundle = creator.createBundle(executionDataStore);
		// TODO use source encoding of target project
		final SourceFileCollection locator = new SourceFileCollection(
				getCompileSourceRoots(project), sourceEncoding);
		checkForMissingDebugInformation(bundle);
		visitor.visitBundle(bundle, locator);
	}

	@Override
	void loadExecutionData() throws MavenReportException {
		final ExecFileLoader loader = new ExecFileLoader();
		for (final Object dependencyObject : getProject().getDependencies()) {
			final Dependency dependency = (Dependency) dependencyObject;
			final MavenProject project = findProjectFromReactor(dependency);
			if (project != null) {
				// TODO Use configured location from project
				// TODO Clarify when to include exec data from integration tests
				final File execFile = new File(project.getBuild()
						.getDirectory(), "jacoco.exec");
				if (execFile.exists()) {
					getLog().info("Loading execution data file " + execFile);
					try {
						loader.load(execFile);
					} catch (final IOException e) {
						throw new MavenReportException(
								"Unable to read execution data file "
										+ execFile + ": " + e.getMessage(), e);
					}
				}
			}
		}
		sessionInfoStore = loader.getSessionInfoStore();
		executionDataStore = loader.getExecutionDataStore();
	}

	private MavenProject findProjectFromReactor(final Dependency d) {
		for (final MavenProject p : reactorProjects) {
			if (p.getGroupId().equals(d.getGroupId())
					&& p.getArtifactId().equals(d.getArtifactId())
					&& p.getVersion().equals(d.getVersion())) {
				return p;
			}
		}
		return null;
	}

}
