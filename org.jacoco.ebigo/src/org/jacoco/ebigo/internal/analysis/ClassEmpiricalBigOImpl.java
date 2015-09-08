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

import static org.jacoco.ebigo.internal.util.ValidationUtils.validateNotNull;

import java.util.SortedSet;

import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.analysis.ILine;
import org.jacoco.core.analysis.ISourceNode;
import org.jacoco.ebigo.analysis.IClassEmpiricalBigO;
import org.jacoco.ebigo.fit.Fit;
import org.jacoco.ebigo.fit.FitCalculator;
import org.jacoco.ebigo.fit.FitType;

/**
 * The results of an Empirical Big-O analysis for a single class.
 * 
 * @author Omer Azmon
 */
public class ClassEmpiricalBigOImpl implements IClassEmpiricalBigO {
	private static final Fit[] EMPTY_FIT_ARRAY = new Fit[0];

	private static void validateClassCoverage(IClassCoverage[] ccs) {
		validateNotNull("ccs", ccs);

		if (ccs.length < 1) {
			return;
		}

		int firstLine = ccs[0].getFirstLine();
		int lastLine = ccs[0].getLastLine();

		// handle line count mismatch
		for (int idx = 1; idx < ccs.length; idx++) {
			if ((ccs[idx].getFirstLine() != firstLine)
					|| (ccs[idx].getLastLine() != lastLine)) {
				throw new IllegalArgumentException(
						"Mismatched line counts between workloads in class "
								+ ccs[0].getName());
			}
		}
	}

	private final IClassCoverage[] ccs;
	private Fit[] lineFits;

	/**
	 * Construct
	 * 
	 * @param ccs
	 *            the array of IClassCoverage object in X-axis order. One per
	 *            workload.
	 */
	public ClassEmpiricalBigOImpl(IClassCoverage[] ccs) {
		validateClassCoverage(ccs);
		this.ccs = ccs;
		this.lineFits = null;
	}

	/**
	 * Returns the array of IClassCoverage object in X-axis order. One per
	 * workload.
	 * 
	 * @return the array of IClassCoverage object.
	 */
	public IClassCoverage[] getMatchedCoverageClasses() {
		return ccs;
	}

	/*
	 * Returns the array of best fits. One per line in the class.
	 * 
	 * @return the array of best fits.
	 */
	public Fit[] getLineFits() {
		return lineFits;
	}

	/**
	 * Do a BigO analysis on this class. The results are stored in this object
	 * and can be fetched using the {@code getLineFits} method.
	 * 
	 * @param fitTypes
	 *            the fit types to consider
	 * @param xValues
	 *            the X-axis values to use for the fit.
	 * @return {@code true} upon success; Otherwise, {@code false}
	 */
	public boolean analyze(final FitType[] fitTypes, final int[] xValues) {
		validateNotNull("fitTypes", fitTypes);
		validateNotNull("xValues", xValues);

		if (ccs.length < 1) {
			lineFits = EMPTY_FIT_ARRAY;
			return false;
		}

		final ILine[] lineArray = new ILine[ccs.length];

		int firstLine = ccs[0].getFirstLine();
		int lastLine = ccs[0].getLastLine();
		if (firstLine == ISourceNode.UNKNOWN_LINE) {
			lineFits = EMPTY_FIT_ARRAY;
			return false;
		}

		Fit[] fitArray = new Fit[lastLine - firstLine + 1];
		for (int i = firstLine; i <= lastLine; i++) {
			int[] execCounts = new int[lineArray.length];
			for (int idx = 0; idx < ccs.length; idx++) {
				lineArray[idx] = ccs[idx].getLine(i);
				execCounts[idx] = lineArray[idx].getInstructionCounter()
						.getExecutionCount();
			}

			SortedSet<Fit> fitSet = FitCalculator.calcFitSet(fitTypes,
					ccs[0].getName() + ": Line " + i, xValues, execCounts);

			// save fit
			fitArray[i - firstLine] = fitSet.isEmpty() ? null : fitSet.first();
		}

		lineFits = fitArray;

		return true;
	}
}
