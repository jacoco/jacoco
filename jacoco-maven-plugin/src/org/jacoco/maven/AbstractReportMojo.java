/*******************************************************************************
 * Copyright (c) 2009, 2016 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *    Cristiano Costantini - maven plugin reporting options
 *
 *******************************************************************************/
package org.jacoco.maven;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.doxia.siterenderer.Renderer;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.reporting.AbstractMavenReport;
import org.apache.maven.reporting.MavenReportException;
import org.jacoco.core.analysis.IBundleCoverage;
import org.jacoco.core.analysis.ICoverageNode;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.data.SessionInfoStore;
import org.jacoco.core.tools.ExecFileLoader;
import org.jacoco.report.FileMultiReportOutput;
import org.jacoco.report.IReportGroupVisitor;
import org.jacoco.report.IReportVisitor;
import org.jacoco.report.ISourceFileLocator;
import org.jacoco.report.MultiReportVisitor;
import org.jacoco.report.csv.CSVFormatter;
import org.jacoco.report.html.HTMLFormatter;
import org.jacoco.report.xml.XMLFormatter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
	 * A list of artifacts, selected from the project runtime dependencies, that
	 * will be included into the report. The projects are specified with
	 * [groupId]:artifactId.
	 * 
	 * @parameter
	 */
	List<String> includeArtifacts;
	/**
	 * Flag used to include test sources in report.
	 * 
	 * @parameter property="jacoco.tests.include" default-value="false"
	 */
	boolean includeTests;

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
	 * Flag used enable generation of CSV report
	 * 
	 * @parameter property="jacoco.report.csv" default-value="true"
	 */
	boolean csvReport;
	/**
	 * Flag used enable generation of XML report
	 * 
	 * @parameter property="jacoco.report.xml" default-value="true"
	 */
	boolean xmlReport;
	/**
	 * Flag used enable generation of HTML report
	 * 
	 * @parameter property="jacoco.report.html" default-value="true"
	 */
	boolean htmlReport;

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
	SessionInfoStore sessionInfoStore;
	ExecutionDataStore executionDataStore;

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
	public abstract void setReportOutputDirectory(
			final File reportOutputDirectory);

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
		loadExecutionData();
		try {
			final IReportVisitor visitor = createVisitor(locale);
			visitor.visitInfo(sessionInfoStore.getInfos(),
					executionDataStore.getContents());
			createReport(visitor);
			visitor.visitEnd();
		} catch (final IOException e) {
			throw new MavenReportException("Error while creating report: "
					+ e.getMessage(), e);
		}
	}

	void loadExecutionData() throws MavenReportException {
		final ExecFileLoader loader = new ExecFileLoader();
		try {
			loader.load(getDataFile());
		} catch (final IOException e) {
			throw new MavenReportException(
					"Unable to read execution data file " + getDataFile()
							+ ": " + e.getMessage(),
					e);
		}
		sessionInfoStore = loader.getSessionInfoStore();
		executionDataStore = loader.getExecutionDataStore();
	}

	void createReport(final IReportGroupVisitor visitor) throws IOException {

		if (includeTests
				|| (includeArtifacts != null && includeArtifacts.size() > 0)) {
			final IReportGroupVisitor groupVisitor = visitor
					.visitGroup(getProject().getName());

			createProjectReport(groupVisitor,
					getProject().getName() + " - Main",
					getProject().getBuild().getOutputDirectory());

			if (includeTests) {
				createProjectReport(groupVisitor,
						getProject().getName() + " - Tests",
						getProject().getBuild().getTestOutputDirectory());
			}

			if (includeArtifacts != null && includeArtifacts.size() > 0) {
				final Map<String, Artifact> artifactsMap = createArtifactsMap();
				for (final String includeArtifact : includeArtifacts) {
					createDependencyReport(groupVisitor,
							artifactsMap.get(includeArtifact));
				}
			}
		} else {
			createProjectReport(visitor, getProject().getName(),
					getProject().getBuild().getOutputDirectory());
		}
	}

	private Map<String, Artifact> createArtifactsMap() {
		final Map<String, Artifact> artifactsMap = new HashMap<String, Artifact>();
		for (final Object obj : getProject().getArtifacts()) {
			if (obj instanceof Artifact) {
				final Artifact artifact = (Artifact) obj;
				artifactsMap.put(
						artifact.getGroupId() + ":" + artifact.getArtifactId(),
						artifact);
				if (!artifactsMap.containsKey(":" + artifact.getArtifactId())) {
					artifactsMap.put(":" + artifact.getArtifactId(),
							artifact);
				}
			}
		}
		return artifactsMap;
	}

	private void createProjectReport(final IReportGroupVisitor visitor,
			final String reportName, final String directory)
					throws IOException {
		final FileFilter fileFilter = new FileFilter(this.getIncludes(),
				this.getExcludes());
		final BundleCreator creator = new BundleCreator(fileFilter,
				getLog(), reportName);

		final IBundleCoverage bundle = creator
				.createBundleOfDirectory(executionDataStore, directory);

		final SourceFileCollection locator = new SourceFileCollection(
				getCompileSourceRoots(), sourceEncoding);
		checkForMissingDebugInformation(bundle);
		visitor.visitBundle(bundle, locator);
	}

	private void createDependencyReport(final IReportGroupVisitor visitor,
			final Artifact artifact)
					throws IOException {

		final String artifactCoordinates = artifact.getGroupId() + ":"
				+ artifact.getArtifactId() + ":" + artifact.getVersion();
		final ArtifactBundleCreator creator = new ArtifactBundleCreator(
				getLog(),
				artifactCoordinates);
		final IBundleCoverage bundle = creator.createBundleOfArtifact(
				executionDataStore, artifact);

		final ISourceFileLocator locator = new MavenSourcesArtifactLocator(
				artifact, sourceEncoding);
		checkForMissingDebugInformation(bundle);
		visitor.visitBundle(bundle, locator);
	}

	void checkForMissingDebugInformation(final ICoverageNode node) {
		if (node.getClassCounter().getTotalCount() > 0
				&& node.getLineCounter().getTotalCount() == 0) {
			getLog().warn(
					"To enable source code annotation class files have to be compiled with debug information.");
		}
	}

	IReportVisitor createVisitor(final Locale locale) throws IOException {
		final List<IReportVisitor> visitors = new ArrayList<IReportVisitor>();
		getOutputDirectoryFile().mkdirs();

		if (xmlReport) {
			final XMLFormatter xmlFormatter = new XMLFormatter();
			xmlFormatter.setOutputEncoding(outputEncoding);
			visitors.add(
					xmlFormatter.createVisitor(new FileOutputStream(new File(
							getOutputDirectoryFile(), "jacoco.xml"))));
		}
		if (csvReport) {
			final CSVFormatter csvFormatter = new CSVFormatter();
			csvFormatter.setOutputEncoding(outputEncoding);
			visitors.add(
					csvFormatter.createVisitor(new FileOutputStream(new File(
							getOutputDirectoryFile(), "jacoco.csv"))));
		}
		if (htmlReport) {
			final HTMLFormatter htmlFormatter = new HTMLFormatter();
			htmlFormatter.setOutputEncoding(outputEncoding);
			htmlFormatter.setLocale(locale);
			visitors.add(htmlFormatter.createVisitor(new FileMultiReportOutput(
					getOutputDirectoryFile())));
		}
		return new MultiReportVisitor(visitors);
	}

	File resolvePath(final String path) {
		File file = new File(path);
		if (!file.isAbsolute()) {
			file = new File(getProject().getBasedir(), path);
		}
		return file;
	}

	List<File> getCompileSourceRoots() {
		final List<File> result = new ArrayList<File>();
		for (final Object path : getProject().getCompileSourceRoots()) {
			result.add(resolvePath((String) path));
		}
		for (final Object path : getProject().getTestCompileSourceRoots()) {
			result.add(resolvePath((String) path));
		}
		return result;
	}

	List<File> getTestCompileSourceRoots() {
		final List<File> result = new ArrayList<File>();
		for (final Object path : getProject().getTestCompileSourceRoots()) {
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

	abstract File getDataFile();

	abstract File getOutputDirectoryFile();

}
