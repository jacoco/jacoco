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

/**
 * Column with a graphical bar that represents the total amount of items in with
 * length, and the coverage ratio with a red/green sections. The implementation
 * is stateful, instances must not be used in parallel.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public class BarColumn implements ICoverageTableColumn {

	private static final int WIDTH = 120;

	private final String header;

	private final CounterEntity entity;

	private final NumberFormat integerFormat = DecimalFormat
			.getIntegerInstance();

	private int max;

	/**
	 * Creates a new column that is based on the {@link ICounter} for the given
	 * entity.
	 * 
	 * @param header
	 *            column header caption
	 * @param entity
	 *            counter entity for visualization
	 */
	public BarColumn(final String header, final CounterEntity entity) {
		this.header = header;
		this.entity = entity;
	}

	public void init(final List<ICoverageTableItem> items,
			final ICoverageNode total) {
		this.max = 0;
		for (final ICoverageTableItem item : items) {
			final int count = item.getNode().getCounter(entity).getTotalCount();
			if (count > this.max) {
				this.max = count;
			}
		}
	}

	public void header(final HTMLElement tr, final Resources resources,
			final ReportOutputFolder base) throws IOException {
		tr.td().text(header);
	}

	public void footer(final HTMLElement tr, final ICoverageNode total,
			final Resources resources, final ReportOutputFolder base)
			throws IOException {
		tr.td();
	}

	public void item(final HTMLElement tr, final ICoverageTableItem item,
			final Resources resources, final ReportOutputFolder base)
			throws IOException {
		final HTMLElement td = tr.td();
		if (max > 0) {
			final ICounter counter = item.getNode().getCounter(entity);
			final int missed = counter.getMissedCount();
			bar(td, missed, Resources.REDBAR, resources, base);
			final int covered = counter.getCoveredCount();
			bar(td, covered, Resources.GREENBAR, resources, base);
		}
	}

	private void bar(final HTMLElement td, final int count, final String image,
			final Resources resources, final ReportOutputFolder base)
			throws IOException {
		final int width = count * WIDTH / max;
		if (width > 0) {
			td.img(resources.getLink(base, image), width, 10, integerFormat
					.format(count));
		}
	}

}
