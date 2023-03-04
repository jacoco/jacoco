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
	String getLink(ReportOutputFolder base);

	/**
	 * Returns the display label used for the link.
	 *
	 * @return display label
	 */
	String getLinkLabel();

	/**
	 * Optional style class to be associated with the link.
	 *
	 * @return link style class or <code>null</code>
	 */
	String getLinkStyle();

}
