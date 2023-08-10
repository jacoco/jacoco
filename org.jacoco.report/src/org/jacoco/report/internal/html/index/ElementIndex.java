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

import java.util.HashMap;
import java.util.Map;

import org.jacoco.report.internal.ReportOutputFolder;
import org.jacoco.report.internal.html.ILinkable;

/**
 * An index over all report pages that allows queries according to certain
 * criteria.
 */
public class ElementIndex implements IIndexUpdate {

	private final ReportOutputFolder baseFolder;

	private final Map<Long, String> allClasses = new HashMap<Long, String>();

	/**
	 * Creates a new empty index for a HTML report.
	 *
	 * @param baseFolder
	 *            base folder where all links are calculated relative to
	 */
	public ElementIndex(final ReportOutputFolder baseFolder) {
		this.baseFolder = baseFolder;
	}

	/**
	 * Returns the link to the class with the given identifier if a
	 * corresponding page exists.
	 *
	 * @param classid
	 *            class identifier
	 * @return Link or null
	 */
	public String getLinkToClass(final long classid) {
		return allClasses.get(Long.valueOf(classid));
	}

	// === IIndexUpdater ===

	public void addClass(final ILinkable link, final long classid) {
		allClasses.put(Long.valueOf(classid), link.getLink(baseFolder));
	}

}
