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

import static org.jacoco.core.analysis.ICoverageNode.CounterEntity.BRANCH;
import static org.jacoco.core.analysis.ICoverageNode.CounterEntity.CLASS;
import static org.jacoco.core.analysis.ICoverageNode.CounterEntity.INSTRUCTION;
import static org.jacoco.core.analysis.ICoverageNode.CounterEntity.LINE;
import static org.jacoco.core.analysis.ICoverageNode.CounterEntity.METHOD;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Arrays;

import org.jacoco.core.analysis.ICoverageNode.ElementType;
import org.junit.Test;

/**
 * Unit tests for {@link CoverageNodeImpl}.
 * 
 * @author Marc R. Hoffmann
 * @version $qualified.bundle.version$
 */
public class CoverageNodeImplTest {

	@Test
	public void testProperties() {
		ICoverageNode node = new CoverageNodeImpl(ElementType.GROUP, "sample",
				false);
		assertEquals(ElementType.GROUP, node.getElementType());
		assertEquals("sample", node.getName());
	}

	@Test
	public void testInitWithoutLines() {
		ICoverageNode node = new CoverageNodeImpl(ElementType.GROUP, "sample",
				false);
		assertEquals(CounterImpl.COUNTER_0_0, node.getBranchCounter());
		assertEquals(CounterImpl.COUNTER_0_0, node.getInstructionCounter());
		assertEquals(CounterImpl.COUNTER_0_0, node.getLineCounter());
		assertEquals(CounterImpl.COUNTER_0_0, node.getMethodCounter());
		assertEquals(CounterImpl.COUNTER_0_0, node.getClassCounter());
		assertNull(node.getLines());
	}

	@Test
	public void testInitWithLines() {
		ICoverageNode node = new CoverageNodeImpl(ElementType.CLASS, "Sample",
				true);
		assertEquals(CounterImpl.COUNTER_0_0, node.getBranchCounter());
		assertEquals(CounterImpl.COUNTER_0_0, node.getInstructionCounter());
		assertEquals(CounterImpl.COUNTER_0_0, node.getLineCounter());
		assertEquals(CounterImpl.COUNTER_0_0, node.getMethodCounter());
		assertEquals(CounterImpl.COUNTER_0_0, node.getClassCounter());
		assertNotNull(node.getLines());
	}

	@Test
	public void testIncrementWithoutLines() {
		CoverageNodeImpl parent = new CoverageNodeImpl(ElementType.GROUP,
				"sample", false);
		ICoverageNode child = new CoverageNodeImpl(ElementType.GROUP, "sample",
				false) {
			{
				instructionCounter = CounterImpl.getInstance(42, 41);
				branchCounter = CounterImpl.getInstance(25, 15);
				lineCounter = CounterImpl.getInstance(8, 3);
				methodCounter = CounterImpl.getInstance(22, 21);
				classCounter = CounterImpl.getInstance(12, 11);
			}
		};
		parent.increment(child);
		assertEquals(CounterImpl.getInstance(42, 41),
				parent.getCounter(INSTRUCTION));
		assertEquals(CounterImpl.getInstance(42, 41),
				parent.getInstructionCounter());
		assertEquals(CounterImpl.getInstance(25, 15), parent.getCounter(BRANCH));
		assertEquals(CounterImpl.getInstance(25, 15), parent.getBranchCounter());
		assertEquals(CounterImpl.getInstance(8, 3), parent.getCounter(LINE));
		assertEquals(CounterImpl.getInstance(8, 3), parent.getLineCounter());
		assertEquals(CounterImpl.getInstance(22, 21), parent.getCounter(METHOD));
		assertEquals(CounterImpl.getInstance(22, 21), parent.getMethodCounter());
		assertEquals(CounterImpl.getInstance(12, 11), parent.getCounter(CLASS));
		assertEquals(CounterImpl.getInstance(12, 11), parent.getClassCounter());
	}

