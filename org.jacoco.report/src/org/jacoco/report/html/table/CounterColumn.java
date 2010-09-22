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
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;

import org.jacoco.core.analysis.ICounter;
import org.jacoco.core.analysis.ICoverageNode;
import org.jacoco.core.analysis.ICoverageNode.CounterEntity;
import org.jacoco.report.ReportOutputFolder;
import org.jacoco.report.html.HTMLElement;
import org.jacoco.report.html.resources.Resources;

/**
 * Column that prints the counter values of entities for each item and a summary
 * in the footer. If the total number of items is zero, no column is emitted at
 * all. The implementation is stateful, instances must not be used in parallel.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public abstract class CounterColumn implements ICoverageTableColumn {

	/**
	 * Creates a new column that shows the total count for the given entity.
	 * 
	 * @param header
	 *            column header caption
	 * @param style
	 *            style class for table cells created for this column
	 * @param entity
	 *            counter entity for this column
	 * @return column instance
	 */
	public static CounterColumn newTotal(final String header,
			final String style, final CounterEntity entity) {
		return new CounterColumn(header, style, entity) {
			@Override
			protected int getValue(final ICounter counter) {
				return counter.getTotalCount();
			}
		};
	}

	/**
	 * Creates a new column that shows the missed count for the given entity.
	 * 
	 * @param header
	 *            column header caption
	 * @param style
	 *            style class for table cells created for this column
	 * @param entity
	 *            counter entity for this column
	 * @return column instance
	 */
	public static CounterColumn newMissed(final String header,
			final String style, final CounterEntity entity) {
		return new CounterColumn(header, style, entity) {
			@Override
			protected int getValue(final ICounter counter) {
				return counter.getMissedCount();
			}
		};
	}

	/**
	 * Creates a new column that shows the covered count for the given entity.
	 * 
	 * @param header
	 *            column header caption
	 * @param style
	 *            style class for table cells created for this column
	 * @param entity
	 *            counter entity for this column
	 * @return column instance
	 */
	public static CounterColumn newCovered(final String header,
			final String style, final CounterEntity entity) {
		return new CounterColumn(header, style, entity) {
			@Override
			protected int getValue(final ICounter counter) {
				return counter.getCoveredCount();
			}
		};
	}

	private final String header;

	private final String style;

	private final CounterEntity entity;

	private boolean visible;

	private final NumberFormat integerFormat = DecimalFormat
			.getIntegerInstance();

	/**
	 * Creates a new column that is based on the {@link ICounter} for the given
	 * entity.
	 * 
	 * @param header
	 *            column header caption
	 * @param style
	 *            style class for table cells created for this column
	 * @param entity
	 *            counter entity for this column
	 */
	protected CounterColumn(final String header, final String style,
			final CounterEntity entity) {
		this.header = header;
		this.style = style;
		this.entity = entity;
	}

	public void init(final List<ICoverageTableItem> items,
			final ICoverageNode total) {
		for (final ICoverageTableItem i : items) {
			if (i.getNode().getCounter(entity).getTotalCount() > 0) {
				visible = true;
				return;
			}
		}
		visible = false;
	}

	public boolean isVisible() {
		return visible;
	}

	public String getStyle() {
		return style;
	}

	public void header(final HTMLElement td, final Resources resources,
			final ReportOutputFolder base) throws IOException {
		td.text(header);
	}

	public void footer(final HTMLElement td, final ICoverageNode total,
			final Resources resources, final ReportOutputFolder base)
			throws IOException {
		cell(td, total);
	}

	public void item(final HTMLElement td, final ICoverageTableItem item,
			final Resources resources, final ReportOutputFolder base)
			throws IOException {
		cell(td, item.getNode());
	}

	private void cell(final HTMLElement td, final ICoverageNode node)
			throws IOException {
		final int value = getValue(node.getCounter(entity));
		td.text(integerFormat.format(value));
	}

	/**
	 * Retrieves the respective value from the counter.
	 * 
	 * @param counter
	 *            counter object
	 * @return value of interest
	 */
	protected abstract int getValue(ICounter counter);

}
