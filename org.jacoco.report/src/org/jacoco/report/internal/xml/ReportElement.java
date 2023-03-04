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
package org.jacoco.report.internal.xml;

import java.io.IOException;
import java.io.OutputStream;

import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.analysis.ICounter;
import org.jacoco.core.analysis.ICoverageNode.CounterEntity;
import org.jacoco.core.analysis.ILine;
import org.jacoco.core.analysis.IMethodCoverage;
import org.jacoco.core.data.SessionInfo;

/**
 * A {@link XMLElement} with utility methods to create JaCoCo XML reports.
 */
public class ReportElement extends XMLElement {

	private static final String PUBID = "-//JACOCO//DTD Report 1.1//EN";

	private static final String SYSTEM = "report.dtd";

	/**
	 * Creates a <code>report</code> root element for a XML report.
	 *
	 * @param name
	 *            value for the name attribute
	 * @param encoding
	 *            character encoding used for output
	 * @param output
	 *            output stream will be closed if the root element is closed
	 * @throws IOException
	 *             in case of problems with the underlying output
	 */
	public ReportElement(final String name, final OutputStream output,
			final String encoding) throws IOException {
		super("report", PUBID, SYSTEM, true, encoding, output);
		attr("name", name);
	}

	private ReportElement(final String name, final ReportElement parent)
			throws IOException {
		super(name, parent);
	}

	@Override
	public ReportElement element(final String name) throws IOException {
		return new ReportElement(name, this);
	}

	private ReportElement namedElement(final String elementName,
			final String name) throws IOException {
		final ReportElement element = element(elementName);
		element.attr("name", name);
		return element;
	}

	/**
	 * Creates a 'sessioninfo' element.
	 *
	 * @param info
	 *            info object to write out
	 * @throws IOException
	 *             in case of problems with the underlying output
	 */
	public void sessioninfo(final SessionInfo info) throws IOException {
		final ReportElement sessioninfo = element("sessioninfo");
		sessioninfo.attr("id", info.getId());
		sessioninfo.attr("start", info.getStartTimeStamp());
		sessioninfo.attr("dump", info.getDumpTimeStamp());
	}

	/**
	 * Creates a 'group' element.
	 *
	 * @param name
	 *            value for the name attribute
	 * @return 'group' element
	 * @throws IOException
	 *             in case of problems with the underlying output
	 */
	public ReportElement group(final String name) throws IOException {
		return namedElement("group", name);
	}

	/**
	 * Creates a 'package' element.
	 *
	 * @param name
	 *            value for the name attribute
	 * @return 'package' element
	 * @throws IOException
	 *             in case of problems with the underlying output
	 */
	public ReportElement packageElement(final String name) throws IOException {
		return namedElement("package", name);
	}

	/**
	 * Creates a 'class' element.
	 *
	 * @param coverage
	 *            class coverage node to write out
	 * @return 'class' element
	 * @throws IOException
	 *             in case of problems with the underlying output
	 */
	public ReportElement classElement(final IClassCoverage coverage)
			throws IOException {
		final ReportElement element = namedElement("class", coverage.getName());
		element.attr("sourcefilename", coverage.getSourceFileName());
		return element;
	}

	/**
	 * Creates a 'method' element.
	 *
	 * @param coverage
	 *            method coverage node to write out
	 * @return 'method' element
	 * @throws IOException
	 *             in case of problems with the underlying output
	 */
	public ReportElement method(final IMethodCoverage coverage)
			throws IOException {
		final ReportElement element = namedElement("method",
				coverage.getName());
		element.attr("desc", coverage.getDesc());
		final int line = coverage.getFirstLine();
		if (line != -1) {
			element.attr("line", line);
		}
		return element;
	}

	/**
	 * Creates a 'sourcefile' element.
	 *
	 * @param name
	 *            value for the name attribute
	 * @return 'sourcefile' element
	 * @throws IOException
	 *             in case of problems with the underlying output
	 */
	public ReportElement sourcefile(final String name) throws IOException {
		return namedElement("sourcefile", name);
	}

	/**
	 * Creates a 'line' element.
	 *
	 * @param nr
	 *            line number
	 * @param line
	 *            line object to write out
	 *
	 * @throws IOException
	 *             in case of problems with the underlying output
	 */
	public void line(final int nr, final ILine line) throws IOException {
		final ReportElement element = element("line");
		element.attr("nr", nr);
		counterAttributes(element, "mi", "ci", line.getInstructionCounter());
		counterAttributes(element, "mb", "cb", line.getBranchCounter());
	}

	/**
	 * Creates a 'counter' element.
	 *
	 * @param counterEntity
	 *            entity of this counter
	 *
	 * @param counter
	 *            counter object to write out
	 *
	 * @throws IOException
	 *             in case of problems with the underlying output
	 */
	public void counter(final CounterEntity counterEntity,
			final ICounter counter) throws IOException {
		final ReportElement counterNode = element("counter");
		counterNode.attr("type", counterEntity.name());
		counterAttributes(counterNode, "missed", "covered", counter);
	}

	private static void counterAttributes(final XMLElement element,
			final String missedattr, final String coveredattr,
			final ICounter counter) throws IOException {
		element.attr(missedattr, counter.getMissedCount());
		element.attr(coveredattr, counter.getCoveredCount());
	}

}
