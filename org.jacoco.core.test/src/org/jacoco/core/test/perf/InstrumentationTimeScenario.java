/*******************************************************************************
 * Copyright (c) 2009 Mountainminds GmbH & Co. KG and others
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

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import org.jacoco.core.instr.Instrumenter;
import org.jacoco.core.runtime.IRuntime;
import org.jacoco.core.runtime.LoggerRuntime;
import org.jacoco.core.test.TargetLoader;
import org.objectweb.asm.ClassReader;

/**
 * Scenario to measure the time taken by the instrumentation process itself.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public class InstrumentationTimeScenario extends TimedScenario {

	private final Class<?> target;

	private final int count;

	protected InstrumentationTimeScenario(Class<?> target, int count) {
		super(String
				.format("%s class instrumentations", Integer.valueOf(count)));
		this.target = target;
		this.count = count;
	}

	@Override
	protected Runnable getInstrumentedRunnable() throws Exception {
		final InputStream in = TargetLoader.getClassData(target);
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		int c;
		while ((c = in.read()) != -1) {
			out.write(c);
		}
		final byte[] buffer = out.toByteArray();
		final IRuntime runtime = new LoggerRuntime();
		return new Runnable() {

			public void run() {
				for (int i = 0; i < count; i++) {
					ClassReader reader = new ClassReader(buffer);

					final Instrumenter instr = new Instrumenter(runtime);
					instr.instrument(reader);
				}
			}

		};
	}

}
