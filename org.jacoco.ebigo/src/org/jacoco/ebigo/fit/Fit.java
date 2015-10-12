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

import org.jacoco.core.analysis.EBigOFunction;

/**
 * The result of an attempt to fit a set of points to a curve. The fitting is
 * done by the <code>FitCalcluator</code> class methods. All fits are using
 * Linear Regression. A transform is perform before and after the regression.
 * Where X is the variable, m is the slope, and b is the intercept, for
 * <ul>
 * <li>Linear: b + m X
 * <li>PowerLaw: b X^m
 * <li>Exponential: b e^(mX)
 * <li>Logarithmic: b _ m ln(X)
 * </ul>
 * 
 * @author Omer Azmon
 */
public class Fit {
	/** The curve type to which the fitting was done */
	public final FitType type;

	/** The number of points considered */
	public int n;

	/** The best fit intercept (b) for this curve type. */
	public double intercept;

	/** The best fit slope (m) for this curve type. */
	public double slope;

	/**
	 * The <a href="http://www.xycoon.com/coefficient1.htm"> coefficient of
	 * determination</a>, usually denoted r-square.
	 */
	public double rSquared;

	/**
	 * The half-width of a 95% confidence interval for the slope estimate. This
	 * is the value that is used to compare the 'goodness' of various fits
	 */
	public double confidence;

	/**
	 * Construct a Fit
	 * 
	 * @param fitType
	 *            The curve type to which the fitting was done
	 */
	public Fit(final FitType fitType) {
		this.type = fitType;
		this.n = 0; // number of pairs
	}

	/**
	 * Returns a String representation of the function described by this fit.
	 * For example: <code>10 + 3x</code>
	 * 
	 * @return the function described by this fit
	 */
	public String getFitFunction() {
		switch (type) {
		case Logarithmic:
			if (0 == slope)
				return String.format("%.2f", intercept);
			if (1 == slope && 0 == intercept)
				return String.format("ln(x)");
			if (1 == slope)
				return String.format("%.2f + ln(x)", intercept);
			if (0 == intercept)
				return String.format("%.2f ln(x)", slope);
			return String.format("%.2f + %.2f ln(x)", intercept, slope);
		case Linear:
			if (0 == slope)
				return String.format("%.2f", intercept);
			if (1 == slope && 0 == intercept)
				return "x";
			if (1 == slope)
				return String.format("%.2f + x", intercept);
			if (0 == intercept)
				return String.format("%.2fx", slope);
			return String.format("%.2f + %.2fx", intercept, slope);
		case PowerLaw:
			if (0 == intercept)
				return String.format("%.2f", 0D);
			if (0 == slope)
				return String.format("%.2f", intercept);
			if (1 == intercept && 1 == slope)
				return "x";
			if (1 == slope)
				return String.format("%.2fx", intercept);
			if (1 == intercept)
				return String.format("x^%.2f", slope);
			return String.format("%.2fx^%.2f", intercept, slope);
		case Exponential:
			if (0 == intercept)
				return String.format("%.2f", 0D);
			if (0 == slope)
				return String.format("%.2f", intercept);
			if (1 == intercept && 1 == slope)
				return "e^x";
			if (1 == intercept)
				return String.format("e^(%.2fx)", slope);
			if (1 == slope)
				return String.format("%.2fe^x", intercept);
			return String.format("%.2fe^(%.2fx)", intercept, slope);
		default:
			return String.format("%s(%.2f, %.2f)", type.name(), intercept,
					slope);
		}
	}

	/**
	 * Returns a String representation of the order of magnitude of the function
	 * described by this fit. For example: if the function is
	 * <code>10 + 3x</code>, the order of magnitude is <code>n</code>
	 * 
	 * @return the order of magnitude of the function described by this fit
	 */
	public String getOrderFunction() {
		switch (type) {
		case Logarithmic:
			return 0 == slope ? "1" : "log(n)";
		case Linear:
			return 0 == slope ? "1" : "n";
		case PowerLaw:
			if (0 == intercept || 0 == slope)
				return "1";
			if (1 == slope)
				return "n";
			return String.format("n^%.2f", slope);
		case Exponential:
			if (0 == intercept || 0 == slope)
				return "1";
			return "2^n";
		default:
			return String.format("%s(%f,%f)", type.name(), intercept, slope);
		}
	}

	/**
	 * Returns the E-Big-O function object resulting from this fit.
	 * 
	 * @return the E-Big-O function object resulting from this fit.
	 */
	public EBigOFunction getEBigOFunction() {
		return new EBigOFunction(type.getType(), slope, intercept);
	}

	@Override
	public String toString() {
		return "Fit [type=" + type + ", n=" + n + ", slope=" + slope
				+ ", intercept=" + intercept + ", r-squared=" + rSquared
				+ ", confidence=" + confidence + "]";
	}

}