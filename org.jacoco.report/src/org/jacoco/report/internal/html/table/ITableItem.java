/*******************************************************************************
 * Copyright (c) 2009, 2019 Mountainminds GmbH & Co. KG and Contributors
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
