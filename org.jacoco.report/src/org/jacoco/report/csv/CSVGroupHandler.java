/*******************************************************************************
 * Copyright (c) 2009, 2015 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *    Brock Janiczak - initial API and implementation
 * 
 *******************************************************************************/
package org.jacoco.report.csv;

import java.io.IOException;

import org.jacoco.core.analysis.IBundleCoverage;
import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.analysis.IPackageCoverage;
import org.jacoco.report.IReportGroupVisitor;
import org.jacoco.report.ISourceFileLocator;

/**
 * Report visitor that handles coverage information for groups.
 */
class CSVGroupHandler implements IReportGroupVisitor {

	private final ClassRowWriter writer;

	private final String groupName;

	public CSVGroupHandler(final ClassRowWriter writer) {
		this(writer, null);
	}

	private CSVGroupHandler(final ClassRowWriter writer, final String groupName) {
		this.writer = writer;
		this.groupName = groupName;
	}

	public void visitBundle(final IBundleCoverage bundle,
			final ISourceFileLocator locator) throws IOException {
		final String name = appendName(bundle.getName());
		for (final IPackageCoverage p : bundle.getPackages()) {
			final String packageName = p.getName();
			for (final IClassCoverage c : p.getClasses()) {
				writer.writeRow(name, packageName, c);
			}
		}
	}

	public IReportGroupVisitor visitGroup(final String name) throws IOException {
		return new CSVGroupHandler(writer, appendName(name));
	}

	private String appendName(final String name) {
		return groupName == null ? name : (groupName + "/" + name);
	}

}
