/*******************************************************************************
 * Copyright (c) 2009, 2019 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.report.internal.html.page;

import org.jacoco.core.analysis.ICoverageNode;
import org.jacoco.core.analysis.ISourceFileCoverage;
import org.jacoco.report.internal.ReportOutputFolder;
import org.jacoco.report.internal.html.resources.Styles;
import org.jacoco.report.internal.html.table.ITableItem;

/**
 * Table items representing a source file which cannot be linked.
 * 
 */
final class SourceFileItem implements ITableItem {

	private final ICoverageNode node;

	SourceFileItem(final ISourceFileCoverage node) {
		this.node = node;
	}

	public String getLinkLabel() {
		return node.getName();
	}

	public String getLinkStyle() {
		return Styles.EL_SOURCE;
	}

	public String getLink(final ReportOutputFolder base) {
		return null;
	}

	public ICoverageNode getNode() {
		return node;
	}

}
