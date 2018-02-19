/*******************************************************************************
 * Copyright (c) 2009, 2019 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - generalized structure, line info
 *    
 *******************************************************************************/
package org.jacoco.report.internal.badge;

import java.io.IOException;

import org.jacoco.core.analysis.IBundleCoverage;
import org.jacoco.report.ISourceFileLocator;
import org.jacoco.report.internal.AbstractGroupVisitor;

/**
 * Visitor to just summarize the total counters of all nested groups.
 */
public class TotalVisitor extends AbstractGroupVisitor {

	/**
	 * @param name
	 *            name of the group
	 */
	public TotalVisitor(final String name) {
		super(name);
	}

	@Override
	protected void handleBundle(final IBundleCoverage bundle,
			final ISourceFileLocator locator) throws IOException {
	}

	@Override
	protected AbstractGroupVisitor handleGroup(final String name)
			throws IOException {
		return new TotalVisitor(name);
	}

	@Override
	protected void handleEnd() throws IOException {
	}

}
