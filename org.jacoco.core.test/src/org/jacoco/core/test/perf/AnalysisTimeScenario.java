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
package org.jacoco.core.test.perf;

import java.util.concurrent.Callable;

import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.analysis.ICoverageVisitor;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.test.TargetLoader;

/**
 * Scenario to measure the time taken by the instrumentation process itself.
 */
public class AnalysisTimeScenario extends TimedScenario {

	private final Class<?> target;

	private final int count;

	protected AnalysisTimeScenario(Class<?> target, int count) {
		super(String.format("analysing %s classes", Integer.valueOf(count)));
		this.target = target;
		this.count = count;
	}

	@Override
	protected Callable<Void> getInstrumentedCallable() throws Exception {
		final byte[] bytes = TargetLoader.getClassDataAsBytes(target);
		final ExecutionDataStore executionData = new ExecutionDataStore();
		ICoverageVisitor visitor = new ICoverageVisitor() {
			public void visitCoverage(IClassCoverage coverage) {
			}
		};
		final Analyzer analyzer = new Analyzer(executionData, visitor);
		return new Callable<Void>() {
			public Void call() throws Exception {
				for (int i = 0; i < count; i++) {
					analyzer.analyzeClass(bytes, target.getName());
				}
				return null;
			}
		};
	}
}
