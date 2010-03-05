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
package org.jacoco.report;

import java.io.IOException;

import org.jacoco.core.analysis.ICoverageNode;

/**
 * Interface for all implementations that produce a particular report format.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public interface IReportFormatter {

	/**
	 * Creates a visitor for root of a coverage data tree.
	 * 
	 * @param session
	 *            session root node
	 * 
	 * @return visitor for the root node
	 * @throws IOException
	 */
	public IReportVisitor createReportVisitor(ICoverageNode session)
			throws IOException;

}
