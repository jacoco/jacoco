/*******************************************************************************
 * Copyright (c) 2009, 2016 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.core.internal.analysis;

import org.jacoco.core.analysis.ISourceFileCoverage;
import org.jacoco.core.analysis.ISourceNode;

/**
 * Implementation of {@link ISourceFileCoverage}.
 */
public class SourceFileCoverageImpl extends SourceNodeImpl implements
		ISourceFileCoverage {

	private final String packagename;

	/**
	 * Creates a source file data object with the given parameters.
	 * 
	 * @param name
	 *            name of the source file
	 * @param packagename
	 *            vm name of the package the source file belongs to
	 */
	public SourceFileCoverageImpl(final String name, final String packagename) {
		super(ElementType.SOURCEFILE, name);
		this.packagename = packagename;
	}

	@Override
	public void increment(final ISourceNode child) {
		super.increment(child);

		// DO NOT COPY overall BigO value, as that requires recalculation

		// copy E-Big-O lines
		if (child.containsEBigO()) {
			final int first = child.getFirstLine();
			final int last = child.getLastLine();
			for (int line = first; line <= last; line++) {
				setLineEBigOFunction(child.getLineEBigOFunction(line), line);
			}
		}
	}

	// === ISourceFileCoverage implementation ===

	public String getPackageName() {
		return packagename;
	}

}