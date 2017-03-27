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
package org.jacoco.core.analysis;

import static org.jacoco.core.analysis.ICoverageNode.CounterEntity.BRANCH;
import static org.jacoco.core.analysis.ICoverageNode.CounterEntity.CLASS;
import static org.jacoco.core.analysis.ICoverageNode.CounterEntity.COMPLEXITY;
import static org.jacoco.core.analysis.ICoverageNode.CounterEntity.INSTRUCTION;
import static org.jacoco.core.analysis.ICoverageNode.CounterEntity.LINE;
import static org.jacoco.core.analysis.ICoverageNode.CounterEntity.METHOD;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.jacoco.core.analysis.ICoverageNode.ElementType;
import org.jacoco.core.internal.analysis.CounterImpl;
import org.junit.Test;

/**
 * Unit tests for {@link CoverageNodeImpl}.
 */
public class CoverageNodeImplTest {

	@Test
	public void testProperties() {
		ICoverageNode node = new CoverageNodeImpl(ElementType.GROUP, "sample");
		assertEquals(ElementType.GROUP, node.getElementType());
		assertEquals("sample", node.getName());
	}

	@Test
	public void testInit() {
		ICoverageNode node = new CoverageNodeImpl(ElementType.GROUP, "sample");
		assertEquals(CounterImpl.COUNTER_0_0, node.getBranchCounter());
		assertEquals(CounterImpl.COUNTER_0_0, node.getInstructionCounter());
		assertEquals(CounterImpl.COUNTER_0_0, node.getLineCounter());
		assertEquals(CounterImpl.COUNTER_0_0, node.getComplexityCounter());
		assertEquals(CounterImpl.COUNTER_0_0, node.getMethodCounter());
		assertEquals(CounterImpl.COUNTER_0_0, node.getClassCounter());
	}

	@Test
	public void testIncrement() {
		CoverageNodeImpl parent = new CoverageNodeImpl(ElementType.GROUP,
				"sample");
		ICoverageNode child = new CoverageNodeImpl(ElementType.GROUP,
				"sample") {
			{
				instructionCounter = CounterImpl.getInstance(1, 41);
				branchCounter = CounterImpl.getInstance(10, 15);
				lineCounter = CounterImpl.getInstance(5, 3);
				complexityCounter = CounterImpl.getInstance(4, 2);
				methodCounter = CounterImpl.getInstance(1, 21);
				classCounter = CounterImpl.getInstance(1, 11);
			}
		};
		parent.increment(child);
		assertEquals(CounterImpl.getInstance(1, 41),
				parent.getCounter(INSTRUCTION));
		assertEquals(CounterImpl.getInstance(1, 41),
				parent.getInstructionCounter());
		assertEquals(CounterImpl.getInstance(10, 15),
				parent.getCounter(BRANCH));
		assertEquals(CounterImpl.getInstance(10, 15),
				parent.getBranchCounter());
		assertEquals(CounterImpl.getInstance(5, 3), parent.getCounter(LINE));
		assertEquals(CounterImpl.getInstance(5, 3), parent.getLineCounter());
		assertEquals(CounterImpl.getInstance(4, 2),
				parent.getCounter(COMPLEXITY));
		assertEquals(CounterImpl.getInstance(4, 2),
				parent.getComplexityCounter());
		assertEquals(CounterImpl.getInstance(1, 21), parent.getCounter(METHOD));
		assertEquals(CounterImpl.getInstance(1, 21), parent.getMethodCounter());
		assertEquals(CounterImpl.getInstance(1, 11), parent.getCounter(CLASS));
		assertEquals(CounterImpl.getInstance(1, 11), parent.getClassCounter());
	}

