/*******************************************************************************
 * Copyright (c) 2009, 2015 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Brock Janiczak -initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.report.xml;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.List;

import org.jacoco.core.analysis.IBundleCoverage;
import org.jacoco.core.data.ExecutionData;
import org.jacoco.core.data.SessionInfo;
import org.jacoco.report.IReportVisitor;
import org.jacoco.report.ISourceFileLocator;
import org.jacoco.report.internal.AbstractGroupVisitor;
import org.jacoco.report.internal.xml.XMLCoverageWriter;
import org.jacoco.report.internal.xml.XMLDocument;
import org.jacoco.report.internal.xml.XMLElement;
import org.jacoco.report.internal.xml.XMLGroupVisitor;

/**
 * Report formatter that creates a single XML file for a coverage session
 */
public class XMLFormatter {

	private static final String PUBID = "-//JACOCO//DTD Report 1.0//EN";

	private static final String SYSTEM = "report.dtd";

	private String outputEncoding = "UTF-8";

	/**
	 * Sets the encoding used for generated XML document. Default is UTF-8.
	 * 
	 * @param outputEncoding
	 *            XML output encoding
	 */
	public void setOutputEncoding(final String outputEncoding) {
		this.outputEncoding = outputEncoding;
	}

	/**
	 * Creates a new visitor to write a report to the given stream.
	 * 
	 * @param output
	 *            output stream to write the report to
	 * @return visitor to emit the report data to
	 * @throws IOException
	 *             in case of problems with the output stream
	 */
	public IReportVisitor createVisitor(final OutputStream output)
			throws IOException {
		final XMLElement root = new XMLDocument("report", PUBID, SYSTEM,
				outputEncoding, true, output);
		class RootVisitor extends XMLGroupVisitor implements IReportVisitor {

			RootVisitor(final XMLElement element) throws IOException {
				super(element, null);
			}

			private List<SessionInfo> sessionInfos;

			public void visitInfo(final List<SessionInfo> sessionInfos,
					final Collection<ExecutionData> executionData)
					throws IOException {
				this.sessionInfos = sessionInfos;
			}

			@Override
			protected void handleBundle(final IBundleCoverage bundle,
					final ISourceFileLocator locator) throws IOException {
				writeHeader(bundle.getName());
				XMLCoverageWriter.writeBundle(bundle, element);
			}

			@Override
			protected AbstractGroupVisitor handleGroup(final String name)
					throws IOException {
				writeHeader(name);
				return new XMLGroupVisitor(element, name);
			}

			private void writeHeader(final String name) throws IOException {
				element.attr("name", name);
				for (final SessionInfo i : sessionInfos) {
					final XMLElement sessioninfo = root.element("sessioninfo");
					sessioninfo.attr("id", i.getId());
					sessioninfo.attr("start", i.getStartTimeStamp());
					sessioninfo.attr("dump", i.getDumpTimeStamp());
				}
			}

			@Override
			protected void handleEnd() throws IOException {
				element.close();
			}
		}
		return new RootVisitor(root);
	}

}
