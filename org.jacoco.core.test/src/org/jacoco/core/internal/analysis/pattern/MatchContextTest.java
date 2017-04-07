/*******************************************************************************
 * Copyright (c) 2009, 2017 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.internal.analysis.pattern;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link MatchContext}.
 */
public class MatchContextTest {

	private MatchContext ctx;

	@Before
	public void before() {
		ctx = new MatchContext();
	}

	@Test
	public void isLocal_shouldReturnFalse_whenNoVariableIsSet() {
		assertFalse(ctx.isLocal(5, "foo"));
	}

	@Test
	public void isLocal_shouldReturnFalse_whenOnlyVariableAtLowerIndexIsSet() {
		ctx.setLocal(2, "foo");
		assertFalse(ctx.isLocal(200, "foo"));
	}

	@Test
	public void isLocal_shouldReturnFalse_whenVariableWithDifferentNameIsSet() {
		ctx.setLocal(5, "bar");
		assertFalse(ctx.isLocal(5, "foo"));
	}

	@Test
	public void setLocal_shouldOverwritePreviousDefinition_whenNewVariableNameIsGiven() {
		ctx.setLocal(5, "foo");
		ctx.setLocal(5, "bar");
		assertTrue(ctx.isLocal(5, "bar"));
	}

	@Test
	public void setLocal_shouldExpandInternalStorage_whenVariableWithBigIndexIsSet() {
		ctx.setLocal(5, "foo");
		ctx.setLocal(500, "bar");
		assertTrue(ctx.isLocal(5, "foo"));
		assertTrue(ctx.isLocal(500, "bar"));
	}

}
