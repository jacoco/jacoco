/*******************************************************************************
 * Copyright (c) 2009, 2013 Mountainminds GmbH & Co. KG and Contributors
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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.maven.project.MavenProject;
import org.apache.maven.reporting.MavenReportException;

/**
 * Creates a code coverage report from an exec file appended to from multiple
 * reactor projects (HTML, XML, and CSV).
 * 
 * @goal pom-report
 * @aggregate
 */
public class ReportPomMojo extends ReportMojo {

	/**
	 * The projects in the reactor.
	 * 
	 * @parameter expression="${reactorProjects}"
	 * @readonly
	 */
	private List<MavenProject> reactorProjects;

	@Override
	public boolean canGenerateReport() {
		if (!"pom".equals(project.getPackaging())) {
			getLog().info(
					"Skipping JaCoCo since this is not of packaging type 'pom'");
			return false;
		}
		if (skip) {
			getLog().info("Skipping JaCoCo execution");
			return false;
		}
		if (!dataFile.exists()) {
			getLog().info(
					"Skipping JaCoCo execution due to missing execution data file");
			return false;
		}
		return true;
	}

	@Override
	protected void executeReport(final Locale locale)
			throws MavenReportException {
		if (sourceFolders == null) {
			sourceFolders = new ArrayList<String>();
		}
		if (classFolders == null) {
			classFolders = new ArrayList<String>();
		}

		for (final MavenProject reactor : reactorProjects) {
			sourceFolders.addAll(reactor.getCompileSourceRoots());
			classFolders.add(reactor.getBuild().getOutputDirectory());
		}

		super.executeReport(locale);
	}
}
