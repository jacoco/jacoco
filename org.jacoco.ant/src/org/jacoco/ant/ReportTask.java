/*******************************************************************************
 * Copyright (c) 2009, 2013 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    Martin Hare Robertson - check coverage
 *    
 *******************************************************************************/
package org.jacoco.ant;

import static java.lang.String.format;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.types.resources.FileResource;
import org.apache.tools.ant.types.resources.Union;
import org.apache.tools.ant.util.FileUtils;
import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.IBundleCoverage;
import org.jacoco.core.analysis.ICoverageNode;
import org.jacoco.core.data.ExecFileLoader;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.data.SessionInfoStore;
import org.jacoco.report.FileMultiReportOutput;
import org.jacoco.report.IMultiReportOutput;
import org.jacoco.report.IReportGroupVisitor;
import org.jacoco.report.IReportVisitor;
import org.jacoco.report.MultiReportVisitor;
import org.jacoco.report.ZipMultiReportOutput;
import org.jacoco.report.csv.CSVFormatter;
import org.jacoco.report.html.HTMLFormatter;
import org.jacoco.report.xml.XMLFormatter;

/**
 * Task for coverage report generation. Experimental implementation that needs
 * refinement.
 */
public class ReportTask extends Task {

	/**
	 * The source files are specified in a resource collection with additional
	 * attributes.
	 */
	public static class SourceFilesElement extends Union {

		String encoding = null;

		int tabWidth = 4;

		/**
		 * Defines the optional source file encoding. If not set the platform
		 * default is used.
		 * 
		 * @param encoding
		 *            source file encoding
		 */
		public void setEncoding(final String encoding) {
			this.encoding = encoding;
		}

		/**
		 * Sets the tab stop width for the source pages. Default value is 4.
		 * 
		 * @param tabWidth
		 *            number of characters per tab stop
		 */
		public void setTabwidth(final int tabWidth) {
			if (tabWidth <= 0) {
				throw new BuildException("Tab width must be greater than 0");
			}
			this.tabWidth = tabWidth;
		}

	}

	/**
	 * Container element for class file groups.
	 */
	public static class GroupElement {

		private final List<GroupElement> children = new ArrayList<GroupElement>();

		private final Union classfiles = new Union();

		private final SourceFilesElement sourcefiles = new SourceFilesElement();

		private String name;

		/**
		 * Sets the name of the group.
		 * 
		 * @param name
		 *            name of the group
		 */
		public void setName(final String name) {
			this.name = name;
		}

		/**
		 * Creates a new child group.
		 * 
		 * @return new child group
		 */
		public GroupElement createGroup() {
			final GroupElement group = new GroupElement();
			children.add(group);
			return group;
		}

		/**
		 * Returns the nested resource collection for class files.
		 * 
		 * @return resource collection for class files
		 */
		public Union createClassfiles() {
			return classfiles;
		}

		/**
		 * Returns the nested resource collection for source files.
		 * 
		 * @return resource collection for source files
		 */
		public SourceFilesElement createSourcefiles() {
			return sourcefiles;
		}

	}

	/**
	 * Interface for child elements that define formatters.
	 */
	private interface IFormatterElement {

		IReportVisitor createVisitor() throws IOException;

	}

	/**
	 * Formatter Element for HTML reports.
	 */
	public class HTMLFormatterElement implements IFormatterElement {

		private File destdir;

		private File destfile;

		private String footer = "";

		private String encoding = "UTF-8";

		private Locale locale = Locale.getDefault();

		/**
		 * Sets the output directory for the report.
		 * 
		 * @param destdir
		 *            output directory
		 */
		public void setDestdir(final File destdir) {
			this.destdir = destdir;
		}

		/**
		 * Sets the Zip output file for the report.
		 * 
		 * @param destfile
		 *            Zip output file
		 */
		public void setDestfile(final File destfile) {
			this.destfile = destfile;
		}

		/**
		 * Sets an optional footer text that will be displayed on every report
		 * page.
		 * 
		 * @param text
		 *            footer text
		 */
		public void setFooter(final String text) {
			this.footer = text;
		}

		/**
		 * Sets the output encoding for generated HTML files. Default is UTF-8.
		 * 
		 * @param encoding
		 *            output encoding
		 */
		public void setEncoding(final String encoding) {
			this.encoding = encoding;
		}

		/**
		 * Sets the locale for generated text output. By default the platform
		 * locale is used.
		 * 
		 * @param locale
		 *            text locale
		 */
		public void setLocale(final Locale locale) {
			this.locale = locale;
		}

		public IReportVisitor createVisitor() throws IOException {
			final IMultiReportOutput output;
			if (destfile != null) {
				if (destdir != null) {
					throw new BuildException(
							"Either destination directory or file must be supplied, not both",
							getLocation());
				}
				final FileOutputStream stream = new FileOutputStream(destfile);
				output = new ZipMultiReportOutput(stream);

			} else {
				if (destdir == null) {
					throw new BuildException(
							"Destination directory or file must be supplied for html report",
							getLocation());
				}
				output = new FileMultiReportOutput(destdir);
			}
			final HTMLFormatter formatter = new HTMLFormatter();
			formatter.setFooterText(footer);
			formatter.setOutputEncoding(encoding);
			formatter.setLocale(locale);
			return formatter.createVisitor(output);
		}

	}

