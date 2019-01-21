/*******************************************************************************
 * Copyright (c) 2009, 2019 Mountainminds GmbH & Co. KG and Contributors
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

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

/**
 * Base class for JaCoCo Mojos.
 */
public abstract class AbstractJacocoMojo extends AbstractMojo {

	/**
	 * Maven project.
	 */
	@Parameter(property = "project", readonly = true)
	private MavenProject project;

	/**
	 * Flag used to suppress execution.
	 */
	@Parameter(property = "jacoco.skip", defaultValue = "false")
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

}
