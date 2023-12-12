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

import org.jacoco.core.instr.Instrumenter;
import org.jacoco.core.runtime.IRuntime;
import org.jacoco.core.runtime.LoggerRuntime;
import org.jacoco.core.test.TargetLoader;

/**
 * Scenario to measure the overhead in terms of additional byte code size
 * through instrumentation.
 */
public class InstrumentationSizeSzenario implements IPerfScenario {

	private final Class<?> target;

	public InstrumentationSizeSzenario(Class<?> target) {
		this.target = target;
	}

	public void run(IPerfOutput output) throws Exception {
		final IRuntime runtime = new LoggerRuntime();
		final Instrumenter instr = new Instrumenter(runtime);
		final byte[] original = TargetLoader.getClassDataAsBytes(target);
		final byte[] instrumented = instr.instrument(original, "");
		output.writeByteResult("instrumented class", instrumented.length,
				original.length);
	}

}
