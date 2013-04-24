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
	 * @return the coverage data.
	 * @throws IOException
	 *             if class files can't be read
	 */
	public IBundleCoverage createBundle(
			final ExecutionDataStore executionDataStore) throws IOException {
		final CoverageBuilder builder = new CoverageBuilder();
		final Analyzer analyzer = new Analyzer(executionDataStore, builder);

		for (final File file : getFilesToAnalyze()) {
			analyzer.analyzeAll(file);
		}

		return builder.getBundle(this.project.getName());
	}

	/**
	 * Get files to analyze in classes and test-classes directories.
	 * 
	 * @return list of files.
	 * @throws IOException
	 *             if class files can't be read
	 */
	private List<File> getFilesToAnalyze() throws IOException {
		final List<File> filesToAnalyze = getFilesToAnalyze(this.project
				.getBuild().getOutputDirectory());
		final List<File> testFilesToAnalyze = getFilesToAnalyze(this.project
				.getBuild().getTestOutputDirectory());
		filesToAnalyze.addAll(testFilesToAnalyze);

		return filesToAnalyze;
	}

	/**
	 * Get files to analyze in specified directories.
	 * 
	 * @param directory
	 *            is searched.
	 * @return list of files.
	 * @throws IOException
	 *             if class files can't be read
	 */
	private List<File> getFilesToAnalyze(final String directory)
			throws IOException {
		@SuppressWarnings("unchecked")
		final List<File> filesToAnalyze = FileUtils.getFiles(
				new File(directory), fileFilter.getIncludes(),
				fileFilter.getExcludes());

		return filesToAnalyze;
	}
}
