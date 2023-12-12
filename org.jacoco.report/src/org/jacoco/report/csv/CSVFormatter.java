/*******************************************************************************
 * Copyright (c) 2009, 2023 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Brock Janiczak - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.report.csv;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Collection;
import java.util.List;

import org.jacoco.core.data.ExecutionData;
import org.jacoco.core.data.SessionInfo;
import org.jacoco.report.ILanguageNames;
import org.jacoco.report.IReportVisitor;
import org.jacoco.report.JavaNames;

/**
 * Report formatter that will create a single CSV file. By default the filename
 * used will be the name of the session.
 */
public class CSVFormatter {

	private ILanguageNames languageNames = new JavaNames();

	private String outputEncoding = "UTF-8";

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

	/**
	 * Creates a new visitor to write a report to the given stream.
	 *
	 * @param output
	 *            output stream to write the report to
	 * @return visitor to emit the report data to
	 * @throws IOException
	 *             in case of problems with the output stream
	 */
	public IReportVisitor createVisitor(final OutputStream output)
			throws IOException {
		final DelimitedWriter writer = new DelimitedWriter(
				new OutputStreamWriter(output, outputEncoding));
		final ClassRowWriter rowWriter = new ClassRowWriter(writer,
				languageNames);
		class Visitor extends CSVGroupHandler implements IReportVisitor {
			Visitor() {
				super(rowWriter);
			}

			public void visitInfo(final List<SessionInfo> sessionInfos,
					final Collection<ExecutionData> executionData)
					throws IOException {
				// Info not used for CSV report
			}

			public void visitEnd() throws IOException {
				writer.close();
			}
		}
		return new Visitor();
	}

}
