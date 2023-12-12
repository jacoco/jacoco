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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jacoco.core.analysis.IBundleCoverage;
import org.jacoco.core.data.ExecutionData;
import org.jacoco.core.data.SessionInfo;

/**
 * A report visitor that is composed from multiple other visitors. This can be
 * used to create more than one report format in one run.
 */
public class MultiReportVisitor extends MultiGroupVisitor
		implements IReportVisitor {

	private final List<IReportVisitor> visitors;

	/**
	 * New visitor delegating to all given visitors.
	 *
	 * @param visitors
	 *            visitors to delegate to
	 */
	public MultiReportVisitor(final List<IReportVisitor> visitors) {
		super(visitors);
		this.visitors = visitors;
	}

	public void visitInfo(final List<SessionInfo> sessionInfos,
			final Collection<ExecutionData> executionData) throws IOException {
		for (final IReportVisitor v : visitors) {
			v.visitInfo(sessionInfos, executionData);
		}
	}

	public void visitEnd() throws IOException {
		for (final IReportVisitor v : visitors) {
			v.visitEnd();
		}
	}

}

class MultiGroupVisitor implements IReportGroupVisitor {

	private final List<? extends IReportGroupVisitor> visitors;

	MultiGroupVisitor(final List<? extends IReportGroupVisitor> visitors) {
		this.visitors = visitors;
	}

	public void visitBundle(final IBundleCoverage bundle,
			final ISourceFileLocator locator) throws IOException {
		for (final IReportGroupVisitor v : visitors) {
			v.visitBundle(bundle, locator);
		}
	}

	public IReportGroupVisitor visitGroup(final String name)
			throws IOException {
		final List<IReportGroupVisitor> children = new ArrayList<IReportGroupVisitor>();
		for (final IReportGroupVisitor v : visitors) {
			children.add(v.visitGroup(name));
		}
		return new MultiGroupVisitor(children);
	}

}
