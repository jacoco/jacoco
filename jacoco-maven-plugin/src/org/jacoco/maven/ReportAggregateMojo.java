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

import java.util.List;
import java.util.Locale;

import org.apache.maven.model.Dependency;
import org.apache.maven.project.MavenProject;
import org.apache.maven.reporting.MavenReportException;

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
		for (final Object dependencyObject : getProject().getDependencies()) {
			final Dependency dependency = (Dependency) dependencyObject;
			final MavenProject project = findProjectFromReactor(dependency);
			if (project != null) {
				// TODO Collect projects with source and class files
				// TODO Collect projects with exec files
				getLog().info("project: " + project.getArtifactId());
				getLog().info(
						"compiledSourceRoots: "
								+ project.getCompileSourceRoots());
				getLog().info(
						"targetDirectory: " + project.getBuild().getDirectory());
				getLog().info(
						"jacoco.destFile: "
								+ project.getProperties().getProperty(
										"jacoco.destFile"));
			}
			// TODO generate structures report
		}
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
