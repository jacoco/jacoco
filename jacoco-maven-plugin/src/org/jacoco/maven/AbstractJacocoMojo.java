/*******************************************************************************
 * Copyright (c) 2009, 2013 Mountainminds GmbH & Co. KG and Contributors
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

import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

/**
 * Base class for JaCoCo Mojos.
 */
public abstract class AbstractJacocoMojo extends AbstractMojo {

	/**
	 * Maven project.
	 * 
	 * @parameter expression="${project}"
	 * @readonly
	 */
	private MavenProject project;

	/**
	 * A list of class files to include in instrumentation/analysis/reports. May
	 * use wildcard characters (* and ?). When not specified everything will be
	 * included.
	 * 
	 * @parameter
	 */
	private List<String> includes;

	/**
	 * A list of class files to exclude from instrumentation/analysis/reports.
	 * May use wildcard characters (* and ?). When not specified nothing will be
	 * excluded.
	 * 
	 * @parameter
	 */
	private List<String> excludes;

	/**
	 * Name of file that contains a list of class files to exclude from
	 * instrumentation/analysis/reports. May use wildcard characters (* and ?).
	 * When not specified nothing will be excluded. If excludeFile is specified,
	 * the value of excludesList will be ignored
	 * 
	 * @parameter
	 */
	private String excludeFile;

	/**
	 * Flag used to suppress execution.
	 * 
	 * @parameter expression="${jacoco.skip}" default-value="false"
	 */
	private boolean skip;

	public final void execute() throws MojoExecutionException,
			MojoFailureException {
		if ("pom".equals(project.getPackaging())) {
			getLog().info(
					"Skipping JaCoCo for project with packaging type 'pom'");
			skipMojo();
			return;
		}
		if (skip) {
			getLog().info("Skipping JaCoCo execution");
			skipMojo();
			return;
		}
		loadReferenceData();
		executeMojo();
	}

	private void loadReferenceData() throws MojoExecutionException {
		// load exclude list
		if (!StringUtils.isEmpty(excludeFile)) {
			getLog().debug("Reading exclude file " + excludeFile);
			excludes = new ArrayList<String>();
			LineNumberReader reader = null;
			try {
				reader = new LineNumberReader(new FileReader(excludeFile));
				String line = null;
				while ((line = reader.readLine()) != null) {
					final String cleanLine = line.trim();
					if (!StringUtils.isEmpty(cleanLine)) {
						excludes.add(cleanLine);
					}
				}
			} catch (final IOException e) {
				throw new MojoExecutionException("Cannot load file "
						+ excludeFile, e);
			} finally {
				if (reader != null) {
					try {
						reader.close();
					} catch (final IOException e) {
						getLog().warn("Cannot close file " + excludeFile, e);
					}
				}
			}
		}
	}

	/**
	 * Executes Mojo.
	 * 
	 * @throws MojoExecutionException
	 *             if an unexpected problem occurs. Throwing this exception
	 *             causes a "BUILD ERROR" message to be displayed.
	 * @throws MojoFailureException
	 *             if an expected problem (such as a compilation failure)
	 *             occurs. Throwing this exception causes a "BUILD FAILURE"
	 *             message to be displayed.
	 */
	protected abstract void executeMojo() throws MojoExecutionException,
			MojoFailureException;

	/**
	 * Skips Mojo.
	 */
	protected void skipMojo() {
	}

	/**
	 * @return Maven project
	 */
	protected final MavenProject getProject() {
		return project;
	}

	/**
	 * Returns the list of class files to include.
	 * 
	 * @return class files to include, may contain wildcard characters
	 */
	protected List<String> getIncludes() {
		return includes;
	}

	/**
	 * Returns the list of class files to exclude.
	 * 
	 * @return class files to exclude, may contain wildcard characters
	 */
	protected List<String> getExcludes() {
		if (getLog().isDebugEnabled()) {
			getLog().debug("Using exclude list: " + excludes);
		}
		return excludes;
	}

}
