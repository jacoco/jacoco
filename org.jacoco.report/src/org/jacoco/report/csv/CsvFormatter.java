/*******************************************************************************
 * Copyright (c) 2009, 2010 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Brock Janiczak - initial API and implementation
 *    
 * $Id: $
 *******************************************************************************/
package org.jacoco.report.csv;

import java.io.IOException;

import org.jacoco.core.analysis.ICoverageNode;
import org.jacoco.report.ILanguageNames;
import org.jacoco.report.IReportFormatter;
import org.jacoco.report.IReportVisitor;
import org.jacoco.report.ISingleReportOutput;
import org.jacoco.report.JavaNames;

/**
 * Report formatter that will create a single CSV file. By default the filename
 * used will be the name of the session.
 * 
 * @author Brock Janiczak
 * @version $Revision: $
 */
public class CsvFormatter implements IReportFormatter {

	private ISingleReportOutput output;

	private ILanguageNames languageNames = new JavaNames();

	private String outputEncoding = "UTF-8";

	public IReportVisitor createReportVisitor(final ICoverageNode session)
			throws IOException {

		if (output == null) {
			throw new IllegalStateException("No report output set.");
		}

		return new CsvReportFile(languageNames, output.createFile(),
				outputEncoding);
	}

	/**
	 * Sets the report output callback for this report formatter. This is a
	 * mandatory property.
	 * 
	 * @param output
	 *            file output
	 */
	public void setReportOutput(final ISingleReportOutput output) {
		this.output = output;
	}

	/**
	 * Sets the implementation for language name display. Java language names
	 * are defined by default.
	 * 
	 * @param languageNames
	 *            converter for language specific names
	 */
	public void setLanguageNames(final ILanguageNames languageNames) {
		this.languageNames = languageNames;
	}

	/**
	 * Returns the language names call-back used in this report.
	 * 
	 * @return language names
	 */
	public ILanguageNames getLanguageNames() {
		return languageNames;
	}

	/**
	 * Sets the encoding used for generated CSV document. Default is UTF-8.
	 * 
	 * @param outputEncoding
	 *            CSV output encoding
	 */
	public void setOutputEncoding(final String outputEncoding) {
		this.outputEncoding = outputEncoding;
	}

}
