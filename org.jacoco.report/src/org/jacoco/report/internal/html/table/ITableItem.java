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

import org.jacoco.core.analysis.ICoverageNode;
import org.jacoco.report.internal.html.ILinkable;

/**
 * Interface for a item (row) in a coverage data table.
 */
public interface ITableItem extends ILinkable {

	/**
	 * Returns the corresponding node data.
	 *
	 * @return node data
	 */
	ICoverageNode getNode();

}
