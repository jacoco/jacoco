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
package org.jacoco.report;

import java.io.IOException;

import org.jacoco.core.analysis.IBundleCoverage;

/**
 * Output-Interface for hierarchical report structures. To allow sequential
 * processing and save memory the group structure has to be traversed in a "deep
 * first" fashion. The interface is implemented by the report formatters and can
 * be used to emit coverage report structures.
 *
 * The following constraints apply in using {@link IReportGroupVisitor}
 * instances:
 *
 * <ul>
 * <li>A visitor instance can be used to either submit bundles (
 * {@link #visitBundle(IBundleCoverage, ISourceFileLocator)}) or groups
 * {@link #visitGroup(String)}). Bundles and groups are not allowed for the same
 * visitor.</li>
 * <li>When creating nested groups with {@link #visitGroup(String)} the
 * hierarchy has to be processed in a "deep first" manner.</li>
 * </ul>
 */
public interface IReportGroupVisitor {

	/**
	 * Called to add a bundle to the the report.
	 *
	 * @param bundle
	 *            a bundle to include in the report
	 * @param locator
	 *            source locator for this bundle
	 * @throws IOException
	 *             in case of IO problems with the report writer
	 */
	void visitBundle(IBundleCoverage bundle, ISourceFileLocator locator)
			throws IOException;

	/**
	 * Called to add a new group to the report. The returned
	 * {@link IReportGroupVisitor} instance can be used to add nested bundles or
	 * groups. The content of the group has to be completed before this or any
	 * parent visitor can be used again ("deep first").
	 *
	 * @param name
	 *            name of the group
	 * @return visitor for the group's content
	 * @throws IOException
	 *             in case of IO problems with the report writer
	 */
	IReportGroupVisitor visitGroup(String name) throws IOException;

}
