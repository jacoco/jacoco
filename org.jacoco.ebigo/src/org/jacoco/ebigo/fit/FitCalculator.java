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

import static org.jacoco.ebigo.internal.util.ValidationUtils.validateNotNull;

import java.util.Collections;
import java.util.SortedSet;

import org.apache.commons.math3.stat.regression.SimpleRegression;

/**
 * A utility that calculates simple linear regression fits to various types of
 * curves whose coordinate system can be transformed such that they become
 * linear.
 * 
 * @author Omer Azmon
 */
public final class FitCalculator {
	private FitCalculator() {
	}

	private static Fit makeFitEmpty(Fit fit) {
		fit.n = 0;
		return fit;
	}

	/**
	 * Perform a linear regression fit to a set of points.
	 * 
	 * @param fitType
	 *            the curve type to fit
	 * @param xs
	 *            the X axis values
	 * @param ys
	 *            the Y axis values
	 * @return the result of the fit attempt. If there is not enough variation
	 *         in the X's as shown by the significance level of the slope (i.e
	 *         correlation), an empty fit is returned. That is 'n' is set to
	 *         zero.
	 * @throws IllegalArgumentException
	 *             if fitType, loc, xs, or ys are null; if the number of X's and
	 *             Y's don't match; there are 3 or less points (X's and Y's)
	 */
	public static Fit calcSingleFit(final FitType fitType, final int[] xs, final int[] ys) {
		validateNotNull("fitType", fitType);
		validateNotNull("xs", xs);
		validateNotNull("ys", ys);
		if (xs.length <= 3) {
			throw new IllegalArgumentException(
					"Not enough points, less than 3 X's");
		}
		if (ys.length != xs.length) {
			throw new IllegalArgumentException("STRANGE ERROR: xs.length="
					+ xs.length + " but ys.length=" + ys.length);
		}

		Fit fit = new Fit(fitType);
		SimpleRegression regression = new SimpleRegression(true);
		for (int idx = 0; idx < xs.length; ++idx) {
			if (ys[idx] <= 0) {
				continue;
			}
				
			if (fit.type.isDoLogX() && (xs[idx] <= 0)) {
				continue;
			}

			// Now take the logs if applicable
			double x = fit.type.isDoLogX() ? Math.log(xs[idx]) : xs[idx];
			double y = fit.type.isDoLogY() ? Math.log(ys[idx]) : ys[idx];

			regression.addData(x, y);
		}

		if( regression.getN() < 3) {
			return makeFitEmpty(fit);
		}
		
		if (Double.isNaN(regression.getSignificance())) {
			// don't return a fix if there's no variation in the x's
			return makeFitEmpty(fit);
		}

		fit.n = (int) regression.getN();
		fit.intercept = fit.type.isDoLogX() ? Math.exp(regression
				.getIntercept()) : regression.getIntercept();
		fit.slope = regression.getSlope();
		fit.rSquared = regression.getRSquare();
		fit.confidence = regression.getSlopeConfidenceInterval();
		return fit;
	}

	/**
	 * Perform a linear regression fit to a set of points.
	 * 
	 * @param fitTypes
	 *            the curve types (plural) to fit
	 * @param xs
	 *            the X axis values
	 * @param ys
	 *            the Y axis values
	 * @return a set of the resulting fit attempts sorted from best to worst
	 *         according to the 'confidence' value of each fit. If there is not
	 *         enough variation in any X's as shown by the significance level of
	 *         the slope (i.e correlation), an empty fit is included in the set.
	 *         That is its 'n' is set to zero.
	 * @throws IllegalArgumentException
	 *             if fitType, loc, xs, or ys are null; if the number of X's and
	 *             Y's don't match; there are 3 or less points (X's and Y's)
	 */
	public static SortedSet<Fit> calcFitSet(final FitType[] fitTypes,
			final int[] xs, final int[] ys) {
		SortedFitSet fitSet = new SortedFitSet();
		for (FitType fitType : fitTypes) {
			Fit fit = FitCalculator.calcSingleFit(fitType, xs, ys);
			if(fit.n > 0) {
				fitSet.add(FitCalculator.calcSingleFit(fitType, xs, ys));
			}
		}
		return Collections.unmodifiableSortedSet(fitSet);
	}
}
