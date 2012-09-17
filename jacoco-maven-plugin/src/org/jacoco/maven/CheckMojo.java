/*******************************************************************************
 * Copyright (c) 2009, 2012 Mountainminds GmbH & Co. KG and Contributors
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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;
import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.IBundleCoverage;
import org.jacoco.core.analysis.ICounter;
import org.jacoco.core.analysis.ICoverageNode;
import org.jacoco.core.analysis.ICoverageNode.CounterEntity;
import org.jacoco.core.data.ExecutionDataReader;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.data.SessionInfoStore;

/**
 * Checks that the code coverage metrics are being met.
 * 
 * @goal check
 * @phase verify
 * @requiresProject true
 * @threadSafe
 */
public class CheckMojo extends AbstractJacocoMojo {

	private static final String INSUFFICIENT_COVERAGE = "Insufficient code coverage for %s: %2$.2f%% < %3$.2f%%";
	private static final String CHECK_FAILED = "Coverage checks have not been met. See report for details.";
	private static final String CHECK_SUCCESS = "All coverage checks have been met.";

	/**
	 * Check configuration. Used to specify minimum coverage percentages that
	 * must be met.
	 * 
	 * @parameter
	 * @required
	 */
	private CheckConfiguration check;

	/**
	 * Halt the build if any of the checks fail.
	 * 
	 * @parameter expression="${jacoco.haltOnFailure}" default-value="true"
	 * @required
	 */
	private boolean haltOnFailure;

	/**
	 * File with execution data.
	 * 
	 * @parameter default-value="${project.build.directory}/jacoco.exec"
	 */
	private File dataFile;

	private SessionInfoStore sessionInfoStore;

	private ExecutionDataStore executionDataStore;

	private boolean canCheckCoverage() {
		if (!dataFile.exists()) {
			getLog().info(
					"Skipping JaCoCo execution due to missing execution data file");
			return false;
		}
		return true;
	}

	@Override
	public void executeMojo() throws MojoExecutionException,
			MojoExecutionException {
		if (!canCheckCoverage()) {
			return;
		}
		executeCheck();
	}

	private void executeCheck() throws MojoExecutionException {
		try {
			loadExecutionData();
		} catch (final IOException e) {
			throw new MojoExecutionException(
					"Unable to read execution data file " + dataFile + ": "
							+ e.getMessage(), e);
		}
		try {
			if (check()) {
				this.getLog().info(CHECK_SUCCESS);
			} else {
				this.handleFailure();
			}
		} catch (final IOException e) {
			throw new MojoExecutionException(
					"Error while checking coverage: " + e.getMessage(), e);
		}
	}

	private void loadExecutionData() throws IOException {
		sessionInfoStore = new SessionInfoStore();
		executionDataStore = new ExecutionDataStore();
		FileInputStream in = null;
		try {
			in = new FileInputStream(dataFile);
			final ExecutionDataReader reader = new ExecutionDataReader(in);
			reader.setSessionInfoVisitor(sessionInfoStore);
			reader.setExecutionDataVisitor(executionDataStore);
			reader.read();
		} finally {
			if (in != null) {
				in.close();
			}
		}
	}

	private boolean check()
			throws IOException {
		final IBundleCoverage bundle = createBundle();
		checkForMissingDebugInformation(bundle);

		boolean passed = true;

		for (final CounterEntity entity : CounterEntity.values()) {
			passed = this.checkCounter(
					entity,
					bundle.getCounter(entity),
					check.getRate(entity)) && passed;
		}

		return passed;
	}

	@SuppressWarnings("boxing")
	private boolean checkCounter(final CounterEntity entity,
			final ICounter counter,
			final double checkRate) {
		boolean passed = true;

		final double rate = counter.getCoveredRatio() * 100;

		if (rate < checkRate) {
			this.getLog()
					.warn(String.format(INSUFFICIENT_COVERAGE, entity.name(),
							rate,
							checkRate));
			passed = false;
		}
		return passed;
	}

	private void handleFailure() throws MojoExecutionException {
		if (this.haltOnFailure) {
			throw new MojoExecutionException(CHECK_FAILED);
		} else {
			this.getLog().warn(CHECK_FAILED);
		}
	}

	private void checkForMissingDebugInformation(final ICoverageNode node) {
		if (node.getClassCounter().getTotalCount() > 0
				&& node.getLineCounter().getTotalCount() == 0) {
			getLog().warn(
					"To enable source code annotation class files have to be compiled with debug information.");
		}
	}

	private IBundleCoverage createBundle() throws IOException {
		final CoverageBuilder builder = new CoverageBuilder();
		final Analyzer analyzer = new Analyzer(executionDataStore, builder);
		final File classesDir = new File(getProject().getBuild()
				.getOutputDirectory());

		final List<File> filesToAnalyze = getFilesToAnalyze(classesDir);

		for (final File file : filesToAnalyze) {
			analyzer.analyzeAll(file);
		}

		return builder.getBundle(getProject().getName());
	}

	private List<File> getFilesToAnalyze(final File rootDir) throws IOException {
		final String includes;
		if (getIncludes() != null && !getIncludes().isEmpty()) {
			includes = StringUtils.join(getIncludes().iterator(), ",");
		} else {
			includes = "**";
		}
		final String excludes;
		if (getExcludes() != null && !getExcludes().isEmpty()) {
			excludes = StringUtils.join(getExcludes().iterator(), ",");
		} else {
			excludes = "";
		}
		@SuppressWarnings("unchecked")
		final List<File> files = FileUtils
				.getFiles(rootDir, includes, excludes);
		return files;
	}

}
