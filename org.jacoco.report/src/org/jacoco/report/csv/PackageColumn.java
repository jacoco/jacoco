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
import org.jacoco.core.analysis.ICoverageNode;
import org.jacoco.core.analysis.ICoverageNode.ElementType;
import org.jacoco.report.ILanguageNames;
import org.jacoco.report.IReportVisitor;
import org.jacoco.report.ISourceFileLocator;

/**
 * Column containing the package name
 * 
 * @author Brock Janiczak
 * @version $Revision: $
 */
public class PackageColumn implements IReportVisitor, ICsvColumn {

	private final ICsvColumn parent;
	private final CsvReportFile reportFile;
	private final String packageName;

	/**
	 * Creates a new Package Column for the report
	 * 
	 * @param reportFile
	 *            CSV Report context
	 * @param parent
	 *            parent element
	 * @param node
	 *            {@link ElementType#PACKAGE} coverage node
	 */
	public PackageColumn(final CsvReportFile reportFile,
			final ICsvColumn parent, final ICoverageNode node) {
		this.reportFile = reportFile;
		this.parent = parent;
		this.packageName = node.getName();
	}

	public IReportVisitor visitChild(final ICoverageNode node)
			throws IOException {
		if (node.getElementType() == ElementType.SOURCEFILE) {
			return CsvReportFile.NULL_VISITOR;
		}
		return new ClassColumn(reportFile, this, (ClassCoverage) node);
	}

	public void visitEnd(final ISourceFileLocator sourceFileLocator)
			throws IOException {
	}

	public void writeContents(final DelimitedWriter writer) throws IOException {
		final ILanguageNames languageNames = reportFile.getLanguageNames();
		final String value = languageNames.getPackageName(packageName);

		parent.writeContents(writer);
		writer.write(value);
	}

}
