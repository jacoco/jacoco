/*******************************************************************************
 * Copyright (c) 2009, 2025 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    John Oliver, Marc R. Hoffmann, Jan Wloka - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.maven;

import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.StringUtils;

/**
 * <p>
 * Creates a structured code coverage report (HTML, XML, and CSV) from multiple
 * projects within reactor. The report is created from all modules this project
 * depends on. From those projects class and source files as well as JaCoCo
 * execution data files will be collected and aggregated. This mojo will not
 * fork any lifecycle and so need to be added after build generating jacoco
 * data.
 * </p>
 *
 * @since 0.8.13
 */
@Mojo(name = "report-aggregate-all", threadSafe = true, aggregator = true)
public class ReportAggregateAllMojo extends ReportAggregateMojo {

	@Override
	protected List<MavenProject> findDependencies(final String... scopes) {

		List<MavenProject> result = reactorProjects;

		// need to exclude pom projects
		List<MavenProject> nonPomProjects = new ArrayList<>();
		for (MavenProject mavenProject : result) {
			if (!StringUtils.equals("pom", mavenProject.getPackaging())) {
				nonPomProjects.add(mavenProject);
			}
		}
		return nonPomProjects;
	}

}
