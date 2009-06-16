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
package org.jacoco.core.analysis;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;

import org.junit.Test;

/**
 * Unit test for {@link ClassNode}.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public class ClassCoverageDataTest {

	@Test
	public void testProperties() {
		ClassNode data = new ClassNode("Sample",
				new ArrayList<ICoverageDataNode>());
		assertEquals(ICoverageDataNode.ElementType.CLASS, data.getElementType());
		assertEquals("Sample", data.getName());
	}

	@Test
	public void testGetPackageName1() {
		ClassNode data = new ClassNode("ClassInDefaultPackage",
				new ArrayList<ICoverageDataNode>());
		assertEquals("", data.getPackagename());
	}

	@Test
	public void testGetPackageName2() {
		ClassNode data = new ClassNode("org/jacoco/examples/Sample",
				new ArrayList<ICoverageDataNode>());
		assertEquals("org/jacoco/examples", data.getPackagename());
	}

	@Test
	public void testEmptyClass() {
		ICoverageDataSummary data = new ClassNode("Sample",
				new ArrayList<ICoverageDataNode>());
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
		final ArrayList<ICoverageDataNode> methods = new ArrayList<ICoverageDataNode>();
		methods.add(createMethod(false));
		methods.add(createMethod(false));
		ICoverageDataSummary data = new ClassNode("Sample", methods);
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
		final ArrayList<ICoverageDataNode> methods = new ArrayList<ICoverageDataNode>();
		methods.add(createMethod(false));
		methods.add(createMethod(true));
		ICoverageDataSummary data = new ClassNode("Sample", methods);
		assertEquals(10, data.getInstructionCounter().getTotalCount(), 0.0);
		assertEquals(5, data.getInstructionCounter().getCoveredCount(), 0.0);
		assertEquals(2, data.getBlockCounter().getTotalCount(), 0.0);
		assertEquals(1, data.getBlockCounter().getCoveredCount(), 0.0);
		assertEquals(2, data.getMethodCounter().getTotalCount(), 0.0);
		assertEquals(1, data.getMethodCounter().getCoveredCount(), 0.0);
		assertEquals(1, data.getClassCounter().getTotalCount(), 0.0);
		assertEquals(1, data.getClassCounter().getCoveredCount(), 0.0);
	}

	private ICoverageDataNode createMethod(boolean covered) {
		final MethodNode m = new MethodNode("sample", "()V", null);
		m.addBlock(5, new int[0], covered);
		return m;
	}

}
