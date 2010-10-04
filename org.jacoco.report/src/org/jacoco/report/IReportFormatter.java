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
 *******************************************************************************/
package org.jacoco.report;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.jacoco.core.analysis.ICoverageNode;
import org.jacoco.core.data.ExecutionData;
import org.jacoco.core.data.SessionInfo;

/**
 * Interface for all implementations that produce a particular report format.
 * 
 * @author Marc R. Hoffmann
 * @version $qualified.bundle.version$
 */
public interface IReportFormatter {

	/**
	 * Creates a visitor for root of a coverage data tree.
	 * 
	 * @param root
	 *            report root node
	 * @param sessionInfos
	 *            list of chronological ordered {@link SessionInfo} objects
	 *            where execution data has been collected for this report.
	 * @param executionData
	 *            collection of all {@link ExecutionData} objects that are
	 *            considered for this report
	 * 
	 * @return visitor for the root node
	 * @throws IOException
	 */
	public IReportVisitor createReportVisitor(ICoverageNode root,
			List<SessionInfo> sessionInfos,
			Collection<ExecutionData> executionData) throws IOException;

}
