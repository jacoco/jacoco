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
 * Column that prints the number of missed entities and the total number for
 * each item and a summary in the footer. If the total number of items is zero,
 * no column is emitted at all. The implementation is stateful, instances must
 * not be used in parallel.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public class CounterColumn implements ICoverageTableColumn {

	private final String header;

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
	 * @param entity
	 *            counter entity for this column
	 */
	public CounterColumn(final String header, final CounterEntity entity) {
		this.header = header;
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

	public void header(final HTMLElement tr, final Resources resources,
			final ReportOutputFolder base) throws IOException {
		if (visible) {
			tr.td(Styles.CTR2, 3).text(header);
		}
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
		if (visible) {
			tr.td(); // extra column to allow alignment to the right
			final ICounter c = node.getCounter(entity);
			tr.td(Styles.CTR1).text(
					integerFormat.format(c.getMissedCount())).text(" / ");
			tr.td(Styles.CTR2).text(integerFormat.format(c.getTotalCount()));
		}
	}

}
