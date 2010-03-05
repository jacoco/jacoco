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
 * $Id: $
 *******************************************************************************/
package org.jacoco.core.test.perf;

import org.jacoco.core.instr.Instrumenter;
import org.jacoco.core.runtime.IRuntime;
import org.jacoco.core.runtime.LoggerRuntime;
import org.jacoco.core.test.TargetLoader;
import org.objectweb.asm.ClassReader;

/**
 * This scenario runs a given scenario twice and reports the execution time:
 * Once on its original version, once in a instrumented version.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public class ExecuteInstrumentedCodeScenario extends TimedScenario {

	private final Class<? extends Runnable> target;

	protected ExecuteInstrumentedCodeScenario(String description,
			Class<? extends Runnable> target) {
		super(description);
		this.target = target;
	}

	@Override
	protected Runnable getInstrumentedRunnable() throws Exception {
		ClassReader reader = new ClassReader(TargetLoader.getClassData(target));
		IRuntime runtime = new LoggerRuntime();
		runtime.startup();
		final Instrumenter instr = new Instrumenter(runtime);
		final byte[] instrumentedBuffer = instr.instrument(reader);
		final TargetLoader loader = new TargetLoader(target, instrumentedBuffer);

		return (Runnable) loader.newTargetInstance();
	}

	@Override
	protected Runnable getReferenceRunnable() throws Exception {
		return target.newInstance();
	}

}
