/*******************************************************************************
 * Copyright (c) 2009, 2015 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Omer Azmon - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.maven;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.codehaus.plexus.util.FileUtils;
import org.jacoco.core.analysis.IAnalyzer;
import org.jacoco.core.tools.ICoverageFetcherStyle;
import org.jacoco.core.tools.LoggingBridge;
import org.jacoco.report.tools.ReportGenerator;

/**
 * A internal binding class between the reporting packages that translates the
 * Maven style into the common style.
 * <p>
 * While the base class has default values. Maven is expected to override them
 * all.
 */
class MavenReportGenerator extends ReportGenerator {
	private static final String MSG_SKIPPING = "Skipping JaCoCo execution due to missing execution data file:";

	private boolean skip = false;
	private File dataFile;
	private List<String> includes;
	private List<String> excludes;

	/**
	 * Construct an instance.
	 * 
	 * @param mojo
	 *            used to extract the logger from the mojo. This needs to be
	 *            this way as the logger, per maven, may not be stored, but one
	 *            must invoke the Mojo's {@code getlog} method everytime.
	 */
	public MavenReportGenerator(final AbstractMojo mojo) {
		super(new MavenLoggingBridge(mojo));
	}

	public boolean isSkip() {
		return skip;
	}

	public void setSkip(final boolean skip) {
		this.skip = skip;
	}

	public File getDataFile() {
		return dataFile;
	}

	public void setDataFile(final File dataFile) {
		this.dataFile = dataFile;
	}

	public List<String> getIncludes() {
		return includes;
	}

	public void setIncludes(final List<String> includes) {
		this.includes = includes;
	}

	public List<String> getExcludes() {
		return excludes;
	}

	public void setExcludes(final List<String> excludes) {
		this.excludes = excludes;
	}

	/**
	 * This is the method invoked by the Maven reporting mojo. This is so, as
	 * the setting are not set normally until the execute method is invoked.
	 * 
	 * @param skip
	 *            should the report be skipped
	 * @param dataFile
	 *            the execution data file or directory where it is
	 * @param classesDirectories
	 *            the base directory for the classes.
	 * @return {@code true} if the report can be generated; Otherwise
	 *         {@code false}
	 */
	public boolean canGenerateReport(final boolean skip, final File dataFile,
			final List<File> classesDirectories) {
		setSkip(skip);
		setDataFile(dataFile);
		setClassesDirectories(classesDirectories);
		return canGenerateReport();
	}

	/**
	 * This method override the parent to provide Maven's rules. But is not the
	 * method invoked by the Maven mojo.
	 */
	@Override
	protected boolean canGenerateReport() {
		if (skip) {
			getLog().info(
					"Skipping JaCoCo execution because property jacoco.skip is set.");
			return false;
		}
		if (!dataFile.exists()) {
			getLog().info(MSG_SKIPPING + dataFile);
			return false;
		}
		final File classesDirectory = getClassesDirectories().get(0);
		if (!classesDirectory.exists()) {
			getLog().info(
					"Skipping JaCoCo execution due to missing classes directory:"
							+ getClassesDirectories());
			return false;
		}
		return true;
	}

	@Override
	protected final void analyzeExecutionData(final IAnalyzer analyzer)
			throws IOException {
		final FileFilter fileFilter = new FileFilter(includes, excludes);
		@SuppressWarnings("unchecked")
		final List<File> listOfFilesToAnalyze = FileUtils.getFiles(
				getClassesDirectories().get(0), fileFilter.getIncludes(),
				fileFilter.getExcludes());
		for (final File file : listOfFilesToAnalyze) {
			analyzer.analyzeAll(file);
		}
	}

	@Override
	protected final void loadExecutionData(
			final ICoverageFetcherStyle dataFetcher) throws IOException {
		try {
			dataFetcher.loadExecutionData(dataFile);
		} catch (final IOException e) {
			final IOException ex = new IOException(
					"Unable to read execution data files in " + dataFile + ": "
							+ e.getMessage());
			ex.initCause(e);
			throw ex;
		}
	}

	/**
	 * A bridge between the generic jacoco core logging and the maven logging.
	 */
	private static class MavenLoggingBridge implements LoggingBridge {
		private final AbstractMojo mojo;

		public MavenLoggingBridge(final AbstractMojo mojo) {
			this.mojo = mojo;
		}

		public void info(final String msg) {
			mojo.getLog().info(msg);
		}

		public void warning(final String msg) {
			mojo.getLog().warn(msg);
		}

		public void severe(final String msg) {
			mojo.getLog().error(msg);
		}
	}

}