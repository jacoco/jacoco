/*******************************************************************************
 * Copyright (c) 2009, 2011 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.examples;

import java.io.File;
import java.io.IOException;

import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.analysis.ICoverageVisitor;
import org.jacoco.core.data.ExecutionDataStore;

/**
 * This example reads given Java class files, directories or JARs and dumps
 * information about the classes.
 */
public class ClassInfo implements ICoverageVisitor {

	private final Analyzer analyzer;

	private ClassInfo() {
		analyzer = new Analyzer(new ExecutionDataStore(), this);
	}

	private void dumpInfo(final String file) throws IOException {
		analyzer.analyzeAll(new File(file));
	}

	public void visitCoverage(final IClassCoverage coverage) {
		System.out.printf("class name:   %s%n", coverage.getName());
		System.out.printf("class id:     %016x%n",
				Long.valueOf(coverage.getId()));
		System.out.printf("instructions: %s%n", Integer.valueOf(coverage
				.getInstructionCounter().getTotalCount()));
		System.out.printf("branches:     %s%n",
				Integer.valueOf(coverage.getBranchCounter().getTotalCount()));
		System.out.printf("lines:        %s%n",
				Integer.valueOf(coverage.getLineCounter().getTotalCount()));
		System.out.printf("methods:      %s%n",
				Integer.valueOf(coverage.getMethodCounter().getTotalCount()));
		System.out.printf("complexity:   %s%n%n", Integer.valueOf(coverage
				.getComplexityCounter().getTotalCount()));
	}

	/**
	 * Reads all class file specified as the arguments and dumps information
	 * about it to <code>stdout</code>.
	 * 
	 * @param args
	 *            list of class files
	 * @throws IOException
	 */
	public static void main(final String[] args) throws IOException {
		final ClassInfo info = new ClassInfo();
		for (final String file : args) {
			info.dumpInfo(file);
		}
	}

}
