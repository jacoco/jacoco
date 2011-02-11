/*******************************************************************************
 * Copyright (c) 2009, 2011 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.report.internal;

import java.io.IOException;

import org.jacoco.core.analysis.CoverageNodeImpl;
import org.jacoco.core.analysis.IBundleCoverage;
import org.jacoco.core.analysis.ICoverageNode.ElementType;
import org.jacoco.report.IReportGroupVisitor;
import org.jacoco.report.ISourceFileLocator;

/**
 * Internal base visitor to calculate group counter summaries for hierarchical
 * reports.
 */
public abstract class AbstractGroupVisitor implements IReportGroupVisitor {

	/** coverage node for this group to total counters */
	protected final CoverageNodeImpl total;

	private AbstractGroupVisitor lastChild;

	/**
	 * Creates a new group with the given name.
	 * 
	 * @param name
	 *            name for the coverage node created internally
	 */
	protected AbstractGroupVisitor(final String name) {
		total = new CoverageNodeImpl(ElementType.GROUP, name);
	}

	public final void visitBundle(final IBundleCoverage bundle,
			final ISourceFileLocator locator) throws IOException {
		total.increment(bundle);
		handleBundle(bundle, locator);
	}

	/**
	 * Called to handle the given bundle in a specific way.
	 * 
	 * @param bundle
	 * @param locator
	 * @throws IOException
	 */
	protected abstract void handleBundle(IBundleCoverage bundle,
			ISourceFileLocator locator) throws IOException;

	public final IReportGroupVisitor visitGroup(final String name)
			throws IOException {
		if (lastChild != null) {
			lastChild.visitEnd();
			total.increment(lastChild.total);
		}
		lastChild = handleGroup(name);
		return lastChild;
	}

	/**
	 * Called to handle a group with the given name in a specific way.
	 * 
	 * @param name
	 * @return created child group
	 * @throws IOException
	 */
	protected abstract AbstractGroupVisitor handleGroup(final String name)
			throws IOException;

	/**
	 * Must be called at the end of every group.
	 * 
	 * @throws IOException
	 */
	public final void visitEnd() throws IOException {
		if (lastChild != null) {
			lastChild.visitEnd();
			total.increment(lastChild.total);
		}
		handleEnd();
	}

	/**
	 * Called to handle the end of this group in a specific way.
	 * 
	 * @throws IOException
	 */
	protected abstract void handleEnd() throws IOException;

}
