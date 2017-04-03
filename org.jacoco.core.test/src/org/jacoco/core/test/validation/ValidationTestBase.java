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
package org.jacoco.core.test.validation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;

import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.ICounter;
import org.jacoco.core.analysis.ILine;
import org.jacoco.core.analysis.ISourceFileCoverage;
import org.jacoco.core.data.ExecutionData;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.internal.analysis.CounterImpl;
import org.jacoco.core.test.InstrumentingLoader;
import org.jacoco.core.test.TargetLoader;
import org.jacoco.core.test.validation.targets.Stubs;
import org.junit.Before;

/**
 * Base class for validation tests. It executes the given class under code
 * coverage and provides the coverage results for validation.
 */
public abstract class ValidationTestBase {

	protected static final boolean isJDKCompiler = Compiler.DETECT.isJDK();

	private static final String[] STATUS_NAME = new String[4];

	{
		STATUS_NAME[ICounter.EMPTY] = "EMPTY";
		STATUS_NAME[ICounter.NOT_COVERED] = "NOT_COVERED";
		STATUS_NAME[ICounter.FULLY_COVERED] = "FULLY_COVERED";
		STATUS_NAME[ICounter.PARTLY_COVERED] = "PARTLY_COVERED";
	}

	private final String srcFolder;

	private final Class<?> target;

	private ISourceFileCoverage sourceCoverage;

	private Source source;

	private InstrumentingLoader loader;

	protected ValidationTestBase(final String srcFolder, final Class<?> target) {
		this.srcFolder = srcFolder;
		this.target = target;
	}

	protected ValidationTestBase(final Class<?> target) {
		this("src", target);
	}

	@Before
	public void setup() throws Exception {
		final ExecutionDataStore store = execute();
		analyze(store);
		source = Source.getSourceFor(srcFolder, target);
	}

	private ExecutionDataStore execute() throws Exception {
		loader = new InstrumentingLoader(target);
		run(loader.loadClass(target.getName()));
		return loader.collect();
	}

	protected void run(final Class<?> targetClass) throws Exception {
		targetClass.getMethod("main", String[].class).invoke(null,
				(Object) new String[0]);
	}

	private void analyze(final ExecutionDataStore store) throws IOException {
		final CoverageBuilder builder = new CoverageBuilder();
		final Analyzer analyzer = new Analyzer(store, builder);
		for (ExecutionData data : store.getContents()) {
			analyze(analyzer, data);
		}

		String srcName = target.getName().replace('.', '/') + ".java";
		for (ISourceFileCoverage file : builder.getSourceFiles()) {
			if (srcName.equals(file.getPackageName() + "/" + file.getName())) {
				sourceCoverage = file;
				return;
			}
		}
		fail("No source node found for " + srcName);
	}

	private void analyze(final Analyzer analyzer, final ExecutionData data)
			throws IOException {
		final byte[] bytes = TargetLoader.getClassDataAsBytes(
				target.getClassLoader(), data.getName());
		analyzer.analyzeClass(bytes, data.getName());
	}

	protected void assertMethodCount(final int expectedTotal) {
		assertEquals(expectedTotal,
				sourceCoverage.getMethodCounter().getTotalCount());
	}

	protected void assertLine(final String tag, final int status) {
		final int nr = source.getLineNumber(tag);
		final ILine line = sourceCoverage.getLine(nr);
		final String msg = String.format("Status in line %s: %s",
				Integer.valueOf(nr), source.getLine(nr));
		final int insnStatus = line.getInstructionCounter().getStatus();
		assertEquals(msg, STATUS_NAME[status], STATUS_NAME[insnStatus]);
	}

	protected void assertLine(final String tag, final int status,
			final int missedBranches, final int coveredBranches) {
		assertLine(tag, status);
		final int nr = source.getLineNumber(tag);
		final ILine line = sourceCoverage.getLine(nr);
		final String msg = String.format("Branches in line %s: %s",
				Integer.valueOf(nr), source.getLine(nr));
		assertEquals(msg + " branches",
				CounterImpl.getInstance(missedBranches, coveredBranches),
				line.getBranchCounter());
	}

	protected void assertLogEvents(String... events) throws Exception {
		final Method getter = Class.forName(Stubs.class.getName(), false,
				loader).getMethod("getLogEvents");
		assertEquals("Log events", Arrays.asList(events), getter.invoke(null));
	}

}
