/*******************************************************************************
 * Copyright (c) 2009, 2018 Mountainminds GmbH & Co. KG and Contributors
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
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;

import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.ICounter;
import org.jacoco.core.analysis.ILine;
import org.jacoco.core.data.ExecutionData;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.internal.analysis.CounterImpl;
import org.jacoco.core.test.InstrumentingLoader;
import org.jacoco.core.test.TargetLoader;
import org.jacoco.core.test.validation.Source.Line;
import org.jacoco.core.test.validation.targets.Stubs;
import org.junit.Before;
import org.junit.Test;

/**
 * Base class for validation tests. It executes the given class under code
 * coverage and provides the coverage results for validation.
 */
public abstract class ValidationTestBase {

	protected static final boolean isJDKCompiler = Compiler.DETECT.isJDK();

	protected static final JavaVersion JAVA_VERSION = new JavaVersion(
			System.getProperty("java.version"));

	private static final String[] STATUS_NAME = new String[4];

	{
		STATUS_NAME[ICounter.EMPTY] = "EMPTY";
		STATUS_NAME[ICounter.NOT_COVERED] = "NOT_COVERED";
		STATUS_NAME[ICounter.FULLY_COVERED] = "FULLY_COVERED";
		STATUS_NAME[ICounter.PARTLY_COVERED] = "PARTLY_COVERED";
	}

	private final Class<?> target;

	private Source source;

	private InstrumentingLoader loader;

	protected ValidationTestBase(final Class<?> target) {
		this.target = target;
	}

	@Before
	public void setup() throws Exception {
		final ExecutionDataStore store = execute();
		analyze(store);
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
		source = Source.load(target, builder.getBundle("Test"));
	}

	private void analyze(final Analyzer analyzer, final ExecutionData data)
			throws IOException {
		final byte[] bytes = TargetLoader
				.getClassDataAsBytes(target.getClassLoader(), data.getName());
		analyzer.analyzeClass(bytes, data.getName());
	}

	/**
	 * All single line comments are interpreted as statements in the following
	 * format:
	 * 
	 * <pre>
	 * // statement1() statement2()
	 * </pre>
	 */
	@Test
	public void execute_assertions_in_comments() throws IOException {
		for (Line line : source.getLines()) {
			String exec = line.getComment();
			if (exec != null) {
				StatementParser.parse(exec, new StatementExecutor(this, line),
						line.toString());
			}
		}
	}

	@Test
	public void last_line_in_coverage_data_should_be_less_or_equal_to_number_of_lines_in_source_file() {
		assertTrue(String.format(
				"Last line in coverage data (%d) should be less or equal to number of lines in source file (%d)",
				Integer.valueOf(source.getCoverage().getLastLine()),
				Integer.valueOf(source.getLines().size())),
				source.getCoverage().getLastLine() <= source.getLines().size());
	}

	/*
	 * Predefined assertion methods:
	 */

	private void assertCoverage(final Line line, final int insnStatus,
			final int missedBranches, final int coveredBranches) {
		final ILine coverage = line.getCoverage();

		String msg = String.format("Instructions (%s)", line);
		final int actualStatus = coverage.getInstructionCounter().getStatus();
		assertEquals(msg, STATUS_NAME[insnStatus], STATUS_NAME[actualStatus]);

		msg = String.format("Branches (%s)", line);
		assertEquals(msg,
				CounterImpl.getInstance(missedBranches, coveredBranches),
				coverage.getBranchCounter());
	}

	public void assertFullyCovered(final Line line, final int missedBranches,
			final int coveredBranches) {
		assertCoverage(line, ICounter.FULLY_COVERED, missedBranches,
				coveredBranches);
	}

	public void assertFullyCovered(final Line line) {
		assertFullyCovered(line, 0, 0);
	}

	public void assertPartlyCovered(final Line line, final int missedBranches,
			final int coveredBranches) {
		assertCoverage(line, ICounter.PARTLY_COVERED, missedBranches,
				coveredBranches);
	}

	public void assertPartlyCovered(final Line line) {
		assertPartlyCovered(line, 0, 0);
	}

	public void assertNotCovered(final Line line, final int missedBranches,
			final int coveredBranches) {
		assertCoverage(line, ICounter.NOT_COVERED, missedBranches,
				coveredBranches);
	}

	public void assertNotCovered(final Line line) {
		assertNotCovered(line, 0, 0);
	}

	public void assertEmpty(final Line line) {
		assertCoverage(line, ICounter.EMPTY, 0, 0);
	}

	protected void assertLogEvents(String... events) throws Exception {
		final Method getter = Class
				.forName(Stubs.class.getName(), false, loader)
				.getMethod("getLogEvents");
		assertEquals("Log events", Arrays.asList(events), getter.invoke(null));
	}

	protected void assertMethodCount(final int expectedTotal) {
		assertEquals(expectedTotal,
				source.getCoverage().getMethodCounter().getTotalCount());
	}

}
