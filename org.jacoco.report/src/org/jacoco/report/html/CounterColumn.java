/*******************************************************************************
 * Copyright (c) 2009 Mountainminds GmbH & Co. KG and others
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
package org.jacoco.report.html;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;

import org.jacoco.core.analysis.ICounter;
import org.jacoco.core.analysis.ICoverageNode;
import org.jacoco.core.analysis.ICoverageNode.CounterEntity;
import org.jacoco.report.ReportOutputFolder;
import org.jacoco.report.html.resources.Resources;
import org.jacoco.report.html.resources.Styles;

/**
 * Column that prints the coverage data for each item and a summary in the
 * footer.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public class CounterColumn implements ICoverageTableColumn {

	private final String header;

	private final CounterEntity entity;

	private final NumberFormat integerFormat = DecimalFormat
			.getIntegerInstance();

	private final NumberFormat percentageFormat = DecimalFormat
			.getPercentInstance();

	/**
	 * Creates a new column that is based on the {@link ICounter} for the given
	 * entity.
	 * 
	 * @param header
	 *            column header caption
	 * @param entity
	 *            counter entity for this column
	 */
	public CounterColumn(final String header, final CounterEntity entity) {
		this.header = header;
		this.entity = entity;
	}

	public void init(final List<ICoverageTableItem> items,
			final ICoverageNode total) {
	}

	public void header(final HTMLElement tr, final Resources resources,
			final ReportOutputFolder base) throws IOException {
		tr.td(3).text(header);
	}

	public void footer(final HTMLElement tr, final ICoverageNode total,
			final Resources resources, final ReportOutputFolder base)
			throws IOException {
		cell(tr, total);
	}

	public void item(final HTMLElement tr, final ICoverageTableItem item,
			final Resources resources, final ReportOutputFolder base)
			throws IOException {
		cell(tr, item.getNode());
	}

	private void cell(final HTMLElement tr, final ICoverageNode node)
			throws IOException {
		final ICounter counter = node.getCounter(entity);
		tr.td(Styles.CTR1)
				.text(integerFormat.format(counter.getCoveredCount())).text(
						" / ");
		final int total = counter.getTotalCount();
		tr.td(Styles.CTR2).text(integerFormat.format(total)).text(" = ");
		final HTMLElement td3 = tr.td(Styles.CTR3);
		if (total == 0) {
			td3.text("n/a");
		} else {
			td3.text(percentageFormat.format(counter.getCoveredRatio()));
		}
	}

}
