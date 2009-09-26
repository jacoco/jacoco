/*******************************************************************************
 * Copyright (c) 2009 Mountainminds GmbH & Co. KG and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Brock Janiczak -initial API and implementation
 *    
 * $Id: $
 *******************************************************************************/
package org.jacoco.report.xml;

import java.io.IOException;

import org.jacoco.core.analysis.ICoverageNode;
import org.jacoco.report.FileReportOutput;
import org.jacoco.report.IReportFormatter;
import org.jacoco.report.IReportVisitor;

/**
 * Report formatter that creates a single XML file for a coverage session
 * 
 * @author Brock Janiczak
 * @version $Revision: $
 */
public class XMLFormatter implements IReportFormatter {

	private FileReportOutput fileReportOutput;

	public IReportVisitor createReportVisitor(final ICoverageNode session)
			throws IOException {
		return new XMLReportFile(fileReportOutput, session.getName() + ".xml");
	}

	/**
	 * Set the report output helper for this report formatter
	 * 
	 * @param fileReportOutput
	 *            Report output helper
	 */
	public void setReportOutput(final FileReportOutput fileReportOutput) {
		this.fileReportOutput = fileReportOutput;
	}

}
