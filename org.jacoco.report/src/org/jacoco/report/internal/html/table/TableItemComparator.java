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

import java.util.Comparator;

import org.jacoco.core.analysis.ICoverageNode;

/**
 * Adapter to sort table items based on their coverage nodes.
 */
class TableItemComparator implements Comparator<ITableItem> {

	private final Comparator<ICoverageNode> comparator;

	TableItemComparator(final Comparator<ICoverageNode> comparator) {
		this.comparator = comparator;
	}

	public int compare(final ITableItem i1, final ITableItem i2) {
		return comparator.compare(i1.getNode(), i2.getNode());
	}

}
