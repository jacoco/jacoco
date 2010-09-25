/*******************************************************************************
 * Copyright (c) 2009, 2010 Mountainminds GmbH & Co. KG and Contributors
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

import static org.jacoco.core.analysis.ICoverageNode.CounterEntity.BLOCK;
import static org.jacoco.core.analysis.ICoverageNode.CounterEntity.CLASS;
import static org.jacoco.core.analysis.ICoverageNode.CounterEntity.INSTRUCTION;
import static org.jacoco.core.analysis.ICoverageNode.CounterEntity.LINE;
import static org.jacoco.core.analysis.ICoverageNode.CounterEntity.METHOD;

import java.io.IOException;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import org.jacoco.core.analysis.CounterComparator;
import org.jacoco.core.analysis.ICoverageNode;
import org.jacoco.core.analysis.ICoverageNode.CounterEntity;
import org.jacoco.core.data.ExecutionData;
import org.jacoco.core.data.SessionInfo;
import org.jacoco.report.ILanguageNames;
import org.jacoco.report.IMultiReportOutput;
import org.jacoco.report.IReportFormatter;
import org.jacoco.report.IReportVisitor;
import org.jacoco.report.ISourceFileLocator;
import org.jacoco.report.JavaNames;
import org.jacoco.report.ReportOutputFolder;
import org.jacoco.report.html.index.ElementIndex;
import org.jacoco.report.html.index.IIndexUpdate;
import org.jacoco.report.html.resources.Resources;
import org.jacoco.report.html.resources.Styles;
import org.jacoco.report.html.table.BarColumn;
import org.jacoco.report.html.table.CounterColumn;
import org.jacoco.report.html.table.IColumnRenderer;
import org.jacoco.report.html.table.LabelColumn;
import org.jacoco.report.html.table.PercentageColumn;
import org.jacoco.report.html.table.Table;

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

	private ElementIndex index;

	private SessionsPage sessionsPage;

	private final Table defaultTable;

	/**
	 * New instance with default settings.
	 */
	public HTMLFormatter() {
		defaultTable = createDefaultTable();
	}

	private Table createDefaultTable() {
		final Table table = new Table();
		table.add("Element", null, new LabelColumn(),
				new Comparator<ICoverageNode>() {
					public int compare(final ICoverageNode n1,
							final ICoverageNode n2) {
						// TODO: Use ITableItem.getLinkLabel()
						return n1.getName().compareTo(n2.getName());
					}
				}, false);
		table.add(
				"Missed Instructions",
				null,
				new BarColumn(INSTRUCTION),
				CounterComparator.MISSEDITEMS
						.reverse()
						.on(CounterEntity.INSTRUCTION)
						.second(CounterComparator.TOTALITEMS.reverse().on(
								CounterEntity.INSTRUCTION)), true);
		table.add("Cov.", Styles.CTR2, new PercentageColumn(INSTRUCTION),
				CounterComparator.MISSEDRATIO.on(INSTRUCTION), false);
		addMissedTotalColumns(table, "Classes", CLASS);
		addMissedTotalColumns(table, "Methods", METHOD);
		addMissedTotalColumns(table, "Blocks", BLOCK);
		addMissedTotalColumns(table, "Lines", LINE);
		return table;
	}

	private static void addMissedTotalColumns(final Table table,
			final String label, final CounterEntity entity) {
		final IColumnRenderer r1 = CounterColumn.newMissed(entity);
		final Comparator<ICoverageNode> c1 = CounterComparator.MISSEDITEMS
				.reverse().on(entity);
		table.add("Missed", Styles.CTR1, r1, c1, false);
		final Comparator<ICoverageNode> c2 = CounterComparator.TOTALITEMS
				.reverse().on(entity);
		final IColumnRenderer r2 = CounterColumn.newTotal(entity);
		table.add(label, Styles.CTR2, r2, c2, false);
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

	public Table getTable() {
		return defaultTable;
	}

	public String getFooterText() {
		return footerText;
	}

	public ILinkable getSessionsPage() {
		return sessionsPage;
	}

	public String getOutputEncoding() {
		return outputEncoding;
	}

	public IIndexUpdate getIndexUpdate() {
		return index;
	}

	// === IReportFormatter ===

	public IReportVisitor createReportVisitor(final ICoverageNode rootNode,
			final List<SessionInfo> sessionInfos,
			final Collection<ExecutionData> executionData) throws IOException {
		if (output == null) {
			throw new IllegalStateException("No report output set.");
		}
		final ReportOutputFolder root = new ReportOutputFolder(output);
		resources = new Resources(root);
		resources.copyResources();
		index = new ElementIndex(root);
		final GroupPage rootpage = new GroupPage(rootNode, null, root, this) {
			@Override
			public String getLinkStyle() {
				return Styles.EL_REPORT;
			}

			@Override
			public void visitEnd(final ISourceFileLocator sourceFileLocator)
					throws IOException {
				super.visitEnd(sourceFileLocator);
				sessionsPage.renderDocument();
			}
		};
		sessionsPage = new SessionsPage(sessionInfos, executionData, index,
				rootpage, root, this);
		return rootpage;
	}

}
