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

import java.util.concurrent.Callable;

import org.jacoco.core.instr.Instrumenter;
import org.jacoco.core.runtime.IRuntime;
import org.jacoco.core.runtime.LoggerRuntime;
import org.jacoco.core.runtime.RuntimeData;
import org.jacoco.core.test.TargetLoader;
import org.objectweb.asm.ClassReader;

/**
 * This scenario runs a given scenario twice and reports the execution time:
 * Once on its original version, once in a instrumented version.
 */
public class ExecuteInstrumentedCodeScenario extends TimedScenario {

	private final Class<? extends Callable<Void>> target;

	protected ExecuteInstrumentedCodeScenario(String description,
			Class<? extends Callable<Void>> target) {
		super(description);
		this.target = target;
	}

	@Override
	@SuppressWarnings("unchecked")
	protected Callable<Void> getInstrumentedCallable() throws Exception {
		ClassReader reader = new ClassReader(TargetLoader.getClassData(target));
		IRuntime runtime = new LoggerRuntime();
		runtime.startup(new RuntimeData());
		final Instrumenter instr = new Instrumenter(runtime);
		final byte[] instrumentedBuffer = instr.instrument(reader);
		final TargetLoader loader = new TargetLoader();

		return (Callable<Void>) loader.add(target, instrumentedBuffer)
				.newInstance();
	}

	@Override
	protected Callable<Void> getReferenceCallable() throws Exception {
		return target.newInstance();
	}

}
