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
package org.jacoco.examples;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collection;

import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.analysis.ICounter;
import org.jacoco.core.analysis.ICounter.CounterValue;
import org.jacoco.ebigo.analysis.EmpiricalBigOBuilder;
import org.jacoco.ebigo.analysis.IClassEmpiricalBigO;
import org.jacoco.ebigo.fit.Fit;

/**
 * Sample printout of an Empirical Big-O analysis results.
 * 
 * @author Omer Azmon
 */
public class EmpiricalBigOAnalysisPrinter {

	/**
	 * @param results
	 * @throws IOException
	 */
	public static void dump(final EmpiricalBigOBuilder results)
			throws IOException {
		final int[] xValues = results.getXAxisValues().getXValues();

		// Let's dump some metrics and line coverage information:
		System.out.println("====================");
		System.out.println("Analysis by " + results.getAttributeName());
		System.out.println("xAxis value=" + Arrays.toString(xValues));
		System.out.println();

		final Collection<IClassEmpiricalBigO> classBigOResults = results
				.getClasses();
		for (final IClassEmpiricalBigO classBigOResult : classBigOResults) {
			final IClassCoverage[] ccs = classBigOResult
					.getMatchedCoverageClasses();
			final ICounter[] instructionCounters = new ICounter[ccs.length];
			final ICounter[] branchCounters = new ICounter[ccs.length];
			final ICounter[] lineCounters = new ICounter[ccs.length];
			final ICounter[] methodCounters = new ICounter[ccs.length];
			final ICounter[] complexityCounters = new ICounter[ccs.length];
			final ICounter[] classCounters = new ICounter[ccs.length];
			for (int idx = 0; idx < ccs.length; idx++) {
				instructionCounters[idx] = ccs[idx].getInstructionCounter();
				branchCounters[idx] = ccs[idx].getBranchCounter();
				lineCounters[idx] = ccs[idx].getLineCounter();
				methodCounters[idx] = ccs[idx].getMethodCounter();
				complexityCounters[idx] = ccs[idx].getComplexityCounter();
				classCounters[idx] = ccs[idx].getClassCounter();
			}

			System.out.println("-=-=-=-=-=-=-=-=-=-=");
			System.out.printf("Coverage of class %s%n", ccs[0].getName());

			printCounter(System.out, "instructions", instructionCounters);
			printCounter(System.out, "branches", branchCounters);
			printCounter(System.out, "lines", lineCounters);
			printCounter(System.out, "methods", methodCounters);
			printCounter(System.out, "class", classCounters);
			printCounter(System.out, "class-complexity", complexityCounters);

			final int firstLine = ccs[0].getFirstLine();
			final int lastLine = ccs[0].getLastLine();
			final Fit[] lineFits = classBigOResult.getLineFits();
			for (int i = firstLine; i <= lastLine; i++) {
				final Fit fit = lineFits[i - firstLine];
				System.out.printf("Line %s: %s %s%n", Integer.valueOf(i),
						getColor(ccs[0].getLine(i).getStatus()),
						getFitString(fit));
			}
		}
		System.out.println();

		System.out.printf("-=-=-=-=-=-=-=-=-=-=%n");
	}

	private static String makeCounterString(final ICounter[] counters,
			final CounterValue counterValueEnum) {
		if (counterValueEnum != CounterValue.TOTALEXECCOUNT) {
			return Integer.toString((int) counters[0]
					.getValue(counterValueEnum));
		}
		final StringBuffer buffer = new StringBuffer();
		for (int idx = 0; idx < counters.length; idx++) {
			if (idx > 0) {
				buffer.append('/');
			}
			buffer.append((int) counters[idx].getValue(counterValueEnum));
		}
		return buffer.toString();

	}

	private static void printCounter(final PrintStream out, final String unit,
			final ICounter[] counters) {
		final String missed = makeCounterString(counters,
				CounterValue.MISSEDCOUNT);
		final String total = makeCounterString(counters,
				CounterValue.TOTALCOUNT);
		final String executions = makeCounterString(counters,
				CounterValue.TOTALEXECCOUNT);
		out.printf("%s of %s %s missed", missed, total, unit);
		if (!unit.contains("complexity")) {
			out.printf(", with %s executions", executions);
		}
		out.println();
	}

	private static String getFitString(final Fit fit) {
		if (fit == null) {
			return "";
		}
		return getFitColor(fit) + " Empirical-Big-O='" + fit.getOrderFunction()
				+ "', Exact Function='" + fit.getFitFunction() + "'";
	}

	private static String getColor(final int status) {
		switch (status) {
		case ICounter.NOT_COVERED:
			return "coverage-red";
		case ICounter.PARTLY_COVERED:
			return "coverage-yellow";
		case ICounter.FULLY_COVERED:
			return "coverage-green";
		}
		return "";
	}

	private static String getFitColor(final Fit fit) {
		if (fit == null) {
			return "";
		}
		switch (fit.type) {
		default:
			return "e-bigo-red";
		case PowerLaw:
			return "e-bigo-yellow";
		case Linear:
		case Log:
			return "e-bigo-green";
		}
	}
}
