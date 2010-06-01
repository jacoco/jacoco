/*******************************************************************************
 * Copyright (c) 2009, 2010 Mountainminds GmbH & Co. KG and Contributors
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
import java.util.Collection;
import java.util.List;

import org.jacoco.core.analysis.ICoverageNode;
import org.jacoco.core.data.ExecutionData;
import org.jacoco.core.data.SessionInfo;
import org.jacoco.report.IReportFormatter;
import org.jacoco.report.IReportVisitor;
import org.jacoco.report.ISingleReportOutput;

/**
 * Report formatter that creates a single XML file for a coverage session
 * 
 * @author Brock Janiczak
 * @version $Revision: $
 */
public class XMLFormatter implements IReportFormatter {

	private static final String PUBID = "-//JACOCO//DTD Report 1.0//EN";

	private static final String SYSTEM = "report.dtd";

	private ISingleReportOutput output;

	private String outputEncoding = "UTF-8";

	public IReportVisitor createReportVisitor(final ICoverageNode rootNode,
			final List<SessionInfo> sessionInfos,
			final Collection<ExecutionData> executionData) throws IOException {

		if (output == null) {
			throw new IllegalStateException("No report output set.");
		}
		final XMLElement root = new XMLDocument("report", PUBID, SYSTEM,
				outputEncoding, true, output.createFile());
		return new XMLReportNodeHandler(root, rootNode) {
			@Override
			protected void insertElementsBefore(final XMLElement element)
					throws IOException {
				writeSessionInfos(element, sessionInfos);
			}
		};
	}

	private void writeSessionInfos(final XMLElement root,
			final List<SessionInfo> infos) throws IOException {
		for (final SessionInfo i : infos) {
			final XMLElement sessioninfo = root.element("sessioninfo");
			sessioninfo.attr("id", i.getId());
			sessioninfo.attr("start", i.getStartTimeStamp());
			sessioninfo.attr("dump", i.getDumpTimeStamp());
		}
	}

	/**
	 * Sets the report output callback for this report formatter. This is a
	 * mandatory property.
	 * 
	 * @param output
	 *            report output
	 */
	public void setReportOutput(final ISingleReportOutput output) {
		this.output = output;
	}

	/**
	 * Sets the encoding used for generated XML document. Default is UTF-8.
	 * 
	 * @param outputEncoding
	 *            XML output encoding
	 */
	public void setOutputEncoding(final String outputEncoding) {
		this.outputEncoding = outputEncoding;
	}

}
