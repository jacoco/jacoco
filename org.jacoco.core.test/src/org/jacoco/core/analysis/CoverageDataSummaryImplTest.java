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

import static org.jacoco.core.analysis.ICoverageDataSummary.CounterEntity.BLOCK;
import static org.jacoco.core.analysis.ICoverageDataSummary.CounterEntity.CLASS;
import static org.jacoco.core.analysis.ICoverageDataSummary.CounterEntity.INSTRUCTION;
import static org.jacoco.core.analysis.ICoverageDataSummary.CounterEntity.LINE;
import static org.jacoco.core.analysis.ICoverageDataSummary.CounterEntity.METHOD;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Test;

/**
 * Unit tests for {@link CoverageDataSummaryImpl}.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public class CoverageDataSummaryImplTest {

	@Test
	public void testInit() {
		ICoverageDataSummary sum = new CoverageDataSummaryImpl();
		assertEquals(0, sum.getBlockCounter().getTotalCount(), 0.0);
		assertEquals(0, sum.getBlockCounter().getCoveredCount(), 0.0);
		assertEquals(0, sum.getInstructionCounter().getTotalCount(), 0.0);
		assertEquals(0, sum.getInstructionCounter().getCoveredCount(), 0.0);
		assertEquals(0, sum.getLineCounter().getTotalCount(), 0.0);
		assertEquals(0, sum.getLineCounter().getCoveredCount(), 0.0);
		assertEquals(0, sum.getMethodCounter().getTotalCount(), 0.0);
		assertEquals(0, sum.getMethodCounter().getCoveredCount(), 0.0);
		assertEquals(0, sum.getClassCounter().getTotalCount(), 0.0);
		assertEquals(0, sum.getClassCounter().getCoveredCount(), 0.0);
	}

	@Test
	public void testAdd() {
		CoverageDataSummaryImpl sum = new CoverageDataSummaryImpl();
		ICoverageDataSummary child = new CoverageDataSummaryImpl() {
			{
				instructionCounter = CounterImpl.getInstance(42, 41);
				blockCounter = CounterImpl.getInstance(32, 31);
				lineCounter = CounterImpl.getInstance(8, 3);
				methodCounter = CounterImpl.getInstance(22, 21);
				classCounter = CounterImpl.getInstance(12, 11);
			}
		};
		sum.add(child);
		assertEquals(42, sum.getCounter(INSTRUCTION).getTotalCount(), 0.0);
		assertEquals(42, sum.getInstructionCounter().getTotalCount(), 0.0);
		assertEquals(41, sum.getInstructionCounter().getCoveredCount(), 0.0);
		assertEquals(32, sum.getCounter(BLOCK).getTotalCount(), 0.0);
		assertEquals(32, sum.getBlockCounter().getTotalCount(), 0.0);
		assertEquals(31, sum.getBlockCounter().getCoveredCount(), 0.0);
		assertEquals(8, sum.getCounter(LINE).getTotalCount(), 0.0);
		assertEquals(8, sum.getLineCounter().getTotalCount(), 0.0);
		assertEquals(3, sum.getLineCounter().getCoveredCount(), 0.0);
		assertEquals(22, sum.getCounter(METHOD).getTotalCount(), 0.0);
		assertEquals(22, sum.getMethodCounter().getTotalCount(), 0.0);
		assertEquals(21, sum.getMethodCounter().getCoveredCount(), 0.0);
		assertEquals(12, sum.getCounter(CLASS).getTotalCount(), 0.0);
		assertEquals(12, sum.getClassCounter().getTotalCount(), 0.0);
		assertEquals(11, sum.getClassCounter().getCoveredCount(), 0.0);
	}

	@Test
	public void testAddSummaries() {
		CoverageDataSummaryImpl sum = new CoverageDataSummaryImpl();
		ICoverageDataSummary child1 = new CoverageDataSummaryImpl() {
			{
				blockCounter = CounterImpl.getInstance(5, 2);
			}
		};
		ICoverageDataSummary child2 = new CoverageDataSummaryImpl() {
			{
				blockCounter = CounterImpl.getInstance(3, 3);
			}
		};
		sum.addSummaries(Arrays.asList(child1, child2));
		assertEquals(8, sum.getBlockCounter().getTotalCount(), 0.0);
		assertEquals(5, sum.getBlockCounter().getCoveredCount(), 0.0);
	}

}
