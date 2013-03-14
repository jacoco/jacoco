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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.maven.project.MavenProject;
import org.apache.maven.reporting.MavenReportException;

/**
 * Creates a code coverage report from multiple reactor projects (HTML, XML, and
 * CSV).
 * 
 * @goal aggregate-report
 * @requiresProject false
 * @aggregate
 */
public class ReportAggregateMojo extends ReportMojo {

	/**
	 * Flag used to suppress execution.
	 * 
	 * @parameter expression="${jacoco.skip}" default-value="false"
	 */
	private boolean skip;

	/**
	 * The projects in the reactor.
	 * 
	 * @parameter expression="${reactorProjects}"
	 * @readonly
	 */
	private List<MavenProject> reactorProjects;

	/**
	 * The relative path from the root of each reactor that says where the
	 * jacoco.exec file has been placed.
	 * 
	 * @parameter default-value="target/jacoco.exec"
	 * @readonly
	 */
	private String reactorDataFile;

	/**
	 * Path to the output file for aggregated execution data.
	 * 
	 * @parameter expression="${jacoco.destFile}"
	 *            default-value="${project.build.directory}/jacoco.exec"
	 */
	private File destFile;

	@Override
	public boolean canGenerateReport() {
		if (!"pom".equals(getProject().getPackaging())) {
			getLog().info(
					"Skipping JaCoCo since this is not of packaging type 'pom'");
			return false;
		}
		if (skip) {
			getLog().info("Skipping JaCoCo execution");
			return false;
		}
		return true;
	}

	@Override
	protected void executeReport(final Locale locale)
			throws MavenReportException {

		concatenateExecFiles();
		setDataFile(destFile);

		List<String> sourceFolders = super.getSourceFolders();
		if (sourceFolders == null) {
			sourceFolders = new ArrayList<String>();
		}

		List<String> classFolders = super.getClassFolders();
		if (classFolders == null) {
			classFolders = new ArrayList<String>();
		}

		for (final MavenProject reactor : reactorProjects) {
			if (reactor != getProject()) {
				sourceFolders.addAll(reactor.getCompileSourceRoots());
				classFolders.add(reactor.getBuild().getOutputDirectory());
			}
		}

		setSourceFolders(sourceFolders);
		setClassFolders(classFolders);
		super.executeReport(locale);
	}

	private void concatenateExecFiles() {
		try {
			FileOutputStream fos = null;
			try {

				if (!destFile.getParentFile().exists()) {
					destFile.getParentFile().mkdirs();
				}

				fos = new FileOutputStream(destFile);

				for (final MavenProject reactor : reactorProjects) {
					if (reactor != getProject()) {
						final File input = new File(reactor.getBasedir(),
								reactorDataFile);
						if (input.exists() && input.isFile()) {
							concatenateFile(fos, input);
						}
					}

				}
			} finally {
				if (fos != null) {
					fos.close();
				}
			}
		} catch (final IOException e) {
			throw new RuntimeException(
					"Failed to concatenate jacoco.exec files", e);
		}
	}

	private void concatenateFile(final FileOutputStream fos, final File input)
			throws FileNotFoundException, IOException {
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(input);

			final byte[] buff = new byte[1024];
			int read = -1;
			while ((read = fis.read(buff)) != -1) {
				fos.write(buff, 0, read);
			}
		} finally {
			if (fis != null) {
				fis.close();
			}
		}
	}
}
