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
package org.jacoco.cli.internal.commands;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jacoco.cli.internal.Command;
import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.analysis.ICounter;
import org.jacoco.core.analysis.ICoverageNode;
import org.jacoco.core.analysis.ICoverageVisitor;
import org.jacoco.core.analysis.ILine;
import org.jacoco.core.analysis.IMethodCoverage;
import org.jacoco.core.data.ExecutionDataStore;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

/**
 * The <code>classinfo</code> command.
 */
public class ClassInfo extends Command {

	@Argument(usage = "location of Java class files", metaVar = "<classlocations>")
	List<File> classfiles = new ArrayList<File>();

	@Option(name = "--verbose", usage = "show method and line number details")
	boolean verbose = false;

	@Override
	public String description() {
		return "Print information about Java class files at the provided location.";
	}

	@Override
	public int execute(final PrintWriter out, final PrintWriter err)
			throws IOException {
		if (classfiles.isEmpty()) {
			out.println("[WARN] No class files provided.");
		} else {
			final Analyzer analyzer = new Analyzer(new ExecutionDataStore(),
					new Printer(out));
			for (final File file : classfiles) {
				analyzer.analyzeAll(file);
			}
		}
		return 0;
	}

	private class Printer implements ICoverageVisitor {

		private final PrintWriter out;

		Printer(final PrintWriter out) {
			this.out = out;
			out.println("  INST   BRAN   LINE   METH   CXTY   ELEMENT");
		}

		public void visitCoverage(final IClassCoverage coverage) {
			final String desc = String.format("class 0x%016x %s",
					Long.valueOf(coverage.getId()), coverage.getName());
			printDetails(desc, coverage);
			if (verbose) {
				for (final Iterator<IMethodCoverage> i = coverage.getMethods()
						.iterator(); i.hasNext();) {
					printMethod(i.next(), i.hasNext());
				}
			}
		}

		private void printMethod(final IMethodCoverage method,
				final boolean more) {
			final String desc = String.format("+- method %s%s",
					method.getName(), method.getDesc());
			printDetails(desc, method);
			for (int nr = method.getFirstLine(); nr <= method
					.getLastLine(); nr++) {
				printLine(method.getLine(nr), nr, more ? "| " : "  ");
			}
		}

		private void printLine(final ILine line, final int nr,
				final String indent) {
			if (line.getStatus() != ICounter.EMPTY) {
				out.printf("%6s %6s                        %s +- line %s%n",
						total(line.getInstructionCounter()),
						total(line.getBranchCounter()), indent,
						Integer.valueOf(nr));
			}
		}

		private void printDetails(final String description,
				final ICoverageNode coverage) {
			out.printf("%6s %6s %6s %6s %6s   %s%n",
					total(coverage.getInstructionCounter()),
					total(coverage.getBranchCounter()),
					total(coverage.getLineCounter()),
					total(coverage.getMethodCounter()),
					total(coverage.getComplexityCounter()), description);
		}

		private String total(final ICounter counter) {
			return String.valueOf(counter.getTotalCount());
		}

	}

}
