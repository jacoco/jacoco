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
package org.jacoco.core.internal.analysis;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collections;

import org.jacoco.core.analysis.ICoverageNode;
import org.jacoco.core.analysis.ISourceNode;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit test for {@link ClassCoverageImpl}.
 */
public class ClassCoverageImplTest {

	private ClassCoverageImpl node;

	@Before
	public void setup() {
		node = new ClassCoverageImpl("Sample", 12345, false);
	}

	@Test
	public void testDefaults() {
		assertEquals(ICoverageNode.ElementType.CLASS, node.getElementType());
		assertEquals("Sample", node.getName());
		assertEquals(12345, node.getId());
		assertFalse(node.isNoMatch());
		assertEquals(Collections.emptyList(), node.getMethods());
	}

	@Test
	public void testSignature() {
		node.setSignature("LSample;");
		assertEquals("LSample;", node.getSignature());
	}

	@Test
	public void testSuperName() {
		node.setSuperName("java/lang/Object");
		assertEquals("java/lang/Object", node.getSuperName());
	}

	@Test
	public void testInterfaces() {
		node.setInterfaces(new String[] { "A", "B" });
		assertArrayEquals(new String[] { "A", "B" }, node.getInterfaceNames());
	}

	@Test
	public void testSourceFileName() {
		node.setSourceFileName("Sample.java");
		assertEquals("Sample.java", node.getSourceFileName());
	}

	@Test
	public void testNoMatch() {
		ClassCoverageImpl node = new ClassCoverageImpl("Sample", 12345, true);
		assertTrue(node.isNoMatch());
	}

	@Test
	public void testGetPackageName1() {
		ClassCoverageImpl node = new ClassCoverageImpl("ClassInDefaultPackage",
				0, false);
		assertEquals("", node.getPackageName());
	}

	@Test
	public void testGetPackageName2() {
		ClassCoverageImpl data = new ClassCoverageImpl(
				"org/jacoco/examples/Sample", 0, false);
		assertEquals("org/jacoco/examples", data.getPackageName());
	}

	@Test
	public void testEmptyClass() {
		assertEquals(CounterImpl.COUNTER_0_0, node.getInstructionCounter());
		assertEquals(CounterImpl.COUNTER_0_0, node.getBranchCounter());
		assertEquals(CounterImpl.COUNTER_0_0, node.getMethodCounter());
		assertEquals(CounterImpl.COUNTER_0_0, node.getClassCounter());
	}

	@Test
	public void testAddMethodMissed() {
		node.addMethod(createMethod(false));
		assertEquals(CounterImpl.COUNTER_1_0, node.getInstructionCounter());
		assertEquals(CounterImpl.COUNTER_1_0, node.getMethodCounter());
		assertEquals(CounterImpl.COUNTER_1_0, node.getClassCounter());
	}

	@Test
	public void testAddMethodCovered() {
		node.addMethod(createMethod(true));
		assertEquals(CounterImpl.COUNTER_0_1, node.getInstructionCounter());
		assertEquals(CounterImpl.COUNTER_0_1, node.getMethodCounter());
		assertEquals(CounterImpl.COUNTER_0_1, node.getClassCounter());
	}

	private MethodCoverageImpl createMethod(boolean covered) {
		final MethodCoverageImpl m = new MethodCoverageImpl("sample", "()V",
				null);
		m.increment(covered ? CounterImpl.COUNTER_0_1 : CounterImpl.COUNTER_1_0,
				CounterImpl.COUNTER_0_0, ISourceNode.UNKNOWN_LINE);
		m.incrementMethodCounter();
		return m;
	}

}
