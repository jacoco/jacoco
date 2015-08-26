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
package org.jacoco.ebigo.analysis;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.internal.analysis.ClassCoverageImpl;
import org.jacoco.ebigo.core.EmpiricalBigOWorkloadStore;
import org.jacoco.ebigo.fit.FitType;
import org.jacoco.ebigo.internal.analysis.ClassEmpiricalBigOImpl;
import org.junit.Test;

public class EmpiricalBigOBuilderTest {

	@Test(expected = IllegalArgumentException.class)
	public void constructor_nullFitType() {
		new EmpiricalBigOBuilder(null, null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void constructor_enptyFitType() {
		new EmpiricalBigOBuilder(new FitType[0], null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void constructor_nullAttribute() {
		new EmpiricalBigOBuilder(FitType.values(), null);
	}

	@Test
	public void constructor() {
		EmpiricalBigOBuilder instance = new EmpiricalBigOBuilder(
				FitType.values(), "ATTRIBUTE");
		assertArrayEquals(FitType.values(), instance.getFitTypes());
		assertEquals("ATTRIBUTE", instance.getAttributeName());
		assertNull(instance.getXAxisValues());
		assertTrue(instance.getClasses().isEmpty());
	}

	@Test
	public void testVisitXAxis() {
		EmpiricalBigOWorkloadStore store = new EmpiricalBigOWorkloadStore(
				"ATTRIBUTE");
		XAxisValues xAxisValues = new XAxisValues(store, "ATTRIBUTE");
		EmpiricalBigOBuilder instance = new EmpiricalBigOBuilder(
				FitType.values(), "ATTRIBUTE");

		instance.visitXAxis(xAxisValues);

		assertArrayEquals(FitType.values(), instance.getFitTypes());
		assertEquals("ATTRIBUTE", instance.getAttributeName());
		assertSame(xAxisValues, instance.getXAxisValues());
		assertTrue(instance.getClasses().isEmpty());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testVisitXAxis_null() {
		EmpiricalBigOWorkloadStore store = new EmpiricalBigOWorkloadStore(
				"ATTRIBUTE");
		XAxisValues xAxisValues = new XAxisValues(store, "ATTRIBUTE");
		EmpiricalBigOBuilder instance = new EmpiricalBigOBuilder(
				FitType.values(), "ATTRIBUTE");

		instance.visitXAxis(null);
	}

	@Test(expected = IllegalStateException.class)
	public void testVisitXAxisTwice() {
		EmpiricalBigOWorkloadStore store = new EmpiricalBigOWorkloadStore(
				"ATTRIBUTE");
		XAxisValues xAxisValues = new XAxisValues(store, "ATTRIBUTE");
		EmpiricalBigOBuilder instance = new EmpiricalBigOBuilder(
				FitType.values(), "ATTRIBUTE");

		instance.visitXAxis(xAxisValues);
		instance.visitXAxis(xAxisValues);
	}

	@Test
	public void testVisitEmpiricalBigO_emptyClassList() {
		IClassEmpiricalBigO empiricalBigO = new ClassEmpiricalBigOImpl(
				new IClassCoverage[0]);
		EmpiricalBigOBuilder instance = new EmpiricalBigOBuilder(
				FitType.values(), "ATTRIBUTE");

		instance.visitEmpiricalBigO(empiricalBigO);

		assertArrayEquals(FitType.values(), instance.getFitTypes());
		assertEquals("ATTRIBUTE", instance.getAttributeName());
		assertNull(instance.getXAxisValues());
		assertTrue(instance.getClasses().isEmpty());

	}

	@Test
	public void testVisitEmpiricalBigO_hasClassList() {
		IClassCoverage[] ccs = new IClassCoverage[1];
		ccs[0] = new ClassCoverageImpl("Sample", 123L, false, "", null, null);
		IClassEmpiricalBigO empiricalBigO = new ClassEmpiricalBigOImpl(ccs);
		EmpiricalBigOBuilder instance = new EmpiricalBigOBuilder(
				FitType.values(), "ATTRIBUTE");

		instance.visitEmpiricalBigO(empiricalBigO);

		assertArrayEquals(FitType.values(), instance.getFitTypes());
		assertEquals("ATTRIBUTE", instance.getAttributeName());
		assertNull(instance.getXAxisValues());
		assertEquals(1, instance.getClasses().size());

	}

	@Test
	public void testVisitEmpiricalBigO_addExactDup() {
		IClassCoverage[] ccs = new IClassCoverage[1];
		ccs[0] = new ClassCoverageImpl("Sample", 123L, false, "", null, null);
		IClassEmpiricalBigO empiricalBigO = new ClassEmpiricalBigOImpl(ccs);
		EmpiricalBigOBuilder instance = new EmpiricalBigOBuilder(
				FitType.values(), "ATTRIBUTE");

		instance.visitEmpiricalBigO(empiricalBigO);
		instance.visitEmpiricalBigO(empiricalBigO);

		assertArrayEquals(FitType.values(), instance.getFitTypes());
		assertEquals("ATTRIBUTE", instance.getAttributeName());
		assertNull(instance.getXAxisValues());
		assertEquals(1, instance.getClasses().size());

	}

	@Test(expected = IllegalArgumentException.class)
	public void testVisitEmpiricalBigO_addDifferentDup() {
		IClassCoverage[] ccs1 = new IClassCoverage[1];
		ccs1[0] = new ClassCoverageImpl("Sample", 123L, false, "", null, null);
		IClassEmpiricalBigO empiricalBigO1 = new ClassEmpiricalBigOImpl(ccs1);

		IClassCoverage[] ccs2 = new IClassCoverage[1];
		ccs2[0] = new ClassCoverageImpl("Sample", 222L, false, "", null, null);
		IClassEmpiricalBigO empiricalBigO2 = new ClassEmpiricalBigOImpl(ccs2);

		EmpiricalBigOBuilder instance = new EmpiricalBigOBuilder(
				FitType.values(), "ATTRIBUTE");

		instance.visitEmpiricalBigO(empiricalBigO1);
		instance.visitEmpiricalBigO(empiricalBigO2);

		assertArrayEquals(FitType.values(), instance.getFitTypes());
		assertEquals("ATTRIBUTE", instance.getAttributeName());
		assertNull(instance.getXAxisValues());
		assertEquals(1, instance.getClasses().size());

	}
}
