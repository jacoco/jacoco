/*******************************************************************************
 * Copyright (c) 2009, 2016 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Oliver - initial implementation
 *
 *******************************************************************************/
package org.jacoco.maven;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.reporting.MavenReportException;
import org.apache.maven.shared.model.fileset.FileSet;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Creates a code coverage report from multiple reactor projects (HTML, XML, and
 * CSV).
 *
 * @goal aggregate-report
 * @requiresProject false
 * @aggregator
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

	/**
	 * This optionally provide a set of jacoco exec files to combine into an aggregated report.
	 *
	 * Note that you need an <tt>implementation</tt> hint on <tt>fileset</tt>
	 * with Maven 2 (not needed with Maven 3):
	 *
	 * <pre>
	 * <code>
	 * &lt;fileSets&gt;
	 *   &lt;fileSet implementation="org.apache.maven.shared.model.fileset.FileSet"&gt;
	 *     &lt;directory&gt;${project.parent.build.directory}&lt;/directory&gt;
	 *     &lt;includes&gt;
	 *       &lt;include&gt;*.exec&lt;/include&gt;
	 *     &lt;/includes&gt;
	 *   &lt;/fileSet&gt;
	 * &lt;/fileSets&gt;
	 * </code>
	 * </pre>
	 *
	 * @parameter property="jacoco.fileSets"
	 */
	private List<FileSet> fileSets;

	private ArrayList<String> sourceFolders;

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

		try {
			concatenateExecFiles();
		} catch (MojoExecutionException e) {
			throw new MavenReportException("Failed to combine report exec files", e);
		}

		detectSourceAndClassFolders();

		super.executeReport(locale);
	}

	private void detectSourceAndClassFolders() {
		List<String> classFolders = super.getClassFolders();
		if (classFolders == null) {
			classFolders = new ArrayList<String>();
		}

		sourceFolders = new ArrayList<String>();
		for (final MavenProject reactor : reactorProjects) {
			if (reactor != getProject()) {
				sourceFolders.addAll(reactor.getCompileSourceRoots());
				classFolders.add(reactor.getBuild().getOutputDirectory());
			}
		}
		setClassFolders(classFolders);
	}

	@Override
  File getDataFile() {
    if(destFile== null) {
      return super.getDataFile();
    }
    return destFile;
  }

  @Override
	List<File> getCompileSourceRoots() {
		final List<File> result = super.getCompileSourceRoots();

		for (String sourceFolder : sourceFolders) {
			result.add(new File(sourceFolder));
		}

		return result;
	}

	private void concatenateExecFiles() throws MojoExecutionException {
		if(fileSets==null || fileSets.isEmpty()) {
			fileSets = new ArrayList<FileSet>();

			for (final MavenProject reactor : reactorProjects) {
				if (reactor != getProject()) {
					final File input = new File(reactor.getBasedir(),
									reactorDataFile);
					if (input.exists() && input.isFile()) {
						FileSet dir = new FileSet();
						dir.addInclude(input.getName());
						dir.setDirectory(input.getParent());
						fileSets.add(dir);
					}
				}
			}
		}

		new ExecFileMerger(fileSets, destFile, getLog()).merge();
	}
}
