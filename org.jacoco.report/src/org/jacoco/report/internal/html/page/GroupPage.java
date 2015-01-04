/*******************************************************************************
 * Copyright (c) 2009, 2015 Mountainminds GmbH & Co. KG and Contributors
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
import org.jacoco.report.internal.ReportOutputFolder;
import org.jacoco.report.internal.html.IHTMLReportContext;

/**
 * Page showing coverage information for a node that groups other nodes. The
 * page shows a table of linked nodes.
 */
public class GroupPage extends TablePage<ICoverageNode> {

	/**
	 * Creates a new visitor in the given context.
	 * 
	 * @param node
	 *            corresponding coverage data
	 * @param parent
	 *            optional hierarchical parent
	 * @param folder
	 *            base folder to create this page in
	 * @param context
	 *            settings context
	 */
	public GroupPage(final ICoverageNode node, final ReportPage parent,
			final ReportOutputFolder folder, final IHTMLReportContext context) {
		super(node, parent, folder, context);
	}

	@Override
	protected String getOnload() {
		return "initialSort(['breadcrumb', 'coveragetable'])";
	}

	@Override
	protected String getFileName() {
		return "index.html";
	}

}
