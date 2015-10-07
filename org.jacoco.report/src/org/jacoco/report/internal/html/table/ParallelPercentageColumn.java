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

import org.jacoco.core.analysis.ICounter;
import org.jacoco.core.analysis.ICoverageNode;
import org.jacoco.report.internal.ReportOutputFolder;
import org.jacoco.report.internal.html.HTMLElement;
import org.jacoco.report.internal.html.resources.Resources;

/**
 * Column that prints the parallel percentage for each item and the total
 * percentage in the footer. The implementation is stateless, instances might be
 * used in parallel.
 */
public class ParallelPercentageColumn implements IColumnRenderer {

	private static final double ROUND_DOWN = 0.000004;

	private final NumberFormat percentageFormat;

	private final Comparator<ITableItem> comparator;

	/**
	 * Creates a new column that is based on the {@link ICounter} for the given
	 * entity.
	 * 
	 * @param locale
	 *            locale for rendering numbers
	 */
	public ParallelPercentageColumn(final Locale locale) {
		this.percentageFormat = DecimalFormat.getPercentInstance(locale);
		this.percentageFormat.setMaximumFractionDigits(3);
		comparator = new TableItemComparator(new Comparator<ICoverageNode>() {

			public int compare(final ICoverageNode o1, final ICoverageNode o2) {
				return (int) (o1.getParallelPercent() - o2.getParallelPercent());
			}
		});
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
		double parallelPercent = (node.getParallelPercent() / 100) - ROUND_DOWN;
		if (parallelPercent < 0) {
			parallelPercent = 0;
		}
		td.text(percentageFormat.format(parallelPercent));
	}

	public Comparator<ITableItem> getComparator() {
		return comparator;
	}

}
