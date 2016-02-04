/*******************************************************************************
 * Copyright (c) 2009, 2016 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.core.runtime;

import java.util.concurrent.atomic.AtomicIntegerArray;

import org.jacoco.core.data.ProbeMode;
import org.jacoco.core.internal.instr.ProbeArrayService;
import org.jacoco.core.internal.instr.ProbeDoubleIntArray;
import org.junit.AfterClass;
import org.junit.BeforeClass;

/**
 * Unit tests for {@link SystemPropertiesRuntime}.
 */
public class SystemPropertiesRuntimeTest {

	public static class SystemPropertiesExistsTest extends
			RuntimeTestBase<boolean[]> {

		@BeforeClass
		public static void setupProbeMode() {
			ProbeArrayService.reset();
			ProbeArrayService.configure(ProbeMode.exists);
		}

		@AfterClass
		public static void cleanupProbeMode() {
			ProbeArrayService.reset();
		}

		@Override
		IRuntime createRuntime() {
			return new SystemPropertiesRuntime();
		}
	}

	public static class SystemPropertiesCountTest extends
			RuntimeTestBase<AtomicIntegerArray> {

		@BeforeClass
		public static void setupProbeMode() {
			ProbeArrayService.reset();
			ProbeArrayService.configure(ProbeMode.count);
		}

		@AfterClass
		public static void cleanupProbeMode() {
			ProbeArrayService.reset();
		}

		@Override
		IRuntime createRuntime() {
			return new SystemPropertiesRuntime();
		}
	}

	public static class SystemPropertiesParallelTest extends
			RuntimeTestBase<ProbeDoubleIntArray> {

		@BeforeClass
		public static void setupProbeMode() {
			ProbeArrayService.reset();
			ProbeArrayService.configure(ProbeMode.parallelcount);
		}

		@AfterClass
		public static void cleanupProbeMode() {
			ProbeArrayService.reset();
		}

		@Override
		IRuntime createRuntime() {
			return new SystemPropertiesRuntime();
		}
	}
}
