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
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.jacoco.core.analysis.CoverageNodeImpl;
import org.jacoco.core.analysis.ICoverageNode;
import org.jacoco.core.analysis.ICoverageNode.CounterEntity;
import org.jacoco.core.analysis.ICoverageNode.ElementType;
import org.jacoco.report.ILanguageNames;
import org.jacoco.report.IReportVisitor;
import org.jacoco.report.ISourceFileLocator;

/**
 * File containing all coverage information in the session.
 * 
 * @author Brock Janiczak
 * @version $Revision: $
 */
public class CsvReportFile implements IReportVisitor {
	/**
	 * Counters that will be written out at the lowest level of the report. By
	 * default, this is at the class level
	 */
	public static CounterEntity[] COUNTERS = { CounterEntity.METHOD,
			CounterEntity.BLOCK, CounterEntity.LINE, CounterEntity.INSTRUCTION };
	/* default */static final IReportVisitor NULL_VISITOR = new NullVisitor();
	private final DelimitedWriter writer;
	private final ILanguageNames languageNames;

	/**
	 * Creates a new CSV report from the supplied configuration and session data
	 * 
	 * @param languageNames
	 *            Language name callback used for name translation
	 * @param writer
	 *            {@link Writer} for CSV output
	 * @throws IOException
	 *             Thrown if there were problems creating the output CSV file
	 */
	public CsvReportFile(final ILanguageNames languageNames, final Writer writer)
			throws IOException {
		this.languageNames = languageNames;
		this.writer = new DelimitedWriter(writer);
		writeHeader(this.writer);
	}

	/**
	 * Creates a new CSV report from the supplied configuration and session data
	 * 
	 * @param languageNames
	 *            Language name callback used for name translation
	 * @param output
	 *            {@link OutputStream} to the CSV file to
	 * @param encoding
	 *            character encoding of the CSV file
	 * @throws IOException
	 *             Thrown if there were problems creating the output CSV file
	 */
	public CsvReportFile(final ILanguageNames languageNames,
			final OutputStream output, final String encoding)
			throws IOException {
		this(languageNames, new OutputStreamWriter(output, encoding));
	}

	public IReportVisitor visitChild(final ICoverageNode node)
			throws IOException {

		if (node.getElementType() != ElementType.GROUP) {
			final ICoverageNode emptyCoverage = new CoverageNodeImpl(
					ElementType.GROUP, "", false);
			final GroupColumn dummy = new GroupColumn(this, emptyCoverage);
			return new BundleColumn(this, dummy, node);
		}

		return new GroupColumn(this, node);
	}

	public void visitEnd(final ISourceFileLocator sourceFileLocator)
			throws IOException {

		writer.close();

	}

	/**
	 * Returns the writer used for output of this report
	 * 
	 * @return delimited writer
	 */
	public DelimitedWriter getWriter() {
		return writer;
	}

	/**
	 * Returns the language names call-back used in this report.
	 * 
	 * @return language names
	 */
	public ILanguageNames getLanguageNames() {
		return languageNames;
	}

	private void writeHeader(final DelimitedWriter writer) throws IOException {
		writer.write("GROUP", "BUNDLE", "PACKAGE", "CLASS");

		for (final CounterEntity entity : COUNTERS) {
			writer.write(entity.name() + "_COVERED");
			writer.write(entity.name() + "_MISSED");
		}

		writer.nextLine();

	}

	/**
	 * Report visitor that ignores its content
	 */
	private static class NullVisitor implements IReportVisitor {

		private NullVisitor() {
		}

		public IReportVisitor visitChild(final ICoverageNode node)
				throws IOException {
			return this;
		}

		public void visitEnd(final ISourceFileLocator sourceFileLocator)
				throws IOException {
		}

	}
}
