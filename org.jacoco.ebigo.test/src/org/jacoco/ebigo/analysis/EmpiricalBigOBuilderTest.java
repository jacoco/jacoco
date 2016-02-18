/*******************************************************************************
 * Copyright (c) 2009, 2016 Mountainminds GmbH & Co. KG and Contributors
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
import org.jacoco.core.analysis.ISourceNode;
import org.jacoco.core.data.ProbeMode;
import org.jacoco.core.internal.analysis.ClassCoverageImpl;
import org.jacoco.core.internal.analysis.CounterImpl;
import org.jacoco.core.internal.analysis.MethodCoverageImpl;
import org.jacoco.ebigo.core.EmpiricalBigOWorkloadStore;
import org.jacoco.ebigo.core.WorkloadAttributeMapBuilder;
import org.jacoco.ebigo.fit.FitType;
import org.jacoco.ebigo.internal.analysis.ClassEmpiricalBigOImpl;
import org.junit.Test;

public class EmpiricalBigOBuilderTest {

	@Test
	public void constructor_nullFitTypeAndAttribute() {
		EmpiricalBigOBuilder instance = new EmpiricalBigOBuilder(null, null);
		assertArrayEquals(FitType.values(), instance.getFitTypes());
		assertEquals(WorkloadAttributeMapBuilder.DEFAULT_ATTRIBUTE,
				instance.getAttributeName());
	}

	@Test
	public void constructor_enptyFitType() {
		EmpiricalBigOBuilder instance = new EmpiricalBigOBuilder(
				new FitType[0], null);
		assertArrayEquals(FitType.values(), instance.getFitTypes());
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

		ClassCoverageImpl cc = new ClassCoverageImpl("Sample", 123L, false);
		cc.addMethod(createMethod(true));
		IClassCoverage[] ccs = new IClassCoverage[] { cc };
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

		ClassCoverageImpl cc = new ClassCoverageImpl("Sample", 123L, false);
		cc.addMethod(createMethod(true));
		IClassCoverage[] ccs = new IClassCoverage[] { cc };
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

	@Test(expected = IllegalStateException.class)
	public void testVisitEmpiricalBigO_addDifferentDup() {

		ClassCoverageImpl cc1 = new ClassCoverageImpl("Sample", 123L, false);
		cc1.addMethod(createMethod(true));
		IClassCoverage[] ccs1 = new IClassCoverage[] { cc1 };
		IClassEmpiricalBigO empiricalBigO1 = new ClassEmpiricalBigOImpl(ccs1);

		ClassCoverageImpl cc2 = new ClassCoverageImpl("Sample", 222L, false);
		cc2.addMethod(createMethod(true));
		IClassCoverage[] ccs2 = new IClassCoverage[] { cc2 };
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

	private MethodCoverageImpl createMethod(boolean covered) {
		final MethodCoverageImpl m = new MethodCoverageImpl("sample", "()V",
				null, ProbeMode.exists);
		m.increment(
				covered ? CounterImpl.COUNTER_0_1 : CounterImpl.COUNTER_1_0,
				CounterImpl.COUNTER_0_0, ISourceNode.UNKNOWN_LINE);
		m.incrementMethodCounter(0);
		return m;
	}

}
