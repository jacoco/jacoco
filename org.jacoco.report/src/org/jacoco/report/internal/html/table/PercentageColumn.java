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
package org.jacoco.report.internal.html.table;

import java.io.IOException;
import java.text.DecimalFormat;
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
 * Column that prints the coverage percentage for each item and the total
 * percentage in the footer. The implementation is stateless, instances might be
 * used in parallel.
 */
public class PercentageColumn implements IColumnRenderer {

	private final CounterEntity entity;

	private final NumberFormat percentageFormat;

	private final Comparator<ITableItem> comparator;

	/**
	 * Creates a new column that is based on the {@link ICounter} for the given
	 * entity.
	 * 
	 * @param entity
	 *            counter entity for this column
	 * @param locale
	 *            locale for rendering numbers
	 */
	public PercentageColumn(final CounterEntity entity, final Locale locale) {
		this.entity = entity;
		this.percentageFormat = DecimalFormat.getPercentInstance(locale);
		comparator = new TableItemComparator(
				CounterComparator.MISSEDRATIO.on(entity));
	}

	public boolean init(final List<? extends ITableItem> items,
			final ICoverageNode total) {
		return true;
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
		final ICounter counter = node.getCounter(entity);
		final int total = counter.getTotalCount();
		if (total == 0) {
			td.text("n/a");
		} else {
			td.text(percentageFormat.format(counter.getCoveredRatio()));
		}
	}

	public Comparator<ITableItem> getComparator() {
		return comparator;
	}

}