	/**
	 * Formatter Element for CSV reports.
	 */
	public class CSVFormatterElement implements IFormatterElement {

		private File destfile;

		private String encoding = "UTF-8";

		/**
		 * Sets the output file for the report.
		 * 
		 * @param destfile
		 *            output file
		 */
		public void setDestfile(final File destfile) {
			this.destfile = destfile;
		}

		public IReportVisitor createVisitor() throws IOException {
			if (destfile == null) {
				throw new BuildException(
						"Destination file must be supplied for csv report",
						getLocation());
			}
			final CSVFormatter formatter = new CSVFormatter();
			formatter.setOutputEncoding(encoding);
			return formatter.createVisitor(new FileOutputStream(destfile));
		}

		/**
		 * Sets the output encoding for generated XML file. Default is UTF-8.
		 * 
		 * @param encoding
		 *            output encoding
		 */
		public void setEncoding(final String encoding) {
			this.encoding = encoding;
		}

	}

	/**
	 * Formatter Element for XML reports.
	 */
	public class XMLFormatterElement implements IFormatterElement {

		private File destfile;

		private String encoding = "UTF-8";

		/**
		 * Sets the output file for the report.
		 * 
		 * @param destfile
		 *            output file
		 */
		public void setDestfile(final File destfile) {
			this.destfile = destfile;
		}

		/**
		 * Sets the output encoding for generated XML file. Default is UTF-8.
		 * 
		 * @param encoding
		 *            output encoding
		 */
		public void setEncoding(final String encoding) {
			this.encoding = encoding;
		}

		public IReportVisitor createVisitor() throws IOException {
			if (destfile == null) {
				throw new BuildException(
						"Destination file must be supplied for xml report",
						getLocation());
			}
			final XMLFormatter formatter = new XMLFormatter();
			formatter.setOutputEncoding(encoding);
			return formatter.createVisitor(new FileOutputStream(destfile));
		}

	}

	/**
	 * Formatter Element for HTML reports.
	 */
	public class CheckCoverageElement {

		private static final double NO_TARGET = -1;

		private double instructions = NO_TARGET;
		private double branches = NO_TARGET;
		private double lines = NO_TARGET;

		private int insCovered = 0;
		private int insTotal = 0;

		private int branchCovered = 0;
		private int branchTotal = 0;

		private int lineCovered = 0;
		private int lineTotal = 0;

		/**
		 * Sets the percentage of instructions required
		 * 
		 * @param instructions
		 */
		public void setInstructions(final double instructions) {
			this.instructions = instructions;
		}

		/**
		 * Sets the percentage of branches required
		 * 
		 * @param branches
		 */
		public void setBranches(final double branches) {
			this.branches = branches;
		}

		/**
		 * Sets the percentage of lines required
		 * 
		 * @param lines
		 */
		public void setLines(final double lines) {
			this.lines = lines;
		}

		/**
		 * Add coverage data from this bundle
		 * 
		 * @param bundle
		 */
		public void visitBundle(final IBundleCoverage bundle) {
			insCovered += bundle.getInstructionCounter().getCoveredCount();
			insTotal += bundle.getInstructionCounter().getTotalCount();

			branchCovered += bundle.getBranchCounter().getCoveredCount();
			branchTotal += bundle.getBranchCounter().getTotalCount();

			lineCovered += bundle.getLineCounter().getCoveredCount();
			lineTotal += bundle.getLineCounter().getTotalCount();
		}

		/**
		 * Check that required coverage has been reached
		 */
		@SuppressWarnings("boxing")
		public void visitEnd() {
			String error = "";
			int failCount = 0;
			int passCount = 0;

			final int[] covered = new int[] { insCovered, branchCovered,
					lineCovered };
			final int[] total = new int[] { insTotal, branchTotal, lineTotal };
			final double[] target = new double[] { instructions, branches,
					lines };
			final String[] description = new String[] { "Instructions",
					"Branches", "Lines" };

			for (int ii = 0; ii < 3; ii++) {
				if (target[ii] != NO_TARGET) {
					final double actual;
					if (total[ii] > 0) {
						final double fraction = ((double) covered[ii] / (double) total[ii]);
						actual = (int) (fraction / 100.0);
					} else {
						actual = 100.0;
					}

					if (actual < target[ii]) {
						error += String
								.format(description[ii]
										+ " coverage requirement not met: %.2f < %.2f\n",
										actual, target[ii]);
						failCount++;
					} else {
						System.out.println(description[ii]
								+ " coverage requirement met.");
						passCount++;
					}
				}
			}

			if ((failCount == 0) && (passCount == 0)) {
				throw new BuildException("No coverage target set");
			} else if (failCount > 0) {
				System.out.println(error);
				throw new BuildException(error);
			}
		}
	}

	private final Union executiondataElement = new Union();

	private SessionInfoStore sessionInfoStore;

	private ExecutionDataStore executionDataStore;

	private final GroupElement structure = new GroupElement();

