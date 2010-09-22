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
 * Renderer for a table of {@link ITableItem}s.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public class Table {

	private final List<Column> columns;

	private final Comparator<ITableItem> comparator;

	/**
	 * Create a new table with the given columns.
	 * 
	 * @param comparator
	 *            comparator for sorting the table items
	 */
	public Table(final Comparator<ICoverageNode> comparator) {
		this.columns = new ArrayList<Table.Column>();
		this.comparator = new Comparator<ITableItem>() {
			public int compare(final ITableItem i1,
					final ITableItem i2) {
				return comparator.compare(i1.getNode(), i2.getNode());
			}
		};
	}

	/**
	 * Adds a new column with the given properties to the table.
	 * 
	 * @param header
	 *            column header caption
	 * @param style
	 *            optional CSS style class name for the td-Elements of this
	 *            column
	 * @param renderer
	 *            callback for column rendering
	 */
	public void add(final String header, final String style,
			final IColumnRenderer renderer) {
		columns.add(new Column(header, style, renderer));
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
			final Collection<? extends ITableItem> items,
			final ICoverageNode total, final Resources resources,
			final ReportOutputFolder base) throws IOException {
		final List<ITableItem> sortedItems = sort(items);
		final HTMLElement table = parent.table(Styles.COVERAGETABLE);
		header(table, sortedItems, total, resources, base);
		footer(table, total, resources, base);
		body(table, sortedItems, resources, base);
	}

	private void header(final HTMLElement table,
			final List<ITableItem> items, final ICoverageNode total,
			final Resources resources, final ReportOutputFolder base)
			throws IOException {
		final HTMLElement tr = table.thead().tr();
		for (final Column c : columns) {
			c.visible = c.renderer.init(items, total);
			if (c.visible) {
				tr.td(c.style).text(c.header);
			}
		}
	}

	private void footer(final HTMLElement table, final ICoverageNode total,
			final Resources resources, final ReportOutputFolder base)
			throws IOException {
		final HTMLElement tr = table.tfoot().tr();
		for (final Column c : columns) {
			if (c.visible) {
				c.renderer.footer(tr.td(c.style), total, resources, base);
			}
		}
	}

	private void body(final HTMLElement table,
			final List<ITableItem> items, final Resources resources,
			final ReportOutputFolder base) throws IOException {
		final HTMLElement tbody = table.tbody();
		for (final ITableItem item : items) {
			final HTMLElement tr = tbody.tr();
			for (final Column c : columns) {
				if (c.visible) {
					c.renderer.item(tr.td(c.style), item, resources, base);
				}
			}
		}
	}

	private List<ITableItem> sort(
			final Collection<? extends ITableItem> items) {
		final ArrayList<ITableItem> result = new ArrayList<ITableItem>(
				items);
		Collections.sort(result, comparator);
		return result;
	}

	private static class Column {

		final String header;

		final String style;

		final IColumnRenderer renderer;

		boolean visible;

		Column(final String header, final String style,
				final IColumnRenderer renderer) {
			this.header = header;
			this.style = style;
			this.renderer = renderer;
		}
	}

}
