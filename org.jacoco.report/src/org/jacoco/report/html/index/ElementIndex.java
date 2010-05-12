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
package org.jacoco.report.html.index;

import java.util.HashMap;
import java.util.Map;

import org.jacoco.report.ReportOutputFolder;
import org.jacoco.report.html.ReportPage;

/**
 * An index over all report pages that allows queries according to certain
 * criteria.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
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

	public void addClass(final ReportPage page, final long classid) {
		allClasses.put(Long.valueOf(classid), page.getLink(baseFolder));
	}

}
