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
import java.math.BigDecimal;
import java.math.RoundingMode;
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
		this.percentageFormat = NumberFormat.getPercentInstance(locale);
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
			td.text(format(counter.getCoveredRatio()));
		}
	}

	/**
	 * Ratio 199/(1+199)=0.995 must be displayed as "99%", not as "100%".
	 * Unfortunately {@link NumberFormat} uses {@link RoundingMode#HALF_EVEN} by
	 * default and ability to change available only starting from JDK 6, so
	 * perform rounding using {@link RoundingMode#FLOOR} before formatting.
	 */
	private String format(double ratio) {
		return percentageFormat.format(
				BigDecimal.valueOf(ratio).setScale(2, RoundingMode.FLOOR));
	}

	public Comparator<ITableItem> getComparator() {
		return comparator;
	}

}
