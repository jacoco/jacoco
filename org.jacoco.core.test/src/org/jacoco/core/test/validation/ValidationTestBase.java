/*******************************************************************************
 * Copyright (c) 2009, 2010 Mountainminds GmbH & Co. KG and Contributors
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
import org.jacoco.core.analysis.ClassCoverage;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.ILines;
import org.jacoco.core.analysis.SourceFileCoverage;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.instr.Instrumenter;
import org.jacoco.core.runtime.IRuntime;
import org.jacoco.core.runtime.SystemPropertiesRuntime;
import org.jacoco.core.test.TargetLoader;
import org.junit.Before;
import org.objectweb.asm.ClassReader;

/**
 * Base class for validation tests. It executes the given class under code
 * coverage and provides the coverage results for validation.
 * 
 * @author Marc R. Hoffmann
 * @version $qualified.bundle.version$
 */
public abstract class ValidationTestBase {

	private static final String[] STATUS_NAME = new String[4];

	{
		STATUS_NAME[ILines.NO_CODE] = "NO_CODE";
		STATUS_NAME[ILines.NOT_COVERED] = "NOT_COVERED";
		STATUS_NAME[ILines.FULLY_COVERED] = "FULLY_COVERED";
		STATUS_NAME[ILines.PARTLY_COVERED] = "PARTLY_COVERED";
	}

	protected final Class<?> target;

	protected ClassCoverage classCoverage;

	protected SourceFileCoverage sourceCoverage;

	protected ILines lineCoverage;

	protected Source source;

	protected ValidationTestBase(final Class<?> target) {
		this.target = target;
	}

	@Before
	public void setup() throws Exception {
		final ClassReader reader = new ClassReader(
				TargetLoader.getClassData(target));
		final ExecutionDataStore store = execute(reader);
		analyze(reader, store);
		source = Source.getSourceFor(target);
	}

	private ExecutionDataStore execute(final ClassReader reader)
			throws Exception {
		IRuntime runtime = new SystemPropertiesRuntime();
		runtime.startup();
		final byte[] bytes = new Instrumenter(runtime).instrument(reader);
		final TargetLoader loader = new TargetLoader(target, bytes);
		run(loader.getTargetClass());
		final ExecutionDataStore store = new ExecutionDataStore();
		runtime.collect(store, null, false);
		runtime.shutdown();
		return store;
	}

	protected abstract void run(final Class<?> targetClass) throws Exception;

	private void analyze(final ClassReader reader,
			final ExecutionDataStore store) {
		final CoverageBuilder builder = new CoverageBuilder();
		final Analyzer analyzer = new Analyzer(store, builder);
		analyzer.analyzeClass(reader);
		final Collection<ClassCoverage> classes = builder.getClasses();
		assertEquals(1, classes.size(), 0.0);
		classCoverage = classes.iterator().next();
		final Collection<SourceFileCoverage> files = builder.getSourceFiles();
		assertEquals(1, files.size(), 0.0);
		sourceCoverage = files.iterator().next();
		lineCoverage = sourceCoverage.getLines();
	}

	protected void assertLine(final String tag, final int status) {
		final int nr = source.getLineNumber(tag);
		final String line = source.getLine(nr);
		String msg = String.format("L%s: %s", Integer.valueOf(nr), line);
		assertEquals(msg, STATUS_NAME[status],
				STATUS_NAME[lineCoverage.getStatus(nr)]);
	}

}
