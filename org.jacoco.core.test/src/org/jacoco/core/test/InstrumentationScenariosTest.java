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
package org.jacoco.core.test;

import java.io.IOException;

import org.jacoco.core.instr.Analyzer;
import org.jacoco.core.instr.Instrumenter;
import org.jacoco.core.runtime.IRuntime;
import org.jacoco.core.runtime.LoggerRuntime;
import org.jacoco.core.test.ClassDataRecorder.BlockData;
import org.jacoco.core.test.targets.Target_init_01;
import org.jacoco.core.test.targets.Target_init_02;
import org.jacoco.core.test.targets.Target_init_03;
import org.jacoco.core.test.targets.Target_init_04;
import org.jacoco.core.test.targets.Target_init_05;
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
public class InstrumentationScenariosTest {

	private IRuntime runtime;

	@Before
	public void setup() throws Exception {
		runtime = new LoggerRuntime();
		runtime.startup();
	}

	@Test
	public void init_01() throws Exception {
		final ClassDataRecorder rec = runScenario(Target_init_01.class);
		final BlockData block0 = rec.getMethod("<init>").getBlock(0);
		block0.assertLines(23);
		block0.assertCovered();
	}

	@Test
	public void init_02() throws Exception {
		final ClassDataRecorder rec = runScenario(Target_init_02.class);
		final BlockData block0 = rec.getMethod("<init>").getBlock(0);
		block0.assertLines(25, 26);
		block0.assertCovered();
	}

	@Test
	public void init_03() throws Exception {
		final ClassDataRecorder rec = runScenario(Target_init_03.class);
		final BlockData block0 = rec.getMethod("<init>").getBlock(0);
		block0.assertLines(23, 25);
		block0.assertCovered();
	}

	@Test
	public void init_04() throws Exception {
		final ClassDataRecorder rec = runScenario(Target_init_04.class);
		final BlockData block0 = rec.getMethod("<init>").getBlock(0);
		block0.assertLines(25, 27, 28);
		block0.assertCovered();
	}

	@Test
	public void init_05() throws Exception {
		final ClassDataRecorder rec = runScenario(Target_init_05.class);
		final BlockData block0 = rec.getMethod("<init>").getBlock(0);
		block0.assertLines(29);
		block0.assertCovered();
	}

	private ClassDataRecorder runScenario(Class<?> clazz) throws IOException,
			InstantiationException, IllegalAccessException {

		ClassReader reader = new ClassReader(TargetLoader.getClassData(clazz));

		final ClassDataRecorder rec = new ClassDataRecorder();
		final Analyzer analyzer = new Analyzer(rec);
		analyzer.analyze(reader);

		final Instrumenter instr = new Instrumenter(runtime);
		final byte[] instrumentedBuffer = instr.instrument(reader);
		final TargetLoader loader = new TargetLoader(clazz, instrumentedBuffer);

		final Object obj = loader.newTargetInstance();
		if (obj instanceof Runnable) {
			((Runnable) obj).run();
		}

		runtime.collect(rec, false);
		return rec;
	}
}
