/*******************************************************************************
 * Copyright (c) 2009, 2016 Mountainminds GmbH & Co. KG and Contributors
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

/**
 * Same as <code>report</code>, but provides default values suitable for
 * integration-tests:
 * <ul>
 * <li>bound to <code>report-integration</code> phase</li>
 * <li>different <code>dataFile</code></li>
 * </ul>
 * 
 * @phase verify
 * @goal report-aggregate
 * @requiresProject true
 * @threadSafe
 * @since 0.7.7
 */
public class ReportAggregateMojo extends AbstractReportMojo {

	/**
	 * Accumulated data files.
	 */
	private static Set<File> dataFiles = Collections
			.synchronizedSet(new HashSet<File>());

	/**
	 * The projects in the reactor.
	 * 
	 * @parameter expression="${reactorProjects}"
	 * @readonly
	 */
	private List<MavenProject> reactorProjects;

	/**
	 * The executed projects.
	 */
	private static Set<MavenProject> executedProjects = Collections
			.synchronizedSet(new HashSet<MavenProject>());

	@Override
	public void setReportOutputDirectory(final File reportOutputDirectory) {
	}

	@Override
	List<File> getDataFiles() {
		final List<File> files = new ArrayList<File>();
		for (final File file : dataFiles) {
			if (file.exists()) {
				files.add(file);
			}
		}
		return files;
	}

	@Override
	protected String getOutputDirectory() {
		return getOutputDirectoryFile().getAbsolutePath();
	}

	@Override
	public String getOutputName() {
		return "jacoco/index";
	}

	@Override
	public String getName(final Locale locale) {
		return "JaCoCo Test";
	}

	@Override
	public void execute() throws MojoExecutionException {
		executedProjects.add(getProject());
		super.execute();
	}

	public static void accumulate(final File dataFile) {
		dataFiles.add(dataFile);
	}

	@Override
	File getOutputDirectoryFile() {
		return new File(
				reactorProjects.get(0).getReporting().getOutputDirectory(),
				"jacoco");
	}

	@Override
	public boolean canGenerateReport() {
		if (executedProjects.size() != reactorProjects.size()) {
			getLog().info("Skipping JaCoCo execution till all projects ran");
			return false;
		}

		executedProjects.clear();

		return super.canGenerateReport();
	}

	@Override
	List<File> getCompileSourceRoots() {
		final List<File> result = new ArrayList<File>();
		for (final MavenProject project : reactorProjects) {
			for (final Object path : project.getCompileSourceRoots()) {
				final File sourceRoot = resolvePath(project, (String) path);
				if (sourceRoot.exists()) {
					result.add(sourceRoot);
				}
			}
		}
		return result;
	}

	@Override
	List<File> getClassesDirectories() {
		final List<File> result = new ArrayList<File>();
		for (final MavenProject project : reactorProjects) {
			final File outputDirectory = new File(
					project.getBuild().getOutputDirectory());
			if (outputDirectory.exists()) {
				result.add(outputDirectory);
			}
		}
		return result;
	}

}
