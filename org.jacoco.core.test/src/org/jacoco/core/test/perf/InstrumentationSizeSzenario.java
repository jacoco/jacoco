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
package org.jacoco.core.test.perf;

import org.jacoco.core.instr.Instrumenter;
import org.jacoco.core.runtime.IRuntime;
import org.jacoco.core.runtime.LoggerRuntime;
import org.jacoco.core.test.TargetLoader;
import org.objectweb.asm.ClassReader;

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
		ClassReader reader = new ClassReader(TargetLoader.getClassData(target));
		final Instrumenter instr = new Instrumenter(runtime);
		instr.instrument(reader);
		output.writeByteResult("instrumented class",
				instr.instrument(reader).length, reader.b.length);
	}

}
