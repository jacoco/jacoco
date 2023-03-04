/*******************************************************************************
 * Copyright (c) 2009, 2023 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Mads Mohr Christensen - implementation of MergeMojo
 *
 *******************************************************************************/
package org.jacoco.maven;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.shared.model.fileset.FileSet;
import org.apache.maven.shared.model.fileset.util.FileSetManager;
import org.jacoco.core.tools.ExecFileLoader;

/**
 * Mojo for merging a set of execution data files (*.exec) into a single file
 *
 * @since 0.6.4
 */
@Mojo(name = "merge", defaultPhase = LifecyclePhase.GENERATE_RESOURCES, threadSafe = true)
public class MergeMojo extends AbstractJacocoMojo {

	private static final String MSG_SKIPPING = "Skipping JaCoCo merge execution due to missing execution data files";

	/**
	 * Path to the output file for execution data.
	 */
	@Parameter(property = "jacoco.destFile", defaultValue = "${project.build.directory}/jacoco.exec")
	private File destFile;

	/**
	 * This mojo accepts any number of execution data file sets.
	 *
	 * <pre>
	 * <code>
	 * &lt;fileSets&gt;
	 *   &lt;fileSet&gt;
	 *     &lt;directory&gt;${project.build.directory}&lt;/directory&gt;
	 *     &lt;includes&gt;
	 *       &lt;include&gt;*.exec&lt;/include&gt;
	 *     &lt;/includes&gt;
	 *   &lt;/fileSet&gt;
	 * &lt;/fileSets&gt;
	 * </code>
	 * </pre>
	 */
	@Parameter(required = true)
	private List<FileSet> fileSets;

	@Override
	protected void executeMojo()
			throws MojoExecutionException, MojoFailureException {
		if (!canMergeReports()) {
			return;
		}
		executeMerge();
	}

	private boolean canMergeReports() {
		if (fileSets == null || fileSets.isEmpty()) {
			getLog().info(MSG_SKIPPING);
			return false;
		}
		return true;
	}

	private void executeMerge() throws MojoExecutionException {
		final ExecFileLoader loader = new ExecFileLoader();

		load(loader);
		save(loader);
	}

	private void load(final ExecFileLoader loader)
			throws MojoExecutionException {
		final FileSetManager fileSetManager = new FileSetManager(getLog());
		for (final FileSet fileSet : fileSets) {
			for (final String includedFilename : fileSetManager
					.getIncludedFiles(fileSet)) {
				final File inputFile = new File(fileSet.getDirectory(),
						includedFilename);
				if (inputFile.isDirectory()) {
					continue;
				}
				try {
					getLog().info("Loading execution data file "
							+ inputFile.getAbsolutePath());
					loader.load(inputFile);
				} catch (final IOException e) {
					throw new MojoExecutionException(
							"Unable to read " + inputFile.getAbsolutePath(), e);
				}
			}
		}
	}

	private void save(final ExecFileLoader loader)
			throws MojoExecutionException {
		if (loader.getExecutionDataStore().getContents().isEmpty()) {
			getLog().info(MSG_SKIPPING);
			return;
		}
		getLog().info("Writing merged execution data to "
				+ destFile.getAbsolutePath());
		try {
			loader.save(destFile, false);
		} catch (final IOException e) {
			throw new MojoExecutionException(
					"Unable to write merged file " + destFile.getAbsolutePath(),
					e);
		}
	}

}
