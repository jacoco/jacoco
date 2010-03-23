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

import static java.lang.String.format;

import java.io.IOException;

import org.jacoco.core.analysis.ClassCoverage;
import org.jacoco.core.analysis.ICoverageNode;
import org.jacoco.core.analysis.ICoverageNode.ElementType;
import org.jacoco.report.IReportVisitor;
import org.jacoco.report.ISourceFileLocator;

/**
 * Report visitor that handles coverage information for packages.
 * 
 * @author Brock Janiczak
 * @version $Revision: $
 */
class CsvPackageHandler implements IReportVisitor {

	private final ClassRowWriter writer;

	private final String groupName;

	private final String packageName;

	public CsvPackageHandler(final ClassRowWriter writer,
			final String groupName, final String packageName) {
		this.writer = writer;
		this.groupName = groupName;
		this.packageName = packageName;
	}

	public IReportVisitor visitChild(final ICoverageNode node)
			throws IOException {
		final ElementType type = node.getElementType();
		switch (type) {
		case CLASS:
			final ClassCoverage classNode = (ClassCoverage) node;
			writer.writeRow(groupName, packageName, classNode);
			return IReportVisitor.NOP;
		case SOURCEFILE:
			return IReportVisitor.NOP;
		}
		throw new IllegalStateException(format("Unexpected child node %s.",
				type));
	}

	public void visitEnd(final ISourceFileLocator sourceFileLocator) {
	}

}
