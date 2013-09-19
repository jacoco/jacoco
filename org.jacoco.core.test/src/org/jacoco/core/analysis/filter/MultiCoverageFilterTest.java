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
import static org.jacoco.core.analysis.filter.CoverageFilters.not;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;

/**
 * Unit tests for {@link MultiCoverageFilter}.
 */
public class MultiCoverageFilterTest {
	@Test
	public void testFilterAllowed() {
		assertTrue(new MultiCoverageFilter(Arrays.asList(ALLOW_ALL, ALLOW_ALL))
				.shouldInclude(null));
	}

	@Test
	public void testFilterNotAllowed() {
		assertFalse(new MultiCoverageFilter(Arrays.asList(ALLOW_ALL,
				not(ALLOW_ALL))).shouldInclude(null));
	}
}
