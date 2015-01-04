/*******************************************************************************
 * Copyright (c) 2009, 2015 Mountainminds GmbH & Co. KG and Contributors
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

import java.util.Collection;

import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.analysis.ICounter;
import org.jacoco.core.analysis.ILine;
import org.jacoco.core.analysis.ISourceFileCoverage;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.data.SessionInfoStore;
import org.jacoco.core.instr.Instrumenter;
import org.jacoco.core.internal.analysis.CounterImpl;
import org.jacoco.core.runtime.IRuntime;
import org.jacoco.core.runtime.RuntimeData;
import org.jacoco.core.runtime.SystemPropertiesRuntime;
import org.jacoco.core.test.TargetLoader;
import org.junit.Before;
import org.objectweb.asm.ClassReader;

/**
 * Base class for validation tests. It executes the given class under code
 * coverage and provides the coverage results for validation.
 */
public abstract class ValidationTestBase {

	private static final String[] STATUS_NAME = new String[4];

	{
		STATUS_NAME[ICounter.EMPTY] = "NO_CODE";
		STATUS_NAME[ICounter.NOT_COVERED] = "NOT_COVERED";
		STATUS_NAME[ICounter.FULLY_COVERED] = "FULLY_COVERED";
		STATUS_NAME[ICounter.PARTLY_COVERED] = "PARTLY_COVERED";
	}

	protected final Class<?> target;

	protected IClassCoverage classCoverage;

	protected ISourceFileCoverage sourceCoverage;

	protected Source source;

	protected TargetLoader loader;

	protected ValidationTestBase(final Class<?> target) {
		this.target = target;
	}

	@Before
	public void setup() throws Exception {
		loader = new TargetLoader();
		final ClassReader reader = new ClassReader(
				TargetLoader.getClassData(target));
		final ExecutionDataStore store = execute(reader);
		analyze(reader, store);
		source = Source.getSourceFor(target);
	}

	private ExecutionDataStore execute(final ClassReader reader)
			throws Exception {
		RuntimeData data = new RuntimeData();
		IRuntime runtime = new SystemPropertiesRuntime();
		runtime.startup(data);
		final byte[] bytes = new Instrumenter(runtime).instrument(reader);
		run(loader.add(target, bytes));
		final ExecutionDataStore store = new ExecutionDataStore();
		data.collect(store, new SessionInfoStore(), false);
		runtime.shutdown();
		return store;
	}

	protected abstract void run(final Class<?> targetClass) throws Exception;

	private void analyze(final ClassReader reader,
			final ExecutionDataStore store) {
		final CoverageBuilder builder = new CoverageBuilder();
		final Analyzer analyzer = new Analyzer(store, builder);
		analyzer.analyzeClass(reader);
		final Collection<IClassCoverage> classes = builder.getClasses();
		assertEquals(1, classes.size(), 0.0);
		classCoverage = classes.iterator().next();
		final Collection<ISourceFileCoverage> files = builder.getSourceFiles();
		assertEquals(1, files.size(), 0.0);
		sourceCoverage = files.iterator().next();
	}

	protected void assertLine(final String tag, final int status) {
		final int nr = source.getLineNumber(tag);
		final ILine line = sourceCoverage.getLine(nr);
		final String msg = String.format("Status in line %s: %s",
				Integer.valueOf(nr), source.getLine(nr));
		final int insnStatus = line.getInstructionCounter().getStatus();
		assertEquals(msg, STATUS_NAME[status], STATUS_NAME[insnStatus]);
	}

	protected void assertLine(final String tag, final int missedBranches,
			final int coveredBranches) {
		final int nr = source.getLineNumber(tag);
		final ILine line = sourceCoverage.getLine(nr);
		final String msg = String.format("Branches in line %s: %s",
				Integer.valueOf(nr), source.getLine(nr));
		assertEquals(msg + " branches",
				CounterImpl.getInstance(missedBranches, coveredBranches),
				line.getBranchCounter());
	}

	protected void assertLine(final String tag, final int status,
			final int missedBranches, final int coveredBranches) {
		assertLine(tag, status);
		assertLine(tag, missedBranches, coveredBranches);
	}

}
