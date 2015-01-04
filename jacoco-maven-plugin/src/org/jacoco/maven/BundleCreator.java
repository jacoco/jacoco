/*******************************************************************************
 * Copyright (c) 2009, 2015 Mountainminds GmbH & Co. KG and Contributors
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

import static java.lang.String.format;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;
import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.IBundleCoverage;
import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.data.ExecutionDataStore;

/**
 * Creates an IBundleCoverage.
 */
public final class BundleCreator {

	private final MavenProject project;
	private final FileFilter fileFilter;
	private final Log log;

	/**
	 * Construct a new BundleCreator given the MavenProject and FileFilter.
	 * 
	 * @param project
	 *            the MavenProject
	 * @param fileFilter
	 *            the FileFilter
	 * @param log
	 *            for log output
	 */
	public BundleCreator(final MavenProject project,
			final FileFilter fileFilter, final Log log) {
		this.project = project;
		this.fileFilter = fileFilter;
		this.log = log;
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
		final File classesDir = new File(this.project.getBuild()
				.getOutputDirectory());

		@SuppressWarnings("unchecked")
		final List<File> filesToAnalyze = FileUtils.getFiles(classesDir,
				fileFilter.getIncludes(), fileFilter.getExcludes());

		for (final File file : filesToAnalyze) {
			analyzer.analyzeAll(file);
		}

		final IBundleCoverage bundle = builder
				.getBundle(this.project.getName());
		logBundleInfo(bundle, builder.getNoMatchClasses());

		return bundle;
	}

	private void logBundleInfo(final IBundleCoverage bundle,
			final Collection<IClassCoverage> nomatch) {
		log.info(format("Analyzed bundle '%s' with %s classes",
				bundle.getName(),
				Integer.valueOf(bundle.getClassCounter().getTotalCount())));
		if (!nomatch.isEmpty()) {
			log.warn(format(
					"Classes in bundle '%s' do no match with execution data. "
							+ "For report generation the same class files must be used as at runtime.",
					bundle.getName()));
			for (final IClassCoverage c : nomatch) {
				log.warn(format("Execution data for class %s does not match.",
						c.getName()));
			}
		}
	}

}
