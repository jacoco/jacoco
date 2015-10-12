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
package org.jacoco.core.analysis;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class EBigOFunctionTest {
	@Test(expected = IllegalArgumentException.class)
	public void testConstructNullType() {
		new EBigOFunction(null, 0, 0);
	}

	@Test
	public void testConstruction() {
		final EBigOFunction instance = new EBigOFunction(
				EBigOFunction.Type.Exponential, 4.33D, 6.77D);
		assertEquals(EBigOFunction.Type.Exponential, instance.getType());
		assertEquals(4.33D, instance.getSlope(), 0.000001D);
		assertEquals(6.77D, instance.getIntercept(), 0.000001D);
	}

	@Test
	public void testGetOrderOfMagnitude_logSlopeZero() {
		final EBigOFunction instance = new EBigOFunction(
				EBigOFunction.Type.Logarithmic, 0D, 6.77D);
		assertEquals("1", instance.getOrderOfMagnitude());
	}

	@Test
	public void testGetOrderOfMagnitude_logSlopeOther() {
		final EBigOFunction instance = new EBigOFunction(
				EBigOFunction.Type.Logarithmic, 4.33D, 6.77D);
		assertEquals("log(n)", instance.getOrderOfMagnitude());
	}

	@Test
	public void testGetOrderOfMagnitude_linearSlopeZero() {
		final EBigOFunction instance = new EBigOFunction(
				EBigOFunction.Type.Linear, 0D, 6.77D);
		assertEquals("1", instance.getOrderOfMagnitude());
	}

	@Test
	public void testGetOrderOfMagnitude_linearSlopeOther() {
		final EBigOFunction instance = new EBigOFunction(
				EBigOFunction.Type.Linear, 4.33D, 6.77D);
		assertEquals("n", instance.getOrderOfMagnitude());
	}

	@Test
	public void testGetOrderOfMagnitude_powerlawSlopeZero() {
		final EBigOFunction instance = new EBigOFunction(
				EBigOFunction.Type.PowerLaw, 0D, 6.77D);
		assertEquals("1", instance.getOrderOfMagnitude());
	}

	@Test
	public void testGetOrderOfMagnitude_powerlawInterceptZero() {
		final EBigOFunction instance = new EBigOFunction(
				EBigOFunction.Type.PowerLaw, 4.33D, 0D);
		assertEquals("1", instance.getOrderOfMagnitude());
	}

	@Test
	public void testGetOrderOfMagnitude_powerlawSlopeOne() {
		final EBigOFunction instance = new EBigOFunction(
				EBigOFunction.Type.PowerLaw, 1D, 6.77D);
		assertEquals("n", instance.getOrderOfMagnitude());
	}

	@Test
	public void testGetOrderOfMagnitude_powerlawOther() {
		final EBigOFunction instance = new EBigOFunction(
				EBigOFunction.Type.PowerLaw, 4.33D, 6.77D);
		assertEquals("n^4.33 ", instance.getOrderOfMagnitude());
	}

	@Test
	public void testGetOrderOfMagnitude_expSlopeZero() {
		final EBigOFunction instance = new EBigOFunction(
				EBigOFunction.Type.Exponential, 0D, 6.77D);
		assertEquals("1", instance.getOrderOfMagnitude());
	}

	@Test
	public void testGetOrderOfMagnitude_expInterceptZero() {
		final EBigOFunction instance = new EBigOFunction(
				EBigOFunction.Type.Exponential, 4.33D, 0D);
		assertEquals("1", instance.getOrderOfMagnitude());
	}

	@Test
	public void testGetOrderOfMagnitude_expOther() {
		final EBigOFunction instance = new EBigOFunction(
				EBigOFunction.Type.Exponential, 4.33D, 6.77D);
		assertEquals("2^n", instance.getOrderOfMagnitude());
	}

	@Test
	public void testEquals_toNull() {
		final EBigOFunction instance = new EBigOFunction(
				EBigOFunction.Type.Exponential, 4.33D, 6.77D);
		assertFalse(instance.equals(null));
	}

	@Test
	public void testEquals_toSelf() {
		final EBigOFunction instance = new EBigOFunction(
				EBigOFunction.Type.Exponential, 4.33D, 6.77D);
		assertTrue(instance.equals(instance));
		assertEquals(instance.hashCode(), instance.hashCode());
		assertEquals(0, instance.compareTo(instance));
	}

	@Test
	public void testEquals_toObject() {
		final EBigOFunction instance = new EBigOFunction(
				EBigOFunction.Type.Exponential, 4.33D, 6.77D);
		assertFalse(instance.equals(new Object()));
	}

	@Test
	public void testEquals_differentType() {
		final EBigOFunction instance = new EBigOFunction(
				EBigOFunction.Type.Exponential, 4.33D, 6.77D);
		final EBigOFunction other = new EBigOFunction(
				EBigOFunction.Type.Linear, 4.33D, 6.77D);
		assertFalse(instance.equals(other));
	}

	@Test
	public void testEquals_differentSlope() {
		final EBigOFunction instance = new EBigOFunction(
				EBigOFunction.Type.Exponential, 4.33D, 6.77D);
		final EBigOFunction other = new EBigOFunction(
				EBigOFunction.Type.Exponential, 4.34D, 6.77D);
		assertFalse(instance.equals(other));
	}

	@Test
	public void testEquals_differentIntercept() {
		final EBigOFunction instance = new EBigOFunction(
				EBigOFunction.Type.Exponential, 4.33D, 6.77D);
		final EBigOFunction other = new EBigOFunction(
				EBigOFunction.Type.Exponential, 4.33D, 6.78D);
		assertFalse(instance.equals(other));
	}

	@Test
	public void testEquals_equals() {
		final EBigOFunction instance = new EBigOFunction(
				EBigOFunction.Type.Exponential, 4.33D, 6.77D);
		final EBigOFunction other = new EBigOFunction(
				EBigOFunction.Type.Exponential, 4.33D, 6.77D);
		assertTrue(instance.equals(other));
		assertEquals(instance.hashCode(), other.hashCode());
		assertEquals(0, instance.compareTo(other));
	}

	@Test
	public void testCompareTo_equalsDifferentSlope() {
		final EBigOFunction instance = new EBigOFunction(
				EBigOFunction.Type.Logarithmic, 4.33D, 6.77D);
		final EBigOFunction other = new EBigOFunction(
				EBigOFunction.Type.Logarithmic, 1.33D, 1.77D);
		assertTrue(instance.compareTo(other) == 0);
	}

	@Test
	public void testCompareTo_less0() {
		final EBigOFunction instance = new EBigOFunction(
				EBigOFunction.Type.Logarithmic, 4.33D, 6.77D);
		final EBigOFunction other = null;
		assertTrue(instance.compareTo(other) < 0);
	}

	@Test
	public void testCompareTo_less1() {
		final EBigOFunction instance = new EBigOFunction(
				EBigOFunction.Type.Logarithmic, 4.33D, 6.77D);
		final EBigOFunction other = new EBigOFunction(
				EBigOFunction.Type.Linear, 4.33D, 1.77D);
		assertTrue(instance.compareTo(other) < 0);
	}

	@Test
	public void testCompareTo_less2() {
		final EBigOFunction instance = new EBigOFunction(
				EBigOFunction.Type.Linear, 4.33D, 6.77D);
		final EBigOFunction other = new EBigOFunction(
				EBigOFunction.Type.PowerLaw, 4.33D, 1.77D);
		assertTrue(instance.compareTo(other) < 0);
	}

	@Test
	public void testCompareTo_less3() {
		final EBigOFunction instance = new EBigOFunction(
				EBigOFunction.Type.PowerLaw, 4.33D, 6.77D);
		final EBigOFunction other = new EBigOFunction(
				EBigOFunction.Type.Exponential, 4.33D, 1.77D);
		assertTrue(instance.compareTo(other) < 0);
	}

	@Test
	public void testCompareTo_lessDifferentSlope() {
		final EBigOFunction instance = new EBigOFunction(
				EBigOFunction.Type.Exponential, 1.33D, 6.77D);
		final EBigOFunction other = new EBigOFunction(
				EBigOFunction.Type.Exponential, 4.33D, 1.77D);
		assertTrue(instance.compareTo(other) < 0);
	}

	@Test
	public void testToString() {
		final EBigOFunction instance = new EBigOFunction(
				EBigOFunction.Type.Exponential, 1.33D, 6.77D);
		assertEquals(
				"EBigOFunction [type=Exponential, slope=1.33, intercept=6.77]",
				instance.toString());
	}
}