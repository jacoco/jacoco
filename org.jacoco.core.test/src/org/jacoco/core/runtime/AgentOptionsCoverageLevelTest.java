/*******************************************************************************
 * Copyright (c) 2009, 2025 Mountainminds GmbH & Co. KG and Contributors
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
package org.jacoco.core.runtime;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Unit tests for {@link AgentOptions} coverage level functionality.
 */
public class AgentOptionsCoverageLevelTest {

	@Test
	public void testDefaultCoverageLevel() {
		final AgentOptions options = new AgentOptions();
		assertEquals("full", options.getCoverageLevel());
	}

	@Test
	public void testSetCoverageLevelFull() {
		final AgentOptions options = new AgentOptions();
		options.setCoverageLevel("full");
		assertEquals("full", options.getCoverageLevel());
	}

	@Test
	public void testSetCoverageLevelMethod() {
		final AgentOptions options = new AgentOptions();
		options.setCoverageLevel("method");
		assertEquals("method", options.getCoverageLevel());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSetInvalidCoverageLevel() {
		final AgentOptions options = new AgentOptions();
		options.setCoverageLevel("invalid");
	}

	@Test
	public void testParseCoverageLevelFromString() {
		final AgentOptions options = new AgentOptions("coveragelevel=method");
		assertEquals("method", options.getCoverageLevel());
	}

	@Test
	public void testCoverageLevelInToString() {
		final AgentOptions options = new AgentOptions();
		options.setCoverageLevel("method");
		assertEquals("coveragelevel=method", options.toString());
	}

	@Test
	public void testCoverageLevelWithOtherOptions() {
		final AgentOptions options = new AgentOptions();
		options.setDestfile("test.exec");
		options.setCoverageLevel("method");
		final String string = options.toString();
		// The options should contain both settings
		assert string.contains("destfile=test.exec");
		assert string.contains("coveragelevel=method");
	}
}
