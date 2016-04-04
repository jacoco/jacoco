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

import static java.lang.String.format;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.IBundleCoverage;
import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.data.ExecutionData;
import org.jacoco.core.data.SessionInfo;
import org.jacoco.core.tools.ExecFileLoader;

/**
 * Creates IBundleCoverage instances. Execution data has to be loaded before
 * coverage can be calculated.
 */
public final class BundleCreator {

	private final Log log;
	private final ExecFileLoader loader;

	/**
	 * Construct a new BundleCreator given the MavenProject and FileFilter.
	 * 
	 * @param log
	 *            for log output
	 */
	public BundleCreator(final Log log) {
		this.log = log;
		this.loader = new ExecFileLoader();
	}

	/**
	 * Loads the given execution data file.
	 * 
	 * @param execFile
	 *            execution data file to load
	 * @throws IOException
	 *             if the file can't be loaded
	 */
	public void loadExecutionData(final File execFile) throws IOException {
		loader.load(execFile);
	}

	/**
	 * @return all session infos loaded from execution data files
	 */
	public List<SessionInfo> getSessionInfos() {
		return loader.getSessionInfoStore().getInfos();
	}

	/**
	 * @return all execution data loaded from execution data files
	 */
	public Collection<ExecutionData> getExecutionData() {
		return loader.getExecutionDataStore().getContents();
	}

	/**
	 * Create an IBundleCoverage for the given project.
	 * 
	 * @param project
	 *            the MavenProject
	 * @param includes
	 *            list of includes patterns
	 * @param excludes
	 *            list of excludes patterns
	 * @return the coverage data.
	 * @throws IOException
	 *             if class files can't be read
	 */
	public IBundleCoverage createBundle(final MavenProject project,
			final List<String> includes, final List<String> excludes)
			throws IOException {
		final CoverageBuilder builder = new CoverageBuilder();
		final File classesDir = new File(project.getBuild()
				.getOutputDirectory());

		if (classesDir.isDirectory()) {
			final Analyzer analyzer = new Analyzer(
					loader.getExecutionDataStore(), builder);
			final FileFilter filter = new FileFilter(includes, excludes);
			for (final File file : filter.getFiles(classesDir)) {
				analyzer.analyzeAll(file);
			}
		}

		final IBundleCoverage bundle = builder.getBundle(project.getName());
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
		if (bundle.getClassCounter().getTotalCount() > 0
				&& bundle.getLineCounter().getTotalCount() == 0) {
			log.warn("To enable source code annotation class files have to be compiled with debug information.");
		}
	}

}
