/*******************************************************************************
 * Copyright (c) 2009, 2016 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.test.validation;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Collection;

import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.analysis.ISourceFileCoverage;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.test.InstrumentingLoader;

class BadCycleTestBase extends ValidationTestBase {

	protected final InstrumentingLoader loader = new InstrumentingLoader();

	BadCycleTestBase(final Class<?> target) throws Exception {
		super(target);
	}

	BadCycleTestBase(final String srcFolder, final Class<?> target)
			throws Exception {
		super(srcFolder, target);
	}

	@Override
	public final void setup() throws Exception {
		// nop
	}

	@Override
	protected final void run(Class<?> targetClass) throws Exception {
		// nop
	}

	final void analyze(Class<?> cls) throws IOException {
		final byte[] bytes = loader.getClassBytes(cls.getName());
		final ExecutionDataStore store = loader.collect();

		final CoverageBuilder builder = new CoverageBuilder();
		final Analyzer analyzer = new Analyzer(store, builder);
		analyzer.analyzeClass(bytes, "TestTarget");
		final Collection<IClassCoverage> classes = builder.getClasses();
		assertEquals(1, classes.size(), 0.0);
		classCoverage = classes.iterator().next();
		final Collection<ISourceFileCoverage> files = builder.getSourceFiles();
		assertEquals(1, files.size(), 0.0);
		sourceCoverage = files.iterator().next();

		source = Source.getSourceFor(srcFolder, target);
	}

}
