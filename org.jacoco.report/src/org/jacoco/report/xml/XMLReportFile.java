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
import java.io.OutputStream;

import org.jacoco.core.analysis.ICoverageNode;
import org.jacoco.report.IReportVisitor;
import org.jacoco.report.ISourceFileLocator;

/**
 * Report visitor that will generate an XML report of the coverage data
 * 
 * @author Brock Janiczak
 * @version $Revision: $
 */
public class XMLReportFile extends XMLDocument implements IReportVisitor {

	private static final String ROOT = "report";

	private static final String PUBID = "//JaCoCo//DTD Report 1.0//EN";

	private static final String SYSTEM = "http://www.jacoco.org/report.dtd";

	/**
	 * Creates a new Report file
	 * 
	 * @param output
	 *            Report output
	 * @param encoding
	 *            Encoding of the XML file
	 * @throws IOException
	 *             IO Error creating report file
	 */
	public XMLReportFile(final String encoding, final OutputStream output)
			throws IOException {
		super(ROOT, PUBID, SYSTEM, encoding, output);
	}

	public IReportVisitor visitChild(final ICoverageNode node)
			throws IOException {
		return new GroupNode(this, node);
	}

	public void visitEnd(final ISourceFileLocator sourceFileLocator)
			throws IOException {

		this.close();
	}

}
