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

import org.jacoco.core.analysis.IClassCoverage;

/**
 * Several helpful {@link ICoverageFilter} instances.
 */
public class CoverageFilters {

	/**
	 * Always returns true (every {@link IClassCoverage} should be included).
	 */
	public static final ICoverageFilter ALLOW_ALL = new ICoverageFilter() {
		public boolean shouldInclude(final IClassCoverage coverage) {
			return true;
		}
	};

	/**
	 * Creates a {@link ICoverageFilter} that only includes
	 * {@link IClassCoverage} that have a class name matching the regex.
	 * 
	 * @param regex
	 * 
	 * @return the filter
	 */
	public static ICoverageFilter includeClassRegex(final String regex) {
		return new ICoverageFilter() {
			public boolean shouldInclude(final IClassCoverage coverage) {
				return coverage.getName().matches(regex);
			}
		};
	}

	/**
	 * Creates a {@link ICoverageFilter} that excludes all
	 * {@link IClassCoverage} that have a class name matching the regex.
	 * 
	 * @param regex
	 * 
	 * @return the filter
	 */
	public static ICoverageFilter excludeClassRegex(final String regex) {
		return not(includeClassRegex(regex));
	}

	/**
	 * Creates a {@link ICoverageFilter} that only includes
	 * {@link IClassCoverage} that have a package name matching the regex. The
	 * package name has the format "com.jacoco.example"
	 * 
	 * @param regex
	 * 
	 * @return the filter
	 */
	public static ICoverageFilter includePackageRegex(final String regex) {
		return new ICoverageFilter() {
			public boolean shouldInclude(final IClassCoverage coverage) {
				return coverage.getPackageName().replace('/', '.')
						.matches(regex);
			}
		};
	}

	/**
	 * Creates a {@link ICoverageFilter} that excludes all
	 * {@link IClassCoverage} that have a package name matching the regex. The
	 * package name has the format "com.jacoco.example"
	 * 
	 * @param regex
	 * 
	 * @return the filter
	 */
	public static ICoverageFilter excludePackageRegex(final String regex) {
		return not(includePackageRegex(regex));
	}

	/**
	 * Creates a {@link ICoverageFilter} that inverts the filter that is passed
	 * in.
	 * 
	 * @param filter
	 *            to be inverted
	 * 
	 * @return the inverted filter
	 */
	public static ICoverageFilter not(final ICoverageFilter filter) {
		return new ICoverageFilter() {
			public boolean shouldInclude(final IClassCoverage coverage) {
				return !filter.shouldInclude(coverage);
			}
		};
	}
}