/*******************************************************************************
 * Copyright (c) 2009, 2023 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *    Kyle Lieber - implementation of CheckMojo
 *
 *******************************************************************************/
package org.jacoco.maven;

import static java.lang.String.format;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.IBundleCoverage;
import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.tools.ExecFileLoader;
import org.jacoco.report.IReportGroupVisitor;
import org.jacoco.report.IReportVisitor;
import org.jacoco.report.ISourceFileLocator;
import org.jacoco.report.MultiReportVisitor;
import org.jacoco.report.check.IViolationsOutput;
import org.jacoco.report.check.Rule;
import org.jacoco.report.check.RulesChecker;

/**
 * Encapsulates the tasks to create reports for Maven projects. Instances are
 * supposed to be used in the following sequence:
 *
 * <ol>
 * <li>Create an instance</li>
 * <li>Load one or multiple exec files with
 * <code>loadExecutionData()</code></li>
 * <li>Add one or multiple formatters with <code>addXXX()</code> methods</li>
 * <li>Create the root visitor with <code>initRootVisitor()</code></li>
 * <li>Process one or multiple projects with <code>processProject()</code></li>
 * </ol>
 */
final class ReportSupport {

	private final Log log;
	private final ExecFileLoader loader;
	private final List<IReportVisitor> formatters;

	/**
	 * Construct a new instance with the given log output.
	 *
	 * @param log
	 *            for log output
	 */
	public ReportSupport(final Log log) {
		this.log = log;
		this.loader = new ExecFileLoader();
		this.formatters = new ArrayList<IReportVisitor>();
	}

	/**
	 * Loads the given execution data file.
	 *
	 * @param execFile
	 *            execution data file to load
	 * @throws IOException
	 *             if the file can't be loaded
	 */
	public void loadExecutionData(final File execFile) throws IOException {
		log.info("Loading execution data file " + execFile);
		loader.load(execFile);
	}

	public void addVisitor(final IReportVisitor visitor) {
		formatters.add(visitor);
	}

	public void addRulesChecker(final List<Rule> rules,
			final IViolationsOutput output) {
		final RulesChecker checker = new RulesChecker();
		checker.setRules(rules);
		formatters.add(checker.createVisitor(output));
	}

	public IReportVisitor initRootVisitor() throws IOException {
		final IReportVisitor visitor = new MultiReportVisitor(formatters);
		visitor.visitInfo(loader.getSessionInfoStore().getInfos(),
				loader.getExecutionDataStore().getContents());
		return visitor;
	}

	/**
	 * Calculates coverage for the given project and emits it to the report
	 * group without source references
	 *
	 * @param visitor
	 *            group visitor to emit the project's coverage to
	 * @param project
	 *            the MavenProject
	 * @param includes
	 *            list of includes patterns
	 * @param excludes
	 *            list of excludes patterns
	 * @throws IOException
	 *             if class files can't be read
	 */
	public void processProject(final IReportGroupVisitor visitor,
			final MavenProject project, final List<String> includes,
			final List<String> excludes) throws IOException {
		processProject(visitor, project.getArtifactId(), project, includes,
				excludes, new NoSourceLocator());
	}

	/**
	 * Calculates coverage for the given project and emits it to the report
	 * group including source references
	 *
	 * @param visitor
	 *            group visitor to emit the project's coverage to
	 * @param bundleName
	 *            name for this project in the report
	 * @param project
	 *            the MavenProject
	 * @param includes
	 *            list of includes patterns
	 * @param excludes
	 *            list of excludes patterns
	 * @param srcEncoding
	 *            encoding of the source files within this project
	 * @throws IOException
	 *             if class files can't be read
	 */
	public void processProject(final IReportGroupVisitor visitor,
			final String bundleName, final MavenProject project,
			final List<String> includes, final List<String> excludes,
			final String srcEncoding) throws IOException {
		processProject(visitor, bundleName, project, includes, excludes,
				new SourceFileCollection(project, srcEncoding));
	}

	private void processProject(final IReportGroupVisitor visitor,
			final String bundleName, final MavenProject project,
			final List<String> includes, final List<String> excludes,
			final ISourceFileLocator locator) throws IOException {
		final CoverageBuilder builder = new CoverageBuilder();
		final File classesDir = new File(
				project.getBuild().getOutputDirectory());

		if (classesDir.isDirectory()) {
			final Analyzer analyzer = new Analyzer(
					loader.getExecutionDataStore(), builder);
			final FileFilter filter = new FileFilter(includes, excludes);
			for (final File file : filter.getFiles(classesDir)) {
				analyzer.analyzeAll(file);
			}
		}

		final IBundleCoverage bundle = builder.getBundle(bundleName);
		logBundleInfo(bundle, builder.getNoMatchClasses());

		visitor.visitBundle(bundle, locator);
	}

	private void logBundleInfo(final IBundleCoverage bundle,
			final Collection<IClassCoverage> nomatch) {
		log.info(format("Analyzed bundle '%s' with %s classes",
				bundle.getName(),
				Integer.valueOf(bundle.getClassCounter().getTotalCount())));
		if (!nomatch.isEmpty()) {
			log.warn(format(
					"Classes in bundle '%s' do not match with execution data. "
							+ "For report generation the same class files must be used as at runtime.",
					bundle.getName()));
			for (final IClassCoverage c : nomatch) {
				log.warn(format("Execution data for class %s does not match.",
						c.getName()));
			}
		}
		if (bundle.containsCode()
				&& bundle.getLineCounter().getTotalCount() == 0) {
			log.warn(
					"To enable source code annotation class files have to be compiled with debug information.");
		}
	}

	private class NoSourceLocator implements ISourceFileLocator {

		public Reader getSourceFile(final String packageName,
				final String fileName) {
			return null;
		}

		public int getTabWidth() {
			return 0;
		}
	}

	private class SourceFileCollection implements ISourceFileLocator {

		private final List<File> sourceRoots;
		private final String encoding;

		public SourceFileCollection(final MavenProject project,
				final String encoding) {
			this.sourceRoots = getCompileSourceRoots(project);
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

	private static List<File> getCompileSourceRoots(
			final MavenProject project) {
		final List<File> result = new ArrayList<File>();
		for (final Object path : project.getCompileSourceRoots()) {
			result.add(resolvePath(project, (String) path));
		}
		return result;
	}

	private static File resolvePath(final MavenProject project,
			final String path) {
		File file = new File(path);
		if (!file.isAbsolute()) {
			file = new File(project.getBasedir(), path);
		}
		return file;
	}

}
