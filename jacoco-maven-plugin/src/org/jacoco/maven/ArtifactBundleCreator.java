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

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.logging.Log;
import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.IBundleCoverage;
import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.data.ExecutionDataStore;

import java.io.IOException;
import java.util.Collection;

import static java.lang.String.format;

/**
 * Creates an IBundleCoverage for a Maven dependency artifact.
 */
public final class ArtifactBundleCreator {

	private final Log log;
	private final String bundleName;

	/**
	 * Construct a new ArtifactBundleCreator a Bundle name.
	 *
	 * @param log
	 *            the log output
	 * @param name
	 *            of the Bundle
	 */
	public ArtifactBundleCreator(final Log log, final String name) {

		this.log = log;
		this.bundleName = name;
	}

	/**
	 * Create an IBundleCoverage for the given ExecutionDataStore for the code
	 * in Maven Artifact.
	 * 
	 * @param executionDataStore
	 *            the execution data
	 * @param artifact
	 *            the Maven Artifact of the dependency
	 * @return the coverage data
	 * @throws IOException
	 *             if class files can't be read.
	 */
	public IBundleCoverage createBundleOfArtifact(
			final ExecutionDataStore executionDataStore,
			final Artifact artifact)
					throws IOException {

		final CoverageBuilder builder = new CoverageBuilder();
		final Analyzer analyzer = new Analyzer(executionDataStore, builder);
		analyzer.analyzeAll(artifact.getFile());

		final IBundleCoverage bundle = builder.getBundle(bundleName);
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
