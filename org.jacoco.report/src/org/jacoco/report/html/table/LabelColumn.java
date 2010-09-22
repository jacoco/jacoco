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
package org.jacoco.report.html.table;

import java.io.IOException;
import java.util.List;

import org.jacoco.core.analysis.ICoverageNode;
import org.jacoco.report.ReportOutputFolder;
import org.jacoco.report.html.HTMLElement;
import org.jacoco.report.html.resources.Resources;

/**
 * Column for the item label. The implementation is stateless, instances might
 * be used in parallel.
 * 
 * @see ICoverageTableItem#getLabel()
 * @see ICoverageTableItem#getLink(org.jacoco.report.ReportOutputFolder)
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public class LabelColumn implements ICoverageTableColumn {

	public void init(final List<ICoverageTableItem> items,
			final ICoverageNode total) {
	}

	public boolean isVisible() {
		return true;
	}

	public String getStyle() {
		return null;
	}

	public void header(final HTMLElement td, final Resources resources,
			final ReportOutputFolder base) throws IOException {
		td.text("Element");
	}

	public void footer(final HTMLElement td, final ICoverageNode total,
			final Resources resources, final ReportOutputFolder base)
			throws IOException {
		td.text("Total");
	}

	public void item(final HTMLElement td, final ICoverageTableItem item,
			final Resources resources, final ReportOutputFolder base)
			throws IOException {
		td.a(item, base);
	}

}
