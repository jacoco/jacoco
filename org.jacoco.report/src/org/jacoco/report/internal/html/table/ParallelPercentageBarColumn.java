/*******************************************************************************
 * Copyright (c) 2009, 2016 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Omer Azmon - initial API and implementation
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
 * Column with a graphical bar that represents the parallel percent. The
 * implementation is stateful, instances must not be used in parallel.
 */
public class ParallelPercentageBarColumn implements IColumnRenderer {

	private static final int WIDTH = 120;

	private final NumberFormat integerFormat;

	private int max;

	private final Comparator<ITableItem> comparator;

	/**
	 * Creates a new column that is based on the {@link ICounter} for the given
	 * entity.
	 * 
	 * @param locale
	 *            locale for rendering numbers
	 */
	public ParallelPercentageBarColumn(final Locale locale) {
		this.integerFormat = DecimalFormat.getIntegerInstance(locale);
		comparator = new TableItemComparator(new Comparator<ICoverageNode>() {

			public int compare(final ICoverageNode o1, final ICoverageNode o2) {
				return Double.compare(o1.getParallelPercent(),
						o2.getParallelPercent());
			}
		});
	}

	public boolean init(final List<? extends ITableItem> items,
			final ICoverageNode total) {
		this.max = 0;
		for (final ITableItem item : items) {
			final int count = item.getNode().getInstructionCounter()
					.getExecutionCount();
			if (count > this.max) {
				this.max = count;
			}
		}
		return true;
	}

	public void footer(final HTMLElement td, final ICoverageNode total,
			final Resources resources, final ReportOutputFolder base)
			throws IOException {
		final int parallel = total.getBranchCounter().getExecutionCount();
		final int covered = total.getInstructionCounter().getExecutionCount();
		td.text(integerFormat.format(parallel)).text(" of ")
				.text(integerFormat.format(covered));
	}

	public void item(final HTMLElement td, final ITableItem item,
			final Resources resources, final ReportOutputFolder base)
			throws IOException {
		if (max > 0) {
			final int executed = item.getNode().getInstructionCounter()
					.getExecutionCount();
			final int parallel = item.getNode().getBranchCounter()
					.getExecutionCount();
			bar(td, executed - parallel, Resources.REDBAR, resources, base);
			bar(td, parallel, Resources.GREENBAR, resources, base);
		}
	}

	private void bar(final HTMLElement td, final int count, final String image,
			final Resources resources, final ReportOutputFolder base)
			throws IOException {
		final int width = count * WIDTH / max;
		if (width > 0) {
			td.img(resources.getLink(base, image), width, 10,
					integerFormat.format(count));
		}
	}

	public Comparator<ITableItem> getComparator() {
		return comparator;
	}

}
