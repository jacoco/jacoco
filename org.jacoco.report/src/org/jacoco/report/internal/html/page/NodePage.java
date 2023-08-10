/*******************************************************************************
 * Copyright (c) 2009, 2023 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.report.internal.html.page;

import org.jacoco.core.analysis.ICoverageNode;
import org.jacoco.report.internal.ReportOutputFolder;
import org.jacoco.report.internal.html.IHTMLReportContext;
import org.jacoco.report.internal.html.resources.Resources;
import org.jacoco.report.internal.html.resources.Styles;
import org.jacoco.report.internal.html.table.ITableItem;

/**
 * Report page that represents a coverage node.
 *
 * @param <NodeType>
 *            type of the node represented by this page
 */
public abstract class NodePage<NodeType extends ICoverageNode>
		extends ReportPage implements ITableItem {

	private final NodeType node;

	/**
	 * Creates a new node page.
	 *
	 * @param node
	 *            corresponding node
	 * @param parent
	 *            optional hierarchical parent
	 * @param folder
	 *            base folder to create this page in
	 * @param context
	 *            settings context
	 */
	protected NodePage(final NodeType node, final ReportPage parent,
			final ReportOutputFolder folder, final IHTMLReportContext context) {
		super(parent, folder, context);
		this.node = node;
	}

	// === ILinkable ===

	public String getLinkStyle() {
		if (isRootPage()) {
			return Styles.EL_REPORT;
		} else {
			return Resources.getElementStyle(node.getElementType());
		}
	}

	public String getLinkLabel() {
		return node.getName();
	}

	// === ICoverageTableItem ===

	public NodeType getNode() {
		return node;
	}

}
