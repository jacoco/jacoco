/*******************************************************************************
 * Copyright (c) 2009, 2013 Mountainminds GmbH & Co. KG and Contributors
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
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.maven.doxia.siterenderer.Renderer;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.reporting.AbstractMavenReport;
import org.apache.maven.reporting.MavenReportException;
import org.jacoco.core.analysis.IBundleCoverage;
import org.jacoco.core.analysis.ICoverageNode;
import org.jacoco.core.tools.ExecFileLoader;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.data.SessionInfoStore;
import org.jacoco.report.FileMultiReportOutput;
import org.jacoco.report.IReportGroupVisitor;
import org.jacoco.report.IReportVisitor;
import org.jacoco.report.ISourceFileLocator;
import org.jacoco.report.MultiReportVisitor;
import org.jacoco.report.csv.CSVFormatter;
import org.jacoco.report.html.HTMLFormatter;
import org.jacoco.report.xml.XMLFormatter;

/**
 *
 * @author Mirko Friedenhagen
 * @author Lukasz Pielak
 */
public abstract class AbstractReportMojo extends AbstractMavenReport {

    private static final Set<String> SKIPPED_PACKAGINGS = new HashSet(Arrays.asList("pom", "ear"));

	/**
	 * Encoding of the generated reports.
	 *
	 * @parameter expression="${project.reporting.outputEncoding}"
	 * default-value="UTF-8"
	 */
	protected String outputEncoding;
	/**
	 * Encoding of the source files.
	 *
	 * @parameter expression="${project.build.sourceEncoding}"
	 * default-value="UTF-8"
	 */
	protected String sourceEncoding;
	/**
	 * A list of class files to include in the report. May use wildcard
	 * characters (* and ?). When not specified everything will be included.
	 *
	 * @parameter
	 */
	protected List<String> includes;
	/**
	 * A list of class files to exclude from the report. May use wildcard
	 * characters (* and ?). When not specified nothing will be excluded.
	 *
	 * @parameter
	 */
	protected List<String> excludes;
	/**
	 * Flag used to suppress execution.
	 *
	 * @parameter expression="${jacoco.skip}" default-value="false"
	 */
	protected boolean skip;
	/**
	 * Maven project.
	 *
	 * @parameter expression="${project}"
	 * @readonly
	 */
	protected MavenProject project;
	/**
	 * Doxia Site Renderer.
	 *
	 * @component
	 */
	protected Renderer siteRenderer;
	protected SessionInfoStore sessionInfoStore;
	protected ExecutionDataStore executionDataStore;

	public abstract String getOutputName();

	public abstract String getName(final Locale locale);

	public String getDescription(final Locale locale) {
		return getName(locale) + " Coverage Report.";
	}

	@Override
	public boolean isExternalReport() {
		return true;
	}

	@Override
	protected abstract String getOutputDirectory();

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
	protected List<String> getIncludes() {
		return includes;
	}

	/**
	 * Returns the list of class files to exclude from the report.
	 *
	 * @return class files to exclude, may contain wildcard characters
	 */
	protected List<String> getExcludes() {
		return excludes;
	}

	@Override
	public abstract void setReportOutputDirectory(final File reportOutputDirectory);

	@Override
	public boolean canGenerateReport() {
        if (SKIPPED_PACKAGINGS.contains(project.getPackaging())) {
            getLog().info(String.format("Skipping JaCoCo for project with packaging type '%s'", project.getPackaging()));
            return false;
        }
		if (skip) {
			getLog().info("Skipping JaCoCo execution");
			return false;
		}
		if (!getDataFile().exists()) {
			getLog().info("Skipping JaCoCo execution due to missing execution data file");
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
			throw new MojoExecutionException("An error has occurred in " +
					getName(Locale.ENGLISH) + " report generation.", e);
		}
	}

	@Override
	protected void executeReport(final Locale locale) throws MavenReportException {
		loadExecutionData();
		try {
			final IReportVisitor visitor = createVisitor(locale);
			visitor.visitInfo(sessionInfoStore.getInfos(),
					executionDataStore.getContents());
			createReport(visitor);
			visitor.visitEnd();
		} catch (final IOException e) {
			throw new MavenReportException("Error while creating report: " +
					e.getMessage(), e);
		}
	}

	protected void loadExecutionData() throws MavenReportException {
		final ExecFileLoader loader = new ExecFileLoader();
		try {
			loader.load(getDataFile());
		} catch (final IOException e) {
			throw new MavenReportException("Unable to read execution data file " +
					getDataFile() + ": " + e.getMessage(), e);
		}
		sessionInfoStore = loader.getSessionInfoStore();
		executionDataStore = loader.getExecutionDataStore();
	}

	protected void createReport(final IReportGroupVisitor visitor) throws IOException {
		final FileFilter fileFilter = new FileFilter(this.getIncludes(),
				this.getExcludes());
		final BundleCreator creator = new BundleCreator(this.getProject(),
				fileFilter);
		final IBundleCoverage bundle = creator.createBundle(executionDataStore);
		final SourceFileCollection locator = new SourceFileCollection(
				getCompileSourceRoots(), sourceEncoding);
		checkForMissingDebugInformation(bundle);
		visitor.visitBundle(bundle, locator);
	}

	protected void checkForMissingDebugInformation(final ICoverageNode node) {
		if (node.getClassCounter().getTotalCount() > 0
				&& node.getLineCounter().getTotalCount() == 0) {
			getLog().warn("To enable source code annotation class files have to be compiled with debug information.");
		}
	}

	protected IReportVisitor createVisitor(final Locale locale) throws IOException {
		final List<IReportVisitor> visitors = new ArrayList<IReportVisitor>();
		getOutputDirectoryFile().mkdirs();
		final XMLFormatter xmlFormatter = new XMLFormatter();
		xmlFormatter.setOutputEncoding(outputEncoding);
		visitors.add(xmlFormatter.createVisitor(new FileOutputStream(
				new File(getOutputDirectoryFile(), "jacoco.xml"))));
		final CSVFormatter csvFormatter = new CSVFormatter();
		csvFormatter.setOutputEncoding(outputEncoding);
		visitors.add(csvFormatter.createVisitor(new FileOutputStream(
				new File(getOutputDirectoryFile(), "jacoco.csv"))));
		final HTMLFormatter htmlFormatter = new HTMLFormatter();
		htmlFormatter.setOutputEncoding(outputEncoding);
		htmlFormatter.setLocale(locale);
		visitors.add(htmlFormatter.createVisitor(
				new FileMultiReportOutput(getOutputDirectoryFile())));
		return new MultiReportVisitor(visitors);
	}

	protected File resolvePath(final String path) {
		File file = new File(path);
		if (!file.isAbsolute()) {
			file = new File(getProject().getBasedir(), path);
		}
		return file;
	}

	protected List<File> getCompileSourceRoots() {
		final List<File> result = new ArrayList<File>();
		for (final Object path : getProject().getCompileSourceRoots()) {
			result.add(resolvePath((String) path));
		}
		return result;
	}

	private static class SourceFileCollection implements ISourceFileLocator {

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

	abstract protected File getDataFile();

	abstract protected File getOutputDirectoryFile();

}