	/** When a method is treated as covered, then nothing is missed. */
	@Test
	public void testGetCoveredCountersHasNothingMissed() {
		CoverageNodeImpl parent = new CoverageNodeImpl(ElementType.GROUP,
				"sample");
		parent.setTreatAsFullyCovered(true);
		ICoverageNode child = new CoverageNodeImpl(ElementType.GROUP,
				"sample") {
			{
				instructionCounter = CounterImpl.getInstance(1, 41);
				branchCounter = CounterImpl.getInstance(10, 15);
				lineCounter = CounterImpl.getInstance(5, 3);
				complexityCounter = CounterImpl.getInstance(4, 2);
				methodCounter = CounterImpl.getInstance(1, 21);
				classCounter = CounterImpl.getInstance(1, 11);
			}
		};
		parent.increment(child);
		assertEquals(CounterImpl.getInstance(0, 42),
				parent.getCounter(INSTRUCTION));
		assertEquals(CounterImpl.getInstance(0, 42),
				parent.getInstructionCounter());
		assertEquals(CounterImpl.getInstance(0, 25), parent.getCounter(BRANCH));
		assertEquals(CounterImpl.getInstance(0, 25), parent.getBranchCounter());
		assertEquals(CounterImpl.getInstance(0, 8), parent.getCounter(LINE));
		assertEquals(CounterImpl.getInstance(0, 8), parent.getLineCounter());
		assertEquals(CounterImpl.getInstance(0, 6),
				parent.getCounter(COMPLEXITY));
		assertEquals(CounterImpl.getInstance(0, 6),
				parent.getComplexityCounter());
		assertEquals(CounterImpl.getInstance(0, 22), parent.getCounter(METHOD));
		assertEquals(CounterImpl.getInstance(0, 22), parent.getMethodCounter());
		assertEquals(CounterImpl.getInstance(0, 12), parent.getCounter(CLASS));
		assertEquals(CounterImpl.getInstance(0, 12), parent.getClassCounter());
	}

	@Test
	public void testIncrementCollection() {
		CoverageNodeImpl parent = new CoverageNodeImpl(ElementType.GROUP,
				"sample");
		ICoverageNode child1 = new CoverageNodeImpl(ElementType.GROUP,
				"sample") {
			{
				branchCounter = CounterImpl.getInstance(5, 2);
			}
		};
		ICoverageNode child2 = new CoverageNodeImpl(ElementType.GROUP,
				"sample") {
			{
				branchCounter = CounterImpl.getInstance(3, 3);
			}
		};
		parent.increment(Arrays.asList(child1, child2));
		assertEquals(CounterImpl.getInstance(8, 5), parent.getBranchCounter());
	}

	@Test
	public void testGetPlainCopy() {
		ICoverageNode node = new CoverageNodeImpl(ElementType.CLASS, "Sample") {
			{
				classCounter = CounterImpl.getInstance(1, 1);
				methodCounter = CounterImpl.getInstance(2, 2);
				branchCounter = CounterImpl.getInstance(3, 3);
				instructionCounter = CounterImpl.getInstance(4, 4);
				lineCounter = CounterImpl.getInstance(5, 5);
				complexityCounter = CounterImpl.getInstance(6, 6);
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
		assertEquals(CounterImpl.getInstance(6, 6),
				copy.getComplexityCounter());
	}

	@Test
	public void testToString() {
		CoverageNodeImpl node = new CoverageNodeImpl(ElementType.CLASS, "Test");
		assertEquals("Test [CLASS]", node.toString());
	}

	/**
	 * Default behaviour is to only report as executed code that is executed.
	 */
	@Test
	public void testTreatAsFullyCoveredInitialValueIsFalse() {
		CoverageNodeImpl node = new CoverageNodeImpl(ElementType.CLASS, "Test");
		assertFalse(node.isTreatedAsFullyCovered());
	}

	@Test
	public void testTreatAsFullyCoveredCanBeSet() {
		CoverageNodeImpl node = new CoverageNodeImpl(ElementType.CLASS, "Test");
		node.setTreatAsFullyCovered(true);
		assertTrue(node.isTreatedAsFullyCovered());
	}

	@Test
	public void testToStringWhenTreatingAsFullyCovered() {
		CoverageNodeImpl node = new CoverageNodeImpl(ElementType.CLASS, "Test");
		node.setTreatAsFullyCovered(true);
		assertEquals("Test [CLASS TREAT_AS_FULLY_COVERED]", node.toString());
	}

	/**
	 * All branches are fully covered when the not is treated as fully covered,
	 * even if in reality some child branches are missed.
	 */
	@Test
	public void testCoveredCountIsNumberOfBranches() {
		CoverageNodeImpl parent = new CoverageNodeImpl(ElementType.GROUP,
				"sample");
		ICoverageNode child = new CoverageNodeImpl(ElementType.GROUP,
				"sample") {
			{
				instructionCounter = CounterImpl.getInstance(1, 41);
				branchCounter = CounterImpl.getInstance(10, 15);
				lineCounter = CounterImpl.getInstance(5, 3);
				complexityCounter = CounterImpl.getInstance(4, 2);
				methodCounter = CounterImpl.getInstance(1, 21);
				classCounter = CounterImpl.getInstance(1, 11);
			}
		};
		parent.setTreatAsFullyCovered(true);
		parent.increment(child);
		ICounter branch = parent.getCounter(BRANCH);
		assertEquals(ICounter.FULLY_COVERED, branch.getStatus());
		assertEquals(CounterImpl.getInstance(0, 25), branch);
	}
}
