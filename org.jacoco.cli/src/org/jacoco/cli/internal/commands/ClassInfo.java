/*******************************************************************************
 * Copyright (c) 2009, 2017 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
import java.util.List;

import org.jacoco.cli.internal.Command;
import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.analysis.ICoverageVisitor;
import org.jacoco.core.data.ExecutionDataStore;
import org.kohsuke.args4j.Argument;

/**
 * The <code>classinfo</code> command.
 */
public class ClassInfo extends Command {

	@Argument(usage = "location of Java class files", metaVar = "<classlocations>")
	List<File> classfiles = new ArrayList<File>();

	@Override
	public String description() {
		return "Print information about Java class files at the provided location.";
	}

	@Override
	public int execute(final PrintWriter out, final PrintWriter err)
			throws IOException {
		final Analyzer analyzer = new Analyzer(new ExecutionDataStore(),
				new ICoverageVisitor() {
					public void visitCoverage(final IClassCoverage coverage) {
						print(coverage, out);
					}
				});

		for (final File file : classfiles) {
			analyzer.analyzeAll(file);
		}
		return 0;
	}

	private void print(final IClassCoverage coverage, final PrintWriter out) {
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

}
