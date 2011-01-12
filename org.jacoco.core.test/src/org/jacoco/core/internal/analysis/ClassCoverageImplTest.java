/*******************************************************************************
 * Copyright (c) 2009, 2010 Mountainminds GmbH & Co. KG and Contributors
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

import static org.junit.Assert.assertEquals;

import org.jacoco.core.analysis.ICoverageNode;
import org.jacoco.core.analysis.ISourceNode;
import org.junit.Test;

/**
 * Unit test for {@link ClassCoverageImpl}.
 * 
 * @author Marc R. Hoffmann
 * @version $qualified.bundle.version$
 */
public class ClassCoverageImplTest {

	@Test
	public void testProperties() {
		ClassCoverageImpl data = new ClassCoverageImpl("Sample", 12345,
				"LSample;", "java/lang/Object", new String[0]);
		data.setSourceFileName("Sample.java");
		assertEquals(ICoverageNode.ElementType.CLASS, data.getElementType());
		assertEquals("Sample", data.getName());
		assertEquals(12345, data.getId());
		assertEquals("LSample;", data.getSignature());
		assertEquals("java/lang/Object", data.getSuperName());
		assertEquals(0, data.getInterfaceNames().length);
		assertEquals("Sample.java", data.getSourceFileName());
	}

	@Test
	public void testGetPackageName1() {
		ClassCoverageImpl data = new ClassCoverageImpl("ClassInDefaultPackage",
				0, null, "java/lang/Object", new String[0]);
		assertEquals("", data.getPackageName());
	}

	@Test
	public void testGetPackageName2() {
		ClassCoverageImpl data = new ClassCoverageImpl(
				"org/jacoco/examples/Sample", 0, null, "java/lang/Object",
				new String[0]);
		assertEquals("org/jacoco/examples", data.getPackageName());
	}

	@Test
	public void testEmptyClass() {
		ICoverageNode data = new ClassCoverageImpl("Sample", 0, null,
				"java/lang/Object", new String[0]);
		assertEquals(CounterImpl.COUNTER_0_0, data.getInstructionCounter());
		assertEquals(CounterImpl.COUNTER_0_0, data.getBranchCounter());
		assertEquals(CounterImpl.COUNTER_0_0, data.getMethodCounter());
		assertEquals(CounterImpl.COUNTER_1_0, data.getClassCounter());
	}

	@Test
	public void testAddMethodMissed() {
		ClassCoverageImpl data = new ClassCoverageImpl("Sample", 0, null,
				"java/lang/Object", new String[0]);
		data.addMethod(createMethod(false));
		assertEquals(CounterImpl.COUNTER_1_0, data.getInstructionCounter());
		assertEquals(CounterImpl.COUNTER_1_0, data.getMethodCounter());
		assertEquals(CounterImpl.COUNTER_1_0, data.getClassCounter());
	}

	@Test
	public void testAddMethodCovered() {
		ClassCoverageImpl data = new ClassCoverageImpl("Sample", 0, null,
				"java/lang/Object", new String[0]);
		data.addMethod(createMethod(true));
		assertEquals(CounterImpl.COUNTER_0_1, data.getInstructionCounter());
		assertEquals(CounterImpl.COUNTER_0_1, data.getMethodCounter());
		assertEquals(CounterImpl.COUNTER_0_1, data.getClassCounter());
	}

	private MethodCoverageImpl createMethod(boolean covered) {
		final MethodCoverageImpl m = new MethodCoverageImpl("sample", "()V",
				null);
		m.increment(
				covered ? CounterImpl.COUNTER_0_1 : CounterImpl.COUNTER_1_0,
				CounterImpl.COUNTER_0_0, ISourceNode.UNKNOWN_LINE);
		return m;
	}

}
