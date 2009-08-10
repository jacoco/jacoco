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
package org.jacoco.core.test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import org.jacoco.core.instr.Instrumenter;
import org.jacoco.core.runtime.IRuntime;
import org.jacoco.core.runtime.LoggerRuntime;
import org.jacoco.core.test.targets.Target_performance_01;
import org.jacoco.core.test.targets.Target_performance_02;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.asm.ClassReader;

/**
 * Component tests for several different test scenarios. Each tests loads a
 * instrumented class, creates a instance of it and executes it if it implements
 * the {@link Runnable} interface. Afterwards several assertions about the
 * structure and the coverage data are performed. *
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public class RuntimePerformancetest {

	private IRuntime runtime;

	@Before
	public void setup() {
		runtime = new LoggerRuntime();
		runtime.startup();
	}

	@Test
	public void performance01() throws Exception {
		Runnable reference = new Target_performance_01();
		runPerformanceComparison(reference.toString(), reference,
				getInstrumentedRunnable(Target_performance_01.class));
	}

	@Test
	public void performance02() throws Exception {
		Runnable reference = new Target_performance_02();
		runPerformanceComparison(reference.toString(), reference,
				getInstrumentedRunnable(Target_performance_02.class));
	}

	@Test
	public void performance03() throws Exception {
		int count = 10000;
		runPerformance("Instrumenting " + count + " classes",
				new InstrumentationProcess(Target_performance_02.class, count));
	}

	// TODO class initialization

	private class InstrumentationProcess implements Runnable {

		private final byte[] buffer;

		private final int count;

		InstrumentationProcess(Class<?> clazz, int count) throws IOException {
			final String resource = "/" + clazz.getName().replace('.', '/')
					+ ".class";
			final InputStream in = getClass().getResourceAsStream(resource);
			final ByteArrayOutputStream out = new ByteArrayOutputStream();
			int c;
			while ((c = in.read()) != -1) {
				out.write(c);
			}
			this.buffer = out.toByteArray();
			this.count = count;
		}

		public void run() {
			for (int i = 0; i < count; i++) {
				ClassReader reader = new ClassReader(buffer);

				final Instrumenter instr = new Instrumenter(runtime);
				instr.instrument(reader);
			}
		}

	}

	private Runnable getInstrumentedRunnable(Class<? extends Runnable> clazz)
			throws IOException, InstantiationException, IllegalAccessException {

		final String resource = "/" + clazz.getName().replace('.', '/')
				+ ".class";
		ClassReader reader = new ClassReader(getClass().getResourceAsStream(
				resource));

		final Instrumenter instr = new Instrumenter(runtime);
		final byte[] instrumentedBuffer = instr.instrument(reader);
		final TargetLoader loader = new TargetLoader(clazz, instrumentedBuffer);

		return (Runnable) loader.newTargetInstance();
	}

	private void runPerformanceComparison(String description,
			Runnable reference, Runnable subject) {
		long referenceTime = timer(reference);
		long subjectTime = timer(subject);
		double factor = (double) subjectTime / (double) referenceTime;
		System.out.printf(Locale.US,
				"%s reference=%.2fms subject=%.2fms factor=%.2f\n",
				description, (double) referenceTime / 1000000,
				(double) subjectTime / 1000000, factor);
	}

	private void runPerformance(String description, Runnable subject) {
		long subjectTime = timer(subject);
		System.out.printf(Locale.US, "%s subject=%.2fms", description,
				(double) subjectTime / 1000000);
	}

	private long timer(Runnable subject) {
		long start = System.nanoTime();
		subject.run();
		return System.nanoTime() - start;
	}

}
