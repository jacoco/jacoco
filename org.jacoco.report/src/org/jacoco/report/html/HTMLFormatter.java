/*******************************************************************************
 * Copyright (c) 2009 Mountainminds GmbH & Co. KG and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 * $Id: $
 *******************************************************************************/
package org.jacoco.report.html;

import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.Map;

import org.jacoco.core.analysis.CounterComparator;
import org.jacoco.core.analysis.ICoverageNode;
import org.jacoco.core.analysis.ICoverageNode.CounterEntity;
import org.jacoco.core.analysis.ICoverageNode.ElementType;
import org.jacoco.report.ILanguageNames;
import org.jacoco.report.IReportFormatter;
import org.jacoco.report.IMultiReportOutput;
import org.jacoco.report.IReportVisitor;
import org.jacoco.report.JavaNames;
import org.jacoco.report.ReportOutputFolder;
import org.jacoco.report.html.resources.Resources;

/**
 * Formatter for coverage reports in multiple HTML pages.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public class HTMLFormatter implements IReportFormatter, IHTMLReportContext {

	private IMultiReportOutput output;

	private ILanguageNames languageNames = new JavaNames();

	private String footerText = "";

	private String outputEncoding = "UTF-8";

	private Resources resources;

	/**
	 * The default sorting which is absolute not covered instructions and
	 * absolute total instructions as the second criterion.
	 */
	public static final Comparator<ICoverageNode> DEFAULT_SORTING = CounterComparator.NOTCOVEREDITEMS
			.reverse().on(CounterEntity.INSTRUCTION).second(
					CounterComparator.TOTALITEMS.reverse().on(
							CounterEntity.INSTRUCTION));

	/** Column that displays a linked label. */
	public static final ICoverageTableColumn LABEL_COLUMN = new LabelColumn();

	/** Column that displays the number of classes. */
	public static final ICoverageTableColumn CLASS_COUNTER_COLUMN = new CounterColumn(
			"Classes", CounterEntity.CLASS);

	/** Column that displays the number of methods. */
	public static final ICoverageTableColumn METHOD_COUNTER_COLUMN = new CounterColumn(
			"Methods", CounterEntity.METHOD);

	/** Column that displays the number of lines. */
	public static final ICoverageTableColumn LINE_COUNTER_COLUMN = new CounterColumn(
			"Lines", CounterEntity.LINE);

	/** Column that displays the number of basic blocks. */
	public static final ICoverageTableColumn BLOCK_COUNTER_COLUMN = new CounterColumn(
			"Blocks", CounterEntity.BLOCK);

	/** Column that displays the number of byte code instructions */
	public static final ICoverageTableColumn INSTRUCTION_COUNTER_COLUMN = new CounterColumn(
			"Instructions", CounterEntity.INSTRUCTION);

	/** Column that displays the number of byte code instructions */
	public final ICoverageTableColumn instructionBarColumn = new BarColumn("",
			CounterEntity.INSTRUCTION);

	private final CoverageTable defaultTable = new CoverageTable(Arrays.asList(
			LABEL_COLUMN, CLASS_COUNTER_COLUMN, METHOD_COUNTER_COLUMN,
			LINE_COUNTER_COLUMN, BLOCK_COUNTER_COLUMN,
			INSTRUCTION_COUNTER_COLUMN, instructionBarColumn), DEFAULT_SORTING);

	private final Map<ElementType, CoverageTable> tables = new EnumMap<ElementType, CoverageTable>(
			ElementType.class);

	/**
	 * New instance with default settings.
	 */
	public HTMLFormatter() {
		tables.put(ElementType.CLASS, new CoverageTable(Arrays.asList(
				LABEL_COLUMN, METHOD_COUNTER_COLUMN, LINE_COUNTER_COLUMN,
				BLOCK_COUNTER_COLUMN, INSTRUCTION_COUNTER_COLUMN,
				instructionBarColumn), DEFAULT_SORTING));
	}

	/**
	 * Defines the output for files created by the formatter. This is a
	 * mandatory property.
	 * 
	 * @param output
	 *            file output
	 */
	public void setReportOutput(final IMultiReportOutput output) {
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
	 * Sets the optional text that should be included in every footer page.
	 * 
	 * @param footerText
	 *            footer text
	 */
	public void setFooterText(final String footerText) {
		this.footerText = footerText;
	}

	/**
	 * Sets the encoding used for generated HTML pages. Default is UTF-8.
	 * 
	 * @param outputEncoding
	 *            HTML output encoding
	 */
	public void setOutputEncoding(final String outputEncoding) {
		this.outputEncoding = outputEncoding;
	}

	// === IHTMLReportContext ===

	public ILanguageNames getLanguageNames() {
		return languageNames;
	}

	public Resources getResources() {
		return resources;
	}

	public CoverageTable getTable(final ElementType type) {
		final CoverageTable table = tables.get(type);
		return table == null ? defaultTable : table;
	}

	public String getFooterText() {
		return footerText;
	}

	public String getOutputEncoding() {
		return outputEncoding;
	}

	// === IReportFormatter ===

	public IReportVisitor createReportVisitor(final ICoverageNode session)
			throws IOException {
		if (output == null) {
			throw new IllegalStateException("No report output set.");
		}
		final ReportOutputFolder root = new ReportOutputFolder(output);
		resources = new Resources(root);
		resources.copyResources();
		return new GroupPage(session, null, root, this) {

			@Override
			protected ReportOutputFolder getFolder(final ReportOutputFolder base) {
				return base;
			}
		};
	}

}
