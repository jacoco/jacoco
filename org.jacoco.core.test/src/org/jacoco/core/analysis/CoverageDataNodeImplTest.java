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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.jacoco.core.analysis.ICoverageDataNode.ElementType;
import org.junit.Test;

/**
 * Unit tests for {@link CoverageDataNodeImpl}.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public class CoverageDataNodeImplTest {

	@Test
	public void testInit1() {
		ICoverageDataNode node = new CoverageDataNodeImpl(ElementType.CLASS,
				"myname", false);
		assertEquals(ElementType.CLASS, node.getElementType());
		assertEquals("myname", node.getName());
		assertTrue(node.getChilden().isEmpty());
		assertEquals(0, node.getBlockCounter().getTotalCount(), 0.0);
		assertEquals(0, node.getBlockCounter().getCoveredCount(), 0.0);
		assertEquals(0, node.getInstructionCounter().getTotalCount(), 0.0);
		assertEquals(0, node.getInstructionCounter().getCoveredCount(), 0.0);
		assertEquals(0, node.getLineCounter().getTotalCount(), 0.0);
		assertEquals(0, node.getLineCounter().getCoveredCount(), 0.0);
		assertNull(node.getLines());
		assertEquals(0, node.getMethodCounter().getTotalCount(), 0.0);
		assertEquals(0, node.getMethodCounter().getCoveredCount(), 0.0);
		assertEquals(0, node.getClassCounter().getTotalCount(), 0.0);
		assertEquals(0, node.getClassCounter().getCoveredCount(), 0.0);
	}

	@Test
	public void testInit2() {
		ICoverageDataNode node = new CoverageDataNodeImpl(ElementType.CLASS,
				"myname", true);
		assertEquals(ElementType.CLASS, node.getElementType());
		assertEquals("myname", node.getName());
		assertTrue(node.getChilden().isEmpty());
		assertEquals(0, node.getBlockCounter().getTotalCount(), 0.0);
		assertEquals(0, node.getBlockCounter().getCoveredCount(), 0.0);
		assertEquals(0, node.getInstructionCounter().getTotalCount(), 0.0);
		assertEquals(0, node.getInstructionCounter().getCoveredCount(), 0.0);
		assertEquals(0, node.getLineCounter().getTotalCount(), 0.0);
		assertEquals(0, node.getLineCounter().getCoveredCount(), 0.0);
		assertEquals(-1, node.getLines().getFirstLine(), 0.0);
		assertEquals(-1, node.getLines().getLastLine(), 0.0);
		assertEquals(0, node.getMethodCounter().getTotalCount(), 0.0);
		assertEquals(0, node.getMethodCounter().getCoveredCount(), 0.0);
		assertEquals(0, node.getClassCounter().getTotalCount(), 0.0);
		assertEquals(0, node.getClassCounter().getCoveredCount(), 0.0);
	}

	@Test
	public void testAdd1() {
		CoverageDataNodeImpl node = new CoverageDataNodeImpl(
				ElementType.CUSTOM, "myname", false);
		ICoverageDataNode child = new CoverageDataNodeImpl(ElementType.CLASS,
				"myname", true) {
			{
				instructionCounter = CounterImpl.getInstance(42, 41);
				blockCounter = CounterImpl.getInstance(32, 31);
				lines.increment(new int[] { 1, 2, 3, 4, 5 }, false);
				lines.increment(new int[] { 6, 7, 8 }, true);
				methodCounter = CounterImpl.getInstance(22, 21);
				classCounter = CounterImpl.getInstance(12, 11);
			}
		};
		node.add(child);
		assertEquals(Collections.singletonList(child), node.getChilden());
		assertEquals(42, node.getInstructionCounter().getTotalCount(), 0.0);
		assertEquals(41, node.getInstructionCounter().getCoveredCount(), 0.0);
		assertEquals(32, node.getBlockCounter().getTotalCount(), 0.0);
		assertEquals(31, node.getBlockCounter().getCoveredCount(), 0.0);
		assertEquals(8, node.getLineCounter().getTotalCount(), 0.0);
		assertEquals(3, node.getLineCounter().getCoveredCount(), 0.0);
		assertEquals(22, node.getMethodCounter().getTotalCount(), 0.0);
		assertEquals(21, node.getMethodCounter().getCoveredCount(), 0.0);
		assertEquals(12, node.getClassCounter().getTotalCount(), 0.0);
		assertEquals(11, node.getClassCounter().getCoveredCount(), 0.0);
	}

	@Test
	public void testAdd() {
		CoverageDataNodeImpl node = new CoverageDataNodeImpl(
				ElementType.CUSTOM, "myname", true);
		ICoverageDataNode child = new CoverageDataNodeImpl(ElementType.CLASS,
				"myname", true) {
			{
				instructionCounter = CounterImpl.getInstance(42, 41);
				blockCounter = CounterImpl.getInstance(32, 31);
				lines.increment(new int[] { 1, 2 }, false);
				lines.increment(new int[] { 3, 4 }, true);
				methodCounter = CounterImpl.getInstance(22, 21);
				classCounter = CounterImpl.getInstance(12, 11);
			}
		};
		node.add(child);
		assertEquals(Collections.singletonList(child), node.getChilden());
		assertEquals(42, node.getInstructionCounter().getTotalCount(), 0.0);
		assertEquals(41, node.getInstructionCounter().getCoveredCount(), 0.0);
		assertEquals(32, node.getBlockCounter().getTotalCount(), 0.0);
		assertEquals(31, node.getBlockCounter().getCoveredCount(), 0.0);
		assertEquals(4, node.getLineCounter().getTotalCount(), 0.0);
		assertEquals(2, node.getLineCounter().getCoveredCount(), 0.0);
		assertEquals(ILines.NOT_COVERED, node.getLines().getStatus(1));
		assertEquals(ILines.NOT_COVERED, node.getLines().getStatus(2));
		assertEquals(ILines.FULLY_COVERED, node.getLines().getStatus(3));
		assertEquals(ILines.FULLY_COVERED, node.getLines().getStatus(4));
		assertEquals(22, node.getMethodCounter().getTotalCount(), 0.0);
		assertEquals(21, node.getMethodCounter().getCoveredCount(), 0.0);
		assertEquals(12, node.getClassCounter().getTotalCount(), 0.0);
		assertEquals(11, node.getClassCounter().getCoveredCount(), 0.0);
	}

	@Test
	public void testAddAll() {
		CoverageDataNodeImpl node = new CoverageDataNodeImpl(
				ElementType.CUSTOM, "myname", false);
		ICoverageDataNode child1 = new CoverageDataNodeImpl(ElementType.CLASS,
				"myname", false);
		ICoverageDataNode child2 = new CoverageDataNodeImpl(ElementType.CLASS,
				"myname", false);
		final List<ICoverageDataNode> chidren = Arrays
				.asList(new ICoverageDataNode[] { child1, child2 });
		node.addAll(chidren);
		assertEquals(chidren, node.getChilden());
	}

}
