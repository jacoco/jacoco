/*******************************************************************************
 * Copyright (c) 2009, 2013 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Keeping - initial implementation
 *
 *******************************************************************************/
package org.jacoco.cli;

import java.io.File;

import org.kohsuke.args4j.Option;

/**
 * Options for the "report" command.
 */
public class ReportOptions {

	@Option(name = "-input", required = true, usage = "the JaCoCo execution data")
	private File input;

	@Option(name = "-classes", required = true, usage = "directory containing the classes which were instrumented - NOTE: this must point at the original, non-instrumented class files")
	private File classes;

	@Option(name = "-source", required = true, usage = "directory containing the source .java files")
	private File source;

	@Option(name = "-title", required = true, usage = "title for the report")
	private String title;

	@Option(name = "-csv", usage = "file for generating a CSV report")
	private File csv;

	@Option(name = "-html", usage = "directory for generating an HTML report")
	private File html;

	@Option(name = "-xml", usage = "file for generating an XML report")
	private File xml;

	@Option(name = "-source-encoding", usage = "encoding of the source files (default: utf-8)")
	private String sourceEncoding = "utf-8";

	@Option(name = "-tabwidth", usage = "width of a TAB in the source files (default: 4)")
	private int tabWidth = 4;

	/**
	 * Gets the input JaCoCo execution data file.
	 * 
	 * @return the input execution data file
	 */
	public File getInput() {
		return input;
	}

	/**
	 * Gets the path to the original class files.
	 * 
	 * @return the path to the class files
	 */
	public File getClasses() {
		return classes;
	}

	/**
	 * Gets the path to the Java source.
	 * 
	 * @return the path to the source directory
	 */
	public File getSource() {
		return source;
	}

	/**
	 * Gets the title for the coverage report.
	 * 
	 * @return the report title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * Gets the path for the CSV format report, if there is one.
	 * 
	 * @return the path for the CSV report, or {@code null} to omit this format
	 */
	public File getCsv() {
		return csv;
	}

	/**
	 * Gets the path for the HTML format report, if there is one.
	 * 
	 * @return the path for the HTML report, or {@code null} to omit this format
	 */
	public File getHtml() {
		return html;
	}

	/**
	 * Gets the path for the XML format report, if there is one.
	 * 
	 * @return the path for the XML report, or {@code null} to omit this format
	 */
	public File getXml() {
		return xml;
	}

	/**
	 * Gets the encoding of the source files.
	 * 
	 * @return the source encoding
	 */
	public String getSourceEncoding() {
		return sourceEncoding;
	}

	/**
	 * Gets the width of a TAB character in the source files.
	 * 
	 * @return the tab width
	 */
	public int getTabWidth() {
		return tabWidth;
	}

}
