/*******************************************************************************
 * Copyright (c) 2009, 2015 Mountainminds GmbH & Co. KG and Contributors
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

import java.util.List;

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
	 * @parameter property="project"
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
	 * Flag used to suppress execution.
	 * 
	 * @parameter property="jacoco.skip" default-value="false"
	 */
	private boolean skip;

	public final void execute() throws MojoExecutionException,
			MojoFailureException {
		if (skip) {
			getLog().info(
					"Skipping JaCoCo execution because property jacoco.skip is set.");
			skipMojo();
			return;
		}
		executeMojo();
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
		return excludes;
	}

}
