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
import static org.junit.Assert.fail;

import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.IClassCoverage;
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
import org.junit.Test;

/**
 * Base class for validation tests. It executes the given class under code
 * coverage and provides the coverage results for validation.
 */
public abstract class ValidationTestBase {

	private static final Pattern INLINE_ASSERTIONS_PATTERN = Pattern
			.compile("//\\s*>(.*)");

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

	private ISourceFileCoverage sourceCoverage;

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

	@Test
	public void execute_inline_assertions() throws IOException {
		final List<String> lines = source.getLines();
		for (int idx = 0; idx < lines.size(); idx++) {
			final String line = lines.get(idx);
			final Matcher matcher = INLINE_ASSERTIONS_PATTERN.matcher(line);
			if (matcher.find()) {
				final int nr = idx + 1;
				StatementParser.parse(matcher.group(1), new MethodDelegate(nr),
						"line " + (nr));
			}
		}
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

		final String srcName = findSourceFileName(builder,
				target.getName().replace('.', '/'));

		source = new Source(new FileReader("src/" + srcName));

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
		final byte[] bytes = TargetLoader
				.getClassDataAsBytes(target.getClassLoader(), data.getName());
		analyzer.analyzeClass(bytes, data.getName());
	}

	private static String findSourceFileName(
			final CoverageBuilder coverageBuilder, final String className) {
		for (IClassCoverage classCoverage : coverageBuilder.getClasses()) {
			if (className.equals(classCoverage.getName())) {
				return classCoverage.getPackageName() + '/'
						+ classCoverage.getSourceFileName();
			}
		}
		throw new AssertionError();
	}

	protected final Source getSource() {
		return source;
	}

	protected void assertMethodCount(final int expectedTotal) {
		assertEquals(expectedTotal,
				sourceCoverage.getMethodCounter().getTotalCount());
	}

	protected void assertLine(final String tag, final int status) {
		privateAssertLine(tag, status, 0, 0);
	}

	protected void assertLine(final String tag, final int status,
			final int missedBranches, final int coveredBranches) {
		if (missedBranches == 0 && coveredBranches == 0) {
			throw new IllegalArgumentException(
					"Omit redundant specification of zero number of branches");
		}
		privateAssertLine(tag, status, missedBranches, coveredBranches);
	}

	private void privateAssertLine(final String tag, final int status,
			final int missedBranches, final int coveredBranches) {
		final int nr = source.getLineNumber(tag);
		final ILine line = sourceCoverage.getLine(nr);
		final String lineMsg = String.format("line %s: %s", Integer.valueOf(nr),
				source.getLine(nr));
		final int insnStatus = line.getInstructionCounter().getStatus();
		assertEquals("Instructions in " + lineMsg, STATUS_NAME[status],
				STATUS_NAME[insnStatus]);
		assertEquals("Branches in " + lineMsg,
				CounterImpl.getInstance(missedBranches, coveredBranches),
				line.getBranchCounter());
	}

	public void assertCoverage(final int nr, final String insnStatus,
			final int missedBranches, final int coveredBranches) {
		final ILine line = sourceCoverage.getLine(nr);

		String msg = String.format("Instructions in line %s: %s",
				Integer.valueOf(nr), source.getLine(nr));
		final int actualStatus = line.getInstructionCounter().getStatus();
		assertEquals(msg, insnStatus, STATUS_NAME[actualStatus]);

		msg = String.format("Branches in line %s: %s", Integer.valueOf(nr),
				source.getLine(nr));
		assertEquals(msg,
				CounterImpl.getInstance(missedBranches, coveredBranches),
				line.getBranchCounter());
	}

	public void assertFullyCovered(int nr, final int missedBranches,
			final int coveredBranches) {
		assertCoverage(nr, "FULLY_COVERED", missedBranches, coveredBranches);
	}

	public void assertFullyCovered(int nr) {
		assertFullyCovered(nr, 0, 0);
	}

	public void assertPartlyCovered(int nr, final int missedBranches,
			final int coveredBranches) {
		assertCoverage(nr, "PARTLY_COVERED", missedBranches, coveredBranches);
	}

	public void assertPartlyCovered(int nr) {
		assertPartlyCovered(nr, 0, 0);
	}

	public void assertNotCovered(int nr, final int missedBranches,
			final int coveredBranches) {
		assertCoverage(nr, "NOT_COVERED", missedBranches, coveredBranches);
	}

	public void assertNotCovered(int nr) {
		assertNotCovered(nr, 0, 0);
	}

	public void assertEmpty(int nr) {
		assertCoverage(nr, "EMPTY", 0, 0);
	}

	protected void assertLogEvents(String... events) throws Exception {
		final Method getter = Class
				.forName(Stubs.class.getName(), false, loader)
				.getMethod("getLogEvents");
		assertEquals("Log events", Arrays.asList(events), getter.invoke(null));
	}

	private class MethodDelegate implements StatementParser.IStatementVisitor {

		private final int linenr;

		MethodDelegate(int linenr) {
			this.linenr = linenr;
		}

		public void visitInvocation(String ctx, String name, Object... args) {
			final Object[] extArgs = new Object[args.length + 1];
			extArgs[0] = Integer.valueOf(linenr);
			System.arraycopy(args, 0, extArgs, 1, args.length);
			final Object target = ValidationTestBase.this;
			try {
				target.getClass().getMethod(name, getTypes(extArgs))
						.invoke(target, extArgs);
			} catch (InvocationTargetException e) {
				Throwable te = e.getTargetException();
				if (te instanceof AssertionError) {
					throw (AssertionError) te;
				}
				throw new RuntimeException(
						"Error wile processing assertions in " + ctx, te);
			} catch (Exception e) {
				throw new RuntimeException(
						"Error wile processing assertions in " + ctx, e);
			}
		}

		private Class<?>[] getTypes(Object[] instances) {
			final Class<?>[] classes = new Class[instances.length];
			for (int i = 0; i < instances.length; i++) {
				Class<? extends Object> c = instances[i].getClass();
				if (c == Integer.class) {
					c = Integer.TYPE;
				}
				classes[i] = c;
			}
			return classes;
		}

	}

}
