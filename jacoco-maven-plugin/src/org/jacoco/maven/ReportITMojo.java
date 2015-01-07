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

import java.io.File;
import java.util.Locale;

/**
 * Same as <code>report</code>, but provides default values suitable for
 * integration-tests:
 * <ul>
 * <li>bound to <code>report-integration</code> phase</li>
 * <li>different <code>dataFile</code></li>
 * </ul>
 * 
 * @phase verify
 * @goal report-integration
 * @requiresProject true
 * @threadSafe
 * @since 0.6.4
 */
public class ReportITMojo extends AbstractReportMojo {

	/**
	 * Output directory for the reports. Note that this parameter is only
	 * relevant if the goal is run from the command line or from the default
	 * build lifecycle. If the goal is run indirectly as part of a site
	 * generation, the output directory configured in the Maven Site Plugin is
	 * used instead.
	 * 
	 * @parameter default-value="${project.reporting.outputDirectory}/jacoco-it"
	 */
	private File outputDirectory;

	/**
	 * File with execution data.
	 * 
	 * @parameter default-value="${project.build.directory}/jacoco-it.exec"
	 */
	private File dataFile;

	@Override
	protected String getOutputDirectory() {
		return outputDirectory.getAbsolutePath();
	}

	@Override
	public void setReportOutputDirectory(final File reportOutputDirectory) {
		if (reportOutputDirectory != null
				&& !reportOutputDirectory.getAbsolutePath().endsWith(
						"jacoco-it")) {
			outputDirectory = new File(reportOutputDirectory, "jacoco-it");
		} else {
			outputDirectory = reportOutputDirectory;
		}
	}

	@Override
	File getDataFile() {
		return dataFile;
	}

	@Override
	File getOutputDirectoryFile() {
		return outputDirectory;
	}

	@Override
	public String getOutputName() {
		return "jacoco-it/index";
	}

	@Override
	public String getName(final Locale locale) {
		return "JaCoCo IT";
	}
}
