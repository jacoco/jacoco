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
package org.jacoco.report.internal.html.table;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;

import org.jacoco.core.analysis.ICoverageNode;
import org.jacoco.report.internal.ReportOutputFolder;
import org.jacoco.report.internal.html.HTMLElement;
import org.jacoco.report.internal.html.resources.Resources;

/**
 * Renderer for a single column of a coverage table. The methods are always
 * called in the sequence <code>init header footer item*</code>. Implementations
 * might be stateful.
 */
public interface IColumnRenderer {

	/**
	 * Initializes the column before any output method is called.
	 *
	 * @param items
	 *            all items that will be displayed in the table
	 * @param total
	 *            the summary of all coverage data items in the table
	 * @return <code>true</code> if the column should be visible
	 */
	boolean init(List<? extends ITableItem> items, ICoverageNode total);

	/**
	 * Renders the footer for this column.
	 *
	 * @param td
	 *            the parent table cell
	 * @param total
	 *            the summary of all coverage data items in the table
	 * @param resources
	 *            static resources that might be referenced
	 * @param base
	 *            base folder of the table
	 * @throws IOException
	 *             in case of IO problems with the element output
	 */
	void footer(HTMLElement td, ICoverageNode total, Resources resources,
			ReportOutputFolder base) throws IOException;

	/**
	 * Renders a single item in this column.
	 *
	 * @param td
	 *            the parent table cell
	 * @param item
	 *            the item to display
	 * @param resources
	 *            static resources that might be referenced
	 * @param base
	 *            base folder of the table
	 * @throws IOException
	 *             in case of IO problems with the element output
	 */
	void item(HTMLElement td, ITableItem item, Resources resources,
			ReportOutputFolder base) throws IOException;

	/**
	 * Returns the comparator to sort this table column.
	 *
	 * @return comparator for this column
	 */
	Comparator<ITableItem> getComparator();

}
