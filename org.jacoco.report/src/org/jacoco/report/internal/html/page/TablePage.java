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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jacoco.core.analysis.ICoverageNode;
import org.jacoco.report.internal.ReportOutputFolder;
import org.jacoco.report.internal.html.HTMLElement;
import org.jacoco.report.internal.html.IHTMLReportContext;
import org.jacoco.report.internal.html.resources.Resources;
import org.jacoco.report.internal.html.table.ITableItem;

/**
 * Report page that contains a table of items linked to other pages.
 *
 * @param <NodeType>
 *            type of the node represented by this page
 */
public abstract class TablePage<NodeType extends ICoverageNode>
		extends NodePage<NodeType> {

	private final List<ITableItem> items = new ArrayList<ITableItem>();

	/**
	 * Creates a new node page.
	 *
	 * @param node
	 *            corresponding node
	 * @param parent
	 *            optional hierarchical parent
	 * @param folder
	 *            base folder to create this report in
	 * @param context
	 *            settings context
	 */
	protected TablePage(final NodeType node, final ReportPage parent,
			final ReportOutputFolder folder, final IHTMLReportContext context) {
		super(node, parent, folder, context);
	}

	/**
	 * Adds the given item to the table. Method must be called before the page
	 * is rendered.
	 *
	 * @param item
	 *            table item to add
	 */
	public void addItem(final ITableItem item) {
		items.add(item);
	}

	@Override
	protected void head(final HTMLElement head) throws IOException {
		super.head(head);
		head.script(
				context.getResources().getLink(folder, Resources.SORT_SCRIPT));
	}

	@Override
	protected void content(final HTMLElement body) throws IOException {
		context.getTable().render(body, items, getNode(),
				context.getResources(), folder);
		// free memory, otherwise we will keep the complete page tree:
		items.clear();
	}

}
