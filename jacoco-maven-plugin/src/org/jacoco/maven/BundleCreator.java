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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;
import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.IBundleCoverage;
import org.jacoco.core.data.ExecutionDataStore;

/**
 * Creates an IBundleCoverage.
 */
public final class BundleCreator {

	private final MavenProject project;
	private final FileFilter fileFilter;

	/**
	 * Construct a new BundleCreator given the MavenProject and FileFilter.
	 * 
	 * @param project
	 *            the MavenProject
	 * @param fileFilter
	 *            the FileFilter
	 */
	public BundleCreator(final MavenProject project, final FileFilter fileFilter) {
		this.project = project;
		this.fileFilter = fileFilter;
	}

	/**
	 * Create an IBundleCoverage for the given ExecutionDataStore.
	 * 
	 * @param executionDataStore
	 *            the execution data.
	 * @param additionalClassesDirs
	 *            additional class dirs to be scanned for class files
	 * @return the coverage data.
	 * @throws IOException
	 *             if class files can't be read
	 */
	public IBundleCoverage createBundle(
			final ExecutionDataStore executionDataStore,
			final Collection<File> additionalClassesDirs) throws IOException {
		final CoverageBuilder builder = new CoverageBuilder();
		final Analyzer analyzer = new Analyzer(executionDataStore, builder);
		final File classesDir = new File(this.project.getBuild()
				.getOutputDirectory());

		final List<File> filesToAnalyze = new ArrayList<File>();
		addDirToFileList(classesDir, filesToAnalyze);

		if (additionalClassesDirs != null) {
			for (final File file : additionalClassesDirs) {
				addDirToFileList(file, filesToAnalyze);
			}
		}

		for (final File file : filesToAnalyze) {
			analyzer.analyzeAll(file);
		}

		return builder.getBundle(this.project.getName());
	}

	private void addDirToFileList(final File classesDir,
			final List<File> filesToAnalyze) throws IOException {
		if (doesDirectoryExist(classesDir)) {
			filesToAnalyze.addAll(FileUtils.getFiles(classesDir,
					fileFilter.getIncludes(), fileFilter.getExcludes()));
		}
	}

	private boolean doesDirectoryExist(final File classesDir) {
		return classesDir != null && classesDir.exists()
				&& classesDir.isDirectory();
	}
}
