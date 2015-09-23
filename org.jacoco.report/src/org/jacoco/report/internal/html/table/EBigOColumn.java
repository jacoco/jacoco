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
import java.util.Comparator;
import java.util.List;

import org.jacoco.core.analysis.EBigOFunction;
import org.jacoco.core.analysis.ICoverageNode;
import org.jacoco.core.analysis.ISourceNode;
import org.jacoco.report.internal.ReportOutputFolder;
import org.jacoco.report.internal.html.HTMLElement;
import org.jacoco.report.internal.html.resources.Resources;

/**
 * Column for the E-Big-O result. The implementation is stateless, instances
 * might be used in parallel.
 */
public class EBigOColumn implements IColumnRenderer {

	private static final Comparator<ITableItem> COMPARATOR = new Comparator<ITableItem>() {
		public int compare(final ITableItem i1, final ITableItem i2) {
			final ICoverageNode node1 = i1.getNode();
			final ISourceNode sourceNode1 = (ISourceNode) (node1 instanceof ISourceNode ? node1
					: null);
			final ICoverageNode node2 = i2.getNode();
			final ISourceNode sourceNode2 = (ISourceNode) (node2 instanceof ISourceNode ? node2
					: null);
			if (sourceNode1 == sourceNode2) {
				return 0;
			}
			if (sourceNode1 == null) {
				return 1;
			}
			if (sourceNode2 == null) {
				return -1;
			}
			return sourceNode1.getEBigOFunction().compareTo(
					sourceNode1.getEBigOFunction());
		}
	};

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
		if (node instanceof ISourceNode) {
			addEBigO(td, ((ISourceNode) node).getEBigOFunction());
		} else {
			td.text("");
		}
	}

	private void addEBigO(final HTMLElement td, final EBigOFunction ebigo)
			throws IOException {
		HTMLElement span;
		switch (ebigo.getType()) {
		default:
			return;
		case Logarithmic:
			span = td.span("efc");
			break;
		case Linear:
			span = td.span("efc");
			break;
		case PowerLaw:
			span = td.span("epc");
			break;
		case Exponential:
			span = td.span("enc");
			break;
		}
		span.text(ebigo.getOrderOfMagnitude());
	}

	public Comparator<ITableItem> getComparator() {
		return COMPARATOR;
	}

}
