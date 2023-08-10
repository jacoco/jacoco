/*******************************************************************************
 * Copyright (c) 2009, 2023 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.examples;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.analysis.ICoverageVisitor;
import org.jacoco.core.data.ExecutionDataStore;

/**
 * This example reads Java class files, directories or JARs given as program
 * arguments and dumps information about the classes.
 */
public final class ClassInfo implements ICoverageVisitor {

	private final PrintStream out;
	private final Analyzer analyzer;

	/**
	 * Creates a new example instance printing to the given stream.
	 *
	 * @param out
	 *            stream for outputs
	 */
	public ClassInfo(final PrintStream out) {
		this.out = out;
		analyzer = new Analyzer(new ExecutionDataStore(), this);
	}

	/**
	 * Run this example with the given parameters.
	 *
	 * @param args
	 *            command line parameters
	 * @throws IOException
	 *             in case of error reading a input file
	 */
	public void execute(final String[] args) throws IOException {
		for (final String file : args) {
			analyzer.analyzeAll(new File(file));
		}
	}

	public void visitCoverage(final IClassCoverage coverage) {
		out.printf("class name:   %s%n", coverage.getName());
		out.printf("class id:     %016x%n", Long.valueOf(coverage.getId()));
		out.printf("instructions: %s%n", Integer
				.valueOf(coverage.getInstructionCounter().getTotalCount()));
		out.printf("branches:     %s%n",
				Integer.valueOf(coverage.getBranchCounter().getTotalCount()));
		out.printf("lines:        %s%n",
				Integer.valueOf(coverage.getLineCounter().getTotalCount()));
		out.printf("methods:      %s%n",
				Integer.valueOf(coverage.getMethodCounter().getTotalCount()));
		out.printf("complexity:   %s%n%n", Integer
				.valueOf(coverage.getComplexityCounter().getTotalCount()));
	}

	/**
	 * Entry point to run this examples as a Java application.
	 *
	 * @param args
	 *            list of program arguments
	 * @throws IOException
	 *             in case of errors executing the example
	 */
	public static void main(final String[] args) throws IOException {
		new ClassInfo(System.out).execute(args);
	}

}
