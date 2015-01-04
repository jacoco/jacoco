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
package org.jacoco.report.internal.html;

import org.jacoco.report.internal.ReportOutputFolder;

/**
 * Abstraction for items that can be linked to in a report.
 */
public interface ILinkable {

	/**
	 * Returns a relative link to the item that works from the given base
	 * folder.
	 * 
	 * @param base
	 *            folder where the link should be inserted
	 * @return relative link or <code>null</code> if the target does not exist
	 */
	public String getLink(final ReportOutputFolder base);

	/**
	 * Returns the display label used for the link.
	 * 
	 * @return display label
	 */
	public String getLinkLabel();

	/**
	 * Optional style class to be associated with the link.
	 * 
	 * @return link style class or <code>null</code>
	 */
	public String getLinkStyle();

}
