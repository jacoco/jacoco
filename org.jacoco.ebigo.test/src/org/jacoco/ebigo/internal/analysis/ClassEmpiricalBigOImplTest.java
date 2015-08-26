/*******************************************************************************
 * Copyright (c) 2009, 2015 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Omer Azmon - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.ebigo.internal.analysis;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.internal.analysis.ClassCoverageImpl;
import org.jacoco.core.internal.analysis.CounterImpl;
import org.jacoco.ebigo.fit.Fit;
import org.jacoco.ebigo.fit.FitType;
import org.junit.Test;

public class ClassEmpiricalBigOImplTest {

	@Test
	public void testConstructor() {
		IClassCoverage[] ccs = new IClassCoverage[0];
		ClassEmpiricalBigOImpl instance = new ClassEmpiricalBigOImpl(ccs);
		assertSame(ccs, instance.getMatchedCoverageClasses());
		assertNull(instance.getLineFits());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testConstructorWithMismatchLines() {
		IClassCoverage[] ccs = new IClassCoverage[2];
		ccs[0] = makeEmptyClassCoverage(123L, "Sample", false);
		ccs[1] = makeClassCoverage(123L, "Sample", 2, false);
		ClassEmpiricalBigOImpl instance = new ClassEmpiricalBigOImpl(ccs);
	}

	@Test
	public void testEmptyAnalysis() {
		IClassCoverage[] ccs = new IClassCoverage[0];
		ClassEmpiricalBigOImpl instance = new ClassEmpiricalBigOImpl(ccs);
		boolean result = instance.analyze(FitType.values(), new int[0]);
		assertFalse(result);
		assertEquals(0, instance.getLineFits().length);
	}

	@Test
	public void testEmptySampleAnalysis() {
		IClassCoverage[] ccs = new IClassCoverage[5];
		ccs[0] = makeEmptyClassCoverage(123L, "Sample", false);
		ccs[1] = makeEmptyClassCoverage(123L, "Sample", false);
		ccs[2] = makeEmptyClassCoverage(123L, "Sample", false);
		ccs[3] = makeEmptyClassCoverage(123L, "Sample", false);
		ccs[4] = makeEmptyClassCoverage(123L, "Sample", false);
		int[] xValues = new int[] { 1, 2, 3, 4, 5 };

		ClassEmpiricalBigOImpl instance = new ClassEmpiricalBigOImpl(ccs);
		boolean result = instance.analyze(FitType.values(), xValues);
		assertFalse(result);
		assertEquals(0, instance.getLineFits().length);
	}

	@Test
	public void testMultiSampleAnalysis() {
		IClassCoverage[] ccs = new IClassCoverage[5];
		ccs[0] = makeClassCoverage(123L, "Sample", 1, false);
		ccs[1] = makeClassCoverage(123L, "Sample", 2, false);
		ccs[2] = makeClassCoverage(123L, "Sample", 3, false);
		ccs[3] = makeClassCoverage(123L, "Sample", 4, false);
		ccs[4] = makeClassCoverage(123L, "Sample", 5, false);
		int[] xValues = new int[] { 1, 2, 3, 4, 5 };

		ClassEmpiricalBigOImpl instance = new ClassEmpiricalBigOImpl(ccs);
		boolean result = instance.analyze(FitType.values(), xValues);
		assertTrue(result);
		assertEquals(3, instance.getLineFits().length);
		Fit[] lineFits = instance.getLineFits();
		assertEquals(FitType.Linear, lineFits[0].type);
		assertEquals(5, lineFits[0].n);
		assertEquals(1, lineFits[0].slope, 0.00001);
		assertEquals(0, lineFits[0].intercept, 0.00001);
		assertNull(lineFits[2]);
	}

	private ClassCoverageImpl makeEmptyClassCoverage(long id, String name,
			boolean nomatch) {
		final ClassCoverageImpl coverage = new ClassCoverageImpl(name, id,
				nomatch, null, "java/lang/Object", new String[0]);
		coverage.setSourceFileName(null);
		return coverage;
	}

	private ClassCoverageImpl makeClassCoverage(long id, String name, int hits,
			boolean nomatch) {
		final ClassCoverageImpl coverage = new ClassCoverageImpl(name, id,
				nomatch, null, "java/lang/Object", new String[0]);
		coverage.increment(CounterImpl.getInstance(0, 1, hits),
				CounterImpl.COUNTER_0_1, 6);
		coverage.increment(CounterImpl.getInstance(0, 1, hits),
				CounterImpl.COUNTER_0_1, 7);
		coverage.increment(CounterImpl.getInstance(0, 1, 0),
				CounterImpl.COUNTER_0_1, 8);
		coverage.setSourceFileName(null);
		return coverage;
	}

}
