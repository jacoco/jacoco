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
 *******************************************************************************/
package org.jacoco.report.html.table;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Comparator;
import java.util.List;

import org.jacoco.core.analysis.CounterComparator;
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
 * @version $qualified.bundle.version$
 */
public abstract class CounterColumn implements IColumnRenderer {

	/**
	 * Creates a new column that shows the total count for the given entity.
	 * 
	 * @param entity
	 *            counter entity for this column
	 * @return column instance
	 */
	public static CounterColumn newTotal(final CounterEntity entity) {
		return new CounterColumn(entity, CounterComparator.TOTALITEMS.reverse()
				.on(entity)) {
			@Override
			protected int getValue(final ICounter counter) {
				return counter.getTotalCount();
			}
		};
	}

	/**
	 * Creates a new column that shows the missed count for the given entity.
	 * 
	 * @param entity
	 *            counter entity for this column
	 * @return column instance
	 */
	public static CounterColumn newMissed(final CounterEntity entity) {
		return new CounterColumn(entity, CounterComparator.MISSEDITEMS
				.reverse().on(entity)) {
			@Override
			protected int getValue(final ICounter counter) {
				return counter.getMissedCount();
			}
		};
	}

	/**
	 * Creates a new column that shows the covered count for the given entity.
	 * 
	 * @param entity
	 *            counter entity for this column
	 * @return column instance
	 */
	public static CounterColumn newCovered(final CounterEntity entity) {
		return new CounterColumn(entity, CounterComparator.COVEREDITEMS
				.reverse().on(entity)) {
			@Override
			protected int getValue(final ICounter counter) {
				return counter.getCoveredCount();
			}
		};
	}

	private final NumberFormat integerFormat = DecimalFormat
			.getIntegerInstance();

	private final CounterEntity entity;

	private final Comparator<ITableItem> comparator;

	/**
	 * Creates a new column that is based on the {@link ICounter} for the given
	 * entity.
	 * 
	 * @param entity
	 *            counter entity for this column
	 * @param comparator
	 *            comparator for the nodes of this column
	 */
	protected CounterColumn(final CounterEntity entity,
			final Comparator<ICoverageNode> comparator) {
		this.entity = entity;
		this.comparator = new TableItemComparator(comparator);
	}

	public boolean init(final List<? extends ITableItem> items,
			final ICoverageNode total) {
		for (final ITableItem i : items) {
			if (i.getNode().getCounter(entity).getTotalCount() > 0) {
				return true;
			}
		}
		return false;
	}

	public void footer(final HTMLElement td, final ICoverageNode total,
			final Resources resources, final ReportOutputFolder base)
			throws IOException {
		cell(td, total);
	}

	public void item(final HTMLElement td, final ITableItem item,
			final Resources resources, final ReportOutputFolder base)
			throws IOException {
		cell(td, item.getNode());
	}

	private void cell(final HTMLElement td, final ICoverageNode node)
			throws IOException {
		final int value = getValue(node.getCounter(entity));
		td.text(integerFormat.format(value));
	}

	public Comparator<ITableItem> getComparator() {
		return comparator;
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
