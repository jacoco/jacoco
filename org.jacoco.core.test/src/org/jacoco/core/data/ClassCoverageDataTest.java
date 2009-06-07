/*******************************************************************************
 * Copyright (c) 2009 Mountainminds GmbH & Co. KG and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 * $Id: $
 *******************************************************************************/
package org.jacoco.core.data;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;

import org.junit.Test;

/**
 * Unit test for {@link ClassCoverageData}.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public class ClassCoverageDataTest {

	@Test
	public void testProperties() {
		ClassCoverageData data = new ClassCoverageData("Sample", "testbundle",
				new ArrayList<ICoverageData>());
		assertEquals(ICoverageData.ElementType.CLASS, data.getElementType());
		assertEquals("Sample", data.getName());
		assertEquals("testbundle", data.getBundle());
	}

	@Test
	public void testGetPackageName1() {
		ClassCoverageData data = new ClassCoverageData("ClassInDefaultPackage",
				"testbundle", new ArrayList<ICoverageData>());
		assertEquals("", data.getPackagename());
	}

	@Test
	public void testGetPackageName2() {
		ClassCoverageData data = new ClassCoverageData(
				"org/jacoco/examples/Sample", "testbundle",
				new ArrayList<ICoverageData>());
		assertEquals("org/jacoco/examples", data.getPackagename());
	}

	@Test
	public void testEmptyClass() {
		ICoverageData data = new ClassCoverageData("Sample", null,
				new ArrayList<ICoverageData>());
		assertEquals(0, data.getInstructionCounter().getTotalCount(), 0.0);
		assertEquals(0, data.getInstructionCounter().getCoveredCount(), 0.0);
		assertEquals(0, data.getBlockCounter().getTotalCount(), 0.0);
		assertEquals(0, data.getBlockCounter().getCoveredCount(), 0.0);
		assertEquals(0, data.getMethodCounter().getTotalCount(), 0.0);
		assertEquals(0, data.getMethodCounter().getCoveredCount(), 0.0);
		assertEquals(1, data.getClassCounter().getTotalCount(), 0.0);
		assertEquals(0, data.getClassCounter().getCoveredCount(), 0.0);
	}

	@Test
	public void testNotCovered() {
		final ArrayList<ICoverageData> methods = new ArrayList<ICoverageData>();
		methods.add(createMethod(false));
		methods.add(createMethod(false));
		ICoverageData data = new ClassCoverageData("Sample", null, methods);
		assertEquals(10, data.getInstructionCounter().getTotalCount(), 0.0);
		assertEquals(0, data.getInstructionCounter().getCoveredCount(), 0.0);
		assertEquals(2, data.getBlockCounter().getTotalCount(), 0.0);
		assertEquals(0, data.getBlockCounter().getCoveredCount(), 0.0);
		assertEquals(2, data.getMethodCounter().getTotalCount(), 0.0);
		assertEquals(0, data.getMethodCounter().getCoveredCount(), 0.0);
		assertEquals(1, data.getClassCounter().getTotalCount(), 0.0);
		assertEquals(0, data.getClassCounter().getCoveredCount(), 0.0);
	}

	@Test
	public void testCovered() {
		final ArrayList<ICoverageData> methods = new ArrayList<ICoverageData>();
		methods.add(createMethod(false));
		methods.add(createMethod(true));
		ICoverageData data = new ClassCoverageData("Sample", null, methods);
		assertEquals(10, data.getInstructionCounter().getTotalCount(), 0.0);
		assertEquals(5, data.getInstructionCounter().getCoveredCount(), 0.0);
		assertEquals(2, data.getBlockCounter().getTotalCount(), 0.0);
		assertEquals(1, data.getBlockCounter().getCoveredCount(), 0.0);
		assertEquals(2, data.getMethodCounter().getTotalCount(), 0.0);
		assertEquals(1, data.getMethodCounter().getCoveredCount(), 0.0);
		assertEquals(1, data.getClassCounter().getTotalCount(), 0.0);
		assertEquals(1, data.getClassCounter().getCoveredCount(), 0.0);
	}

	private ICoverageData createMethod(boolean covered) {
		final ArrayList<ICoverageData> blocks = new ArrayList<ICoverageData>();
		blocks.add(new BlockCoverageData(5, new int[0], covered));
		return new MethodCoverageData("sample", "()V", null, blocks);
	}

}
