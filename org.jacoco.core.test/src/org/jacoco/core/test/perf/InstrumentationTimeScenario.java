/*******************************************************************************
 * Copyright (c) 2009, 2019 Mountainminds GmbH & Co. KG and Contributors
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

import java.util.concurrent.Callable;

import org.jacoco.core.instr.Instrumenter;
import org.jacoco.core.runtime.LoggerRuntime;
import org.jacoco.core.test.TargetLoader;

/**
 * Scenario to measure the time taken by the instrumentation process itself.
 */
public class InstrumentationTimeScenario extends TimedScenario {

	private final Class<?> target;

	private final int count;

	protected InstrumentationTimeScenario(Class<?> target, int count) {
		super(String.format("instrumenting %s classes", Integer.valueOf(count)));
		this.target = target;
		this.count = count;
	}

	@Override
	protected Callable<Void> getInstrumentedCallable() throws Exception {
		final byte[] bytes = TargetLoader.getClassDataAsBytes(target);
		final Instrumenter instr = new Instrumenter(new LoggerRuntime());
		return new Callable<Void>() {
			public Void call() throws Exception {
				for (int i = 0; i < count; i++) {
					instr.instrument(bytes, "TestTarget");
				}
				return null;
			}
		};
	}

}
