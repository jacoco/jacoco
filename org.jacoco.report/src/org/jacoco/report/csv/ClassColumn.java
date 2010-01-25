/*******************************************************************************
 * Copyright (c) 2009 Mountainminds GmbH & Co. KG and others
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

import org.jacoco.core.analysis.ClassCoverage;
import org.jacoco.core.analysis.ICounter;
import org.jacoco.core.analysis.ICoverageNode;
import org.jacoco.core.analysis.ICoverageNode.CounterEntity;
import org.jacoco.core.analysis.ICoverageNode.ElementType;
import org.jacoco.report.ILanguageNames;
import org.jacoco.report.IReportVisitor;
import org.jacoco.report.ISourceFileLocator;

/**
 * Columns containing the following values for the class:
 * <ol>
 * <li>Class Name</li>
 * <li>Methods Covered</li>
 * <li>Methods Not Covered</li>
 * <li>Blocks Covered</li>
 * <li>Blocks Not Covered</li>
 * <li>Lines Covered</li>
 * <li>Lines Not Covered</li>
 * <li>Instructions Covered</li>
 * <li>Instructions Not Covered</li>
 * </ol>
 * 
 * @author Brock Janiczak
 * @version $Revision: $
 */
public class ClassColumn implements IReportVisitor, ICsvColumn {

	private final ICsvColumn parent;

	private final ClassCoverage node;

	private final CsvReportFile reportFile;

	/**
	 * Creates a new Class Column for the report
	 * 
	 * @param reportFile
	 *            CSV Report context
	 * @param parent
	 *            parent element
	 * @param node
	 *            {@link ElementType#CLASS} coverage node
	 */
	public ClassColumn(final CsvReportFile reportFile, final ICsvColumn parent,
			final ClassCoverage node) {
		this.reportFile = reportFile;
		this.parent = parent;
		this.node = node;
	}

	public IReportVisitor visitChild(final ICoverageNode node)
			throws IOException {
		return CsvReportFile.NULL_VISITOR;
	}

	public void visitEnd(final ISourceFileLocator sourceFileLocator)
			throws IOException {
		final DelimitedWriter writer = reportFile.getWriter();

		parent.writeContents(writer);
		this.writeContents(writer);

	}

	public void writeContents(final DelimitedWriter writer) throws IOException {
		final ILanguageNames languageNames = reportFile.getLanguageNames();
		final String className = languageNames.getClassName(node.getName(),
				node.getSignature(), node.getSuperName(), node
						.getInterfaceNames());
		writer.write(className);

		for (final CounterEntity entity : CsvReportFile.COUNTERS) {

			final ICounter counter = node.getCounter(entity);
			writer.write(counter.getCoveredCount());
			writer.write(counter.getNotCoveredCount());
		}

		writer.nextLine();
	}

}
