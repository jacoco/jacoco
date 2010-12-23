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
package org.jacoco.core.analysis;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

/**
 * Unit test for {@link ClassCoverage}.
 * 
 * @author Marc R. Hoffmann
 * @version $qualified.bundle.version$
 */
public class ClassCoverageTest {

	@Test
	public void testProperties() {
		ClassCoverage data = new ClassCoverage("Sample", 12345, "LSample;",
				"java/lang/Object", new String[0]);
		data.setSourceFileName("Sample.java");
		assertEquals(ICoverageNode.ElementType.CLASS, data.getElementType());
		assertEquals("Sample", data.getName());
		assertEquals(12345, data.getId());
		assertEquals("LSample;", data.getSignature());
		assertEquals("java/lang/Object", data.getSuperName());
		assertEquals(0, data.getInterfaceNames().length);
		assertEquals("Sample.java", data.getSourceFileName());
		assertNotNull(data.getLines());
	}

	@Test
	public void testGetPackageName1() {
		ClassCoverage data = new ClassCoverage("ClassInDefaultPackage", 0,
				null, "java/lang/Object", new String[0]);
		assertEquals("", data.getPackageName());
	}

	@Test
	public void testGetPackageName2() {
		ClassCoverage data = new ClassCoverage("org/jacoco/examples/Sample", 0,
				null, "java/lang/Object", new String[0]);
		assertEquals("org/jacoco/examples", data.getPackageName());
	}

	@Test
	public void testEmptyClass() {
		ICoverageNode data = new ClassCoverage("Sample", 0, null,
				"java/lang/Object", new String[0]);
		assertEquals(CounterImpl.COUNTER_0_0, data.getInstructionCounter());
		assertEquals(CounterImpl.COUNTER_0_0, data.getBranchCounter());
		assertEquals(CounterImpl.COUNTER_0_0, data.getMethodCounter());
		assertEquals(CounterImpl.getInstance(false), data.getClassCounter());
	}

	@Test
	public void testAddMethodMissed() {
		ClassCoverage data = new ClassCoverage("Sample", 0, null,
				"java/lang/Object", new String[0]);
		data.addMethod(createMethod(false));
		assertEquals(CounterImpl.getInstance(false),
				data.getInstructionCounter());
		assertEquals(CounterImpl.getInstance(false), data.getMethodCounter());
		assertEquals(CounterImpl.getInstance(false), data.getClassCounter());
	}

	@Test
	public void testAddMethodCovered() {
		ClassCoverage data = new ClassCoverage("Sample", 0, null,
				"java/lang/Object", new String[0]);
		data.addMethod(createMethod(true));
		assertEquals(CounterImpl.getInstance(true),
				data.getInstructionCounter());
		assertEquals(CounterImpl.getInstance(true), data.getMethodCounter());
		assertEquals(CounterImpl.getInstance(true), data.getClassCounter());
	}

	private MethodCoverage createMethod(boolean covered) {
		final MethodCoverage m = new MethodCoverage("sample", "()V", null);
		m.addInsn(covered, MethodCoverage.UNKNOWN_LINE);
		return m;
	}

}
