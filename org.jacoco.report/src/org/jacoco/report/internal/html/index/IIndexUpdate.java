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
package org.jacoco.report.internal.html.index;

import org.jacoco.report.internal.html.ILinkable;

/**
 * Every report page that should become part of the index must be added via this
 * interface.
 */
public interface IIndexUpdate {

	/**
	 * Adds a class to the index.
	 *
	 * @param link
	 *            link to the class
	 * @param classid
	 *            identifier of the class
	 */
	void addClass(ILinkable link, long classid);

}
