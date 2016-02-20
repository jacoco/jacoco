/*******************************************************************************
 * Copyright (c) 2009, 2016 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Mads Mohr Christensen - implementation of MergeMojo
 *
 *******************************************************************************/
package org.jacoco.maven;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.shared.model.fileset.FileSet;

import java.io.File;
import java.util.List;

/**
 * Mojo for merging a set of execution data files (*.exec) into a single file
 * 
 * @phase generate-resources
 * @goal merge
 * @requiresProject true
 * @threadSafe
 * @since 0.6.4
 */
public class MergeMojo extends AbstractJacocoMojo {

	/**
	 * Path to the output file for execution data.
	 * 
	 * @parameter property="jacoco.destFile"
	 *            default-value="${project.build.directory}/jacoco.exec"
	 */
	private File destFile;

	/**
	 * This mojo accepts any number of execution data file sets.
	 * 
	 * Note that you need an <tt>implementation</tt> hint on <tt>fileset</tt>
	 * with Maven 2 (not needed with Maven 3):
	 * 
	 * <pre>
	 * <code>
	 * &lt;fileSets&gt;
	 *   &lt;fileSet implementation="org.apache.maven.shared.model.fileset.FileSet"&gt;
	 *     &lt;directory&gt;${project.parent.build.directory}&lt;/directory&gt;
	 *     &lt;includes&gt;
	 *       &lt;include&gt;*.exec&lt;/include&gt;
	 *     &lt;/includes&gt;
	 *   &lt;/fileSet&gt;
	 * &lt;/fileSets&gt;
	 * </code>
	 * </pre>
	 * 
	 * @parameter property="jacoco.fileSets"
	 * @required
	 */
	private List<FileSet> fileSets;

	@Override
	protected void executeMojo() throws MojoExecutionException,
			MojoFailureException {
		new ExecFileMerger(fileSets, destFile, getLog()).merge();
	}
}
