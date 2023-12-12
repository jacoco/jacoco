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
import java.text.NumberFormat;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.jacoco.core.analysis.CounterComparator;
import org.jacoco.core.analysis.ICounter;
import org.jacoco.core.analysis.ICoverageNode;
import org.jacoco.core.analysis.ICoverageNode.CounterEntity;
import org.jacoco.report.internal.ReportOutputFolder;
import org.jacoco.report.internal.html.HTMLElement;
import org.jacoco.report.internal.html.resources.Resources;

/**
 * Column that prints the counter values of entities for each item and a summary
 * in the footer. If the total number of items is zero, no column is emitted at
 * all. The implementation is stateful, instances must not be used in parallel.
 */
public abstract class CounterColumn implements IColumnRenderer {

	/**
	 * Creates a new column that shows the total count for the given entity.
	 *
	 * @param entity
	 *            counter entity for this column
	 * @param locale
	 *            locale for rendering numbers
	 * @return column instance
	 */
	public static CounterColumn newTotal(final CounterEntity entity,
			final Locale locale) {
		return new CounterColumn(entity, locale,
				CounterComparator.TOTALITEMS.reverse().on(entity)) {
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
	 * @param locale
	 *            locale for rendering numbers
	 * @return column instance
	 */
	public static CounterColumn newMissed(final CounterEntity entity,
			final Locale locale) {
		return new CounterColumn(entity, locale,
				CounterComparator.MISSEDITEMS.reverse().on(entity)) {
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
	 * @param locale
	 *            locale for rendering numbers
	 * @return column instance
	 */
	public static CounterColumn newCovered(final CounterEntity entity,
			final Locale locale) {
		return new CounterColumn(entity, locale,
				CounterComparator.COVEREDITEMS.reverse().on(entity)) {
			@Override
			protected int getValue(final ICounter counter) {
				return counter.getCoveredCount();
			}
		};
	}

	private final CounterEntity entity;

	private final NumberFormat integerFormat;

	private final Comparator<ITableItem> comparator;

	/**
	 * Creates a new column that is based on the {@link ICounter} for the given
	 * entity.
	 *
	 * @param entity
	 *            counter entity for this column
	 * @param locale
	 *            locale for rendering numbers
	 * @param comparator
	 *            comparator for the nodes of this column
	 */
	protected CounterColumn(final CounterEntity entity, final Locale locale,
			final Comparator<ICoverageNode> comparator) {
		this.entity = entity;
		this.integerFormat = NumberFormat.getIntegerInstance(locale);
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
