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

import org.jacoco.core.analysis.EBigOFunction;
import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.analysis.IMethodCoverage;
import org.jacoco.core.analysis.ISourceNode;
import org.jacoco.core.internal.analysis.ClassCoverageImpl;
import org.jacoco.core.internal.analysis.MethodCoverageImpl;
import org.jacoco.core.internal.analysis.SourceNodeImpl;
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

	private static void validateClassCoverage(final IClassCoverage[] ccs) {
		validateNotNull("ccs", ccs);

		if (ccs.length < 1) {
			return;
		}

		final int firstLine = ccs[0].getFirstLine();
		final int lastLine = ccs[0].getLastLine();

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

	/**
	 * Construct
	 * 
	 * @param ccs
	 *            the array of IClassCoverage object in X-axis order. One per
	 *            workload.
	 */
	public ClassEmpiricalBigOImpl(final IClassCoverage[] ccs) {
		validateClassCoverage(ccs);
		this.ccs = ccs;
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
			return false;
		}

		// Add E-BigO to the class
		calcNodeFit(fitTypes, xValues, ccs);

		final int firstLine = ccs[0].getFirstLine();
		final int lastLine = ccs[0].getLastLine();
		if (firstLine == ISourceNode.UNKNOWN_LINE) {
			return false;
		}

		final int[] execCounts = new int[ccs.length];
		for (int i = firstLine; i <= lastLine; i++) {
			for (int idx = 0; idx < ccs.length; idx++) {
				execCounts[idx] = ccs[idx].getLine(i).getInstructionCounter()
						.getExecutionCount();
			}

			final SortedSet<Fit> fitSet = FitCalculator.calcFitSet(fitTypes,
					xValues, execCounts);
			final EBigOFunction func = fitSet.isEmpty() ? EBigOFunction.UNDEFINED
					: fitSet.first().getEBigOFunction();
			for (int idx = 0; idx < ccs.length; idx++) {
				((ClassCoverageImpl) ccs[idx]).setLineEBigOFunction(func, i);
			}
		}

		// For each method in class, add E-BigO
		final MethodCoverageSetIterator methodSetIterator = new MethodCoverageSetIterator(
				ccs);
		while (methodSetIterator.hasNext()) {
			final IMethodCoverage[] methodSet = methodSetIterator.next();
			calcNodeFit(fitTypes, xValues, methodSet);

			final int firstMethodLine = methodSet[0].getFirstLine();
			final int lastMethodLine = methodSet[0].getLastLine();
			if (firstMethodLine == ISourceNode.UNKNOWN_LINE) {
				continue;
			}
			for (int i = firstMethodLine; i <= lastMethodLine; i++) {
				for (int idx = 0; idx < methodSet.length; idx++) {
					((MethodCoverageImpl) methodSet[idx]).setLineEBigOFunction(
							ccs[0].getLineEBigOFunction(i), i);
				}
			}
		}

		// sourceFiles are handled when class in incremented into source file

		return true;
	}

	private void calcNodeFit(final FitType[] fitTypes, final int[] xValues,
			final ISourceNode[] nodes) {

		final int[] execCounts = new int[nodes.length];
		for (int idx = 0; idx < nodes.length; idx++) {
			execCounts[idx] = nodes[idx].getInstructionCounter()
					.getExecutionCount();
		}

		final SortedSet<Fit> fitSet = FitCalculator.calcFitSet(fitTypes,
				xValues, execCounts);
		final EBigOFunction func = fitSet.isEmpty() ? EBigOFunction.UNDEFINED
				: fitSet.first().getEBigOFunction();

		for (int idx = 0; idx < ccs.length; idx++) {
			((SourceNodeImpl) nodes[idx]).setEBigOFunction(func);
		}
	}
}