	private final List<IFormatterElement> formatters = new ArrayList<IFormatterElement>();

	private CheckCoverageElement checkcoverageElement;

	/**
	 * Returns the nested resource collection for execution data files.
	 * 
	 * @return resource collection for execution files
	 */
	public Union createExecutiondata() {
		return executiondataElement;
	}

	/**
	 * Returns the root group element that defines the report structure.
	 * 
	 * @return root group element
	 */
	public GroupElement createStructure() {
		return structure;
	}

	/**
	 * Creates a new HTML report element.
	 * 
	 * @return HTML report element
	 */
	public HTMLFormatterElement createHtml() {
		final HTMLFormatterElement element = new HTMLFormatterElement();
		formatters.add(element);
		return element;
	}

	/**
	 * Creates a new CSV report element.
	 * 
	 * @return CSV report element
	 */
	public CSVFormatterElement createCsv() {
		final CSVFormatterElement element = new CSVFormatterElement();
		formatters.add(element);
		return element;
	}

	/**
	 * Creates a new XML report element.
	 * 
	 * @return CSV report element
	 */
	public XMLFormatterElement createXml() {
		final XMLFormatterElement element = new XMLFormatterElement();
		formatters.add(element);
		return element;
	}

	/**
	 * Creates a new Check Coverage report element.
	 * 
	 * @return Check Coverage report element
	 */
	public CheckCoverageElement createCheckcoverage() {
		checkcoverageElement = new CheckCoverageElement();
		return checkcoverageElement;
	}

	@Override
	public void execute() throws BuildException {
		loadExecutionData();
		try {
			final IReportVisitor visitor = createVisitor();
			visitor.visitInfo(sessionInfoStore.getInfos(),
					executionDataStore.getContents());
			createReport(visitor, structure);
			visitor.visitEnd();
		} catch (final IOException e) {
			throw new BuildException("Error while creating report", e,
					getLocation());
		}
		if (checkcoverageElement != null) {
			checkcoverageElement.visitEnd();
		}
	}

	private void loadExecutionData() {
		final ExecFileLoader loader = new ExecFileLoader();
		for (final Iterator<?> i = executiondataElement.iterator(); i.hasNext();) {
			final Resource resource = (Resource) i.next();
			log(format("Loading execution data file %s", resource));
			InputStream in = null;
			try {
				in = resource.getInputStream();
				loader.load(in);
			} catch (final IOException e) {
				throw new BuildException(format(
						"Unable to read execution data file %s", resource), e,
						getLocation());
			} finally {
				FileUtils.close(in);
			}
		}
		sessionInfoStore = loader.getSessionInfoStore();
		executionDataStore = loader.getExecutionDataStore();
	}

	private IReportVisitor createVisitor() throws IOException {
		final List<IReportVisitor> visitors = new ArrayList<IReportVisitor>();
		for (final IFormatterElement f : formatters) {
			visitors.add(f.createVisitor());
		}
		return new MultiReportVisitor(visitors);
	}

	private void createReport(final IReportGroupVisitor visitor,
			final GroupElement group) throws IOException {
		if (group.name == null) {
			throw new BuildException("Group name must be supplied",
					getLocation());
		}
		if (group.children.size() > 0) {
			final IReportGroupVisitor groupVisitor = visitor
					.visitGroup(group.name);
			for (final GroupElement child : group.children) {
				createReport(groupVisitor, child);
			}
		} else {
			final IBundleCoverage bundle = createBundle(group);
			log(format("Writing group \"%s\" with %s classes",
					bundle.getName(),
					Integer.valueOf(bundle.getClassCounter().getTotalCount())));
			final SourceFilesElement sourcefiles = group.sourcefiles;
			final AntResourcesLocator locator = new AntResourcesLocator(
					sourcefiles.encoding, sourcefiles.tabWidth);
			locator.addAll(sourcefiles.iterator());
			if (!locator.isEmpty()) {
				checkForMissingDebugInformation(bundle);
			}
			visitor.visitBundle(bundle, locator);

			if (checkcoverageElement != null) {
				checkcoverageElement.visitBundle(bundle);
			}
		}
	}

	private IBundleCoverage createBundle(final GroupElement group)
			throws IOException {
		final CoverageBuilder builder = new CoverageBuilder();
		final Analyzer analyzer = new Analyzer(executionDataStore, builder);
		for (final Iterator<?> i = group.classfiles.iterator(); i.hasNext();) {
			final Resource resource = (Resource) i.next();
			if (resource.isDirectory() && resource instanceof FileResource) {
				analyzer.analyzeAll(((FileResource) resource).getFile());
			} else {
				final InputStream in = resource.getInputStream();
				analyzer.analyzeAll(in);
				in.close();
			}
		}
		return builder.getBundle(group.name);
	}

	private void checkForMissingDebugInformation(final ICoverageNode node) {
		if (node.getClassCounter().getTotalCount() > 0
				&& node.getLineCounter().getTotalCount() == 0) {
			log(format(
					"To enable source code annotation class files for bundle '%s' have to be compiled with debug information",
					node.getName()), Project.MSG_WARN);
		}
	}

}
