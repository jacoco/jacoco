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
import org.jacoco.core.analysis.ICoverageNode.ElementType;
import org.jacoco.report.IReportVisitor;
import org.jacoco.report.ISourceFileLocator;

/**
 * Column containing the bundle name
 * 
 * @author Brock Janiczak
 * @version $Revision: $
 */
public class BundleColumn implements IReportVisitor, ICsvColumn {

	private final ICsvColumn parent;
	private final CsvReportFile reportFile;
	private final String bundleName;

	/**
	 * Creates a new Bundle Column for the report
	 * 
	 * @param reportFile
	 *            CSV Report context
	 * @param parent
	 *            parent element
	 * @param node
	 *            {@link ElementType#BUNDLE} coverage node
	 */
	public BundleColumn(final CsvReportFile reportFile,
			final ICsvColumn parent, final ICoverageNode node) {
		this.reportFile = reportFile;
		this.parent = parent;
		this.bundleName = node.getName();
	}

	public IReportVisitor visitChild(final ICoverageNode node)
			throws IOException {
		return new PackageColumn(reportFile, this, node);
	}

	public void visitEnd(final ISourceFileLocator sourceFileLocator)
			throws IOException {
	}

	public void writeContents(final DelimitedWriter writer) throws IOException {
		parent.writeContents(writer);
		writer.write(bundleName);
	}

}
