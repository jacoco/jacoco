/*******************************************************************************
 * Copyright (c) 2009, 2016 Mountainminds GmbH & Co. KG and Contributors
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.maven.doxia.siterenderer.Renderer;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.reporting.AbstractMavenReport;
import org.apache.maven.reporting.MavenReportException;
import org.jacoco.core.analysis.IBundleCoverage;
import org.jacoco.report.FileMultiReportOutput;
import org.jacoco.report.IReportGroupVisitor;
import org.jacoco.report.IReportVisitor;
import org.jacoco.report.ISourceFileLocator;
import org.jacoco.report.MultiReportVisitor;
import org.jacoco.report.csv.CSVFormatter;
import org.jacoco.report.html.HTMLFormatter;
import org.jacoco.report.xml.XMLFormatter;

/**
 * Base class for creating a code coverage report for tests of a single project
 * in multiple formats (HTML, XML, and CSV).
 */
public abstract class AbstractReportMojo extends AbstractMavenReport {

	/**
	 * Encoding of the generated reports.
	 * 
	 * @parameter property="project.reporting.outputEncoding"
	 *            default-value="UTF-8"
	 */
	String outputEncoding;

	/**
	 * Encoding of the source files.
	 * 
	 * @parameter property="project.build.sourceEncoding" default-value="UTF-8"
	 */
	String sourceEncoding;

	/**
	 * A list of class files to include in the report. May use wildcard
	 * characters (* and ?). When not specified everything will be included.
	 * 
	 * @parameter
	 */
	List<String> includes;

	/**
	 * A list of class files to exclude from the report. May use wildcard
	 * characters (* and ?). When not specified nothing will be excluded.
	 * 
	 * @parameter
	 */
	List<String> excludes;

	/**
	 * Flag used to suppress execution.
	 * 
	 * @parameter property="jacoco.skip" default-value="false"
	 */
	boolean skip;

	/**
	 * Maven project.
	 * 
	 * @parameter property="project"
	 * @readonly
	 */
	MavenProject project;

	/**
	 * Doxia Site Renderer.
	 * 
	 * @component
	 */
	Renderer siteRenderer;

	public String getDescription(final Locale locale) {
		return getName(locale) + " Coverage Report.";
	}

	@Override
	public boolean isExternalReport() {
		return true;
	}

	@Override
	protected MavenProject getProject() {
		return project;
	}

	@Override
	protected Renderer getSiteRenderer() {
		return siteRenderer;
	}

	/**
	 * Returns the list of class files to include in the report.
	 * 
	 * @return class files to include, may contain wildcard characters
	 */
	List<String> getIncludes() {
		return includes;
	}

	/**
	 * Returns the list of class files to exclude from the report.
	 * 
	 * @return class files to exclude, may contain wildcard characters
	 */
	List<String> getExcludes() {
		return excludes;
	}

	@Override
	public boolean canGenerateReport() {
		if (skip) {
			getLog().info(
					"Skipping JaCoCo execution because property jacoco.skip is set.");
			return false;
		}
		if (!getDataFile().exists()) {
			getLog().info(
					"Skipping JaCoCo execution due to missing execution data file:"
							+ getDataFile());
			return false;
		}
		final File classesDirectory = new File(getProject().getBuild()
				.getOutputDirectory());
		if (!classesDirectory.exists()) {
			getLog().info(
					"Skipping JaCoCo execution due to missing classes directory:"
							+ classesDirectory);
			return false;
		}
		return true;
	}

	/**
	 * This method is called when the report generation is invoked directly as a
	 * standalone Mojo.
	 */
	@Override
	public void execute() throws MojoExecutionException {
		if (!canGenerateReport()) {
			return;
		}
		try {
			executeReport(Locale.getDefault());
		} catch (final MavenReportException e) {
			throw new MojoExecutionException("An error has occurred in "
					+ getName(Locale.ENGLISH) + " report generation.", e);
		}
	}

	@Override
	protected void executeReport(final Locale locale)
			throws MavenReportException {
		try {
			final BundleCreator creator = new BundleCreator(getLog());
			loadExecutionData(creator);
			final IReportVisitor visitor = createVisitor(locale);
			visitor.visitInfo(creator.getSessionInfos(),
					creator.getExecutionData());
			createReport(visitor, creator);
			visitor.visitEnd();
		} catch (final IOException e) {
			throw new MavenReportException("Error while creating report: "
					+ e.getMessage(), e);
		}
	}
	
	void loadExecutionData(final BundleCreator creator) throws IOException {
		creator.loadExecutionData(getDataFile());
	}

	void createReport(final IReportGroupVisitor visitor,
			final BundleCreator creator) throws IOException {
		final IBundleCoverage bundle = creator.createBundle(getProject(),
				this.getIncludes(), this.getExcludes());
		final SourceFileCollection locator = new SourceFileCollection(
				getCompileSourceRoots(getProject()), sourceEncoding);
		visitor.visitBundle(bundle, locator);
	}

	IReportVisitor createVisitor(final Locale locale) throws IOException {
		final List<IReportVisitor> visitors = new ArrayList<IReportVisitor>();
		getOutputDirectoryFile().mkdirs();
		final XMLFormatter xmlFormatter = new XMLFormatter();
		xmlFormatter.setOutputEncoding(outputEncoding);
		visitors.add(xmlFormatter.createVisitor(new FileOutputStream(new File(
				getOutputDirectoryFile(), "jacoco.xml"))));
		final CSVFormatter csvFormatter = new CSVFormatter();
		csvFormatter.setOutputEncoding(outputEncoding);
		visitors.add(csvFormatter.createVisitor(new FileOutputStream(new File(
				getOutputDirectoryFile(), "jacoco.csv"))));
		final HTMLFormatter htmlFormatter = new HTMLFormatter();
		htmlFormatter.setOutputEncoding(outputEncoding);
		htmlFormatter.setLocale(locale);
		visitors.add(htmlFormatter.createVisitor(new FileMultiReportOutput(
				getOutputDirectoryFile())));
		return new MultiReportVisitor(visitors);
	}

	static File resolvePath(final MavenProject project, final String path) {
		File file = new File(path);
		if (!file.isAbsolute()) {
			file = new File(project.getBasedir(), path);
		}
		return file;
	}

	static List<File> getCompileSourceRoots(final MavenProject project) {
		final List<File> result = new ArrayList<File>();
		for (final Object path : project.getCompileSourceRoots()) {
			result.add(resolvePath(project, (String) path));
		}
		return result;
	}

	static class SourceFileCollection implements ISourceFileLocator {

		private final List<File> sourceRoots;
		private final String encoding;

		public SourceFileCollection(final List<File> sourceRoots,
				final String encoding) {
			this.sourceRoots = sourceRoots;
			this.encoding = encoding;
		}

		public Reader getSourceFile(final String packageName,
				final String fileName) throws IOException {
			final String r;
			if (packageName.length() > 0) {
				r = packageName + '/' + fileName;
			} else {
				r = fileName;
			}
			for (final File sourceRoot : sourceRoots) {
				final File file = new File(sourceRoot, r);
				if (file.exists() && file.isFile()) {
					return new InputStreamReader(new FileInputStream(file),
							encoding);
				}
			}
			return null;
		}

		public int getTabWidth() {
			return 4;
		}
	}

	abstract File getDataFile();

	abstract File getOutputDirectoryFile();

}
