/*******************************************************************************
 * Copyright (c) 2009, 2017 Mountainminds GmbH & Co. KG and Contributors
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

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.util.ArrayList;
import java.util.List;

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
     * @component
     */
    private ArtifactResolver resolver;

    /**
     * Path to local repository
     *
     * @parameter default-value="${localRepository}"
     */
    private ArtifactRepository localRepository;

    /**
     * Flag to enable multi-module project coverage
     *
     * @parameter default-value="false"
     */
    private boolean isMultiModule;

    /**
     * Patter for classes copied if multi-module option is enabled.
     *
     * @parameter
     */
    private String packagePattern;

    /**
	 * A list of class files to include in instrumentation/analysis/reports. May
	 * use wildcard characters (* and ?). When not specified everything will be
	 * included.
	 */
	@Parameter
	private List<String> includes;

	/**
	 * A list of class files to exclude from instrumentation/analysis/reports.
	 * May use wildcard characters (* and ?). When not specified nothing will be
	 * excluded.
	 */
	@Parameter
	private List<String> excludes;

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

    /**
     * @return true if project is multi-module project, false if not
     */
    public final boolean isMultiModuleProject() {
        return isMultiModule;
    }

    /**
     * In most of cases the user wants only classes of project without external jars
     *
     * @return package patter
     */
    public final String getPackagePattern() {
        return packagePattern;
    }


    /**
     * Get path of dependency jar
     *
     * @param artifact  Dependency artifact
     * @return          path to dependency jar located into local repository. Return null if it was not found.
     */
    public String getDependencyJarPath(Artifact artifact)
    {
        try {
            resolver.resolve(artifact, new ArrayList(), localRepository);
            return artifact.getFile().getPath();
        } catch (ArtifactResolutionException e) {
            getLog().error(e);
        } catch (ArtifactNotFoundException e) {
            getLog().error(e);
        }

        return null;
    }
}
