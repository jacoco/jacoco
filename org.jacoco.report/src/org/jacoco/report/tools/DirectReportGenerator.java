/*******************************************************************************
 * Copyright (c) 2009, 2016 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Omer Azmon - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.report.tools;

import java.io.File;
import java.io.IOException;

import org.jacoco.core.analysis.IAnalyzer;
import org.jacoco.core.tools.ICoverageFetcherStyle;

/**
 * A implementation of ReportGenerator for use in running the report generator
 * directly from a test, instead of from a maven plugin or ant task.
 */
public class DirectReportGenerator extends ReportGenerator {
	private static final File DEFAULT_DATA_FILE = new File("target/jacoco.exec");

	private File dataFile;

	/**
	 * Constructor
	 */
	public DirectReportGenerator() {
		dataFile = DEFAULT_DATA_FILE;
	}

	/**
	 * Set the input data file/directory.
	 * 
	 * @param dataFile
	 *            the input data file/directory.
	 * @return this object
	 */
	public final DirectReportGenerator setDataFile(final File dataFile) {
		this.dataFile = dataFile == null ? DEFAULT_DATA_FILE : dataFile;
		return this;
	}

	/**
	 * Get the input data file/directory.
	 * 
	 * @return the input data file/directory.
	 */
	public File getDataFile() {
		return dataFile;
	}

	@Override
	protected final void analyzeExecutionData(final IAnalyzer analyzer)
			throws IOException {

		for (final File file : getClassesDirectories()) {
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

	@Override
	public boolean canGenerateReport() {
		if (!getDataFile().exists()) {
			getLog().info(
					"Skipping JaCoCo execution due to missing execution data file:"
							+ getDataFile());
			return false;
		}
		if (!classesDirectoriesExist()) {
			getLog().info(
					"Skipping JaCoCo execution due to missing classes directory:"
							+ getClassesDirectories());
			return false;
		}
		return true;
	}

	private boolean classesDirectoriesExist() {
		if (getClassesDirectories().isEmpty()) {
			return false;
		}
		for (final File classesDirectory : getClassesDirectories()) {
			if (!classesDirectory.exists()) {
				return false;
			}
		}
		return true;
	}

}