/*******************************************************************************
 * Copyright (c) 2009, 2010 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 * $Id: $
 *******************************************************************************/
package org.jacoco.report.html.table;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.jacoco.core.analysis.ICoverageNode;
import org.jacoco.report.ReportOutputFolder;
import org.jacoco.report.html.HTMLElement;
import org.jacoco.report.html.resources.Resources;
import org.jacoco.report.html.resources.Styles;

/**
 * Renderer for a table of {@link ICoverageTableItem}s.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public class CoverageTable {

	private final List<? extends ICoverageTableColumn> columns;
	private final Comparator<ICoverageTableItem> comparator;

	/**
	 * Create a new table with the given columns.
	 * 
	 * @param columns
	 *            columns for this table
	 * @param comparator
	 *            comparator for sorting the table items
	 */
	public CoverageTable(final List<? extends ICoverageTableColumn> columns,
			final Comparator<ICoverageNode> comparator) {
		this.columns = columns;
		this.comparator = new Comparator<ICoverageTableItem>() {
			public int compare(final ICoverageTableItem i1,
					final ICoverageTableItem i2) {
				return comparator.compare(i1.getNode(), i2.getNode());
			}
		};
	}

	/**
	 * Renders a table for the given icon
	 * 
	 * @param parent
	 *            parent element in which the table is created
	 * @param items
	 *            items that will make the table rows
	 * @param total
	 *            the summary of all coverage data items in the table static
	 *            resources that might be referenced
	 * @param resources
	 *            static resources that might be referenced
	 * @param base
	 *            base folder of the table
	 * @throws IOException
	 *             in case of IO problems with the element output
	 */
	public void render(final HTMLElement parent,
			final Collection<? extends ICoverageTableItem> items,
			final ICoverageNode total, final Resources resources,
			final ReportOutputFolder base) throws IOException {
		final List<ICoverageTableItem> sortedItems = sort(items);
		final HTMLElement table = parent.table(Styles.COVERAGETABLE);
		header(table, sortedItems, total, resources, base);
		footer(table, total, resources, base);
		body(table, sortedItems, resources, base);
	}

	private void header(final HTMLElement table,
			final List<ICoverageTableItem> items, final ICoverageNode total,
			final Resources resources, final ReportOutputFolder base)
			throws IOException {
		final HTMLElement tr = table.thead().tr();
		for (final ICoverageTableColumn c : columns) {
			c.init(items, total);
			if (c.isVisible()) {
				c.header(tr.td(c.getStyle()), resources, base);
			}
		}
	}

	private void footer(final HTMLElement table, final ICoverageNode total,
			final Resources resources, final ReportOutputFolder base)
			throws IOException {
		final HTMLElement tr = table.tfoot().tr();
		for (final ICoverageTableColumn c : columns) {
			if (c.isVisible()) {
				c.footer(tr.td(c.getStyle()), total, resources, base);
			}
		}
	}

	private void body(final HTMLElement table,
			final List<ICoverageTableItem> items, final Resources resources,
			final ReportOutputFolder base) throws IOException {
		final HTMLElement tbody = table.tbody();
		for (final ICoverageTableItem item : items) {
			final HTMLElement tr = tbody.tr();
			for (final ICoverageTableColumn c : columns) {
				if (c.isVisible()) {
					c.item(tr.td(c.getStyle()), item, resources, base);
				}
			}
		}
	}

	private List<ICoverageTableItem> sort(
			final Collection<? extends ICoverageTableItem> items) {
		final ArrayList<ICoverageTableItem> result = new ArrayList<ICoverageTableItem>(
				items);
		Collections.sort(result, comparator);
		return result;
	}

}
