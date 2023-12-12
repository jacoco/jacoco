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
package org.jacoco.report.check;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import org.jacoco.core.analysis.ICoverageNode.ElementType;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link Rule}.
 */
public class RuleTest {

	private Rule rule;

	@Before
	public void setup() {
		rule = new Rule();
	}

	@Test
	public void testDefaults() {
		assertEquals(ElementType.BUNDLE, rule.getElement());
		assertEquals(Collections.emptyList(), rule.getLimits());
		assertEquals("*", rule.getIncludes());
		assertEquals("", rule.getExcludes());
	}

	@Test
	public void testSetElement() {
		rule.setElement(ElementType.PACKAGE);
		assertEquals(ElementType.PACKAGE, rule.getElement());
	}

	@Test
	public void testSetLimits() {
		Limit l1 = new Limit();
		Limit l2 = new Limit();
		Limit l3 = new Limit();
		rule.setLimits(Arrays.asList(l1, l2, l3));
		assertEquals(Arrays.asList(l1, l2, l3), rule.getLimits());
	}

	@Test
	public void testCreateLimit() {
		Limit l1 = new Limit();
		Limit l2 = new Limit();
		rule.setLimits(new ArrayList<Limit>(Arrays.asList(l1, l2)));
		Limit l3 = rule.createLimit();
		assertEquals(Arrays.asList(l1, l2, l3), rule.getLimits());
	}

	@Test
	public void testSetIncludes() {
		rule.setIncludes("Foo*");
		assertEquals("Foo*", rule.getIncludes());
		assertTrue(rule.matches("FooBar"));
		assertFalse(rule.matches("Other"));
	}

	@Test
	public void testSetExcludes() {
		rule.setExcludes("Foo*");
		assertEquals("Foo*", rule.getExcludes());
		assertTrue(rule.matches("Other"));
		assertFalse(rule.matches("FooBar"));
	}

}
