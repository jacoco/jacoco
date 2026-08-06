/*******************************************************************************
 * Copyright (c) 2009, 2026 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    John Keeping - initial implementation
 *    Marc R. Hoffmann - rework
 *
 *******************************************************************************/
package org.jacoco.cli.internal.commands;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jacoco.cli.internal.Command;
import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.IBundleCoverage;
import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.tools.ExecFileLoader;
import org.jacoco.report.DirectorySourceFileLocator;
import org.jacoco.report.FileMultiReportOutput;
import org.jacoco.report.IReportVisitor;
import org.jacoco.report.ISourceFileLocator;
import org.jacoco.report.MultiReportVisitor;
import org.jacoco.report.MultiSourceFileLocator;
import org.jacoco.report.csv.CSVFormatter;
import org.jacoco.report.html.HTMLFormatter;
import org.jacoco.report.xml.XMLFormatter;

import picocli.CommandLine;

/**
 * The <code>report</code> command.
 */
@CommandLine.Command(name = "report", description = "Generate reports in different formats by reading exec and Java class files.")
public class Report extends Command {

	@CommandLine.Parameters(description = "list of JaCoCo *.exec files to read", paramLabel = "<execfiles>")
	List<File> execfiles = new ArrayList<File>();

	@CommandLine.Option(names = "--classfiles", description = "location of Java class files", paramLabel = "<path>", required = true)
	List<File> classfiles = new ArrayList<File>();

	@CommandLine.Option(names = "--sourcefiles", description = "location of the source files", paramLabel = "<path>")
	List<File> sourcefiles = new ArrayList<File>();

	@CommandLine.Option(names = "--tabwith", description = "tab stop width for the source pages (default 4)", paramLabel = "<n>")
	int tabwidth = 4;

	@CommandLine.Option(names = "--name", description = "name used for this report", paramLabel = "<name>")
	String name = "JaCoCo Coverage Report";

	@CommandLine.Option(names = "--encoding", description = "source file encoding (by default platform encoding is used)", paramLabel = "<charset>")
	String encoding;

	@CommandLine.Option(names = "--xml", description = "output file for the XML report", paramLabel = "<file>")
	File xml;

	@CommandLine.Option(names = "--csv", description = "output file for the CSV report", paramLabel = "<file>")
	File csv;

	@CommandLine.Option(names = "--html", description = "output directory for the HTML report", paramLabel = "<dir>")
	File html;

	@Override
	public int execute(final PrintWriter out, final PrintWriter err)
			throws IOException {
		final ExecFileLoader loader = loadExecutionData(out);
		final IBundleCoverage bundle = analyze(loader.getExecutionDataStore(),
				out);
		writeReports(bundle, loader, out);
		return 0;
	}

	private ExecFileLoader loadExecutionData(final PrintWriter out)
			throws IOException {
		final ExecFileLoader loader = new ExecFileLoader();
		if (execfiles.isEmpty()) {
			out.println("[WARN] No execution data files provided.");
		} else {
			for (final File file : execfiles) {
				out.printf("[INFO] Loading execution data file %s.%n",
						file.getAbsolutePath());
				loader.load(file);
			}
		}
		return loader;
	}

	private IBundleCoverage analyze(final ExecutionDataStore data,
			final PrintWriter out) throws IOException {
		final CoverageBuilder builder = new CoverageBuilder();
		final Analyzer analyzer = new Analyzer(data, builder);
		for (final File f : classfiles) {
			analyzer.analyzeAll(f);
		}
		printNoMatchWarning(builder.getNoMatchClasses(), out);
		return builder.getBundle(name);
	}

	private void printNoMatchWarning(final Collection<IClassCoverage> nomatch,
			final PrintWriter out) {
		if (!nomatch.isEmpty()) {
			out.println(
					"[WARN] Some classes do not match with execution data.");
			out.println(
					"[WARN] For report generation the same class files must be used as at runtime.");
			for (final IClassCoverage c : nomatch) {
				out.printf(
						"[WARN] Execution data for class %s does not match.%n",
						c.getName());
			}
		}
	}

	private void writeReports(final IBundleCoverage bundle,
			final ExecFileLoader loader, final PrintWriter out)
			throws IOException {
		out.printf("[INFO] Analyzing %s classes.%n",
				Integer.valueOf(bundle.getClassCounter().getTotalCount()));
		final IReportVisitor visitor = createReportVisitor();
		visitor.visitInfo(loader.getSessionInfoStore().getInfos(),
				loader.getExecutionDataStore().getContents());
		visitor.visitBundle(bundle, getSourceLocator());
		visitor.visitEnd();
	}

	private IReportVisitor createReportVisitor() throws IOException {
		final List<IReportVisitor> visitors = new ArrayList<IReportVisitor>();

		if (xml != null) {
			final XMLFormatter formatter = new XMLFormatter();
			visitors.add(formatter.createVisitor(new FileOutputStream(xml)));
		}

		if (csv != null) {
			final CSVFormatter formatter = new CSVFormatter();
			visitors.add(formatter.createVisitor(new FileOutputStream(csv)));
		}

		if (html != null) {
			final HTMLFormatter formatter = new HTMLFormatter();
			visitors.add(
					formatter.createVisitor(new FileMultiReportOutput(html)));
		}

		return new MultiReportVisitor(visitors);
	}

	private ISourceFileLocator getSourceLocator() {
		final MultiSourceFileLocator multi = new MultiSourceFileLocator(
				tabwidth);
		for (final File f : sourcefiles) {
			multi.add(new DirectorySourceFileLocator(f, encoding, tabwidth));
		}
		return multi;
	}

}
