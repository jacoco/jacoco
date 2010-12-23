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
package org.jacoco.core.test.perf;

import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.ClassCoverage;
import org.jacoco.core.analysis.ICoverageVisitor;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.test.TargetLoader;

/**
 * Scenario to measure the time taken by the instrumentation process itself.
 * 
 * @author Marc R. Hoffmann
 * @version $qualified.bundle.version$
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
	protected Runnable getInstrumentedRunnable() throws Exception {
		final byte[] bytes = TargetLoader.getClassDataAsBytes(target);
		final ExecutionDataStore executionData = new ExecutionDataStore();
		ICoverageVisitor visitor = new ICoverageVisitor() {
			public void visitCoverage(ClassCoverage coverage) {
			}
		};
		final Analyzer analyzer = new Analyzer(executionData, visitor);
		return new Runnable() {
			public void run() {
				for (int i = 0; i < count; i++) {
					analyzer.analyzeClass(bytes);
				}
			}
		};
	}
}
