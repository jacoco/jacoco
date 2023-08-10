/*******************************************************************************
 * Copyright (c) 2009, 2023 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Brock Janiczak - initial API and implementation
 *    Marc R. Hoffmann - generalized structure, line info
 *
 *******************************************************************************/
package org.jacoco.report.internal.xml;

import java.io.IOException;

import org.jacoco.core.analysis.IBundleCoverage;
import org.jacoco.report.ISourceFileLocator;
import org.jacoco.report.internal.AbstractGroupVisitor;

/**
 * A {@link org.jacoco.report.IReportGroupVisitor} that transforms the report
 * structure into XML elements.
 */
public class XMLGroupVisitor extends AbstractGroupVisitor {

	/** XML element of this group */
	protected final ReportElement element;

	/**
	 * New handler for a group with the given name.
	 *
	 * @param element
	 *            XML-Element representing this coverage node. The start tag
	 *            must not be closed yet to allow adding additional attributes.
	 * @param name
	 *            name of the group
	 * @throws IOException
	 *             in case of problems with the underlying writer
	 */
	public XMLGroupVisitor(final ReportElement element, final String name)
			throws IOException {
		super(name);
		this.element = element;
	}

	@Override
	protected void handleBundle(final IBundleCoverage bundle,
			final ISourceFileLocator locator) throws IOException {
		final ReportElement child = element.group(bundle.getName());
		XMLCoverageWriter.writeBundle(bundle, child);
	}

	@Override
	protected AbstractGroupVisitor handleGroup(final String name)
			throws IOException {
		final ReportElement child = element.group(name);
		return new XMLGroupVisitor(child, name);
	}

	@Override
	protected void handleEnd() throws IOException {
		XMLCoverageWriter.writeCounters(total, element);
	}

}
