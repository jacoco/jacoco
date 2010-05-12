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

import org.jacoco.report.html.ReportPage;

/**
 * Every report page that should become part of the index must be added via this
 * interface.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public interface IIndexUpdate {

	/**
	 * Adds a class to the index.
	 * 
	 * @param page
	 *            page of the class
	 * @param classid
	 *            identifier of the class
	 */
	public void addClass(ReportPage page, long classid);

}