	@Test
	public void testIncrementWithLines() {
		CoverageNodeImpl node = new CoverageNodeImpl(ElementType.CLASS,
				"Sample", true);
		ICoverageNode child = new CoverageNodeImpl(ElementType.CLASS, "Sample",
				true) {
			{
				instructionCounter = CounterImpl.getInstance(42, 41);
				branchCounter = CounterImpl.getInstance(25, 15);
				lines.increment(1, false);
				lines.increment(2, false);
				lines.increment(3, true);
				lines.increment(4, true);
				methodCounter = CounterImpl.getInstance(22, 21);
				classCounter = CounterImpl.getInstance(12, 11);
			}
		};
		node.increment(child);
		assertEquals(CounterImpl.getInstance(42, 41),
				node.getInstructionCounter());
		assertEquals(CounterImpl.getInstance(25, 15), node.getBranchCounter());
		assertEquals(CounterImpl.getInstance(22, 21), node.getMethodCounter());
		assertEquals(CounterImpl.getInstance(12, 11), node.getClassCounter());
		assertEquals(CounterImpl.getInstance(4, 2), node.getLineCounter());
		assertEquals(ILines.NOT_COVERED, node.getLines().getStatus(1), 0.0);
		assertEquals(ILines.NOT_COVERED, node.getLines().getStatus(2), 0.0);
		assertEquals(ILines.FULLY_COVERED, node.getLines().getStatus(3), 0.0);
		assertEquals(ILines.FULLY_COVERED, node.getLines().getStatus(4), 0.0);
	}

	@Test
	public void testIncrementCollection() {
		CoverageNodeImpl parent = new CoverageNodeImpl(ElementType.GROUP,
				"sample", false);
		ICoverageNode child1 = new CoverageNodeImpl(ElementType.GROUP,
				"sample", false) {
			{
				branchCounter = CounterImpl.getInstance(5, 2);
			}
		};
		ICoverageNode child2 = new CoverageNodeImpl(ElementType.GROUP,
				"sample", false) {
			{
				branchCounter = CounterImpl.getInstance(3, 3);
			}
		};
		parent.increment(Arrays.asList(child1, child2));
		assertEquals(CounterImpl.getInstance(8, 5), parent.getBranchCounter());
	}

	@Test
	public void testGetPlainCopyWithLines() {
		ICoverageNode node = new CoverageNodeImpl(ElementType.GROUP, "sample",
				true) {
			{
				classCounter = CounterImpl.getInstance(1, 1);
				methodCounter = CounterImpl.getInstance(2, 2);
				branchCounter = CounterImpl.getInstance(3, 3);
				instructionCounter = CounterImpl.getInstance(4, 4);
				lines.increment(1, true);
				lines.increment(2, true);
				lines.increment(3, true);
				lines.increment(4, true);
				lines.increment(5, true);
			}
		};
		ICoverageNode copy = node.getPlainCopy();
		assertEquals(ElementType.GROUP, copy.getElementType());
		assertEquals("sample", copy.getName());
		assertEquals(CounterImpl.getInstance(1, 1), copy.getClassCounter());
		assertEquals(CounterImpl.getInstance(2, 2), copy.getMethodCounter());
		assertEquals(CounterImpl.getInstance(3, 3), copy.getBranchCounter());
		assertEquals(CounterImpl.getInstance(4, 4),
				copy.getInstructionCounter());
		assertEquals(CounterImpl.getInstance(5, 5), copy.getLineCounter());
		assertEquals(1, copy.getLines().getFirstLine(), 0.0);
		assertEquals(5, copy.getLines().getLastLine(), 0.0);
		assertEquals(ILines.FULLY_COVERED, copy.getLines().getStatus(3), 0.0);
	}

	@Test
	public void testGetPlainCopyWithoutLines() {
		ICoverageNode node = new CoverageNodeImpl(ElementType.CLASS, "Sample",
				false) {
			{
				classCounter = CounterImpl.getInstance(1, 1);
				methodCounter = CounterImpl.getInstance(2, 2);
				branchCounter = CounterImpl.getInstance(3, 3);
				instructionCounter = CounterImpl.getInstance(4, 4);
				lineCounter = CounterImpl.getInstance(5, 5);
			}
		};
		ICoverageNode copy = node.getPlainCopy();
		assertEquals(ElementType.CLASS, copy.getElementType());
		assertEquals("Sample", copy.getName());
		assertEquals(CounterImpl.getInstance(1, 1), copy.getClassCounter());
		assertEquals(CounterImpl.getInstance(2, 2), copy.getMethodCounter());
		assertEquals(CounterImpl.getInstance(3, 3), copy.getBranchCounter());
		assertEquals(CounterImpl.getInstance(4, 4),
				copy.getInstructionCounter());
		assertEquals(CounterImpl.getInstance(5, 5), copy.getLineCounter());
		assertNull(copy.getLines());
	}

	@Test
	public void testToString() {
		CoverageNodeImpl node = new CoverageNodeImpl(ElementType.CLASS, "Test",
				false);
		assertEquals("Test [CLASS]", node.toString());
	}

}
