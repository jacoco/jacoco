/*******************************************************************************
 * Copyright (c) 2009, 2013 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.core.analysis.filter;

import java.util.List;

import org.jacoco.core.analysis.IClassCoverage;

/**
 * Aggregate the result of multiple {@link ICoverageFilter} filters, returning
 * true iff all filters return true.
 */
public class MultiCoverageFilter implements ICoverageFilter {

	private final List<ICoverageFilter> filters;

	/**
	 * Create a new filter.
	 * 
	 * @param filters
	 *            filters to aggregate
	 */
	public MultiCoverageFilter(final List<ICoverageFilter> filters) {
		this.filters = filters;
	}

	public boolean shouldInclude(final IClassCoverage coverage) {
		for (final ICoverageFilter filter : filters) {
			if (!filter.shouldInclude(coverage)) {
				return false;
			}
		}
		return true;
	}
}
