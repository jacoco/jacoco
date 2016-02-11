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
package org.jacoco.ebigo.fit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Constructor;
import java.util.Iterator;
import java.util.SortedSet;

import org.junit.Test;

public class FitCalculatorTest {
	private static final double DELTA = 0.0001;

	@Test
	public void testDefaultConstructor() throws Exception {
		Constructor<FitCalculator> constructor = FitCalculator.class
				.getDeclaredConstructor();
		constructor.setAccessible(true);
		constructor.newInstance();
		// Does not throw is all we test
	}

	@Test(expected = IllegalArgumentException.class)
	public void tooFewXs() {
		FitCalculator.calcSingleFit(FitType.Linear, new int[] { 1 },
				new int[] { 3 });
	}

	@Test(expected = IllegalArgumentException.class)
	public void mismatchXsAndYs() {
		FitCalculator.calcSingleFit(FitType.Linear, new int[] { 1, 2, 3, 4 },
				new int[] { 3, 6, 9, 12, 15 });
	}

	@Test
	public void tooFewGoodPoints1() {
		Fit result = FitCalculator.calcSingleFit(FitType.Linear, new int[] { 2,
				1, 0, -1 }, new int[] { -1, 0, 1, 2 });
		assertEquals(0, result.n);
	}

	@Test
	public void tooFewGoodPoints2() {
		Fit result = FitCalculator.calcSingleFit(FitType.PowerLaw, new int[] {
				2, 1, 0, -1 }, new int[] { -1, 0, 1, 2 });
		assertEquals(0, result.n);
	}

	@Test
	public void noChangeInX() {
		Fit fit = FitCalculator.calcSingleFit(FitType.PowerLaw, new int[] { 1,
				1, 1, 1 }, new int[] { 1, 1, 1, 1 });
		assertEquals(0, fit.n);
	}

	@Test
	public void testLogRegression() {
		final int[] X_VALUES = { 1, 2, 3, 4, 5 };
		final int[] Y_VALUES = { 3, 9, 19, 33, 51 };

		Fit fit = FitCalculator.calcSingleFit(FitType.Logarithmic, X_VALUES,
				Y_VALUES);
		assertEquals(FitType.Logarithmic, fit.type);
		assertEquals(5, fit.n);
		assertEquals(27.749304290636864, fit.slope, DELTA);
		assertEquals(0.02815830244860937, fit.intercept, DELTA);
		assertEquals(0.8315274811090818, fit.rSquared, DELTA);
		assertEquals(22.94979380895077, fit.confidence, DELTA);
	}

	@Test
	public void testLinearRegression() {
		final int[] X_VALUES = { 1, 2, 3, 4, 5 };
		final int[] Y_VALUES = { 3, 9, 19, 33, 51 };

		Fit fit = FitCalculator.calcSingleFit(FitType.Linear, X_VALUES,
				Y_VALUES);
		assertEquals(FitType.Linear, fit.type);
		assertEquals(5, fit.n);
		assertEquals(12.0, fit.slope, DELTA);
		assertEquals(-13.0, fit.intercept, DELTA);
		assertEquals(0.9625668449197861, fit.rSquared, DELTA);
		assertEquals(4.348049413491281, fit.confidence, DELTA);
	}

	@Test
	public void testPowerLawRegression() {
		final int[] X_VALUES = { 1, 2, 3, 4, 5 };
		final int[] Y_VALUES = { 3, 9, 19, 33, 51 };

		Fit fit = FitCalculator.calcSingleFit(FitType.PowerLaw, X_VALUES,
				Y_VALUES);
		assertEquals(FitType.PowerLaw, fit.type);
		assertEquals(5, fit.n);
		assertEquals(1.7612897777638985, fit.slope, DELTA);
		assertEquals(2.8499166673709104, fit.intercept, DELTA);
		assertEquals(0.9976840432107573, fit.rSquared, DELTA);
		assertEquals(0.15591933899494556, fit.confidence, DELTA);
	}

	@Test
	public void testExpRegression() {
		final int[] X_VALUES = { 1, 2, 3, 4, 5 };
		final int[] Y_VALUES = { 3, 9, 19, 33, 51 };

		Fit fit = FitCalculator.calcSingleFit(FitType.Exponential, X_VALUES,
				Y_VALUES);
		assertEquals(FitType.Exponential, fit.type);
		assertEquals(5, fit.n);
		assertEquals(0.6965709672242694, fit.slope, DELTA);
		assertEquals(0.6440089061995071, fit.intercept, DELTA);
		assertEquals(0.965957756283913, fit.rSquared, DELTA);
		assertEquals(0.24026795282913407, fit.confidence, DELTA);
	}

	@Test
	public void testMultiFitTypeRegressions() {
		final int[] X_VALUES = { 1, 2, 3, 4, 5 };
		final int[] Y_VALUES = { 3, 9, 19, 33, 51 };

		SortedSet<Fit> fitSet = FitCalculator.calcFitSet(FitType.values(),
				X_VALUES, Y_VALUES);
		Iterator<Fit> iterator = fitSet.iterator();
		assertEquals(FitType.PowerLaw, iterator.next().type);
		assertEquals(FitType.Exponential, iterator.next().type);
		assertEquals(FitType.Linear, iterator.next().type);
		assertEquals(FitType.Logarithmic, iterator.next().type);
		assertFalse(iterator.hasNext());
	}

	@Test
	public void testMultiFitTypeRegressions_noGoodPoints() {
		final int[] X_VALUES = { 1, 1, 1, 1, 1 };
		final int[] Y_VALUES = { 1, 1, 1, 1, 1 };

		SortedSet<Fit> fitSet = FitCalculator.calcFitSet(FitType.values(),
				X_VALUES, Y_VALUES);
		assertTrue(fitSet.isEmpty());
	}
}
