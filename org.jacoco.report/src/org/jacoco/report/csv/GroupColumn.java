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

import org.jacoco.core.analysis.ICoverageNode;
import org.jacoco.core.analysis.ICoverageNode.ElementType;
import org.jacoco.report.IReportVisitor;
import org.jacoco.report.ISourceFileLocator;

/**
 * Column containing the aggregated group name. Consecutive groups will be
 * merged into a single group
 * 
 * @author Brock Janiczak
 * @version $Revision: $
 */
public class GroupColumn implements IReportVisitor, ICsvColumn {
	private String groupName;
	private final CsvReportFile reportFile;

	/**
	 * Creates a new Group Column for the report
	 * 
	 * @param reportFile
	 *            CSV Report context
	 * @param node
	 *            {@link ElementType#GROUP} coverage node
	 */
	public GroupColumn(final CsvReportFile reportFile, final ICoverageNode node) {
		this.reportFile = reportFile;
		groupName = node.getName();
	}

	public IReportVisitor visitChild(final ICoverageNode node)
			throws IOException {
		if (node.getElementType() == ElementType.GROUP) {
			groupName += "/" + node.getName();
			return this;
		}

		return new BundleColumn(reportFile, this, node);
	}

	public void visitEnd(final ISourceFileLocator sourceFileLocator)
			throws IOException {

	}

	public void writeContents(final DelimitedWriter writer) throws IOException {
		writer.write(groupName);
	}
}
