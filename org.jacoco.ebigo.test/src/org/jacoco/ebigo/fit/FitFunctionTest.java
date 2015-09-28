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
package org.jacoco.ebigo.fit;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class FitFunctionTest {

	private final FitType fitType;
	private final double slope;
	private final double intercept;
	private final String expectedFitFunction;
	private final String expectedOrderFunction;

	@SuppressWarnings("boxing")
	@Parameters
	public static Collection<?> data() {
		return Arrays.asList(new Object[][] { //
						{ FitType.Logarithmic, 0, 0, "0.00", "1" }, //
						{ FitType.Logarithmic, 1, 0, "ln(x)", "ln(n)" }, //
						{ FitType.Logarithmic, 2, 0, "2.00 ln(x)", "ln(n)" }, //
						{ FitType.Logarithmic, 0, 1, "1.00", "1" }, //
						{ FitType.Logarithmic, 1, 1, "1.00 + ln(x)", "ln(n)" }, //
						{ FitType.Logarithmic, 2, 1, "1.00 + 2.00 ln(x)",
								"ln(n)" }, //
						{ FitType.Linear, 0, 0, "0.00", "1" }, //
						{ FitType.Linear, 1, 0, "x", "n" }, //
						{ FitType.Linear, 2, 0, "2.00x", "n" }, //
						{ FitType.Linear, 0, 1, "1.00", "1" }, //
						{ FitType.Linear, 1, 1, "1.00 + x", "n" }, //
						{ FitType.Linear, 2, 1, "1.00 + 2.00x", "n" }, //
						{ FitType.PowerLaw, 0, 0, "0.00", "1" }, //
						{ FitType.PowerLaw, 0, 1, "1.00", "1" }, //
						{ FitType.PowerLaw, 1, 1, "x", "n" }, //
						{ FitType.PowerLaw, 1, 2, "2.00x", "n" }, //
						{ FitType.PowerLaw, 2, 1, "x^2.00", "n^2.00" }, //
						{ FitType.PowerLaw, 2, 2, "2.00x^2.00", "n^2.00" }, //
						{ FitType.Exponential, 0, 0, "0.00", "1" }, //
						{ FitType.Exponential, 0, 1, "1.00", "1" }, //
						{ FitType.Exponential, 0, 2, "2.00", "1" }, //
						{ FitType.Exponential, 1, 1, "e^x", "e^n" }, //
						{ FitType.Exponential, 1, 2, "2.00e^x", "e^n" }, //
						{ FitType.Exponential, 2, 1, "e^(2.00x)", "e^n" }, //
						{ FitType.Exponential, 2, 2, "2.00e^(2.00x)", "e^n" }, //
				});
	}

	public FitFunctionTest(final FitType fitType, final double slope,
			final double intercept, final String expectedFitFunction,
			final String expectedOrderFunction) {
		this.fitType = fitType;
		this.slope = slope;
		this.intercept = intercept;
		this.expectedFitFunction = expectedFitFunction;
		this.expectedOrderFunction = expectedOrderFunction;
	}

	@Test
	public void validateFitFunction() {
		Fit fit = new Fit(fitType);
		fit.slope = slope;
		fit.intercept = intercept;
		assertEquals(fit.toString(), expectedFitFunction, fit.getFitFunction());
	}

	@Test
	public void validateOrderFunction() {
		Fit fit = new Fit(fitType);
		fit.slope = slope;
		fit.intercept = intercept;
		assertEquals(fit.toString(), expectedOrderFunction,
				fit.getOrderFunction());
	}
}