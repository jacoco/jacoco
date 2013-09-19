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

import static org.jacoco.core.analysis.filter.CoverageFilters.ALLOW_ALL;
import static org.jacoco.core.analysis.filter.CoverageFilters.excludeClassRegex;
import static org.jacoco.core.analysis.filter.CoverageFilters.excludePackageRegex;
import static org.jacoco.core.analysis.filter.CoverageFilters.includeClassRegex;
import static org.jacoco.core.analysis.filter.CoverageFilters.includePackageRegex;
import static org.jacoco.core.analysis.filter.CoverageFilters.not;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.jacoco.core.internal.analysis.ClassCoverageImpl;
import org.junit.Test;

/**
 * Unit tests for {@link CoverageFilters}.
 */
public class CoverageFiltersTest {
	@Test
	public void testAllowAllFilter() {
		assertTrue(ALLOW_ALL.shouldInclude(null));
	}

	@Test
	public void testFilterInvert() {
		assertFalse(not(ALLOW_ALL).shouldInclude(null));
	}

	@Test
	public void testIncludeClassFilter() {
		ClassCoverageImpl coverage = new ClassCoverageImpl("Name", 0, null,
				null, null);
		assertTrue(includeClassRegex("N.*").shouldInclude(coverage));
		assertFalse(includeClassRegex("Z.*").shouldInclude(coverage));
	}

	@Test
	public void testExcludeClassFilter() {
		ClassCoverageImpl coverage = new ClassCoverageImpl("Name", 0, null,
				null, null);
		assertFalse(excludeClassRegex("N.*").shouldInclude(coverage));
		assertTrue(excludeClassRegex("Z.*").shouldInclude(coverage));
	}

	@Test
	public void testIncludePackageFilter() {
		ClassCoverageImpl coverage = new ClassCoverageImpl(
				"org/jacoco/example/Name", 0, null, null, null);
		assertTrue(includePackageRegex("org\\.jacoco\\.exa.*").shouldInclude(
				coverage));
		assertFalse(includePackageRegex("org\\.jacoco\\.not.*").shouldInclude(
				coverage));
	}

	@Test
	public void testExcludePackageFilter() {
		ClassCoverageImpl coverage = new ClassCoverageImpl(
				"org/jacoco/example/Name", 0, null, null, null);
		assertFalse(excludePackageRegex("org\\.jacoco\\.exa.*").shouldInclude(
				coverage));
		assertTrue(excludePackageRegex("org\\.jacoco\\.not.*").shouldInclude(
				coverage));
	}
}
