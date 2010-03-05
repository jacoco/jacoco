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

import org.jacoco.core.analysis.ICoverageNode;
import org.jacoco.report.ReportOutputFolder;

/**
 * Interface for a item (row) in a coverage data table.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public interface ICoverageTableItem {

	/**
	 * Returns the display name of the item. This might be different from the
	 * node name.
	 * 
	 * @return display name of the item
	 */
	public String getLabel();

	/**
	 * Returns an optional link that the item will be linked to.
	 * 
	 * @param base
	 *            base folder from where the link is created
	 * @return relative link or <code>null</code> if the item has no link
	 */
	public String getLink(ReportOutputFolder base);

	/**
	 * Returns the corresponding node data.
	 * 
	 * @return node data
	 */
	public ICoverageNode getNode();

}
